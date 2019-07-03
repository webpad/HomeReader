package com.ntx.image;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.zlibrary.ui.android.view.ZoomImageView;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import ntx.reader3.R;

public class SwipeActivity extends Activity {

	private int currentItem = 0;
	private int distLimit = 50;
	private int refreshTime = 10; // 10= 1second;
	private Button previousButton, nextButton;
	private TextView page_info, txt_image_path;

	private ZoomImageView image;
	private Bitmap bm;
	private float downX;
	private boolean lastIsInitRatio = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ntx_image_swipe);

		processExtraData();
		initInstantKeys();
		infoText(currentItem);
		initImage();
		setImage(imagePath.get(currentItem));
	}

	private void initInstantKeys() {
		page_info = (TextView) findViewById(R.id.page_info);
		txt_image_path = (TextView) findViewById(R.id.txt_image_path);
		previousButton = (Button) findViewById(R.id.previous_page_button);
		previousButton.setOnClickListener(instantKeyListener);

		nextButton = (Button) findViewById(R.id.next_page_button);
		nextButton.setOnClickListener(instantKeyListener);
	}

	private void infoText(int current) {
		page_info.setText((current + 1) + " of " + imagePath.size());
		txt_image_path.setText(imagePath.get(currentItem));
	}

	private OnClickListener instantKeyListener = new OnClickListener() {
		// TODO
		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.previous_page_button:
				onPrevPage();
				break;
			case R.id.next_page_button:
				onNextPage();
				break;
			}
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	public void initImage() {
		image = (ZoomImageView) findViewById(R.id.image);
		image.initZoom(image);
		image.setBackgroundColor(color.white);
		image.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub

				switch (event.getAction()) { // 判斷觸控的動作

				case MotionEvent.ACTION_DOWN: // 放開
					downX = event.getX();
					lastIsInitRatio = image.isInitRatio;
					return true;
				case MotionEvent.ACTION_UP: // 放開

					if (lastIsInitRatio && image.isInitRatio && image.pointerCount == 1) {
						if (event.getX() - downX > distLimit) { // Right
							onPrevPage();
						} else if (downX - event.getX() > distLimit) {
							onNextPage();
						}
					} else if ((image.pointerCount == 2 && !(lastIsInitRatio && image.isInitRatio))
							|| image.currentStatus == image.STATUS_MOVE) {
						image.refreshScreen(refreshTime);
					}
					image.lastXMove = -1;
					image.lastYMove = -1;
					return true;
				}
				return false;
			}
		});
	}

	public void setImage(String path) {
		bm = resizeBitmapFromFile(path, getWindow().getAttributes().width, getWindow().getAttributes().height);
		image.initZoom(image);
		image.setImageBitmap(bm);
		image.refreshScreen(refreshTime / 2);

		infoText(currentItem);
	}

	public Bitmap resizeBitmapFromFile(String path, int reqWidth, int reqHeight) { // BEST
																					// QUALITY
																					// MATCH

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > reqHeight) {
			inSampleSize = Math.round((float) height / (float) reqHeight);
		}

		int expectedWidth = width / inSampleSize;

		if (expectedWidth > reqWidth) {
			// if(Math.round((float)width / (float)reqWidth) > inSampleSize) //
			// If bigger SampSize..
			inSampleSize = Math.round((float) width / (float) reqWidth);
		}
		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	final ArrayList<String> imagePath = new ArrayList<String>();

	private void processExtraData() {
		Intent intent = getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null) {
			imagePath.clear();
			imagePath.add(intent.getData().getPath());
		} else {
			imagePath.clear();
			imagePath.addAll(intent.getStringArrayListExtra("path"));
			currentItem = intent.getIntExtra("pos", 0);
		}
	}

	private void onPrevPage() {
		if (currentItem <= 0) {
			currentItem = imagePath.size() - 1;
		} else {
			currentItem--;
		}
		setImage(imagePath.get(currentItem));
	}

	public void onNextPage() {
		if (currentItem >= imagePath.size() - 1) {
			currentItem = 0;
		} else {
			currentItem++;
		}
		setImage(imagePath.get(currentItem));
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	// Intent i = new Intent(SwipeActivity.this, LibraryActivity.class);
	// try {
	// Bundle bundle = new Bundle();
	// // bundle.putString("swipe", "swipe");
	// // bundle.putBoolean("fromswipe", true);
	// i.putExtras(bundle);
	// startActivity(i);
	//
	// } catch (Exception e) {
	// // Log.d("JK_Swipe", "e="+e.toString());
	// }
	// finish();
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.gc();
	}

}