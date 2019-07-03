/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.fbreader;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.SelectionCursor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.options.*;
import org.geometerplus.fbreader.util.FixedTextSnippet;
import org.geometerplus.fbreader.util.TextSnippet;

public final class FBView extends ZLTextView {
	private final FBReaderApp myReader;
	private final ViewOptions myViewOptions;
	private final BookElementManager myBookElementManager;
	private long lastClickTime = 0;
	private long threshold = 430;

	FBView(FBReaderApp reader) {
		super(reader);
		myReader = reader;
		myViewOptions = reader.ViewOptions;
		myBookElementManager = new BookElementManager(this);
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
		if (myFooter != null) {
			myFooter.resetTOCMarks();
		}
	}

	private int myStartX;
	private int myStartY;
	private boolean myIsBrightnessAdjustmentInProgress;
	static private int myStartBrightness = 50;

	private TapZoneMap myZoneMap;

	private TapZoneMap getZoneMap() {
		final PageTurningOptions prefs = myReader.PageTurningOptions;
		String id = prefs.TapZoneMap.getValue();
		if ("".equals(id)) {
			id = prefs.Horizontal.getValue() ? "right_to_left" : "up";
		}
		if (myZoneMap == null || !id.equals(myZoneMap.Name)) {
			myZoneMap = TapZoneMap.zoneMap(id);
		}
		return myZoneMap;
	}

	private void onFingerSingleTapLastResort(int x, int y) {
		myReader.runAction(getZoneMap().getActionByCoordinates(
			x, y, getContextWidth(), getContextHeight(),
			isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap
		), x, y);
	}

	@Override
	public void onFingerSingleTap(int x, int y) {
		synchronized (this) {
			if (System.currentTimeMillis() - this.lastClickTime > threshold) {
				isFastMode = false;
			} else {
				isFastMode = true;
			}
			this.lastClickTime = System.currentTimeMillis();
		}
		final ZLTextRegion hyperlinkRegion = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.HyperlinkFilter);
		if (hyperlinkRegion != null) {
			outlineRegion(hyperlinkRegion);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			myReader.runAction(ActionCode.PROCESS_HYPERLINK);
			return;
		}

		final ZLTextRegion bookRegion = findRegion(x, y, 0, ZLTextRegion.ExtensionFilter);
		if (bookRegion != null) {
			myReader.runAction(ActionCode.DISPLAY_BOOK_POPUP, bookRegion);
			return;
		}

		final ZLTextRegion videoRegion = findRegion(x, y, 0, ZLTextRegion.VideoFilter);
		if (videoRegion != null) {
			outlineRegion(videoRegion);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			myReader.runAction(ActionCode.OPEN_VIDEO, (ZLTextVideoRegionSoul) videoRegion.getSoul());
			return;
		}

		final ZLTextHighlighting highlighting = findHighlighting(x, y, maxSelectionDistance());
		if (highlighting instanceof BookmarkHighlighting) {
			myReader.runAction(
				ActionCode.SELECTION_BOOKMARK,
				((BookmarkHighlighting)highlighting).Bookmark
			);
			return;
		}

		if (myReader.isActionEnabled(ActionCode.HIDE_TOAST)) {
			myReader.runAction(ActionCode.HIDE_TOAST);
			return;
		}

		onFingerSingleTapLastResort(x, y);
	}

	@Override
	public boolean isDoubleTapSupported() {
		return myReader.MiscOptions.EnableDoubleTap.getValue();
	}

	@Override
	public void onFingerDoubleTap(int x, int y) {
		myReader.runAction(ActionCode.HIDE_TOAST);

		myReader.runAction(getZoneMap().getActionByCoordinates(
			x, y, getContextWidth(), getContextHeight(), TapZoneMap.Tap.doubleTap
		), x, y);
	}

	@Override
	public void onFingerPress(int x, int y) {
		myStartX = x;
		myReader.runAction(ActionCode.HIDE_TOAST);

		final float maxDist = ZLibrary.Instance().getDisplayDPI() / 4 + 100;
		final SelectionCursor.Which cursor = findSelectionCursor(x, y, maxDist * maxDist);
		if (cursor != null) {
			myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
			moveSelectionCursorTo(cursor, x, y);
			return;
		}

		if (myReader.MiscOptions.AllowScreenBrightnessAdjustment.getValue() && x < getContextWidth() / 10) {
			myIsBrightnessAdjustmentInProgress = true;
			myStartY = y;
//			myStartBrightness = myReader.getViewWidget().getScreenBrightness();
			return;
		}

		startManualScrolling(x, y);
	}

	private boolean isFlickScrollingEnabled() {
		final PageTurningOptions.FingerScrollingType fingerScrolling =
			myReader.PageTurningOptions.FingerScrolling.getValue();
		return
			fingerScrolling == PageTurningOptions.FingerScrollingType.byFlick ||
			fingerScrolling == PageTurningOptions.FingerScrollingType.byTapAndFlick;
	}

	private void startManualScrolling(int x, int y) {
		if (!isFlickScrollingEnabled()) {
			return;
		}

		final boolean horizontal = myReader.PageTurningOptions.Horizontal.getValue();
		final Direction direction = horizontal ? Direction.rightToLeft : Direction.up;
		myReader.getViewWidget().startManualScrolling(x, y, direction);
	}

	@Override
	public void onFingerMove(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			moveSelectionCursorTo(cursor, x, y);
			return;
		}

		synchronized (this) {
			if (myIsBrightnessAdjustmentInProgress) {

				if (x - myStartX > getContextWidth() / 40) {
					myIsBrightnessAdjustmentInProgress = false;
					startManualScrolling(x, y);
				}

				if (x < getContextWidth() / 5) {
					final int delta = (myStartBrightness + 30) * (myStartY - y) / getContextHeight()/6;
					int t = myStartBrightness+delta;
					myReader.getViewWidget().setScreenBrightness(myStartBrightness + delta);
					myStartBrightness +=delta;
					if (myStartBrightness>100){
						myStartBrightness =100;
						myStartY=y;	
					}
				    if(myStartBrightness < 0){
				    	myStartBrightness=0;
				    	myStartY=y;	
				    }
					return;
				}
			}

			if (isFlickScrollingEnabled()) {
				myReader.getViewWidget().scrollManuallyTo(x, y);
			}
		}
	}

	@Override
	public void onFingerRelease(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			releaseSelectionCursor();
		} else if (myIsBrightnessAdjustmentInProgress) {
			myIsBrightnessAdjustmentInProgress = false;
		} else if (isFlickScrollingEnabled()) {
			myReader.getViewWidget().startAnimatedScrolling(
				x, y, myReader.PageTurningOptions.AnimationSpeed.getValue()
			);
		}
	}

	@Override
	public void onFingerScaleCloseTagDialog() {
		myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
		clearSelection();
	}
	
	@Override
	public boolean onFingerLongPress(int x, int y) {
		myReader.runAction(ActionCode.HIDE_TOAST);

		final ZLTextRegion region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();
			boolean doSelectRegion = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				switch (myReader.MiscOptions.WordTappingAction.getValue()) {
				case startSelecting:
					myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
					initSelection(x, y);
					// Jacky 20160524 fix LongPress corner to scrolling issue.
						final SelectionCursor.Which cursor = findSelectionCursor(x, y);
					 if (cursor != null) {
					 moveSelectionCursorTo(cursor, x, y);
					 }
					
					if (getCountOfSelectedWords() > 0) {
						myReader.runAction(ActionCode.SELECTION_SHOW_PANEL);
					}
					return true;
				case selectSingleWord:
				case openDictionary:
					doSelectRegion = true;
					break;
				default:
					break;
				}
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doSelectRegion =
					myReader.ImageOptions.TapAction.getValue() !=
					ImageOptions.TapActionEnum.doNothing;
			} else if (soul instanceof ZLTextHyperlinkRegionSoul) {
				doSelectRegion = true;
			}

			if (doSelectRegion) {
				outlineRegion(region);
				myReader.getViewWidget().reset();
				myReader.getViewWidget().repaint();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onFingerMoveAfterLongPress(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			moveSelectionCursorTo(cursor, x, y);
			return;
		}

		ZLTextRegion region = getOutlinedRegion();
		if (region != null) {
			ZLTextRegion.Soul soul = region.getSoul();
			if (soul instanceof ZLTextHyperlinkRegionSoul ||
				soul instanceof ZLTextWordRegionSoul) {
				if (myReader.MiscOptions.WordTappingAction.getValue() !=
					MiscOptions.WordTappingActionEnum.doNothing) {
					region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
					if (region != null) {
						soul = region.getSoul();
						if (soul instanceof ZLTextHyperlinkRegionSoul
							 || soul instanceof ZLTextWordRegionSoul) {
							outlineRegion(region);
							myReader.getViewWidget().reset();
							myReader.getViewWidget().repaint();
						}
					}
				}
			}
		}
	}

	@Override
	public void onFingerReleaseAfterLongPress(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			releaseSelectionCursor();
			return;
		}

		final ZLTextRegion region = getOutlinedRegion();
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();

			boolean doRunAction = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				doRunAction =
					myReader.MiscOptions.WordTappingAction.getValue() ==
					MiscOptions.WordTappingActionEnum.openDictionary;
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doRunAction =
					myReader.ImageOptions.TapAction.getValue() ==
					ImageOptions.TapActionEnum.openImageView;
			}

			if (doRunAction) {
				myReader.runAction(ActionCode.PROCESS_HYPERLINK);
			}
		}
	}

	@Override
	public void onFingerEventCancelled() {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			releaseSelectionCursor();
		}
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		if (diffX == 0 && diffY == 0) {
			return true;
		}

		final Direction direction = (diffY != 0) ?
			(diffY > 0 ? Direction.down : Direction.up) :
			(diffX > 0 ? Direction.leftToRight : Direction.rightToLeft);

		new MoveCursorAction(myReader, direction).run();
		return true;
	}

	@Override
	public ZLTextStyleCollection getTextStyleCollection() {
		return myViewOptions.getTextStyleCollection();
	}

	@Override
	public ImageFitting getImageFitting() {
		return myReader.ImageOptions.FitToScreen.getValue();
	}

	@Override
	public int getLeftMargin() {
		if (isImage){
			if (imageOnlyCheck())
				return 0;
		}
		return myViewOptions.LeftMargin.getValue();
	}

	@Override
	public int getRightMargin() {
		if (isImage){
			if (imageOnlyCheck())
				return 0;
		}

		return myViewOptions.RightMargin.getValue();
	}

	@Override
	public int getTopMargin() {
		if (isImage){
			if (imageOnlyCheck())
				return 0;
		}

		return myViewOptions.TopMargin.getValue();
	}

	@Override
	public int getBottomMargin() {
		if (isImage){
			if (imageOnlyCheck())
				return 0;
		}

		return myViewOptions.BottomMargin.getValue() + myViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption.getValue()/2;
	}

	@Override
	public int getSpaceBetweenColumns() {
		return myViewOptions.SpaceBetweenColumns.getValue();
	}

	@Override
	public boolean twoColumnView() {
		return getContextHeight() <= getContextWidth() && myViewOptions.TwoColumnView.getValue();
	}

	@Override
	public ZLFile getWallpaperFile() {
		final String filePath = myViewOptions.getColorProfile().WallpaperOption.getValue();
		if ("".equals(filePath)) {
			return null;
		}

		final ZLFile file = ZLFile.createFileByPath(filePath);
		if (file == null || !file.exists()) {
			return null;
		}
		return file;
	}

	@Override
	public ZLPaintContext.FillMode getFillMode() {
		return getWallpaperFile() instanceof ZLResourceFile
			? ZLPaintContext.FillMode.tileMirror
				: myViewOptions.getColorProfile().FillModeOption.getValue();
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myViewOptions.getColorProfile().BackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectionBackgroundColor() {
		return myViewOptions.getColorProfile().SelectionBackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectionForegroundColor() {
		return myViewOptions.getColorProfile().SelectionForegroundOption.getValue();
	}

	@Override
	public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
		final ColorProfile profile = myViewOptions.getColorProfile();
		switch (hyperlink.Type) {
		default:
		case FBHyperlinkType.NONE:
			return profile.RegularTextOption.getValue();
		case FBHyperlinkType.INTERNAL:
		case FBHyperlinkType.FOOTNOTE:
			return myReader.Collection.isHyperlinkVisited(myReader.getCurrentBook(), hyperlink.Id)
					? profile.VisitedHyperlinkTextOption.getValue()
					: profile.HyperlinkTextOption.getValue();
		case FBHyperlinkType.EXTERNAL:
			return profile.HyperlinkTextOption.getValue();
		}
	}

	@Override
	public ZLColor getHighlightingBackgroundColor() {
		return myViewOptions.getColorProfile().HighlightingBackgroundOption.getValue();
	}

	@Override
	public ZLColor getHighlightingForegroundColor() {
		return myViewOptions.getColorProfile().HighlightingForegroundOption.getValue();
	}

	private abstract class Footer implements FooterArea {
		private Runnable UpdateTask = new Runnable() {
			public void run() {
				myReader.getViewWidget().repaint();
			}
		};

		protected ArrayList<TOCTree> myTOCMarks;
		private int myMaxTOCMarksNumber = -1;

		public int getHeight() {
			return myViewOptions.FooterHeight.getValue();
		}

		public synchronized void resetTOCMarks() {
			myTOCMarks = null;
		}

		protected synchronized void updateTOCMarks(BookModel model, int maxNumber) {
			if (myTOCMarks != null && myMaxTOCMarksNumber == maxNumber) {
				return;
			}

			myTOCMarks = new ArrayList<TOCTree>();
			myMaxTOCMarksNumber = maxNumber;

			TOCTree toc = model.TOCTree;
			if (toc == null) {
				return;
			}
			int maxLevel = Integer.MAX_VALUE;
			if (toc.getSize() >= maxNumber) {
				final int[] sizes = new int[10];
				for (TOCTree tocItem : toc) {
					if (tocItem.Level < 10) {
						++sizes[tocItem.Level];
					}
				}
				for (int i = 1; i < sizes.length; ++i) {
					sizes[i] += sizes[i - 1];
				}
				for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
					if (sizes[maxLevel] < maxNumber) {
						break;
					}
				}
			}
			for (TOCTree tocItem : toc.allSubtrees(maxLevel)) {
				myTOCMarks.add(tocItem);
			}
		}

		protected String buildInfoString(PagePosition pagePosition, String separator) {
			final StringBuilder info = new StringBuilder();
			final FooterOptions footerOptions = myViewOptions.getFooterOptions();

			if (footerOptions.showProgressAsPages()) {
				maybeAddSeparator(info, separator);
				info.append(pagePosition.Current);
				info.append("/");
				info.append(pagePosition.Total);
			}
			if (footerOptions.showProgressAsPercentage() && pagePosition.Total != 0) {
				maybeAddSeparator(info, separator);
				info.append(String.valueOf(100 * pagePosition.Current / pagePosition.Total));
				info.append("%");
			}

			if (footerOptions.ShowClock.getValue()) {
				maybeAddSeparator(info, separator);
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			if (footerOptions.ShowBattery.getValue()) {
				maybeAddSeparator(info, separator);
				info.append(myReader.getBatteryLevel());
				info.append("%");
			}
			return info.toString();
		}

		private void maybeAddSeparator(StringBuilder info, String separator) {
			if (info.length() > 0) {
				info.append(separator);
			}
		}

		private List<FontEntry> myFontEntry;
		private Map<String, Integer> myHeightMap = new HashMap<String, Integer>();
		private Map<String, Integer> myCharHeightMap = new HashMap<String, Integer>();
		protected synchronized int setFont(ZLPaintContext context, int height, boolean bold) {
			final String family = myViewOptions.getFooterOptions().Font.getValue();
			if (myFontEntry == null || !family.equals(myFontEntry.get(0).Family)) {
				myFontEntry = Collections.singletonList(FontEntry.systemEntry(family));
			}
			final String key = family + (bold ? "N" : "B") + height;
			final Integer cached = myHeightMap.get(key);
			if (cached != null) {
				context.setFont(myFontEntry, cached, bold, false, false, false);
				final Integer charHeight = myCharHeightMap.get(key);
				return charHeight != null ? charHeight : height;
			} else {
				int h = height + 2;
				int charHeight = height;
				final int max = height < 9 ? height - 1 : height - 2;
				for (; h > 5; --h) {
					context.setFont(myFontEntry, h, bold, false, false, false);
					charHeight = context.getCharHeight('H');
					if (charHeight <= max) {
						break;
					}
				}
				myHeightMap.put(key, h);
				myCharHeightMap.put(key, charHeight);
				return charHeight;
			}
		}
	}

	private class FooterOldStyle extends Footer {
		public synchronized void paint(ZLPaintContext context) {
			final ZLFile wallpaper = getWallpaperFile();
			if (wallpaper != null) {
				context.clear(wallpaper, getFillMode());
			} else {
				context.clear(getBackgroundColor());
			}

			final BookModel model = myReader.Model;
			if (model == null) {
				return;
			}

			// final ZLColor bgColor = getBackgroundColor();
			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
			final ZLColor fillColor = myViewOptions.getColorProfile().FooterFillOption.getValue();

			final int left = myViewOptions.LeftMargin.getValue();
			final int right = context.getWidth() - myViewOptions.RightMargin.getValue();
			final int height = getHeight();
			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;
			setFont(context, height, height > 10);

			final PagePosition pagePosition = FBView.this.pagePosition();

			// draw info text
			final String infoString = buildInfoString(pagePosition, " ");
			final int infoWidth = context.getStringWidth(infoString);
			context.setTextColor(fgColor);
			context.drawString(right - infoWidth, height - delta, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			final int gaugeWidth = gaugeRight - left - 2 * lineWidth;

			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			context.drawLine(left, lineWidth, left, height - lineWidth);
			context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, left, lineWidth);

			final int gaugeInternalRight =
				left + lineWidth + (int)(1.0 * gaugeWidth * pagePosition.Current / pagePosition.Total);

			context.setFillColor(fillColor);
			context.fillRectangle(left + 1, height - 2 * lineWidth, gaugeInternalRight, lineWidth + 1);

			final FooterOptions footerOptions = myViewOptions.getFooterOptions();
			if (footerOptions.ShowTOCMarks.getValue()) {
				updateTOCMarks(model, footerOptions.MaxTOCMarks.getValue());
				final int fullLength = sizeOfFullText();
				for (TOCTree tocItem : myTOCMarks) {
					TOCTree.Reference reference = tocItem.getReference();
					if (reference != null) {
						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
						final int xCoord =
							left + 2 * lineWidth + (int)(1.0 * gaugeWidth * refCoord / fullLength);
						context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
					}
				}
			}
		}
	}

	// private class FooterNewStyle extends Footer {
	// public synchronized void paint(ZLPaintContext context) {
	// final ColorProfile cProfile = myViewOptions.getColorProfile();
	// context.clear(cProfile.FooterNGBackgroundOption.getValue());
	//
	// final BookModel model = myReader.Model;
	// if (model == null) {
	// return;
	// }
	//
	// final ZLColor textColor = cProfile.FooterNGForegroundOption.getValue();
	// final ZLColor readColor = cProfile.FooterNGForegroundOption.getValue();
//			final ZLColor unreadColor = cProfile.FooterNGForegroundUnreadOption.getValue();
	//
	// final int left = getLeftMargin();
	// final int right = context.getWidth() - getRightMargin();
	// final int height = getHeight();
	// final int lineWidth = height <= 12 ? 1 : 2;
	// final int charHeight = setFont(context, height, height > 12);
	//
	// final PagePosition pagePosition = FBView.this.pagePosition();
	//
	// // draw info text
	// final String infoString = buildInfoString(pagePosition, " ");
	// final int infoWidth = context.getStringWidth(infoString);
	// context.setTextColor(textColor);
//			context.drawString(right - infoWidth, (height + charHeight + 1) / 2, infoString);
	//
	// // draw gauge
	// final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
	// final int gaugeInternalRight =
//				left + (int)(1.0 * (gaugeRight - left) * pagePosition.Current / pagePosition.Total + 0.5);
	// final int v = height / 2;
	//
	// context.setLineWidth(lineWidth);
	// context.setLineColor(readColor);
	// context.drawLine(left, v, gaugeInternalRight, v);
	// if (gaugeInternalRight < gaugeRight) {
	// context.setLineColor(unreadColor);
	// context.drawLine(gaugeInternalRight + 1, v, gaugeRight, v);
	// }
	//
	// // draw labels
	// final FooterOptions footerOptions = myViewOptions.getFooterOptions();
	// if (footerOptions.ShowTOCMarks.getValue()) {
	// final TreeSet<Integer> labels = new TreeSet<Integer>();
	// labels.add(left);
	// labels.add(gaugeRight);
	// updateTOCMarks(model, footerOptions.MaxTOCMarks.getValue());
	// final int fullLength = sizeOfFullText();
	// for (TOCTree tocItem : myTOCMarks) {
	// TOCTree.Reference reference = tocItem.getReference();
	// if (reference != null) {
	// final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
//						labels.add(left + (int)(1.0 * (gaugeRight - left) * refCoord / fullLength + 0.5));
	// }
	// }
	// for (int l : labels) {
	// context.setLineColor(l <= gaugeInternalRight ? readColor : unreadColor);
	// context.drawLine(l, v + 3, l, v - lineWidth - 2);
	// }
	// }
	// }
	// }
	boolean isImage = false;
	private class FooterNewStyle extends Footer {
		public synchronized void paint(ZLPaintContext context) {

			isImage = imageOnlyCheck();

/*
			final ColorProfile cProfile = myViewOptions.getColorProfile();
			context.clear(cProfile.FooterNGBackgroundOption.getValue());

			final FBReaderApp reader = myReader;
			if (reader == null) {
				return;
			}
			final BookModel model = reader.Model;
			if (model == null) {
				return;
			}

			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
			final ZLColor fillColor = reader.ViewOptions.getColorProfile().FooterFillOption.getValue();

			final int left = myViewOptions.LeftMargin.getValue();
			final int right = context.getWidth() - myViewOptions.RightMargin.getValue();
			final int height = getHeight();

			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;

			final PagePosition pagePosition = FBView.this.pagePosition();

			final StringBuilder info = new StringBuilder();
			if (reader.ViewOptions.getFooterOptions().showProgressAsPages()) {
				info.append(pagePosition.Current);
				info.append("/");
				info.append(pagePosition.Total);
			}
			if (reader.ViewOptions.getFooterOptions().ShowBattery.getValue()) {
				if (info.length() > 0) {
					info.append("  ");
				}
				info.append(reader.getBatteryLevel());
				info.append("%");
			}
			if (reader.ViewOptions.getFooterOptions().ShowClock.getValue()) {
				if (info.length() > 0) {
					info.append("  ");
				}
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			final String infoString = info.toString();

			final int infoWidth = (context.getStringWidth(infoString) + myViewOptions.RightMargin.getValue() * 2) + 50;

			context.clear(getBackgroundColor());

			// draw info text
			context.setTextColor(fgColor);
			setFont(context, height, height > 10); // Jacky 20160702 setFonSize

			context.drawString(right - infoWidth, height - delta - 1, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			myGaugeWidth = gaugeRight - left - 2 * lineWidth;

			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			context.drawLine(left, lineWidth, left, height - lineWidth);
			context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, left, lineWidth);

			final int gaugeInternalRight =
				left + lineWidth + (int)(1.0 * myGaugeWidth * pagePosition.Current / pagePosition.Total);

			// context.setFillColor(fillColor);
			context.setFillColor(fgColor); // set black color
			context.fillRectangle(left + 1, (height - 2 * lineWidth), gaugeInternalRight, lineWidth + 1);

*/
//			if (reader.ViewOptions.getFooterOptions().ShowTOCMarks.getValue()) {
			// if (myTOCMarks == null) {
			// updateTOCMarks(model, gaugeInternalRight);
			// }
			// final int fullLength = sizeOfFullText();
			// for (TOCTree tocItem : myTOCMarks) {
			// TOCTree.Reference reference = tocItem.getReference();
			// if (reference != null) {
//						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
			// final int xCoord =
//							left + 2 * lineWidth + (int)(1.0 * myGaugeWidth * refCoord / fullLength);
			// context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
			// }
			// }
			// }
			context.clear(getBackgroundColor());
			
			final PagePosition pagePosition = FBView.this.pagePosition();
			tvBookProgress.setText(pagePosition.Current+" / "+pagePosition.Total);
			sbBookProgress.setMax(pagePosition.Total);
			sbBookProgress.setProgress(pagePosition.Current);
		}

		// TODO: remove
//		int myGaugeWidth = 1;
	}
	private Footer myFooter;

	@Override
	public Footer getFooterArea() {
		switch (myViewOptions.ScrollbarType.getValue()) {
		case SCROLLBAR_SHOW_AS_FOOTER:
			if (!(myFooter instanceof FooterNewStyle)) {
				if (myFooter != null) {
					myReader.removeTimerTask(myFooter.UpdateTask);
				}
				myFooter = new FooterNewStyle();
				myReader.addTimerTask(myFooter.UpdateTask, 15000);
			}
			
//			if (!(myFooter instanceof FooterNewStyle)) {
//				final PagePosition pagePosition = FBView.this.pagePosition();
//				tvBookProgress.setText(pagePosition.Current+" / "+pagePosition.Total);
//				sbBookProgress.setMax(pagePosition.Total);
//				sbBookProgress.setProgress(pagePosition.Current);	
//			}
			break;
		case SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE:
			if (!(myFooter instanceof FooterOldStyle)) {
				if (myFooter != null) {
					myReader.removeTimerTask(myFooter.UpdateTask);
				}
				myFooter = new FooterOldStyle();
				myReader.addTimerTask(myFooter.UpdateTask, 15000);
			}
			break;
		default:
			if (myFooter != null) {
				myReader.removeTimerTask(myFooter.UpdateTask);
				myFooter = null;
			}
			break;
		}
		return myFooter;
	}

	@Override
	protected void releaseSelectionCursor() {
		super.releaseSelectionCursor();
		if (getCountOfSelectedWords() > 0) {
			myReader.runAction(ActionCode.SELECTION_SHOW_PANEL);
		}
	}

	public TextSnippet getSelectedSnippet() {
		final ZLTextPosition start = getSelectionStartPosition();
		final ZLTextPosition end = getSelectionEndPosition();
		if (start == null || end == null) {
			return null;
		}
		final TextBuildTraverser traverser = new TextBuildTraverser(this);
		traverser.traverse(start, end);
		return new FixedTextSnippet(start, end, traverser.getText());
	}

	public int getCountOfSelectedWords() {
		final WordCountTraverser traverser = new WordCountTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getCount();
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;
	public static final int SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE = 4;
	public static boolean isFastMode = false;

	@Override
	public int scrollbarType() {
		return myViewOptions.ScrollbarType.getValue();
	}

	@Override
	public Animation getAnimationType() {
		return myReader.PageTurningOptions.Animation.getValue();
	}

	@Override
	protected ZLPaintContext.ColorAdjustingMode getAdjustingModeForImages() {
		if (myReader.ImageOptions.MatchBackground.getValue()) {
			if (ColorProfile.DAY.equals(myViewOptions.getColorProfile().Name)) {
				return ZLPaintContext.ColorAdjustingMode.DARKEN_TO_BACKGROUND;
			} else {
				return ZLPaintContext.ColorAdjustingMode.LIGHTEN_TO_BACKGROUND;
			}
		} else {
			return ZLPaintContext.ColorAdjustingMode.NONE;
		}
	}

	@Override
	public synchronized void onScrollingFinished(PageIndex pageIndex) {
		super.onScrollingFinished(pageIndex);
		myReader.storePosition();
	}

	@Override
	protected ExtensionElementManager getExtensionManager() {
		return myBookElementManager;
	}

	final int IMAGE_LEFT = 0;
	final int IMAGE_RIGHT = 1;

	public boolean imageCheck(Integer... params) {
		int uX = getContextWidth() / 10;
		int uY = getContextHeight() / 10;
		int[] coordinateX = { uX*2, uX*4, uX*6, uX*8, uX*2, uX*4, uX*6, uX*8, uX*2, uX*4, uX*6, uX*8, uX*2, uX*4, uX*6, uX*8};
		int[] coordinateY = { uY*2, uY*2, uY*2, uY*2, uY*4, uY*4, uY*4, uY*4, uY*6, uY*6, uY*6, uY*6, uY*8, uY*8, uY*8, uY*8};

		boolean isPageHasImage = false;
		boolean isPageHasText = false;
		ZLTextRegion hyperlinkRegionImage = null;
		ZLTextRegion hyperlinkRegion = null;

		if (params.length == 0) { // if not image page
			for (int i = 0; i < coordinateX.length; i++) {
				hyperlinkRegion = findRegion(coordinateX[i], coordinateY[i], maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);

				if (hyperlinkRegion != null) {
					ZLTextRegion.Soul soul = hyperlinkRegion.getSoul();
					if (soul instanceof ZLTextWordRegionSoul) {
						isPageHasText = true;
					} else if (soul instanceof ZLTextImageRegionSoul) {
						isPageHasImage = true;
						hyperlinkRegionImage = hyperlinkRegion;
					}
				}
			}
		} else if (params[0] == IMAGE_LEFT) { // select left image page
			for (int i = 0; i < coordinateX.length; i++) {
				if (i == 0 || i == 1 || i == 4 || i == 5 || i == 8 || i == 9 || i == 12 || i == 13) {
					hyperlinkRegion = findRegion(coordinateX[i], coordinateY[i], maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);

					if (hyperlinkRegion != null) {
						isPageHasImage = true;
						hyperlinkRegionImage = hyperlinkRegion;
					}
				}
			}
		} else if (params[0] == IMAGE_RIGHT) { // select right image page
			for (int i = 0; i < coordinateX.length; i++) {
				if (i == 2 || i == 3 || i == 6 || i == 7 || i == 10 || i == 11 || i == 14 || i == 15) {
					hyperlinkRegion = findRegion(coordinateX[i], coordinateY[i], maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);

					if (hyperlinkRegion != null) {
						isPageHasImage = true;
						hyperlinkRegionImage = hyperlinkRegion;
					}
				}
			}
		}

		if (isPageHasImage == true && isPageHasText == false) {
			outlineRegion(hyperlinkRegionImage);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			myReader.runAction(ActionCode.PROCESS_HYPERLINK);
			return true;
		}

		return false;
	}

	public boolean imageOnlyCheck() {
		int uX = getContextWidth() / 10;
		int uY = getContextHeight() / 10;
		int[] coordinateX = { uX*2, uX*4, uX*6, uX*8, uX*2, uX*4, uX*6, uX*8, uX*2, uX*4, uX*6, uX*8, uX*2, uX*4, uX*6, uX*8};
		int[] coordinateY = { uY*2, uY*2, uY*2, uY*2, uY*4, uY*4, uY*4, uY*4, uY*6, uY*6, uY*6, uY*6, uY*8, uY*8, uY*8, uY*8};
		boolean isPageHasImage = false;
		boolean isPageHasText = false;
		ZLTextRegion hyperlinkRegion = null;

		for (int i = 0; i < coordinateX.length; i++) {
			hyperlinkRegion = findRegion(coordinateX[i], coordinateY[i], maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);

			if (hyperlinkRegion != null) {

				ZLTextRegion.Soul soul = hyperlinkRegion.getSoul();

				if (soul instanceof ZLTextWordRegionSoul) {
					isPageHasText = true;
				} else if (soul instanceof ZLTextImageRegionSoul) {
					isPageHasImage = true;
				}
			}
		}

		if (isPageHasImage == true && isPageHasText == false) {
			return true;
		}

		return false;
	}
	
	private TextView tvBookProgress;
	private SeekBar sbBookProgress;
	
	public void setToView(TextView tv, SeekBar sb){
		tvBookProgress = tv;
		sbBookProgress = sb;
	}
}
