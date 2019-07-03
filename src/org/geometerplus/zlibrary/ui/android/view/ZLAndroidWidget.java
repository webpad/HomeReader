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

package org.geometerplus.zlibrary.ui.android.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import com.ntx.api.RefreshClass;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.SystemInfo;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.ui.android.view.animation.AnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.CurlAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.NoneAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.ShiftAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.SlideAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.SlideOldStyleAnimationProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZLAndroidWidget extends MainView implements ZLViewWidget, View.OnLongClickListener {
	public final ExecutorService PrepareService = Executors.newSingleThreadExecutor();

	private final Paint myPaint = new Paint();

	private final int footerOffset = -10;
	private final BitmapManagerImpl myBitmapManager = new BitmapManagerImpl(this);
	private Bitmap myFooterBitmap;
	private final SystemInfo mySystemInfo;
	private ScaleGestureDetector mScaleDetector;
	private boolean isInFastMode = false;
	private long delayFullRefreshAndResetMode = 1000;
	private int refreshPages[] = { 1, 3, 6, 10, 20, 30, 50, 1000000 };

	Handler mHandler = new Handler();
	Runnable runReset = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			doFullRefreshAndResetMode();
		}
	};

	private void doFullRefreshAndResetMode() {
		Intent myIntent = new Intent("ntx.eink_control.GLOBAL_REFRESH");
		myIntent.putExtra("updatemode", RefreshClass.UPDATE_MODE_GLOBAL_RESET);
		myIntent.putExtra("commandFromNtxApp", true);
		getContext().sendBroadcast(myIntent);
		
		Intent quickFullRefreshIntent = new Intent("ntx.eink_control.QUICK_REFRESH");
		quickFullRefreshIntent.putExtra("updatemode", RefreshClass.UPDATE_MODE_SCREEN);
		quickFullRefreshIntent.putExtra("commandFromNtxApp", true);
		getContext().sendBroadcast(quickFullRefreshIntent);
		
		myRefreshCounter = 0;
		FBView.isFastMode = false;
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mySystemInfo = Paths.systemInfo(context);
		init(context);
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mySystemInfo = Paths.systemInfo(context);
		init(context);
	}

	public ZLAndroidWidget(Context context) {
		super(context);
		mySystemInfo = Paths.systemInfo(context);
		init(context);
	}

	private void init(Context context) {
		// next line prevent ignoring first onKeyDown DPad event
		// after any dialog was closed
		setFocusableInTouchMode(true);
		setDrawingCacheEnabled(false);
		setOnLongClickListener(this);
		checkRefreshFrequency(); // Jacky 20160331
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		getAnimationProvider().terminate();
		if (myScreenIsTouched) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			myScreenIsTouched = false;
			view.onScrollingFinished(ZLView.PageIndex.current);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		final Context context = getContext();
		if (context instanceof FBReader) {
			((FBReader) context).createWakeLock();
		} else {
			System.err.println("A surprise: view's context is not an FBReader");
		}
		super.onDraw(canvas);

		// final int w = getWidth();
		// final int h = getMainAreaHeight();

		myBitmapManager.setSize(getWidth(), getMainAreaHeight());
		if (getAnimationProvider().inProgress()) {
			onDrawInScrolling(canvas);
		} else {
			onDrawStatic(canvas);
			ZLApplication.Instance().onRepaintFinished();
		}
	}

	private AnimationProvider myAnimationProvider;
	private ZLView.Animation myAnimationType;
	private AnimationProvider getAnimationProvider() {
		final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
		if (myAnimationProvider == null || myAnimationType != type) {
			myAnimationType = type;
			switch (type) {
			case none:
				myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
				break;
			case curl:
				myAnimationProvider = new CurlAnimationProvider(myBitmapManager);
				break;
			case slide:
				myAnimationProvider = new SlideAnimationProvider(myBitmapManager);
				break;
			case slideOldStyle:
				myAnimationProvider = new SlideOldStyleAnimationProvider(myBitmapManager);
				break;
			case shift:
				myAnimationProvider = new ShiftAnimationProvider(myBitmapManager);
				break;
			}
		}
		return myAnimationProvider;
	}

	private void onDrawInScrolling(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();

		final AnimationProvider animator = getAnimationProvider();
		final AnimationProvider.Mode oldMode = animator.getMode();
		animator.doStep();
		if (animator.inProgress()) {
			animator.draw(canvas);
			if (animator.getMode().Auto) {
				postInvalidate();
			}
			drawFooter(canvas, animator);
		} else {
			switch (oldMode) {
				case AnimatedScrollingForward:
				{
				final ZLView.PageIndex index = animator.getPageToScrollTo();
				myBitmapManager.shift(index == ZLView.PageIndex.next);
				view.onScrollingFinished(index);
				ZLApplication.Instance().onRepaintFinished();
				break;
			}
			case AnimatedScrollingBackward:
				view.onScrollingFinished(ZLView.PageIndex.current);
				break;
			}
			onDrawStatic(canvas);
		}
	}

	@Override
	public void reset() {
		myBitmapManager.reset();
	}

	@Override
	public void repaint() {
		postInvalidate(RefreshClass.UPDATE_MODE_PARTIAL_GL16);
	}

	@Override
	public void startManualScrolling(int x, int y, ZLView.Direction direction) {
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
		animator.startManualScrolling(x, y);
	}

	@Override
	public void scrollManuallyTo(int x, int y) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final AnimationProvider animator = getAnimationProvider();
		if (view.canScroll(animator.getPageToScrollTo(x, y))) {
			animator.scrollTo(x, y);
//			postInvalidate(); // Jacky mark 20160526 is already use at ==> startAnimatedScrolling(int x, int y, int speed)
		}
	}

	@Override
	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
		animator.startAnimatedScrolling(pageIndex, x, y, speed);
		if (animator.getMode().Auto) {
			doPostInvalidate();
		}
	}
	
	private void doPostInvalidate() {
		if (!FBView.isFastMode) {
			if (isInFastMode == true) {
				isInFastMode = false;
			}
			myRefreshCounter++;
			if (myRefreshCounter >= myRefreshFrequency) {
				myRefreshCounter = 0;
				postInvalidate(RefreshClass.UPDATE_MODE_SCREEN);
			} else {
				postInvalidate(RefreshClass.UPDATE_MODE_FULL_GLR16_WAIT);
			}
		} else {
			if (isInFastMode == false) {
				isInFastMode = true;
				myRefreshCounter = 0;
				changeToFastMode();
				postInvalidate(RefreshClass.UPDATE_MODE_SET_FULL_A2_MONO_WAIT);
			} else {
				postInvalidate();
			}
			mHandler.removeCallbacks(runReset);
			mHandler.postDelayed(runReset, delayFullRefreshAndResetMode);
		}
	}

	private void changeToFastMode() {
		Context context = getContext();
		Intent myIntent = new Intent("ntx.eink_control.GLOBAL_REFRESH");
		myIntent.putExtra("updatemode", RefreshClass.UPDATE_MODE_SET_PARTIAL_A2_MONO_WAIT);
		myIntent.putExtra("commandFromNtxApp", true);
		context.sendBroadcast(myIntent);
	}

	@Override
	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
		animator.startAnimatedScrolling(pageIndex, null, null, speed);
		if (animator.getMode().Auto) {
			doPostInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(int x, int y, int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final AnimationProvider animator = getAnimationProvider();
		if (!view.canScroll(animator.getPageToScrollTo(x, y))) {
			animator.terminate();
			return;
		}
		animator.startAnimatedScrolling(x, y, speed);
		//doPostInvalidate();
		postInvalidate();
	}

	void drawOnBitmap(Bitmap bitmap, ZLView.PageIndex index) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view == null) {
			return;
		}

		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
			mySystemInfo,
			new Canvas(bitmap),
			new ZLAndroidPaintContext.Geometry(
				getWidth(),
				getHeight(),
				getWidth(),
				getMainAreaHeight(),
				0,
				0
			),
			view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
		);
		view.paint(context, index);
	}

	private void drawFooter(Canvas canvas, AnimationProvider animator) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final ZLView.FooterArea footer = view.getFooterArea();

		if (footer == null) {
			myFooterBitmap = null;
			return;
		}

		if (myFooterBitmap != null &&
			(myFooterBitmap.getWidth() != getWidth() ||
			 myFooterBitmap.getHeight() != footer.getHeight())) {
			myFooterBitmap = null;
		}
		if (myFooterBitmap == null) {
			myFooterBitmap = Bitmap.createBitmap(getWidth(), footer.getHeight(), Bitmap.Config.RGB_565);
		}
		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
			mySystemInfo,
			new Canvas(myFooterBitmap),
			new ZLAndroidPaintContext.Geometry(
				getWidth(),
				getHeight(),
				getWidth(),
				footer.getHeight(),
				0,
				getMainAreaHeight()
			),
			view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
		);
		footer.paint(context);
		final int voffset = getHeight() - footer.getHeight();
		if (animator != null) {
			animator.drawFooterBitmap(canvas, myFooterBitmap, voffset + footerOffset);
		} else {
			canvas.drawBitmap(myFooterBitmap, 0, voffset + footerOffset, myPaint);
		}
	}

	private void onDrawStatic(final Canvas canvas) {
		canvas.drawBitmap(myBitmapManager.getBitmap(ZLView.PageIndex.current), 0, 0, myPaint);
		drawFooter(canvas, null);
		post(new Runnable() {
			public void run() {
				PrepareService.execute(new Runnable() {
					public void run() {
						final ZLView view = ZLApplication.Instance().getCurrentView();
						final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
							mySystemInfo,
							canvas,
							new ZLAndroidPaintContext.Geometry(
								getWidth(),
								getHeight(),
								getWidth(),
								getMainAreaHeight(),
								0,
								0
							),
							view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
						);
						view.preparePage(context, ZLView.PageIndex.next);
					}
				});
			}
		});
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
		} else {
			ZLApplication.Instance().getCurrentView().onTrackballRotated((int)(10 * event.getX()), (int)(10 * event.getY()));
		}
		return true;
	}

	private class LongClickRunnable implements Runnable {
		@Override
		public void run() {
			if (performLongClick()) {
				myLongClickPerformed = true;
			}
		}
	}
	private volatile LongClickRunnable myPendingLongClickRunnable;
	private volatile boolean myLongClickPerformed;

	private void postLongClickRunnable() {
		myLongClickPerformed = false;
		myPendingPress = false;
		if (myPendingLongClickRunnable == null) {
			myPendingLongClickRunnable = new LongClickRunnable();
		}
		postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout());
	}

	private class ShortClickRunnable implements Runnable {
		@Override
		public void run() {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			view.onFingerSingleTap(myPressedX, myPressedY);
			myPendingPress = false;
			myPendingShortClickRunnable = null;
		}
	}
	private volatile ShortClickRunnable myPendingShortClickRunnable;

	private volatile boolean myPendingPress;
	private volatile boolean myPendingDoubleTap;
	private int myPressedX, myPressedY;
	private boolean myScreenIsTouched;
	private boolean myScaleOnProgress;
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		mScaleDetector.onTouchEvent(event);

		if (myScaleOnProgress)
			return true;

		int x = (int) event.getX();
		int y = (int) event.getY();

		final ZLView view = ZLApplication.Instance().getCurrentView();
		switch (event.getAction()) {
		case MotionEvent.ACTION_CANCEL:
			myPendingDoubleTap = false;
			myPendingPress = false;
			myScreenIsTouched = false;
			myLongClickPerformed = false;
			if (myPendingShortClickRunnable != null) {
				removeCallbacks(myPendingShortClickRunnable);
				myPendingShortClickRunnable = null;
			}
			if (myPendingLongClickRunnable != null) {
				removeCallbacks(myPendingLongClickRunnable);
				myPendingLongClickRunnable = null;
			}
			view.onFingerEventCancelled();
			break;
		case MotionEvent.ACTION_UP:
			if (myPendingDoubleTap) {
				view.onFingerDoubleTap(x, y);
			} else if (myLongClickPerformed) {
				view.onFingerReleaseAfterLongPress(x, y);
			} else {
				if (myPendingLongClickRunnable != null) {
					removeCallbacks(myPendingLongClickRunnable);
					myPendingLongClickRunnable = null;
				}
				if (myPendingPress) {
					if (view.isDoubleTapSupported()) {
						if (myPendingShortClickRunnable == null) {
							myPendingShortClickRunnable = new ShortClickRunnable();
						}
						postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
					} else {
						FBReader fbreader = ((FBReader) getContext());
						if (false == fbreader.navigateClose()){
							view.onFingerSingleTap(x, y);
						}
					}
				} else {
					view.onFingerRelease(x, y);
				}
			}
			myPendingDoubleTap = false;
			myPendingPress = false;
			myScreenIsTouched = false;
			break;
		case MotionEvent.ACTION_DOWN:
			if (myPendingShortClickRunnable != null) {
				removeCallbacks(myPendingShortClickRunnable);
				myPendingShortClickRunnable = null;
				myPendingDoubleTap = true;
			} else {
				postLongClickRunnable();
				myPendingPress = true;
			}
			myScreenIsTouched = true;
			myPressedX = x;
			myPressedY = y;
			break;
			case MotionEvent.ACTION_MOVE:
			{
			final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
				final boolean isAMove =
					Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
			if (isAMove) {
				myPendingDoubleTap = false;
			}
			if (myLongClickPerformed) {
				view.onFingerMoveAfterLongPress(x, y);
			} else {
				if (myPendingPress) {
					if (isAMove) {
						if (myPendingShortClickRunnable != null) {
							removeCallbacks(myPendingShortClickRunnable);
							myPendingShortClickRunnable = null;
						}
						if (myPendingLongClickRunnable != null) {
							removeCallbacks(myPendingLongClickRunnable);
						}
						view.onFingerPress(myPressedX, myPressedY);
						myPendingPress = false;
					}
				}
				if (!myPendingPress) {
					view.onFingerMove(x, y);
				}
			}
			break;
		}
		}

		return true;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			myScaleOnProgress = true;
			
			FBReader fbreader = ((FBReader) getContext());
			fbreader.showNavigationFontSize();
			
			/* 20181213 disable check fontsize flag, because if close bookmark dialog, do not show font size Navigation,
			 * or if onScaleEnd change flag set fbreader.showPopup = "", progress bar is not work.
			 * 
			FBReader fbreader = ((FBReader) getContext());
			
			if (fbreader instanceof FBReader) {
				if (!fbreader.showPopup.equals(FBReader.SHOWPOPUP_FONTSIZE)) {
					fbreader.showNavigationFontSize();
				}
			}
			*/
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			ZLApplication.Instance().getCurrentView().onFingerScaleCloseTagDialog();
			
			FBReader fbreader = ((FBReader) getContext());
			if (fbreader instanceof FBReader) {
				if (detector.getScaleFactor() > 1)
					fbreader.changeNavigationFontSizeProgress(2);
				else
					fbreader.changeNavigationFontSizeProgress(-2);
			}
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			super.onScaleEnd(detector);
			myScaleOnProgress = false;
			FBReader fbreader = ((FBReader) getContext());
			if (fbreader instanceof FBReader) {
				fbreader.setFontSize();
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		return view.onFingerLongPress(myPressedX, myPressedY);
	}

	private int myKeyUnderTracking = -1;
	private long myTrackingStartTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		/* ------ Workaround :: Fix android 4.4 pressed front light key to back issue ------ */
		if (android.os.Build.VERSION.SDK_INT == 19 && keyCode == 97) {
			isFrontLightON();
			return true;
		}
		final ZLView view = ZLApplication.Instance().getCurrentView();
		switch (keyCode) {

		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_VOLUME_DOWN:

			myBitmapManager.shift(true);
			view.onScrollingFinished(ZLView.PageIndex.next);
			postInvalidate();
			return true;

		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_VOLUME_UP:

			myBitmapManager.shift(true);
			view.onScrollingFinished(ZLView.PageIndex.previous);
			postInvalidate();
			return true;
		}

		final ZLApplication application = ZLApplication.Instance();
		final ZLKeyBindings bindings = application.keyBindings();

		if (bindings.hasBinding(keyCode, true) ||
			bindings.hasBinding(keyCode, false)) {
			if (myKeyUnderTracking != -1) {
				if (myKeyUnderTracking == keyCode) {
					return true;
				} else {
					myKeyUnderTracking = -1;
				}
			}
			if (bindings.hasBinding(keyCode, true)) {
				myKeyUnderTracking = keyCode;
				myTrackingStartTime = System.currentTimeMillis();
				return true;
			} else {
				return application.runActionByKey(keyCode, false);
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (myKeyUnderTracking != -1) {
			if (myKeyUnderTracking == keyCode) {
				final boolean longPress = System.currentTimeMillis() >
					myTrackingStartTime + ViewConfiguration.getLongPressTimeout();
				ZLApplication.Instance().runActionByKey(keyCode, longPress);
			}
			myKeyUnderTracking = -1;
			return true;
		} else {
			final ZLKeyBindings bindings = ZLApplication.Instance().keyBindings();
			return
				bindings.hasBinding(keyCode, false) ||
				bindings.hasBinding(keyCode, true);
		}
	}

	@Override
	protected int computeVerticalScrollExtent() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		final AnimationProvider animator = getAnimationProvider();
		if (animator.inProgress()) {
			final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbLength(animator.getPageToScrollTo());
			final int percent = animator.getScrolledPercent();
			return (from * (100 - percent) + to * percent) / 100;
		} else {
			return view.getScrollbarThumbLength(ZLView.PageIndex.current);
		}
	}

	@Override
	protected int computeVerticalScrollOffset() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		final AnimationProvider animator = getAnimationProvider();
		if (animator.inProgress()) {
			final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbPosition(animator.getPageToScrollTo());
			final int percent = animator.getScrolledPercent();
			return (from * (100 - percent) + to * percent) / 100;
		} else {
			return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
		}
	}

	@Override
	protected int computeVerticalScrollRange() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		return view.getScrollbarFullSize();
	}

	private int getMainAreaHeight() {
		final ZLView.FooterArea footer = ZLApplication.Instance().getCurrentView().getFooterArea();
		return footer != null ? getHeight() - footer.getHeight() : getHeight();
	}

	@Override
	protected void updateColorLevel() {
		ViewUtil.setColorLevel(myPaint, myColorLevel);
	}

// ==================================== Jacky 20160331 ====================================
	private int myRefreshCounter = 0;
	private int myRefreshFrequency = 30;

	public void checkRefreshFrequency() {
		// ContentResolver resolver = getContext().getContentResolver();
//        final int currentRefresh = Settings.System.getInt(resolver, SCREEN_REFRESH_FREQUENCY, 6);
		// if(myRefreshFrequency != currentRefresh) {
		// myRefreshFrequency = currentRefresh;
		// }

		String[] pageStr = {
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("eachpage").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each2page").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each3page").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each4page").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each5page").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each6page").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each7page").getValue(),
    			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each8page").getValue(),
    		};
		for (int i = 0; i < pageStr.length; i++) {
			if (ZLibrary.Instance().getScreenRefreshOption().getValue().equals(pageStr[i])) {
				// myRefreshFrequency = i+1;
				myRefreshFrequency = refreshPages[i];
				break;
			}
		}
	}
}
