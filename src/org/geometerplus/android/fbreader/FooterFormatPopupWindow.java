package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.library.RelativePopupWindow;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import ntx.reader3.R;

public class FooterFormatPopupWindow extends RelativePopupWindow {
	private static FooterFormatPopupWindow mInstance;

	private ImageButton btnFontSizeReduce, btnFontSizeIncrease;
	private ImageButton btnAlignNone, btnAlignLeft, btnAlignCenter, btnAlignRight;
	private ImageButton btnLineSpacingReduce, btnLineSpacingIncrease;

	private SeekBar sbFontSize, sbLineSpacing;

	private static View mView;
	private FBReaderApp myFBReaderApp;
	private int fontSize = 20;
	private int lineSpacing = 12;

	private ImageButton btnMarginLinkTopBottom, btnMarginLinkLeftRight;
	private Button btnMarginTop, btnMarginBottom, btnMarginLeft, btnMarginRight;

	private Context mContext;
	public static Callback mCallback;

	public interface Callback {
		void onDismiss();
	}

	public static FooterFormatPopupWindow getInstance(Context ctx) {
		synchronized (FooterFormatPopupWindow.class) {
			if (mInstance == null) {
				mInstance = new FooterFormatPopupWindow(ctx.getApplicationContext(), mView, mCallback);
			}
			return mInstance;
		}
	}

	FooterFormatPopupWindow(Context ctx, View view, Callback callback) {
		super(ctx);
		setContentView(LayoutInflater.from(ctx).inflate(R.layout.popup_menu_footer_format, null));
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(true);
		setAnimationStyle(0);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mCallback = callback;

		myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
		fontSize = getFontSize();

		initView(getContentView());
		mView = view;
		mContext = ctx;
		show();
	}

	private void initView(View v) {

		btnFontSizeReduce = (ImageButton) v.findViewById(R.id.btn_reader_fontsize_reduce);
		btnFontSizeIncrease = (ImageButton) v.findViewById(R.id.btn_reader_fontsize_increase);
		sbFontSize = (SeekBar) v.findViewById(R.id.sb_font_size);

		btnFontSizeReduce.setOnClickListener(onBtnClickListener);
		btnFontSizeIncrease.setOnClickListener(onBtnClickListener);

		sbFontSize.setMax(144);
		sbFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setFontSize(fontSize);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				fontSize = progress;
			}
		});

		btnAlignNone = (ImageButton) v.findViewById(R.id.btn_reader_align_none);
		btnAlignLeft = (ImageButton) v.findViewById(R.id.btn_reader_align_left);
		btnAlignCenter = (ImageButton) v.findViewById(R.id.btn_reader_align_center);
		btnAlignRight = (ImageButton) v.findViewById(R.id.btn_reader_align_right);

		btnAlignNone.setOnClickListener(onBtnClickListener);
		btnAlignLeft.setOnClickListener(onBtnClickListener);
		btnAlignCenter.setOnClickListener(onBtnClickListener);
		btnAlignRight.setOnClickListener(onBtnClickListener);

		btnLineSpacingReduce = (ImageButton) v.findViewById(R.id.btn_reader_line_spacing_reduce);
		btnLineSpacingIncrease = (ImageButton) v.findViewById(R.id.btn_reader_line_spacing_increase);

		btnLineSpacingReduce.setOnClickListener(onBtnClickListener);
		btnLineSpacingIncrease.setOnClickListener(onBtnClickListener);

		sbLineSpacing = (SeekBar) v.findViewById(R.id.sb_line_spacing);
		sbLineSpacing.setMax(20);
		sbLineSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setLineSpacing(lineSpacing);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				lineSpacing = progress;
			}
		});

		btnMarginTop = (Button) v.findViewById(R.id.btn_margin_top);
		btnMarginBottom = (Button) v.findViewById(R.id.btn_margin_bottom);
		btnMarginLeft = (Button) v.findViewById(R.id.btn_margin_left);
		btnMarginRight = (Button) v.findViewById(R.id.btn_margin_right);

		btnMarginLinkTopBottom = (ImageButton) v.findViewById(R.id.btn_top_bot_link);
		btnMarginLinkLeftRight = (ImageButton) v.findViewById(R.id.btn_left_right_link);

		btnMarginTop.setOnClickListener(onBtnClickListener);
		btnMarginBottom.setOnClickListener(onBtnClickListener);
		btnMarginLeft.setOnClickListener(onBtnClickListener);
		btnMarginRight.setOnClickListener(onBtnClickListener);
		btnMarginLinkTopBottom.setOnClickListener(onBtnClickListener);
		btnMarginLinkLeftRight.setOnClickListener(onBtnClickListener);

	}

	private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_reader_fontsize_reduce:
				setFontReduce();
				break;
			case R.id.btn_reader_fontsize_increase:
				setFontIncrease();
				break;
			case R.id.btn_reader_align_none:
				setAlignmentValue(ZLTextAlignmentType.ALIGN_UNDEFINED);
				break;
			case R.id.btn_reader_align_left:
				setAlignmentValue(ZLTextAlignmentType.ALIGN_LEFT);
				break;
			case R.id.btn_reader_align_center:
				setAlignmentValue(ZLTextAlignmentType.ALIGN_CENTER);
				break;
			case R.id.btn_reader_align_right:
				setAlignmentValue(ZLTextAlignmentType.ALIGN_RIGHT);
				break;
			case R.id.btn_reader_line_spacing_reduce:
				setLineSpacing(getLineSpacing() - 1);
				break;
			case R.id.btn_reader_line_spacing_increase:
				setLineSpacing(getLineSpacing() + 1);
				break;
			case R.id.btn_margin_top:
				showPopUpTopMargin();
				break;
			case R.id.btn_margin_bottom:
				showPopUpBottomMargin();
				break;
			case R.id.btn_margin_left:
				showPopUpLeftMargin();
				break;
			case R.id.btn_margin_right:
				showPopUpRightMargin();
				break;
			case R.id.btn_top_bot_link:
				btnMarginLinkTopBottom.setSelected(!btnMarginLinkTopBottom.isSelected());
				break;
			case R.id.btn_left_right_link:
				btnMarginLinkLeftRight.setSelected(!btnMarginLinkLeftRight.isSelected());

				break;
			}
		}
	};

	@SuppressLint("NewApi")
	public void show() {

		if (null != mView) {
			showAtLocation(getContentView(), Gravity.CENTER | Gravity.BOTTOM, 0, mView.getBottom());
		} else {
			showAtLocation(getContentView(), Gravity.CENTER | Gravity.BOTTOM, 0, 0);
		}

		setCurrentValue();
	}

	private void setFontSize(int fontsize) {

		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		option.setValue(fontsize);

		sbFontSize.setProgress(getFontSize());
		refresh();

	}

	private int getFontSize() {
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		return option.getValue();
	}

	private void setFontIncrease() {
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() + 1);
		sbFontSize.setProgress(getFontSize());

		refresh();
	}

	private void setFontReduce() {
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection()
				.getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() - 1);
		sbFontSize.setProgress(getFontSize());

		refresh();
	}

	private void refresh() {
		myFBReaderApp.clearTextCaches();
		myFBReaderApp.getViewWidget().repaint();
	}

	private byte getAlignment() {

		return myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().getAlignment();
	}

	private void setAlignmentValue(byte align) {
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection()
				.getBaseStyle().AlignmentOption;
		option.setValue(align);
		setAlignmentIcon(align);

		refresh();
	}

	private void setAlignmentIcon(byte align) {
		btnAlignNone.setSelected(false);
		btnAlignLeft.setSelected(false);
		btnAlignCenter.setSelected(false);
		btnAlignRight.setSelected(false);
		;

		switch (align) {
		case ZLTextAlignmentType.ALIGN_UNDEFINED:
			btnAlignNone.setSelected(true);
			break;
		case ZLTextAlignmentType.ALIGN_LEFT:
			btnAlignLeft.setSelected(true);
			break;
		case ZLTextAlignmentType.ALIGN_CENTER:
			btnAlignCenter.setSelected(true);
			break;
		case ZLTextAlignmentType.ALIGN_RIGHT:
			btnAlignRight.setSelected(true);
			break;
		}
	}

	private int getLineSpacing() {
		return myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().getLineSpacePercent() / 10;
	}

	private void setLineSpacing(int lineSpacing) {
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection()
				.getBaseStyle().LineSpaceOption;
		option.setValue(lineSpacing);
		sbLineSpacing.setProgress(getLineSpacing());
		refresh();
	}

	private void setCurrentValue() {
		setFontSize(getFontSize());
		setAlignmentValue(getAlignment());
		setLineSpacing(getLineSpacing());

		btnMarginTop.setText("" + myFBReaderApp.ViewOptions.TopMargin.getValue());
		btnMarginBottom.setText("" + myFBReaderApp.ViewOptions.BottomMargin.getValue());
		btnMarginLeft.setText("" + myFBReaderApp.ViewOptions.LeftMargin.getValue());
		btnMarginRight.setText("" + myFBReaderApp.ViewOptions.RightMargin.getValue());

	}

	private SeekbarPopupWindow mSeekbarMarginTopPopupWindow;
	private SeekbarPopupWindow mSeekbarMarginBottomPopupWindow;
	private SeekbarPopupWindow mSeekbarMarginLeftPopupWindow;
	private SeekbarPopupWindow mSeekbarMarginRightPopupWindow;

	/**
	 * 
	 * @param progress
	 * @param saveToData
	 *            : onProgress saveToData = false, onComplete saveToData = true;
	 */
	private void setTopMargin(int progress, boolean saveToData) {
		if (saveToData) {
			myFBReaderApp.ViewOptions.TopMargin.setValue(progress);

			if (btnMarginLinkTopBottom.isSelected()) {
				myFBReaderApp.ViewOptions.BottomMargin.setValue(progress);
			}
			refresh();
		}

		btnMarginTop.setText("" + progress);
		if (btnMarginLinkTopBottom.isSelected())
			btnMarginBottom.setText("" + progress);
	}

	private void setBottomMargin(int progress, boolean saveToData) {
		if (saveToData) {
			myFBReaderApp.ViewOptions.BottomMargin.setValue(progress);

			if (btnMarginLinkTopBottom.isSelected()) {
				myFBReaderApp.ViewOptions.TopMargin.setValue(progress);
			}
			refresh();
		}

		btnMarginBottom.setText("" + progress);
		if (btnMarginLinkTopBottom.isSelected())
			btnMarginTop.setText("" + progress);
	}

	private void setLeftMargin(int progress, boolean saveToData) {
		if (saveToData) {
			myFBReaderApp.ViewOptions.LeftMargin.setValue(progress);

			if (btnMarginLinkLeftRight.isSelected()) {
				myFBReaderApp.ViewOptions.RightMargin.setValue(progress);
			}
			refresh();
		}

		btnMarginLeft.setText("" + progress);
		if (btnMarginLinkLeftRight.isSelected())
			btnMarginRight.setText("" + progress);
	}

	private void setRightMargin(int progress, boolean saveToData) {
		if (saveToData) {
			myFBReaderApp.ViewOptions.RightMargin.setValue(progress);

			if (btnMarginLinkTopBottom.isSelected()) {
				myFBReaderApp.ViewOptions.LeftMargin.setValue(progress);
			}

			refresh();
		}

		btnMarginRight.setText("" + progress);
		if (btnMarginLinkLeftRight.isSelected())
			btnMarginLeft.setText("" + progress);
	}

	private void showPopUpTopMargin() {
		mButtonSelected(btnMarginTop);

		if (mSeekbarMarginTopPopupWindow == null) {
			mSeekbarMarginTopPopupWindow = new SeekbarPopupWindow(mContext, btnMarginTop,
					myFBReaderApp.ViewOptions.TopMargin.getValue(), new SeekbarPopupWindow.Callback() {

						@Override
						public void onProgress(int progress) {
							setTopMargin(progress, false);
						}

						@Override
						public void onComplete(int progress) {
							setTopMargin(progress, true);
						}

						@Override
						public void onDismiss() {
							mButtonSelected(null);
						}
					});
		} else {
			mSeekbarMarginTopPopupWindow.show();
		}
	}

	private void showPopUpBottomMargin() {
		mButtonSelected(btnMarginBottom);

		if (mSeekbarMarginBottomPopupWindow == null) {
			mSeekbarMarginBottomPopupWindow = new SeekbarPopupWindow(mContext, btnMarginBottom,
					myFBReaderApp.ViewOptions.BottomMargin.getValue(), new SeekbarPopupWindow.Callback() {

						@Override
						public void onProgress(int progress) {
							setBottomMargin(progress, false);
						}

						@Override
						public void onComplete(int progress) {
							setBottomMargin(progress, true);
						}

						@Override
						public void onDismiss() {
							mButtonSelected(null);
						}
					});
		} else {
			mSeekbarMarginBottomPopupWindow.show();
		}
	}

	private void showPopUpLeftMargin() {
		mButtonSelected(btnMarginLeft);

		if (mSeekbarMarginLeftPopupWindow == null) {
			mSeekbarMarginLeftPopupWindow = new SeekbarPopupWindow(mContext, btnMarginLeft,
					myFBReaderApp.ViewOptions.LeftMargin.getValue(), new SeekbarPopupWindow.Callback() {

						@Override
						public void onProgress(int progress) {
							setLeftMargin(progress, false);
						}

						@Override
						public void onComplete(int progress) {
							setLeftMargin(progress, true);
							;
						}

						@Override
						public void onDismiss() {
							mButtonSelected(null);
						}
					});
		} else {
			mSeekbarMarginLeftPopupWindow.show();
		}
	}

	private void showPopUpRightMargin() {
		mButtonSelected(btnMarginRight);

		if (mSeekbarMarginRightPopupWindow == null) {
			mSeekbarMarginRightPopupWindow = new SeekbarPopupWindow(mContext, btnMarginRight,
					myFBReaderApp.ViewOptions.RightMargin.getValue(), new SeekbarPopupWindow.Callback() {

						@Override
						public void onProgress(int progress) {
							setRightMargin(progress, false);
						}

						@Override
						public void onComplete(int progress) {
							setRightMargin(progress, true);
						}

						@Override
						public void onDismiss() {
							mButtonSelected(null);
						}
					});
		} else {
			mSeekbarMarginRightPopupWindow.show();
		}
	}

	private void mButtonSelected(Button btn) {
		btnMarginTop.setSelected(false);
		btnMarginBottom.setSelected(false);
		btnMarginLeft.setSelected(false);
		btnMarginRight.setSelected(false);

		if (btn != null)
			btn.setSelected(true);
	}

	@Override
	public void dismiss() {
		super.dismiss();

		mCallback.onDismiss();
	}
}
