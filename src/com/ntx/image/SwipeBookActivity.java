package com.ntx.image;

import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.view.ZoomImageView;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import ntx.reader3.R;

public class SwipeBookActivity extends Activity {
	public static final String URL_KEY = "fbreader.imageview.url";

	private int refreshTime=10; // 10= 1second;
	private TextView txt_image_path;
	private FrameLayout frame_layout;
	
	private ZoomImageView image;
	private ImageView image_scale;

	private boolean lastIsInitRatio=true;
	private boolean isClose=false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ntx_image_swipe);

		final Intent intent = getIntent();

		final String url = intent.getStringExtra(URL_KEY);
		final String prefix = ZLFileImage.SCHEME + "://";
		Bitmap myBitmap=null;
		
		if (url != null && url.startsWith(prefix)) {
			final ZLFileImage image = ZLFileImage.byUrlPath(url.substring(prefix.length()));
			if (image == null) {
				finish();
			}
			try {
				final ZLImageData imageData = ZLImageManager.Instance().getImageData(image);
				myBitmap = ((ZLAndroidImageData)imageData).getFullSizeBitmap();
			} catch (Exception e) {
				e.printStackTrace();
				finish();
			}
		} else {
			finish();
		}
		
		initInstantKeys();
		initImage();
		setImage(myBitmap);

	}
	
	private void initInstantKeys() {
		txt_image_path = (TextView) findViewById(R.id.txt_image_path);
		txt_image_path.setVisibility(View.GONE);
		
		frame_layout=(FrameLayout) findViewById(R.id.frame_layout);
		frame_layout.setVisibility(View.GONE);
		
		image_scale=(ImageView)findViewById(R.id.image_scale);
		image_scale.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@SuppressLint("ClickableViewAccessibility")
	public void initImage(){
		image=(ZoomImageView) findViewById(R.id.image);
		image.initZoom(image);
		image.setBackgroundColor(color.white);
		image.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				
				if (isClose==false){
					image_scale.setImageDrawable(getResources().getDrawable(R.drawable.btn_close));
					isClose=true;
				}
				
				switch (event.getAction()) { // 判斷觸控的動作
			        case MotionEvent.ACTION_DOWN: // 放開
			        	lastIsInitRatio=image.isInitRatio;
			        	return true;
			        case MotionEvent.ACTION_UP: // 放開
	
			        	if ((image.pointerCount==2 && !(lastIsInitRatio && image.isInitRatio)) || image.currentStatus == image.STATUS_MOVE){
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

	public void setImage(Bitmap myBitmap){
		image.initZoom(image);
		image.setImageBitmap(myBitmap);
		image.refreshScreen(refreshTime);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.gc();
	}
}