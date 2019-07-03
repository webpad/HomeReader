package org.geometerplus.android.fbreader.library;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.greenrobot.eventbus.EventBus;

import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.RelativeLayout;

import ntx.reader3.R;

public class MainPageMorePopupWindow extends RelativePopupWindow {
	
	private static MainPageMorePopupWindow mInstance;

	private ImageButton mIBtnRecent;
	private ImageButton mIBtnFavorites;
	private ImageButton mIBtnAuthor;
	private ImageButton mIBtnTitle;
	private ImageButton mIBtnAllBooks;
	private ImageButton mIBtnAllImages;
	private ImageButton mIBtnExtSDcard;

	private static TextView mTvRecent;
	private static TextView mTvFavorites;
	private static TextView mTvAuthor;
	private static TextView mTvTitle;
	private static TextView mTvAllBooks;
	private static TextView mTvAllImages;
	private static TextView mTvExtSDcard;
	
	private EventBus mEventBus;
	public static MainPageMorePopupWindow getInstance(Context ctx) {
		
		synchronized (MainPageMorePopupWindow.class) {
			if (mInstance == null) {
				mInstance = new MainPageMorePopupWindow(ctx.getApplicationContext());
			}
			
			mTvRecent.setText(ZLResource.resource("library").getResource(Globals.ROOT_RECENT).getValue());
			mTvFavorites.setText(ZLResource.resource("library").getResource(Globals.ROOT_FAVORITES).getValue());
			mTvAuthor.setText(ZLResource.resource("library").getResource(Globals.ROOT_BY_AUTHOR).getValue());
			mTvTitle.setText(ZLResource.resource("library").getResource(Globals.ROOT_BY_TITLE).getValue());
			mTvAllBooks.setText(ZLResource.resource("library").getResource(Globals.ROOT_ALL_BOOKS).getValue());
			mTvAllImages.setText(ZLResource.resource("library").getResource(Globals.ROOT_ALL_IMAGES).getValue());
			mTvExtSDcard.setText(ZLResource.resource("library").getResource(Globals.ROOT_EXT_SD).getValue());
			
			return mInstance;
		}
	}

	private MainPageMorePopupWindow(Context ctx) {
		super(ctx);
		setContentView(LayoutInflater.from(ctx).inflate(R.layout.popupwindow_main_page_more, null));
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(true);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		View popupView = getContentView();

		initView(popupView);
		
		mEventBus = EventBus.getDefault();
	}

	private void initView(View v) {	
		
		mIBtnRecent = (ImageButton) v.findViewById(R.id.ibtn_more_recent);
		mIBtnFavorites = (ImageButton) v.findViewById(R.id.ibtn_more_favorite);
		mIBtnAuthor = (ImageButton) v.findViewById(R.id.ibtn_more_author);
		mIBtnTitle = (ImageButton) v.findViewById(R.id.ibtn_more_title);
		mIBtnAllBooks = (ImageButton) v.findViewById(R.id.ibtn_more_all_books);
		mIBtnAllImages = (ImageButton) v.findViewById(R.id.ibtn_more_all_images);
		mIBtnExtSDcard = (ImageButton) v.findViewById(R.id.ibtn_more_ext_sdcard);

		mIBtnRecent.setOnClickListener(onBtnClickListener);
		mIBtnFavorites.setOnClickListener(onBtnClickListener);
		mIBtnAuthor.setOnClickListener(onBtnClickListener);
		mIBtnTitle.setOnClickListener(onBtnClickListener);
		mIBtnAllBooks.setOnClickListener(onBtnClickListener);
		mIBtnAllImages.setOnClickListener(onBtnClickListener);
		mIBtnExtSDcard.setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_recent)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_favorite)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_author)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_title)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_all_books)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_all_images)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_ext_sdcard)).setOnClickListener(onBtnClickListener);
		((RelativeLayout) v.findViewById(R.id.ll_more_ext_sdcard)).setVisibility(RefreshClass.hasExternalSDCard() ? View.VISIBLE : View.GONE);
		
		mTvRecent = (TextView) v.findViewById(R.id.txt_more_recent);
		mTvFavorites = (TextView) v.findViewById(R.id.txt_more_favorite);
		mTvAuthor = (TextView) v.findViewById(R.id.txt_more_author);
		mTvTitle = (TextView) v.findViewById(R.id.txt_more_title);
		mTvAllBooks = (TextView) v.findViewById(R.id.txt_more_all_books);
		mTvAllImages = (TextView) v.findViewById(R.id.txt_more_all_images);
		mTvExtSDcard = (TextView) v.findViewById(R.id.txt_more_ext_sdcard);
	}

	private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {			
			
			switch (v.getId()) {
			case R.id.ll_more_recent:
			case R.id.ibtn_more_recent:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_RECENT);
				break;
			case R.id.ll_more_favorite:
			case R.id.ibtn_more_favorite:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_FAVORITES);
				break;
			case R.id.ll_more_author:
			case R.id.ibtn_more_author:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_AUTHOR);
				break;
			case R.id.ll_more_title:
			case R.id.ibtn_more_title:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_TITLE);
				break;
			case R.id.ll_more_all_books:
			case R.id.ibtn_more_all_books:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_ALL_BOOKS);
				break;
			case R.id.ll_more_all_images:
			case R.id.ibtn_more_all_images:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_ALL_IMAGES);
				break;
			case R.id.ll_more_ext_sdcard:
			case R.id.ibtn_more_ext_sdcard:
				setTab(v); // select tab icon
				callEvent(CallbackEvent.MORE_EXT_SD_CARD);
				break;
			}
			dismiss();
		}
	};
	
	private void callEvent(String event){
		CallbackEvent callbackEvent = new CallbackEvent();
		callbackEvent.setMessage(event);
		mEventBus.post(callbackEvent);
	}
	
	private void setTab(View v){
		mIBtnRecent.setSelected(false);
		mIBtnFavorites.setSelected(false);
		mIBtnAuthor.setSelected(false);
		mIBtnTitle.setSelected(false);
		mIBtnAllBooks.setSelected(false);
		mIBtnAllImages.setSelected(false);
		mIBtnExtSDcard.setSelected(false);
		
		switch (v.getId()) {
		case R.id.ll_more_recent:
		case R.id.ibtn_more_recent:
			mIBtnRecent.setSelected(true);
			break;
		case R.id.ll_more_favorite:
		case R.id.ibtn_more_favorite:
			mIBtnFavorites.setSelected(true);
			break;
		case R.id.ll_more_author:
		case R.id.ibtn_more_author:
			mIBtnAuthor.setSelected(true);
			break;
		case R.id.ll_more_title:
		case R.id.ibtn_more_title:
			mIBtnTitle.setSelected(true);
			break;
		case R.id.ll_more_all_books:
		case R.id.ibtn_more_all_books:
			mIBtnAllBooks.setSelected(true);
			break;
		case R.id.ll_more_all_images:
		case R.id.ibtn_more_all_images:
			mIBtnAllImages.setSelected(true);
			break;
		case R.id.ll_more_ext_sdcard:
		case R.id.ibtn_more_ext_sdcard:
			mIBtnExtSDcard.setSelected(true);
			break;
		}
	}
	
	public void setTitle(String title){
		mIBtnRecent.setSelected(false);
		mIBtnFavorites.setSelected(false);
		mIBtnAuthor.setSelected(false);
		mIBtnTitle.setSelected(false);
		mIBtnAllBooks.setSelected(false);
		mIBtnAllImages.setSelected(false);
		mIBtnExtSDcard.setSelected(false);
		
		if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_RECENT).getValue())) 			mIBtnRecent.setSelected(true);
		else if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_FAVORITES).getValue())) 	mIBtnFavorites.setSelected(true);
		else if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_BY_AUTHOR).getValue())) 	mIBtnAuthor.setSelected(true);
		else if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_BY_TITLE).getValue())) 	mIBtnTitle.setSelected(true);
		else if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_ALL_BOOKS).getValue())) 	mIBtnAllBooks.setSelected(true);
		else if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_ALL_IMAGES).getValue())) 	mIBtnAllImages.setSelected(true);
		else if (title.equals(ZLResource.resource("library").getResource(Globals.ROOT_EXT_SD).getValue())) 	mIBtnExtSDcard.setSelected(true);

	}
}
