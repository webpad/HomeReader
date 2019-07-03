package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.library.RelativePopupWindow;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ntx.reader3.R;

public class FooterSearchPopup extends RelativePopupWindow {
	private static FooterSearchPopup mInstance;

	private Button searchBoxClose, searchBoxSearch, searchBoxBack;
	private EditText searchText;

	private static View mView;
	private FBReaderApp myFBReaderApp;

	private Context mContext;
	public static Callback mCallback;

	public interface Callback {
		void onDismiss();
	}

	public static FooterSearchPopup getInstance(Context ctx) {
		synchronized (FooterSearchPopup.class) {
			if (mInstance == null) {
				mInstance = new FooterSearchPopup(ctx.getApplicationContext(), mView, mCallback);
			}
			return mInstance;
		}
	}

	FooterSearchPopup(Context ctx, View view, Callback callback) {
		super(ctx);
		setContentView(LayoutInflater.from(ctx).inflate(R.layout.popup_menu_footer_search, null));
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(true);
		setAnimationStyle(0);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mCallback = callback;

		myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();

		initView(getContentView());
		mView = view;
		mContext = ctx;
		show();
	}

	private void initView(View v) {

		searchBoxClose = (Button) v.findViewById(R.id.btn_search_box_close);
		searchBoxSearch = (Button) v.findViewById(R.id.btn_search_box_search);
		searchBoxBack = (Button) v.findViewById(R.id.btn_search_box_back);
		searchText = (EditText) v.findViewById(R.id.et_search);
		searchText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {

					searchPattern(searchText.getText().toString());

					return true;
				}
				return false;
			}
		});

		searchBoxClose.setOnClickListener(onBtnClickListener);
		searchBoxSearch.setOnClickListener(onBtnClickListener);
		searchBoxBack.setOnClickListener(onBtnClickListener);

	}

	private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_search_box_close:
				dismiss();
				break;
			case R.id.btn_search_box_search:
				searchPattern(searchText.getText().toString());

				break;
			case R.id.btn_search_box_back:
				searchText.setText("");
				break;
			}
		}
	};

	@SuppressLint("NewApi")
	public void show() {
		showAtLocation(getContentView(), Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 20);
		searchText.requestFocus();
		showInputMethod();
	}

	@Override
	public void dismiss() {
		hideInputMethod(searchText);

		super.dismiss();
		mCallback.onDismiss();
	}

	private void showInputMethod() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	private void hideInputMethod(View view) {
		if (null != view) {
			InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
		}
	}

	private void searchPattern(String pattern) {

		final TextSearchPopup popup = (TextSearchPopup) myFBReaderApp.getPopupById(TextSearchPopup.ID);
		popup.initPosition();
		myFBReaderApp.MiscOptions.TextSearchPattern.setValue(pattern);
		if (myFBReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
			myFBReaderApp.showPopup(popup.getId());
		} else {
			Toast.makeText(mContext, ZLResource.resource("errorMessage").getResource("textNotFound").getValue(),
					Toast.LENGTH_LONG).show();
			popup.StartPosition = null;

		}
		dismiss();

	}
}
