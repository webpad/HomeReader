package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.library.RelativePopupWindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import ntx.reader3.R;

public class SeekbarPopupWindow extends RelativePopupWindow {

	private ImageButton btnReduce, btnIncrease;
	private SeekBar seekbar;
	private Context mContext;

	public Callback mCallback;
	private View mView;
	private int mValue;

	public interface Callback {
		void onProgress(int progress);

		void onComplete(int progress);

		void onDismiss();
	}

	SeekbarPopupWindow(Context ctx, View v, int value, Callback callback) {
		super(ctx);
		setContentView(LayoutInflater.from(ctx).inflate(R.layout.popup_seekbar, null));
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(true);
		setAnimationStyle(0);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		mCallback = callback;
		initView(getContentView());
		mView = v;
		mValue = value;
		mContext = ctx;
		show();
	}

	private void initView(View v) {
		btnReduce = (ImageButton) v.findViewById(R.id.btn_reader_margin_reduce);
		btnIncrease = (ImageButton) v.findViewById(R.id.btn_reader_margin_increase);

		btnReduce.setOnClickListener(onBtnClickListener);
		btnIncrease.setOnClickListener(onBtnClickListener);

		seekbar = (SeekBar) v.findViewById(R.id.sb_margin);
		seekbar.setMax(100);
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mCallback.onComplete(mValue);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mValue = progress;
				if (fromUser) {
					mCallback.onProgress(progress);
				} else {
					mCallback.onComplete(progress);
				}

			}
		});
	}

	private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_reader_margin_reduce:
				seekbar.setProgress(mValue - 1);
				break;
			case R.id.btn_reader_margin_increase:
				seekbar.setProgress(mValue + 1);
				break;
			}
		}
	};

	public void show() {
		int[] location = new int[2];
		mView.getLocationOnScreen(location);

		int mViewWidth = 112;
		int mViewHeight = mView.getBottom() + 20; // mView margin = 20

		int x = location[0] - (mContext.getResources().getDisplayMetrics().widthPixels / 2) + mViewWidth / 2;
		int y = mContext.getResources().getDisplayMetrics().heightPixels - location[1] + mViewHeight;

		seekbar.setProgress(mValue);
		showAtLocation(getContentView(), Gravity.CENTER | Gravity.BOTTOM, x, y);
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		mCallback.onDismiss();
	}
}
