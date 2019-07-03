package org.geometerplus.android.fbreader.library;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import ntx.reader3.R;

public class AlertDialogFragment extends Fragment {
	public static int NEGATIVE_DEFAULT_STRING = android.R.string.cancel;
	
	private static String ARGUMENT_KEY_ALERT_MSG = "alert_msg";
	private static String ARGUMENT_KEY_ALERT_ICON_ID = "icon_id";
	private static String ARGUMENT_KEY_ALERT_TAG = "alert_tag";
	private static String AlertTag = AlertDialogFragment.class.getSimpleName();

	private ImageView mIvAlertIcon;
	private TextView mTvAlertMsg;
	private Button mBtnNegative;
	private Button mBtnPositive;

	private String mAlertMsgStr;
	private int mIconResId;

	private AlertDialogButtonClickListener callback = null;
	private String mDismissFragmentTag;

	private String mNegativeButtonTextString = "";
	private boolean mNegativeButtonVisible = false;
	private String mPositiveButtonTextString = "";

	public static AlertDialogFragment newInstance(String alertMsg, int iconResId, String tag) {
		AlertDialogFragment fragment = new AlertDialogFragment();
		Bundle args = new Bundle();

		args.putString(ARGUMENT_KEY_ALERT_MSG, alertMsg);
		args.putInt(ARGUMENT_KEY_ALERT_ICON_ID, iconResId);
		args.putString(ARGUMENT_KEY_ALERT_TAG, tag);

		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAlertMsgStr = getArguments().getString(ARGUMENT_KEY_ALERT_MSG);
		mIconResId = getArguments().getInt(ARGUMENT_KEY_ALERT_ICON_ID);
		if (getArguments().getString(ARGUMENT_KEY_ALERT_TAG) != null)
			AlertTag = getArguments().getString(ARGUMENT_KEY_ALERT_TAG);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_alert_message, container, false);
		initView(v);
		return v;
	}

	private void initView(View v) {
		mIvAlertIcon = (ImageView) v.findViewById(R.id.iv_alert_icon);
		mIvAlertIcon.setImageResource(mIconResId);
		mTvAlertMsg = (TextView) v.findViewById(R.id.tv_alert_msg);
		mTvAlertMsg.setText(mAlertMsgStr);
		mBtnNegative = (Button) v.findViewById(R.id.btn_negative);
		mBtnNegative.setText(mNegativeButtonTextString);
		if (mNegativeButtonVisible)
			mBtnNegative.setVisibility(View.VISIBLE);
		mBtnNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();

				if (callback != null)
					callback.onNegativeButtonClick(mDismissFragmentTag);
			}
		});

		mBtnPositive = (Button) v.findViewById(R.id.btn_positive);
		if (!mPositiveButtonTextString.isEmpty())
			mBtnPositive.setText(mPositiveButtonTextString);
		mBtnPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();

				if (callback != null)
					callback.onPositiveButtonClick(mDismissFragmentTag);
			}
		});
	}

	public static String getFragmentTag() {
		return AlertTag;
	}

	public void setupNegativeButton(String btnText) {
		if (!btnText.isEmpty())
			mNegativeButtonTextString = btnText;
		mNegativeButtonVisible = true;
	}

	public void setupPositiveButton(String btnText) {
		mPositiveButtonTextString = btnText;
	}

	public void registerAlertDialogButtonClickListener(AlertDialogButtonClickListener listener, String fragmentTag) {
		this.callback = listener;
		this.mDismissFragmentTag = fragmentTag;
	}

	public void dismiss() {
		getActivity().getFragmentManager().beginTransaction().remove(this).commit();
	}
}
