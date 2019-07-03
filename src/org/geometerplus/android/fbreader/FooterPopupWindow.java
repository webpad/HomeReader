package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.library.RelativePopupWindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import ntx.reader3.R;

public class FooterPopupWindow extends RelativePopupWindow {
	private static FooterPopupWindow mInstance;
	private FooterFormatPopupWindow mFooterFormatPopupWindow;
	private FooterSearchPopup mSearcgPopup;

	Button btnFormat, btnSearch, btnChapter, btnBookmark, btnRotate, btnMore;
	public static Callback mCallback;
	private Activity mActivity;
	LinearLayout layout_reader_rotate;
	boolean has_accelerometer;

	public interface Callback {
		void onFormat();

		void onSearch();

		void onChapter();

		void onBookmark();

		void onRotate();

		void onMore();
	}

	public static FooterPopupWindow getInstance(Activity activity, boolean has_accelerometer) {
		synchronized (FooterPopupWindow.class) {
			if (mInstance == null) {
				mInstance = new FooterPopupWindow(activity,has_accelerometer, mCallback);
			}
			return mInstance;
		}
	}

	FooterPopupWindow(Activity activity,  boolean has_accelerometer,Callback callback) {
		super(activity);
		setContentView(LayoutInflater.from(activity).inflate(R.layout.popup_menu_footer, null));
		setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(true);
		setAnimationStyle(0);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.has_accelerometer = has_accelerometer;
		mCallback = callback;
		mActivity = activity;
		initView(getContentView());
		show();
	}

	private void initView(View v) {
		btnFormat = (Button) v.findViewById(R.id.btn_reader_format);
		btnSearch = (Button) v.findViewById(R.id.btn_reader_search);
		btnChapter = (Button) v.findViewById(R.id.btn_reader_chapter);
		btnBookmark = (Button) v.findViewById(R.id.btn_reader_bookmark);
		btnRotate = (Button) v.findViewById(R.id.btn_reader_rotate);
		btnMore = (Button) v.findViewById(R.id.btn_reader_more);
		layout_reader_rotate = (LinearLayout) v.findViewById(R.id.layout_reader_rotate);
		if(has_accelerometer)
			layout_reader_rotate.setVisibility(View.GONE);
		btnFormat.setOnClickListener(onBtnClickListener);
		btnSearch.setOnClickListener(onBtnClickListener);
		btnChapter.setOnClickListener(onBtnClickListener);
		btnBookmark.setOnClickListener(onBtnClickListener);
		btnRotate.setOnClickListener(onBtnClickListener);
		btnMore.setOnClickListener(onBtnClickListener);
	}

	private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_reader_format:
				mButtonSelected(((Button) v));
				showPopUpMenuFormat();
				break;
			case R.id.btn_reader_search:
				mButtonSelected(((Button) v));
				showPopUpMenuSearch();
				break;
			case R.id.btn_reader_chapter:
				mButtonSelected(((Button) v));
				mCallback.onChapter();
				break;
			case R.id.btn_reader_bookmark:
				mButtonSelected(((Button) v));
				mCallback.onBookmark();
				break;
			case R.id.btn_reader_rotate:
				mButtonSelected(((Button) v));
				mCallback.onRotate();
				dismiss();
				break;
			case R.id.btn_reader_more:
				mButtonSelected(((Button) v));
				mCallback.onMore();
				break;
			}
		}
	};

	public void show() {
		btnFormat.setSelected(false);
		btnSearch.setSelected(false);
		btnChapter.setSelected(false);
		btnBookmark.setSelected(false);
		btnRotate.setSelected(false);
		btnMore.setSelected(false);

		showAtLocation(getContentView(), Gravity.CENTER | Gravity.BOTTOM, 0, 0);
	}

	private void mButtonSelected(Button btn) {
		btnFormat.setSelected(false);
		btnSearch.setSelected(false);
		btnChapter.setSelected(false);
		btnBookmark.setSelected(false);
		btnRotate.setSelected(false);
		btnMore.setSelected(false);

		if (btn != null)
			btn.setSelected(true);
	}

	private void showPopUpMenuFormat() {
		if (mFooterFormatPopupWindow == null) {
			mFooterFormatPopupWindow = new FooterFormatPopupWindow(mActivity, getContentView(),
					new FooterFormatPopupWindow.Callback() {

						@Override
						public void onDismiss() {
							// TODO Auto-generated method stub
							mButtonSelected(null);
						}
					});
		} else {
			mFooterFormatPopupWindow.show();
		}
	}
	
	private void showPopUpMenuSearch() {
		if (mSearcgPopup == null) {
			mSearcgPopup = new FooterSearchPopup(mActivity, getContentView(),
					new FooterSearchPopup.Callback() {

						@Override
						public void onDismiss() {
							mButtonSelected(null);
							dismiss();
						}
					});
		} else {
			mSearcgPopup.show();
			
		}
	}
	
}
