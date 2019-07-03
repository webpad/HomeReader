/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geometerplus.android.fbreader.library.HomeWatcher.OnHomePressedListener;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.bookmark.BookmarksActivity;
import org.geometerplus.android.fbreader.httpd.DataService;
import org.geometerplus.fbreader.library.FavoritesTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import com.google.gson.Gson;
import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ntx.reader3.R;

@SuppressLint({ "DefaultLocale", "SdCardPath", "ShowToast", "NewApi" })
public class NtxMainPageActivity extends Activity {

	// category of view
	public static final int TAB_INDEX_NOTE = 1;
	public static final int TAB_INDEX_READER = 2;
	public static final int TAB_INDEX_CALENDAR = 3;
	public static final int TAB_INDEX_SETTING = 4;
	
	private ImageView ivTabNote, ivTabReader, ivTabCalendar, ivTabMore;
	private ImageView ivTab1, ivTab2, ivTab3, ivTab4;
	private String str_ntx_launcher_title_name;
	

	private FrameLayout ll_recent_book1, ll_recent_book2, ll_recent_book3, ll_recent_book4, ll_recent_book5, ll_recent_book6, ll_recent_book7;
	private FrameLayout[] ll_RecentBook = {ll_recent_book1, ll_recent_book2, ll_recent_book3, ll_recent_book4, ll_recent_book5, ll_recent_book6, ll_recent_book7 };

	private ImageView imgv_cover_1, imgv_cover_2, imgv_cover_3, imgv_cover_4, imgv_cover_5, imgv_cover_6, imgv_cover_7;
	private ImageView[] imgCover = {imgv_cover_1, imgv_cover_2, imgv_cover_3, imgv_cover_4, imgv_cover_5, imgv_cover_6, imgv_cover_7 };
	
	private TextView txtv_cover_1, txtv_cover_2, txtv_cover_3, txtv_cover_4, txtv_cover_5, txtv_cover_6, txtv_cover_7 ;
	private TextView[] textvCover = { txtv_cover_1, txtv_cover_2, txtv_cover_3, txtv_cover_4, txtv_cover_5, txtv_cover_6, txtv_cover_7 };
	private TextView bookshelf_title;
	
	private TextView txtMore;
	private FrameLayout ll_imvMore;
	
	private ImageView imgv_bg_under_line;

	private LinearLayout ll_imv_extsd1, ll_imv_extsd2, ll_imv_extsd3, ll_imv_extsd4, ll_imv_extsd5, ll_imv_extsd6, ll_imv_extsd7;
	private LinearLayout[] ll_ExtSD = { ll_imv_extsd1, ll_imv_extsd2, ll_imv_extsd3, ll_imv_extsd4, ll_imv_extsd5, ll_imv_extsd6, ll_imv_extsd7 };
	
	private LinearLayout llExtSDcard;
	private ImageButton ibAllBooks, ibAllImages, ibExtSDcard, ibBookmarks, ibFavorites;
	private TextView txtAllBooks, txtAllImages, txtExtSDcard, txtBookmarks, txtFavorites;
	
	private List<RecentlyBookData> mRecentlyBookList;

	private HomeWatcher mHomeWatcher = new HomeWatcher(this);
	
	private void initGridViewMenu() {

		llExtSDcard = (LinearLayout) findViewById(R.id.ll_ext_sd_card);
		llExtSDcard.setVisibility(RefreshClass.hasExternalSDCard() ? View.VISIBLE : View.GONE);
		
		ibAllBooks = (ImageButton) findViewById(R.id.ib_all_books);
		ibAllImages = (ImageButton) findViewById(R.id.ib_all_images);
		ibExtSDcard = (ImageButton) findViewById(R.id.ib_ext_sd_card);
		ibBookmarks = (ImageButton) findViewById(R.id.ib_bookmarks);
		ibFavorites = (ImageButton) findViewById(R.id.ib_favorites);
		
		ibAllBooks.setOnClickListener(new mOnClickListener());
		ibAllImages.setOnClickListener(new mOnClickListener());
		ibExtSDcard.setOnClickListener(new mOnClickListener());
		ibBookmarks.setOnClickListener(new mOnClickListener());
		ibFavorites.setOnClickListener(new mOnClickListener());
		
		txtAllBooks = (TextView) findViewById(R.id.txt_all_books);
		txtAllImages = (TextView) findViewById(R.id.txt_all_images);
		txtExtSDcard = (TextView) findViewById(R.id.txt_ext_sd_card);
		txtBookmarks = (TextView) findViewById(R.id.txt_bookmarks);
		txtFavorites = (TextView) findViewById(R.id.txt_favorites);

		txtAllBooks.setText(ZLResource.resource("library").getResource(Globals.ROOT_ALL_BOOKS).getValue());
		txtAllImages.setText(ZLResource.resource("library").getResource(Globals.ROOT_ALL_IMAGES).getValue());
		txtExtSDcard.setText(ZLResource.resource("library").getResource(Globals.ROOT_EXT_SD).getValue());
		txtBookmarks.setText(ZLResource.resource("menu").getResource("bookmarks").getValue());
		txtFavorites.setText(ZLResource.resource("library").getResource(Globals.ROOT_FAVORITES).getValue());
		
		txtAllBooks.setOnClickListener(new mOnClickListener());
		txtAllImages.setOnClickListener(new mOnClickListener());
		txtExtSDcard.setOnClickListener(new mOnClickListener());
		txtBookmarks.setOnClickListener(new mOnClickListener());
		txtFavorites.setOnClickListener(new mOnClickListener());
	}
	
	private void checkUpdate() {
		Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {			
				if (Globals.isBuildEventFired) {
					Globals.closeWaitDialog(NtxMainPageActivity.this);
				}
				
				if (!RefreshClass.isEinkHandWritingHardwareType()){
					Globals.closeWaitDialog(NtxMainPageActivity.this);
					return;
				}
				Gson gson = new Gson();
				BufferedReader br = null;
				try {
					FileInputStream fIn = openFileInput("recentBooks.json");
					br = new BufferedReader(new InputStreamReader(fIn));
					mRecentlyBookList = new ArrayList<RecentlyBookData>( Arrays.asList(gson.fromJson(br, RecentlyBookData[].class)));
										
					mRecentlyBookList = checkFileExists(mRecentlyBookList);
					
					if(mRecentlyBookList.size() <= Globals.RECENT_BOOK_MAIN_PAGE_MAX_SIZE)
						ll_imvMore.setVisibility(View.INVISIBLE);
					else
						ll_imvMore.setVisibility(View.VISIBLE);
										
					for(int i = 0; i < Globals.RECENT_BOOK_MAIN_PAGE_MAX_SIZE ; i++) {
						if(i < mRecentlyBookList.size()){
//							setupCover(mRecentlyBookList.get(i), ll_RecentBook[index], imgCover[index], textvCover[index], ll_ExtSD[index]);
							setupCover(mRecentlyBookList.get(i), ll_RecentBook[i], imgCover[i], textvCover[i], ll_ExtSD[i]);
						}else{
							setupCover(null, ll_RecentBook[i], imgCover[i], textvCover[i], ll_ExtSD[i]);
						}
					}
					
				} catch (FileNotFoundException e1) {
					
				} finally {
					if(br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				Globals.closeWaitDialog(NtxMainPageActivity.this);
			}
		};
		
		handler.postDelayed(runnable,100);	
	}

	public class ButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.recent_book1) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(0);
				}
			} else if (v.getId() == R.id.recent_book2) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(1);
				}
			} else if (v.getId() == R.id.recent_book3) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(2);
				}
			} else if (v.getId() == R.id.recent_book4) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(3);
				}
			} else if (v.getId() == R.id.recent_book5) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(4);
				}
			} else if (v.getId() == R.id.recent_book6) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(5);
				}
			} else if (v.getId() == R.id.recent_book7) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					openBookByRecentBookListIndex(6);
				}
			} else if (v.getId() == R.id.ll_imvMore) {
				switchToReaderOperate(Globals.ROOT_RECENT);

			} 
		}
	}
	
	public class ButtonLongClickListener implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			if (v.getId() == R.id.recent_book1) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(0);
				}
			} else if (v.getId() == R.id.recent_book2) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(1);
				}
			} else if (v.getId() == R.id.recent_book3) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(2);
				}
			} else if (v.getId() == R.id.recent_book4) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(3);
				}
			} else if (v.getId() == R.id.recent_book5) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(4);
				}
			} else if (v.getId() == R.id.recent_book6) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(5);
				}
			} else if (v.getId() == R.id.recent_book7) {
				if(mRecentlyBookList != null && !mRecentlyBookList.isEmpty()) {
					longClickBookByRecentBookListIndex(6);
				}
			} 
			return true;
		}
	}
	
	private void showDialogFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.dialog_container, fragment, tag).commit();
    }
	
	private ReaderBookInformationDialogFragment.OnButtonClickListener onBookInformationDialogButtonClickListener = new ReaderBookInformationDialogFragment.OnButtonClickListener() {
        @Override
        public void onDeleteBtnClick(int bookIndex, boolean isImage) {
        	RecentlyBookData rbd = mRecentlyBookList.get(bookIndex);
            deleteBookDialog(rbd.getPath());
        }

        @Override
        public void onOpenBtnClick(int bookIndex) {
        	Globals.openWaitDialog(NtxMainPageActivity.this);
        	openBookByRecentBookListIndex(bookIndex);
        }
    };
    
private void deleteBookDialog(final String path) {
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        String dialogTag = "delete_confirm";
        String deleteConfirmMessage = getResources().getString(R.string.toolbox_message_delete_confirm_book, 1 + "");
        AlertDialogFragment deleteConfirmDialogFragment = AlertDialogFragment.newInstance(deleteConfirmMessage, R.drawable.writing_ic_error,  dialogTag);

        deleteConfirmDialogFragment.setupPositiveButton(getString(android.R.string.yes));
        deleteConfirmDialogFragment.setupNegativeButton(getString(android.R.string.no));
        deleteConfirmDialogFragment.registerAlertDialogButtonClickListener(new AlertDialogButtonClickListener() {

            @Override
            public void onPositiveButtonClick(String fragmentTag) {
            	deleteReaderBook(path);
            }

            @Override
            public void onNegativeButtonClick(String fragmentTag) {
            }
        }, dialogTag);

        ft.replace(R.id.alert_dialog_container, deleteConfirmDialogFragment, dialogTag)
                .commit();
	}
    
    private void deleteReaderBook(String path) {
        String bookName = path.substring(path.lastIndexOf("/") + 1, path.length());
        String bookPath = path.substring(0, path.lastIndexOf("/"));

        File file = new File(bookPath, bookName);
        if (!file.exists())
            return;

        String where = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};
        getContentResolver().delete(MediaStore.Files.getContentUri("external"), where, selectionArgs);

        if (file.exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath());
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            getContentResolver().delete(MediaStore.Files.getContentUri("external"), where, selectionArgs);
        }

        Intent mIntent = new Intent(this,NtxMainPageActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
		startActivity(mIntent);
    }
	
	private void openBookByRecentBookListIndex(int indexOfImageView) {
		for(RecentlyBookData temp : mRecentlyBookList) {
			if(temp.getIndex() == indexOfImageView){
				Globals.openWaitDialog(this);
				
				Intent mIntent = new Intent(this,OpenBookActivity.class);
				
				mIntent.putExtra(Globals.KEY_READER_BOOK_ID, temp.getId());
				mIntent.putExtra(Globals.KEY_READER_BOOK_PATH, temp.getPath());
				mIntent.putExtra(Globals.KEY_READER_BOOK_TITLE, temp.getTitle());
				mIntent.putExtra(Globals.KEY_READER_BOOK_ENCODING, temp.getEncoding());
				mIntent.putExtra(Globals.KEY_READER_BOOK_LANGUAGE, temp.getLanguage());
									
				startActivity(mIntent);
				
			}
		}
	}
	
	private void longClickBookByRecentBookListIndex(int indexOfImageView) {
		for (RecentlyBookData temp : mRecentlyBookList) {
			if (temp.getIndex() == indexOfImageView) {
				RecentlyBookData rbd = mRecentlyBookList.get(indexOfImageView);
				ReaderBookInformationDialogFragment fragment = ReaderBookInformationDialogFragment.newInstance(rbd);
				fragment.setOnButtonClickListener(onBookInformationDialogButtonClickListener);
				showDialogFragment(fragment, ReaderBookInformationDialogFragment.class.getSimpleName());
			}
		}
	}

	private void initInstantKeys() {
		ivTabNote = (ImageView) findViewById(R.id.iv_note);
		ivTabReader = (ImageView) findViewById(R.id.iv_reader);
		ivTabMore = (ImageView) findViewById(R.id.iv_more);
		ivTabCalendar = (ImageView) findViewById(R.id.iv_calendar);

		ivTab1 = (ImageView) findViewById(R.id.iv_tab1);
		ivTab2 = (ImageView) findViewById(R.id.iv_tab2);
		ivTab3 = (ImageView) findViewById(R.id.iv_tab3);
		ivTab4 = (ImageView) findViewById(R.id.iv_tab4);
		
		imgv_bg_under_line = (ImageView) findViewById(R.id.imgv_bg_under_line);

		bookshelf_title = (TextView) findViewById(R.id.bookshelf_title);
		bookshelf_title.setTextColor(Color.BLACK);
		bookshelf_title.setText(ZLResource.resource("library").getResource(Globals.ROOT_RECENT).getValue());
		
		ll_RecentBook[0] = (FrameLayout) findViewById(R.id.recent_book1);
		ll_RecentBook[1] = (FrameLayout) findViewById(R.id.recent_book2);
		ll_RecentBook[2] = (FrameLayout) findViewById(R.id.recent_book3);
		ll_RecentBook[3] = (FrameLayout) findViewById(R.id.recent_book4);
		ll_RecentBook[4] = (FrameLayout) findViewById(R.id.recent_book5);
		ll_RecentBook[5] = (FrameLayout) findViewById(R.id.recent_book6);
		ll_RecentBook[6] = (FrameLayout) findViewById(R.id.recent_book7);

		ll_RecentBook[0].setOnClickListener(new ButtonClickListener());
		ll_RecentBook[1].setOnClickListener(new ButtonClickListener());
		ll_RecentBook[2].setOnClickListener(new ButtonClickListener());
		ll_RecentBook[3].setOnClickListener(new ButtonClickListener());
		ll_RecentBook[4].setOnClickListener(new ButtonClickListener());
		ll_RecentBook[5].setOnClickListener(new ButtonClickListener());
		ll_RecentBook[6].setOnClickListener(new ButtonClickListener());
		
		ll_RecentBook[0].setOnLongClickListener(new ButtonLongClickListener());
		ll_RecentBook[1].setOnLongClickListener(new ButtonLongClickListener());
		ll_RecentBook[2].setOnLongClickListener(new ButtonLongClickListener());
		ll_RecentBook[3].setOnLongClickListener(new ButtonLongClickListener());
		ll_RecentBook[4].setOnLongClickListener(new ButtonLongClickListener());
		ll_RecentBook[5].setOnLongClickListener(new ButtonLongClickListener());
		ll_RecentBook[6].setOnLongClickListener(new ButtonLongClickListener());
		
		imgCover[0] = (ImageView) findViewById(R.id.imgv_cover01);
		imgCover[1] = (ImageView) findViewById(R.id.imgv_cover02);
		imgCover[2] = (ImageView) findViewById(R.id.imgv_cover03);
		imgCover[3] = (ImageView) findViewById(R.id.imgv_cover04);
		imgCover[4] = (ImageView) findViewById(R.id.imgv_cover05);
		imgCover[5] = (ImageView) findViewById(R.id.imgv_cover06);
		imgCover[6] = (ImageView) findViewById(R.id.imgv_cover07);

		textvCover[0] =  (TextView) findViewById(R.id.txtv_cover01);
		textvCover[1] =  (TextView) findViewById(R.id.txtv_cover02);
		textvCover[2] =  (TextView) findViewById(R.id.txtv_cover03);
		textvCover[3] =  (TextView) findViewById(R.id.txtv_cover04);
		textvCover[4] =  (TextView) findViewById(R.id.txtv_cover05);
		textvCover[5] =  (TextView) findViewById(R.id.txtv_cover06);
		textvCover[6] =  (TextView) findViewById(R.id.txtv_cover07);

		ll_ExtSD[0] =  (LinearLayout) findViewById(R.id.ll_imv_extsd1);
		ll_ExtSD[1] =  (LinearLayout) findViewById(R.id.ll_imv_extsd2);
		ll_ExtSD[2] =  (LinearLayout) findViewById(R.id.ll_imv_extsd3);
		ll_ExtSD[3] =  (LinearLayout) findViewById(R.id.ll_imv_extsd4);
		ll_ExtSD[4] =  (LinearLayout) findViewById(R.id.ll_imv_extsd5);
		ll_ExtSD[5] =  (LinearLayout) findViewById(R.id.ll_imv_extsd6);
		ll_ExtSD[6] =  (LinearLayout) findViewById(R.id.ll_imv_extsd7);
		
		txtMore=(TextView) findViewById(R.id.txtMore);
		txtMore.setText(ZLResource.resource("menu").getResource("more").getValue());
		
		ll_imvMore = (FrameLayout) findViewById(R.id.ll_imvMore);
		
		ll_imvMore.setOnClickListener(new ButtonClickListener());
		
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ntx_main_page_activity);

		initInstantKeys();
		initGridViewMenu();
		
		if ((ivTabNote != null) && (ivTabReader != null) && (ivTabMore != null) &&  (ivTabCalendar != null) && (imgv_bg_under_line != null)) {		
			ivTabNote.setOnClickListener(new mOnClickListener());
			ivTabReader.setOnClickListener(new mOnClickListener());
			ivTabMore.setOnClickListener(new mOnClickListener());
			ivTabCalendar.setOnClickListener(new mOnClickListener());
		}
		if (RefreshClass.isEinkHandWritingHardwareType() && RefreshClass.isEinkUsingLargerUI()) {
			imgv_bg_under_line.setVisibility(View.GONE);
		}
		
		mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
			@Override
			public void onHomePressed() {
				checkUpdate();
			}

			@Override
			public void onHomeLongPressed() {
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mHomeWatcher.startWatch();
	}

	@Override
	protected void onNewIntent(Intent intent) {
	 
//		setIntent(intent);
		setTabVisibility(TAB_INDEX_READER);
		
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mHomeWatcher.stopWatch();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static Bitmap resizeBitmapFromFile(String path,
		        int reqWidth, int reqHeight) { // BEST QUALITY MATCH
		  
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
		        inSampleSize = Math.round((float)height / (float)reqHeight);
		    }

		    int expectedWidth = width / inSampleSize;

		    if (expectedWidth > reqWidth) {
		        //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
		        inSampleSize = Math.round((float)width / (float)reqWidth);
		    }
		    options.inSampleSize = inSampleSize;

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeFile(path, options);
	}
	public class mOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			
			switch(v.getId()){
				case R.id.iv_note :
					setTabVisibility(TAB_INDEX_NOTE);
					switchToNote();
					break;
				
				case R.id.iv_reader :
					setTabVisibility(TAB_INDEX_READER);
					checkUpdate();
					break;
				
				case R.id.iv_calendar :
					setTabVisibility(TAB_INDEX_CALENDAR);
					switchToCalendar();
					break;
					
				case R.id.iv_more :
					setTabVisibility(TAB_INDEX_SETTING);
					switchToSettings();
					break;
					
				case R.id.ib_all_books :
				case R.id.txt_all_books :
					switchToReaderOperate(Globals.ROOT_ALL_BOOKS);
					break;
					
				case R.id.ib_all_images :
				case R.id.txt_all_images :
					switchToReaderOperate(Globals.ROOT_ALL_IMAGES);
					break;
					
				case R.id.ib_ext_sd_card :
				case R.id.txt_ext_sd_card :
					switchToReaderOperate(Globals.ROOT_EXT_SD);
					break;
				case R.id.ib_bookmarks :
				case R.id.txt_bookmarks :
					// TODO
					Globals.openWaitDialog(NtxMainPageActivity.this);
					BookmarksActivity.switchToAllBook();
					copyOpenBookmarkBook();
					FBReader.showBookmarks(NtxMainPageActivity.this);
					
					break;	

				case R.id.ib_favorites :
				case R.id.txt_favorites :
					if (FavoritesTree.inFavoritesBook.size()==0){
						Toast.makeText(NtxMainPageActivity.this, ZLResource.resource("errorMessage").getResource("noFavorites").getValue(), Toast.LENGTH_LONG).show();
					}else{
						switchToReaderOperate(Globals.ROOT_FAVORITES);
					}
					
					break;
			}
	
		}
	}

	private void switchToNote() {
		
		Globals.openWaitDialog(this);
		try {
			ComponentName componentName = new ComponentName(Globals.NOTE_PACKAGE, Globals.NOTE_CLASS);
			Intent mIntent = new Intent();
			startActivity(mIntent.setComponent(componentName).setAction(Intent.ACTION_VIEW)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		} catch (Throwable e) {
			Globals.closeWaitDialog(this);
			Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
		}
		
		checkUpdate();
	}
	
	private void switchToCalendar() {
		
		Globals.openWaitDialog(this);
		try {
			ComponentName componentName = new ComponentName(Globals.CALENDAR_PACKAGE, Globals.CALENDAR_CLASS);
			Intent mIntent = new Intent();
			startActivity(mIntent.setComponent(componentName).setAction(Intent.ACTION_VIEW)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			startActivity(mIntent);
		} catch (Throwable e) {
			Globals.closeWaitDialog(this);
			Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
		}
		
		checkUpdate();

	}

	private void switchToSettings() {
		
		Globals.openWaitDialog(this);
		try {
			ComponentName componentName = new ComponentName(Globals.nTools_PACKAGE, Globals.nTools_CLASS);
            Intent mIntent = new Intent();
            mIntent.putExtra("TITLE_NAME", str_ntx_launcher_title_name);
            startActivity(mIntent.setComponent(componentName)
            .setAction(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
			startActivity(mIntent);
		} catch (Throwable e) {
			Globals.closeWaitDialog(this);
			Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
		}
		
		checkUpdate();
	}

	private void switchToReaderOperate(String status) {
		Globals.rootStatus = status;
		Globals.openWaitDialog(this);
		Intent mIntent = new Intent(this,LibraryActivity.class);	
		startActivity(mIntent);
	}
    
	/**
	 * 
	 * @param index
	 *            (index = 0 ==> tab1~4 disable under line; index = 1 ==> tab1 show under line ...
	 */
	public void setTabVisibility(int index) {
		ivTab1.setVisibility(View.GONE);
		ivTab2.setVisibility(View.GONE);
		ivTab3.setVisibility(View.GONE);
		ivTab4.setVisibility(View.GONE);

		switch (index) {
		case 0:
			break;
		case TAB_INDEX_NOTE:
			ivTab1.setVisibility(View.VISIBLE);
			break;
		case TAB_INDEX_READER:
			ivTab2.setVisibility(View.VISIBLE);
			break;
		case TAB_INDEX_CALENDAR:
			ivTab3.setVisibility(View.VISIBLE);
			break;
		case TAB_INDEX_SETTING:
			ivTab4.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mSaveRecentBookListServiceReceiver);
	}
	
    private BroadcastReceiver mSaveRecentBookListServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	checkUpdate();
        }
    };

	@Override
	protected void onResume() {
		super.onResume();
		Globals.closeWaitDialog(this);
		registerReceiver(mSaveRecentBookListServiceReceiver, new IntentFilter(Globals.SAVE_RECENT_BOOK_FINISHED_NOTIFICATION));
		setScreenPortrait();
		
		if (RefreshClass.isEinkHandWritingHardwareType()) {
			checkUpdate();
			setTabVisibility(TAB_INDEX_READER);
		}
		
	}
		
//	private int getBookTypeCoverResourceId(String bookPath) {
//		String bookType = ZLFile.createFileByPath(bookPath).getExtension();
//		int resId = R.drawable.book_cover_default;
//		if (bookType.equalsIgnoreCase("djvu")) {
//			resId = R.drawable.book_cover_djvu;
//		} else if (bookType.equalsIgnoreCase("epub")) {
//			resId = R.drawable.book_cover_epub;
//		} else if (bookType.equalsIgnoreCase("fb2")) {
//			resId = R.drawable.book_cover_fb2;
//		} else if (bookType.equalsIgnoreCase("mobi")) {
//			resId = R.drawable.book_cover_mobi;
//		} else if (bookType.equalsIgnoreCase("pdf")) {
//			resId = R.drawable.book_cover_pdf;
//		} else if (bookType.equalsIgnoreCase("txt")) {
//			resId = R.drawable.book_cover_txt;
//		}
//		return resId;
//	}
	
	String pathPrefix = "/mnt/media_rw/extsd";

	private void setupCover(final RecentlyBookData book, final FrameLayout ll_coverLayout_recent,
			final ImageView coverView_recent, final TextView coverText_recent, final LinearLayout ll_imv_extsd) {
		RefreshClass.setGridBookTitleSize(coverText_recent);

		if (book == null) {
			ll_coverLayout_recent.setVisibility(View.INVISIBLE);
			return;
		} else {
			// if file in ext_SD, remove star button.
			if (ll_imv_extsd != null) {
				if (book.getPath().indexOf(Globals.PATH_EXTERNALSD) != -1 || book.getPath().indexOf(pathPrefix) != -1) {
					ll_imv_extsd.setVisibility(View.VISIBLE);
				} else {
					ll_imv_extsd.setVisibility(View.INVISIBLE);
				}
			}

			ll_coverLayout_recent.setVisibility(View.VISIBLE);
		}

		coverView_recent.setTag(book);
		coverView_recent.setImageDrawable(null);
		coverText_recent.setText(book.getTitle());

		if (!book.hasCover()) {
			coverView_recent.setImageResource(Globals.getCoverTypeTagResourceId(book.getPath()));
			coverView_recent.setBackgroundResource(R.drawable.book_cover_default);
			
			return;
		}
		Bitmap coverBitmap = null;
		String imageFileName = "Cover_" + book.getIndex() + ".png";
		try {
			FileInputStream input = openFileInput(imageFileName);
			coverBitmap = BitmapFactory.decodeStream(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (coverBitmap == null) {
			coverView_recent.setImageResource(Globals.getCoverTypeTagResourceId(book.getPath()));
			coverView_recent.setBackgroundResource(R.drawable.book_cover_default);
		} else {
			coverView_recent.setImageBitmap(coverBitmap);
			
			coverView_recent.setBackgroundDrawable(new BitmapDrawable(coverView_recent.getResources(), coverBitmap));
			coverView_recent.setImageBitmap(BitmapFactory.decodeResource(coverView_recent.getResources(), Globals.getCoverTypeTagResourceId(book.getPath())));
		}
	}
	
    private void setScreenPortrait() {
    	if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
    	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	}
    }
    
    /**
     * Check file path exists, if not found, remove it.
     * @param source
     * @return
     */
    private List<RecentlyBookData> checkFileExists(List<RecentlyBookData> source){
    	List<RecentlyBookData> target = new ArrayList<RecentlyBookData>();
    	    	
    	for(int i = 0; i < source.size(); i++) {
    		try{
    			if((new File(source.get(i).getPath())).exists()){
    				target.add(source.get(i));
    			}
    		}catch(Exception e){
    			// ALog.debug(e.toString());
    		}
    	
		}
    	
    	Collections.sort(target, new SortIndex());

    	return target;
    }
    
	public class SortIndex implements Comparator<Object> {
	    public int compare(Object o1, Object o2) {
	    	RecentlyBookData index1 = (RecentlyBookData) o1;
	    	RecentlyBookData index2 = (RecentlyBookData) o2;

	    	Integer s1 = index1.getIndex() ;
	    	Integer s2 = index2.getIndex() ;
	        
	        return s1.compareTo(s2);
	    }
	}
	
	private void copyOpenBookmarkBook(){
		if (new File(Globals.PATH_SDCARD+"/"+Globals.ASSET_BOOKMARK_FILE).exists()){
        	return;
        }

		InputStream in=null;
    	OutputStream out=null;

    	try {
	        File dest = new File(Globals.PATH_SDCARD+"/"+Globals.ASSET_BOOKMARK_FILE);
	    	in = getAssets().open(Globals.ASSET_BOOKMARK_PATH+"/"+Globals.ASSET_BOOKMARK_FILE);
	    	out = new FileOutputStream(dest);
		    byte[] buffer = new byte[in.available()];
		    int read;
		    while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }

		} catch (IOException e){
		} finally {
	    	try {
	    		if (in != null){
	        		in.close();
	        		in = null;
	        	}

	        	if (out != null){
	        		out.close();
	        		out = null;
	        	}
	    	} catch (IOException e) {
	        }
	    }
	}
}