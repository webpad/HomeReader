/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ntx.reader3.R;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

final class FooterFontSizePopup extends ZLApplication.PopupPanel {
	final static String ID = "FooterFontSizePopup";

	private volatile NavigationWindow myWindow;
	private volatile FBReader myActivity;
	private volatile RelativeLayout myRoot;
	private final FBReaderApp myFBReader;
	private volatile boolean myIsInProgress;
	private SeekBar slider;

	FooterFontSizePopup(FBReaderApp fbReader) {
		super(fbReader);
		myFBReader = fbReader;
		fontsize = getFontSize();
	}

	public void setPanelInfo(FBReader activity, RelativeLayout root) {
		myActivity = activity;
		myRoot = root;
	}

	public void runNavigation() {
		final ZLIntegerRangeOption option =
				myFBReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
				fontsize=option.getValue();
				
		Application.showPopup(ID);
	}

	public boolean closeNavigation() {
		if (myWindow != null)
			if (myWindow.getVisibility() == View.VISIBLE) {
				Application.hideActivePopup();
				myFBReader.getViewWidget().reset();
				myFBReader.getViewWidget().repaint();

				return true;
			}
		
		return false;
	}
	
	public void setUpFontSizeSliderValue(int value) {
			if(fontsize == 0) {
				 ZLIntegerRangeOption option = myFBReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
				 fontsize=option.getValue();
			}
			fontsize = fontsize + value;
			slider.setProgress(fontsize);
		
	}

	@Override
	protected void show_() {
		// Daniel 20180904 : show/hide status bar when navigationPopoup shows out
		myActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		if (myActivity != null) {
			createPanel(myActivity, myRoot);
		}
		if (myWindow != null) {
			myWindow.show();
			setupNavigation();
		}
	}

	@Override
	protected void hide_() {
		// Daniel 20180904 : show/hide status bar when navigationPopoup shows out
		myActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		if (myWindow != null) {
			myWindow.hide();
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void update() {
		if (!myIsInProgress && myWindow != null) {
			setupNavigation();
		}
	}

	private void createPanel(FBReader activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getContext()) {
			return;
		}

		activity.getLayoutInflater().inflate(R.layout.navigation_panel, root);
		myWindow = (NavigationWindow)root.findViewById(R.id.navigation_panel);

		final ImageButton btnFontSizeReduce = (ImageButton) myWindow.findViewById(R.id.btn_reader_fontsize_reduce);
		final ImageButton btnFontSizeIncrease = (ImageButton) myWindow.findViewById(R.id.btn_reader_fontsize_increase);

		btnFontSizeReduce.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setFontReduce();
			}
		});
		btnFontSizeIncrease.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setFontIncrease();
			}
		});
		
		slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		final TextView text = (TextView)myWindow.findViewById(R.id.navigation_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				myIsInProgress = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				myIsInProgress = false;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
						fontsize=progress;
				        if (fontsize < 1) fontsize =1;
				        if (fontsize >150) fontsize = 150;
						text.setText(ZLResource.resource("Preferences").getResource("text").getResource("fontSize").getValue()+" : "+fontsize);

						if (isCountStart==false) {
							refreshCount=refreshLimit;
							moveTime.postDelayed(moveRun, 1);
							isCountStart=true;
						}
				}
			}
		});
		slider.setProgress(fontsize);
	}

	private void setupNavigation() {
		slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);

	        if (fontsize < 1) fontsize =1;
	        if (fontsize >150) fontsize = 150;
			slider.setMax(150);
//			slider.setProgress(fontsize);
	}

	private String makeProgressText(int page, int pagesNumber) {
		final StringBuilder builder = new StringBuilder();
		builder.append(page);
		builder.append("/");
		builder.append(pagesNumber);
		final TOCTree tocElement = myFBReader.getCurrentTOCElement();
		if (tocElement != null) {
			builder.append("  ");

		     ///////////////////////////////////////
	              //support all png epub file
			if(!tocElement.getText().equalsIgnoreCase("開始"))
			///////////////////////////////////////
			builder.append(tocElement.getText());
		}
		return builder.toString();
	}

	final void removeWindow(Activity activity) {
		if (myWindow != null && activity == myWindow.getContext()) {
			final ViewGroup root = (ViewGroup)myWindow.getParent();
			myWindow.hide();
			root.removeView(myWindow);
			myWindow = null;
		}
	}

	public final static int refreshLimit=10;
	public int refreshCount=0;
	public int fontsize=0;
	public boolean isCountStart=false;

	public Handler moveTime = new Handler();
	public Runnable moveRun = new Runnable() {
		@Override
		public void run() {

			if (refreshCount <= 0) {

				setFontSize(fontsize);

				moveTime.removeCallbacks(moveRun);
				return;
			}
			refreshCount--;
			moveTime.postDelayed(this, 100); // Delay 0.1sec
		}
	};

	public void setFontSize() {
		setFontSize(fontsize);
	}

	public void setFontSize(int fontsize){
        if (fontsize < 1) fontsize =1;
        if (fontsize >150) fontsize = 150;
		final ZLIntegerRangeOption option =
				myFBReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
			option.setValue(fontsize);

			final TextView text = (TextView)myWindow.findViewById(R.id.navigation_text);
			text.setText(ZLResource.resource("Preferences").getResource("text").getResource("fontSize").getValue()+" : "+fontsize);
           Log.d("test",""+fontsize);
			myFBReader.clearTextCaches();
			myFBReader.getViewWidget().repaint();

			isCountStart=false;
			refreshCount=refreshLimit;
	}
	
	private void setFontIncrease() {
		final ZLIntegerRangeOption option = myFBReader.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() + 1);
		slider.setProgress(option.getValue());
		refresh();
	}

	private void setFontReduce() {
		final ZLIntegerRangeOption option = myFBReader.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() - 1);
        slider.setProgress(option.getValue());
        refresh();
	}

	private int getFontSize() {
		final ZLIntegerRangeOption option = myFBReader.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() - 1);
        return option.getValue();
        
	}
	
	private void refresh() {
		myFBReader.clearTextCaches();
		myFBReader.getViewWidget().repaint();
	}
}
