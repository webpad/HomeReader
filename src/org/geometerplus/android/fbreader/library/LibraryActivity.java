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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;
import com.ntx.image.SwipeActivity;

import ntx.reader3.R;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.util.DeviceType;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.PackageUtil;
import org.geometerplus.android.util.SearchDialogUtil;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookQuery;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Filter;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.BookTree;
import org.geometerplus.fbreader.library.ExternalViewTree;
import org.geometerplus.fbreader.library.FavoritesTree;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.RootTree;
import org.geometerplus.fbreader.library.SearchResultsTree;
import org.geometerplus.fbreader.library.SyncLabelTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@SuppressLint({ "DefaultLocale", "SdCardPath", "ShowToast", "NewApi" })
public class LibraryActivity extends TreeActivity<LibraryTree>
		implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, IBookCollection.Listener<Book>, View.OnTouchListener {
//	private String TAG="org.geometerplus.android.fbreader.library.LibraryActivity";
	public static final String START_SEARCH_ACTION = "action.fbreader.library.start-search";
	public static BookCollectionShadow myCollection = new BookCollectionShadow();
	public volatile RootTree myRootTree;
	private Book mySelectedBook;

	LibraryTreeAdapter myadapter;
	private static Context mContext;

	// category of view
	public static final int GRID_VIEW = 0;
	public static final int LIST_VIEW = 1;
	public static int currentView = GRID_VIEW; // defaule GRID_VIEW
	public static final int TAB_INDEX_NOTE = 1;
	public static final int TAB_INDEX_READER = 2;
	public static final int TAB_INDEX_CALENDAR = 3;
	public static final int TAB_INDEX_SETTING = 4;

	// bookshelf view
	private GridView bookshelf_gridview;
	private ListView bookshelf_listview;

//	private static String rootStatus = Globals.ROOT_ALL_BOOKS;

	private LinearLayout noteLauncher;
	private ImageView ivTabNote, ivTabReader, ivTabCalendar, ivTabMore;
	private ImageView ivTab1, ivTab2, ivTab3, ivTab4;

	private Button viewButton;
	private Button searchButton;
	private Button refreshButton;

	private Button previousButton;
	private Button nextButton;
	private TextView pageInfo;
	
	private ImageView imgv_bg_under_line;

	public static RelativeLayout libraryGrid;

	private LinearLayout searchBox;
	private Button searchBoxClose, searchBoxSearch, searchBoxBack;
	private EditText searchText;

	private EventBus mEventBus;
    private TextView mTvSearchNotFoundHint;
    private float FLYING_GESTURE_HORIZONTAL_MIN_DISTANCE;
    
	public static boolean isAlive = false; 	// check LibraryActivity.class is start,fixed recent book from other intent crash issue. 
	
	// create default "Books" Folder
	private void createDefaultBooksFolder(){

		final File newFile = new File(Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER);
		if (false == newFile.exists())
			newFile.mkdir();

		final File newDownload = new File(Globals.PATH_SDCARD+"/"+Globals.DOWNLOAD_FOLDER);
		if (false == newDownload.exists())
			newDownload.mkdir();
		
//		final Locale locale = Locale.getDefault();
//		final String langCode=locale.getLanguage() + "_" + locale.getCountry();
//
//		if (RefreshClass.isEink68HandWritingHardwareType()){
//
//			copyUserGuideBookFromAssetToLibrary(
//					Globals.assetDemoBook_path, 				// From Assets Books Path
//					"6.8inch_notebook_"+ langCode + ".epub",	// From Assets File Name
//					Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER,				// Save to path
//					getResources().getString(R.string.user_guide_book_filename) // Save to file name
//					);
//		}
	}

	// ext. copyUserGuideBookFromAssetToLibrary("demo/Guide","6.8inch_notebook_zh_TW.epub","mnt/sdcard/Books","6.8吋電子筆記本.epub");
//	private void copyUserGuideBookFromAssetToLibrary(String assetBooksPath,String assetsFilename, String localPath, String localName){
//
//		if (new File(localPath,localName).exists()){
//        	return;
//        }
//
//		InputStream in=null;
//    	OutputStream out=null;
//    	try {
//	        File dest = new File(localPath,localName);
//	    	in = getAssets().open(assetBooksPath+"/"+assetsFilename);
//	    	out = new FileOutputStream(dest);
//		    byte[] buffer = new byte[in.available()];
//		    int read;
//		    while((read = in.read(buffer)) != -1){
//		    		out.write(buffer, 0, read);
//		    }
//		} catch (IOException e){
//			// Log.e(TAG, ZLResource.resource("library").getResource("copyFail").getValue()+" 1"+"::"+Thread.currentThread().getStackTrace()[2].getLineNumber());
//		} finally {
//	    	try {
//	    		if (in != null){
//	        		in.close();
//	        		in = null;
//	        	}
//
//	        	if (out != null){
//	        		out.close();
//	        		out = null;
//	        	}
//	    	} catch (IOException e) {
//    			// Log.e(TAG, ZLResource.resource("library").getResource("copyFail").getValue()+" 1"+"::"+Thread.currentThread().getStackTrace()[2].getLineNumber());
//	        }
//	    }
//
//		for (String filename : Globals.supportLanguageUserGuideFileName){
//			if (new File(localPath,filename).exists() && filename.equals(localName)==false){
//				try {
//					deleteBook(Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER+"/"+filename);
//				} catch (IOException e) {}
//			}
//		}
//	}

	/**
	 * Library Grid View Set Visibility
	 * @param b
	 */
	private void showLibrary(final boolean b) {

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				libraryGrid.setVisibility( b ? View.VISIBLE : View.INVISIBLE);
			}
		};

		handler.postDelayed(runnable,100);
	}

	private void initInstantKeysNote() {
		ivTabNote = (ImageView) findViewById(R.id.iv_note);
		ivTabReader = (ImageView) findViewById(R.id.iv_reader);
		ivTabMore = (ImageView) findViewById(R.id.iv_more);
		ivTabCalendar = (ImageView) findViewById(R.id.iv_calendar);

		ivTab1 = (ImageView) findViewById(R.id.iv_tab1);
		ivTab2 = (ImageView) findViewById(R.id.iv_tab2);
		ivTab3 = (ImageView) findViewById(R.id.iv_tab3);
		ivTab4 = (ImageView) findViewById(R.id.iv_tab4);

		imgv_bg_under_line = (ImageView) findViewById(R.id.imgv_bg_under_line);

	}

	private void initInstantKeys() {
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemLongClickListener(bookshelfGridviewOnItemLongClickListener);

		bookshelf_listview = (ListView) findViewById(android.R.id.list);
		bookshelf_gridview = (GridView) findViewById(R.id.gridview);

		viewButton = (Button) findViewById(R.id.view_button);
		viewButton.setOnClickListener(instantKeyListener);

		searchButton = (Button) findViewById(R.id.search_button);
		searchButton.setOnClickListener(instantKeyListener);

		refreshButton = (Button) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(instantKeyListener);

		previousButton = (Button) findViewById(R.id.previous_page_button);
		previousButton.setOnClickListener(instantKeyListener);

		nextButton = (Button) findViewById(R.id.next_page_button);
		nextButton.setOnClickListener(instantKeyListener);
		pageInfo = (TextView) findViewById(R.id.page_info);

		noteLauncher = (LinearLayout) findViewById(R.id.ll_lanucher);
		libraryGrid = (RelativeLayout) findViewById(R.id.rl_grid_list_view);

		if (RefreshClass.isEinkHandWritingHardwareType()) {
			noteLauncher.setVisibility(View.VISIBLE);

			initInstantKeysNote();

			bookshelf_gridview.setNumColumns(4);
			bookshelf_gridview.setColumnWidth(2);
			bookshelf_gridview.setVerticalSpacing(100);

		} else {
			noteLauncher.setVisibility(View.GONE);

			bookshelf_gridview.setNumColumns(3);
			bookshelf_gridview.setColumnWidth(2);
		}

		searchBox = (LinearLayout) findViewById(R.id.ll_search);
		searchBoxClose = (Button) findViewById(R.id.btn_search_box_close);
		searchBoxSearch = (Button) findViewById(R.id.btn_search_box_search);
		searchBoxBack = (Button) findViewById(R.id.btn_search_box_back);
		searchText = (EditText) findViewById(R.id.et_search);

		searchBox.setOnClickListener(instantKeyListener);
		searchBoxClose.setOnClickListener(instantKeyListener);
		searchBoxSearch.setOnClickListener(instantKeyListener);
		searchBoxBack.setOnClickListener(instantKeyListener);
		searchText.setOnClickListener(instantKeyListener);

		searchText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {

					hideInputMethod(searchText);

					startBookSearch(searchText.getText().toString());
					return true;
				}
				return false;
			}
		});
		
		mTvSearchNotFoundHint = (TextView) findViewById(R.id.tv_search_result_not_find_hint);
        mTvSearchNotFoundHint.setVisibility(View.GONE);
	}

	private void getView() {
		myadapter = new LibraryTreeAdapter(this, GRID_VIEW);
		bookshelf_listview.setVisibility(View.GONE);
		bookshelf_gridview.setAdapter(myadapter);
		bookshelf_gridview.setOnItemClickListener(bookshelfGridviewOnItemClickListener);
		bookshelf_gridview.setOnItemLongClickListener(bookshelfGridviewOnItemLongClickListener);
		bookshelf_gridview.setOnTouchListener(this);
	}

	private void setView() {
		if (currentView == GRID_VIEW) { // currentView== GRID_VIEW
			bookshelf_listview.setVisibility(View.GONE);
			myadapter.setlayout(GRID_VIEW);
			bookshelf_gridview.setVisibility(View.VISIBLE);
			viewButton.setBackgroundResource(R.drawable.btn_view_style_list);
		} else {

			bookshelf_listview.setVisibility(View.VISIBLE);
			myadapter.setlayout(LIST_VIEW);
			bookshelf_gridview.setVisibility(View.GONE);
			viewButton.setBackgroundResource(R.drawable.btn_view_style_grid);
		}
	}

	private OnClickListener instantKeyListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.view_button:
				if (currentView == LIST_VIEW)
					currentView = GRID_VIEW; // change to GRID_VIEW
				else
					currentView = LIST_VIEW; // change to LIST_VIEW

				setView();
				break;
			case R.id.search_button:
				searchRequest(true);
				break;
			case R.id.refresh_button:
				if (Globals.rootStatus.equals(Globals.ROOT_ALL_BOOKS)){
					Globals.openWaitDialog(LibraryActivity.this);
					refreshButton.setEnabled(false);
					refreshButton.setAlpha(0.1f);
					
					new Thread(new Runnable() {
						@Override
						public void run() {						
							myRootTree.fileFirstLevelTreeAllBooksSortByFileName();
							addFavoriteList();	// refresh Favorites star icon
							
							CallbackEvent callbackEvent = new CallbackEvent();
							callbackEvent.setMessage(CallbackEvent.MORE_ALL_BOOKS_SEARCH_DONE);
							mEventBus.post(callbackEvent);
							
						}
					}).start();			
				}else{
					addFavoriteList();	// refresh Favorites star icon
					refreshCurrentTree();
				}
		
				break;
			case R.id.previous_page_button:
				onPrevPage();
				break;
			case R.id.next_page_button:
				onNextPage();
				break;
			case R.id.btn_search_box_close:
				searchRequest(false);
				searchResultNotFoundMessage(myadapter.getCount()==0);
				break;
			case R.id.btn_search_box_search:
				hideInputMethod(searchText);
				startBookSearch(searchText.getText().toString());

				break;
			case R.id.btn_search_box_back:
				searchText.setText("");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		isAlive = true;

		mContext = getApplicationContext();
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		FLYING_GESTURE_HORIZONTAL_MIN_DISTANCE = Globals.getCMtoPixel(Globals.SWIPE_DISTANCE_THRESHOLD_BY_CM, metrics);
		// without title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mySelectedBook = FBReaderIntents.getBookExtra(getIntent(), myCollection);

		setContentView(R.layout.ntx_launcher_activity);
		
		try{
			createDefaultBooksFolder(); // create default "Books" Folder
		}catch(Exception e){}

		initInstantKeys();
		showLibrary(false);
		
		createDefaultBooksFolder();
		
		getView();
		deleteRootTree();

		if ((ivTabNote != null) && (ivTabReader != null) && (ivTabMore != null) &&  (ivTabCalendar != null) && (imgv_bg_under_line != null)) {
			ivTabNote.setOnClickListener(new mOnClickListener());
			ivTabReader.setOnClickListener(new mOnClickListener());
			ivTabMore.setOnClickListener(new mOnClickListener());
			ivTabCalendar.setOnClickListener(new mOnClickListener());
		}
		if (RefreshClass.isEinkHandWritingHardwareType() && RefreshClass.isEinkUsingLargerUI()) {
			imgv_bg_under_line.setVisibility(View.GONE);
		}else{
			if (!RefreshClass.isEinkHandWritingHardwareType()){
				RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams)libraryGrid.getLayoutParams();
				relativeParams.setMargins(10, 10, 10, 10);  // left, top, right, bottom
				libraryGrid.setLayoutParams(relativeParams);
			}
		}

		myCollection.bindToService(this, new Runnable() {
            @Override
            public void run() {
				setProgressBarIndeterminateVisibility(!myCollection.status().IsComplete);
				myRootTree = new RootTree(myCollection,
						PluginCollection.Instance(Paths.systemInfo(LibraryActivity.this)));
				myCollection.addListener(LibraryActivity.this);
				init(getIntent());
				handler.post(runnable);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		mEventBus = EventBus.getDefault();
		mEventBus.register(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		setTabVisibility(TAB_INDEX_READER);

		if (START_SEARCH_ACTION.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (pattern != null && pattern.length() > 0) {
				startBookSearch(pattern);
			}
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected LibraryTree getTreeByKey(FBTree.Key key) {
		return key != null ? myRootTree.getLibraryTree(key) : myRootTree;
	}

	private synchronized void deleteRootTree() {
		if (myRootTree != null) {
			myCollection.removeListener(this);
			myCollection.unbind();
			myRootTree = null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mEventBus.unregister(this);
	}

	@Override
	protected void onDestroy() {
		deleteRootTree();
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree) tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final int real_position = myadapter.my_numofcontentperpage * (myadapter.pageid - 1) + position;// for page turn setting

		final LibraryTree tree = (LibraryTree) getTreeAdapter().getItem(real_position);

		if (tree instanceof ExternalViewTree) {
//			runOrInstallExternalView(true);
			Toast.makeText(this, "Not support \"" + ZLFile.createFileByPath(tree.getBook().getPath()).getExtension().toUpperCase() + "\" format file", Toast.LENGTH_LONG).show();
		} else {
			final Book book = tree.getBook();
			if (book != null) {
				openBook(book);
			} else {
				try {
					boolean isImage=false;
	            	for (String fileType : Globals.searchImageType){

	            		final int index = ((FileTree)tree).getFile().getPath().lastIndexOf('.');
        				final String myExtension = ((index > 0) ? ((FileTree)tree).getFile().getPath().substring(index).toLowerCase().intern() : "");

    					if (myExtension.equals(fileType) && ((FileTree)tree).getFile().getShortName().substring(0,1).equals(".")==false){
	            			isImage=true;
	            			break;
	            		}
	            	}

	            	if (isImage){
	            		final ArrayList<String> imagePath = new ArrayList<String>();
	            		int current_position=0;
	            		for (int i=0;i<myadapter.getTotalItem();i++){
	            			final String filePath=((FileTree)(LibraryTree) bookshelf_gridview.getAdapter().getItem(i)).getFile().getPath();

	            			final String fileName=((FileTree)(LibraryTree) bookshelf_gridview.getAdapter().getItem(i)).getFile().getShortName();
	            			final int index = fileName.lastIndexOf('.');
	        				final String myExtension = ((index > 0) ? fileName.substring(index).toLowerCase().intern() : "");

	            			for (String fileType : Globals.searchImageType){
            					if (myExtension.equals(fileType) && fileName.substring(0,1).equals(".")==false){
	            					imagePath.add(filePath);
			            			if (((FileTree)tree).getFile().getPath().equals(filePath)){
			            				current_position=imagePath.size()-1;
			            			}

	            				}
	            			}
	            		}

	            		Intent i = new Intent(LibraryActivity.this,SwipeActivity.class);

	    				try{
		    				Bundle bundle = new Bundle();
		    				bundle.putStringArrayList("path", imagePath);
		    				bundle.putInt("pos", current_position);
		    				i.putExtras(bundle);
		    				startActivity(i);
	    				}catch(Exception e){
	    					// ALog.debug(e.toString());
	    				}
	            	}else{

	    				addFavoriteList();	// refresh Favorites star icon

	    				if (tree.getName().equals(ZLResource.resource("library").getResource("extSD").getValue())
	    						&& Environment.getExternalExtSDStorageState().equals(Environment.MEDIA_BAD_REMOVAL)){
	    					Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("fileNotFound").getValue(), Toast.LENGTH_LONG).show();
	    				}else{
	    					openTree(tree);
	    				}
	            	}
				}catch(Exception e){
    				addFavoriteList();	// refresh Favorites star icon

    				if (tree.getName().equals(ZLResource.resource("library").getResource("extSD").getValue())
    						&& Environment.getExternalExtSDStorageState().equals(Environment.MEDIA_BAD_REMOVAL)){
    					Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("fileNotFound").getValue(), Toast.LENGTH_LONG).show();
    				}else{
    					openTree(tree);
    				}
				}
			}
		}
	}

	private OnItemClickListener bookshelfGridviewOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

			final int real_position = myadapter.my_numofcontentperpage * (myadapter.pageid - 1) + position;// for
																											// setting
			final LibraryTree tree = (LibraryTree) bookshelf_gridview.getAdapter().getItem(real_position);
			final Book book = tree.getBook();

			if (book != null) {
				openBook(book);
			} else {
				try {
					boolean isImage=false;
	            	for (String fileType : Globals.searchImageType){

	            		final int index = ((FileTree)tree).getFile().getPath().lastIndexOf('.');
        				final String myExtension = ((index > 0) ? ((FileTree)tree).getFile().getPath().substring(index).toLowerCase().intern() : "");

    					if (myExtension.equals(fileType) && ((FileTree)tree).getFile().getShortName().substring(0,1).equals(".")==false){
	            			isImage=true;
	            			break;
	            		}
	            	}

	            	if (isImage){
	            		final ArrayList<String> imagePath = new ArrayList<String>();
	            		int current_position=0;
	            		for (int i=0;i<myadapter.getTotalItem();i++){
	            			final String filePath=((FileTree)(LibraryTree) bookshelf_gridview.getAdapter().getItem(i)).getFile().getPath();

	            			final String fileName=((FileTree)(LibraryTree) bookshelf_gridview.getAdapter().getItem(i)).getFile().getShortName();
	            			final int index = fileName.lastIndexOf('.');
	        				final String myExtension = ((index > 0) ? fileName.substring(index).toLowerCase().intern() : "");

	            			for (String fileType : Globals.searchImageType){
            					if (myExtension.equals(fileType) && fileName.substring(0,1).equals(".")==false){
	            					imagePath.add(filePath);
			            			if (((FileTree)tree).getFile().getPath().equals(filePath)){
			            				current_position=imagePath.size()-1;
			            			}

	            				}
	            			}
	            		}

	            		Intent i = new Intent(LibraryActivity.this,SwipeActivity.class);

	    				try{
		    				Bundle bundle = new Bundle();
		    				bundle.putStringArrayList("path", imagePath);
		    				bundle.putInt("pos", current_position);
		    				i.putExtras(bundle);
		    				startActivity(i);
	    				}catch(Exception e){
	    					// ALog.debug(e.toString());
	    				}
	            	}else{

	    				addFavoriteList();	// refresh Favorites star icon

	    				if (tree.getName().equals(ZLResource.resource("library").getResource("extSD").getValue())
	    						&& Environment.getExternalExtSDStorageState().equals(Environment.MEDIA_BAD_REMOVAL)){
	    					Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("permissionDenied").getValue(), Toast.LENGTH_LONG).show();
	    				}else{
	    					openTree(tree);
	    				}
	            	}
				}catch(Exception e){
    				addFavoriteList();	// refresh Favorites star icon

    				if (tree.getName().equals(ZLResource.resource("library").getResource("extSD").getValue())
    						&& Environment.getExternalExtSDStorageState().equals(Environment.MEDIA_BAD_REMOVAL)){
    					Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("permissionDenied").getValue(), Toast.LENGTH_LONG).show();
    				}else{
    					openTree(tree);
    				}
				}

			}
		}
	};
	
	private OnItemLongClickListener bookshelfGridviewOnItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			final int real_position = myadapter.my_numofcontentperpage * (myadapter.pageid - 1) + position;// for								// setting
			final LibraryTree tree = (LibraryTree) bookshelf_gridview.getAdapter().getItem(real_position);
			final Book book = tree.getBook();
			if (book != null) {
				File file = new File(book.getPath());
				String fileName = file.getName();
				String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
				HashMap tempData = new HashMap();
				tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_INDEX, real_position);
				tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_TITLE, book.getTitle());
				tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_LANGUAGE, book.getLanguage());
				tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_SIZE, file.length());
				tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_TYPE, fileType.toLowerCase());
				tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_PATH, book.getPath());
				ReaderBookInformationDialogFragment fragment = ReaderBookInformationDialogFragment
						.newInstance(tempData);
				fragment.setOnButtonClickListener(onBookInformationDialogButtonClickListener);
				showDialogFragment(fragment, ReaderBookInformationDialogFragment.class.getSimpleName());
			} else {
				try {
					boolean isImage = false;
					for (String fileType : Globals.searchImageType) {

						final int index = ((FileTree) tree).getFile().getPath().lastIndexOf('.');
						final String myExtension = ((index > 0)
								? ((FileTree) tree).getFile().getPath().substring(index).toLowerCase().intern() : "");

						if (myExtension.equals(fileType)
								&& ((FileTree) tree).getFile().getShortName().substring(0, 1).equals(".") == false) {
							isImage = true;
							break;
						}
					}

					if (isImage) {
						final String filePath = ((FileTree) (LibraryTree) bookshelf_gridview.getAdapter()
								.getItem(real_position)).getFile().getPath();
						final String fileName = ((FileTree) (LibraryTree) bookshelf_gridview.getAdapter()
								.getItem(real_position)).getFile().getShortName();
						final String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
						File file = new File(filePath);
						
						HashMap tempData = new HashMap();
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_INDEX, real_position);
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_TITLE, fileName);
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_LANGUAGE, "");
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_SIZE, file.length());
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_TYPE, fileType.toLowerCase());
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_BOOK_PATH, filePath);
						tempData.put(ReaderBookInformationDialogFragment.ARGUMENT_IS_IMAGE, true);
						ReaderBookInformationDialogFragment fragment = ReaderBookInformationDialogFragment
								.newInstanceImage(tempData);
						fragment.setOnButtonClickListener(onBookInformationDialogButtonClickListener);
						showDialogFragment(fragment, ReaderBookInformationDialogFragment.class.getSimpleName());
					}
				} catch (Exception e) {

				}
			}
			return true;
		}
	};

	private void showDialogFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.dialog_container, fragment, tag).commit();
    }
	
	private ReaderBookInformationDialogFragment.OnButtonClickListener onBookInformationDialogButtonClickListener = new ReaderBookInformationDialogFragment.OnButtonClickListener() {
        @Override
        public void onDeleteBtnClick(int bookIndex, boolean isImage) {
        	if(isImage){
        		final String filePath = ((FileTree) (LibraryTree) bookshelf_gridview.getAdapter()
						.getItem(bookIndex)).getFile().getPath();
        		deleteBookDialog(filePath,isImage);	
        	}else{
        		final LibraryTree tree = (LibraryTree) bookshelf_gridview.getAdapter().getItem(bookIndex);
    			final Book book = tree.getBook();
    			deleteBookDialog(book.getPath(),isImage);	
        	}
        }

        @Override
        public void onOpenBtnClick(int bookIndex) {
        	Globals.openWaitDialog(LibraryActivity.this);
        	final LibraryTree tree = (LibraryTree) bookshelf_gridview.getAdapter().getItem(bookIndex);
			final Book book = tree.getBook();
        	openBook(book);
        }
    };
	
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
			if (v.getId() == R.id.iv_reader) {
				setTabVisibility(TAB_INDEX_READER);
				switchToReaderMainPage();

			} else if (v.getId() == R.id.iv_note) {
				setTabVisibility(TAB_INDEX_NOTE);
				switchToNote();
			} else if (v.getId() == R.id.iv_more) {
				setTabVisibility(TAB_INDEX_SETTING);
				switchToSettings();
			} else if (v.getId() == R.id.iv_calendar) {
				setTabVisibility(TAB_INDEX_CALENDAR);
				switchToCalendar();
			}
		}
	}

	// show BookInfoActivity
	private void showBookInfo(Book book) {
		final Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
		FBReaderIntents.putBookExtra(intent, book);
		OrientationUtil.startActivity(this, intent);

	}

	// Search
	private final ZLStringOption BookSearchPatternOption = new ZLStringOption("BookSearch", "Pattern", "");

	private void openSearchResults() {
		final LibraryTree tree = myRootTree.getSearchResultsTree();
		if (tree != null) {
			openTree(tree);
			previousButton.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.VISIBLE);
			pageInfo.setVisibility(View.VISIBLE);
		}else{
			previousButton.setVisibility(View.GONE);
			nextButton.setVisibility(View.GONE);
			pageInfo.setVisibility(View.GONE);
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
	}

	private void switchToReaderMainPage() {
		Globals.openWaitDialog(this);
		startActivity(new Intent(this,NtxMainPageActivity.class));
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

	}

	private void switchToSettings() {
		Globals.openWaitDialog(this);
		try {
			ComponentName componentName = new ComponentName(Globals.nTools_PACKAGE, Globals.nTools_CLASS);
            Intent mIntent = new Intent();
            startActivity(mIntent.setComponent(componentName)
            .setAction(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
			startActivity(mIntent);
		} catch (Throwable e) {
			Globals.closeWaitDialog(this);
			Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 *
	 * @param index
	 *            (index = 0 ==> tab1~4 disable under line; index = 1 ==> tab1 show under line ...
	 */
	public void setTabVisibility(int index) {
		if (false == RefreshClass.isEinkHandWritingHardwareType())
			return;

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
	public boolean onSearchRequested() {
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			startSearch(BookSearchPatternOption.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(this, LibrarySearchActivity.class, BookSearchPatternOption.getValue(), null);
		}
		return true;
	}

	private interface ContextItemId {
		int OpenBook = 0;
		int ShowBookInfo = 1;
//		int ShareBook = 2;
//		int AddToFavorites = 3;
//		int RemoveFromFavorites = 4;
//		int MarkAsRead = 5;
//		int MarkAsUnread = 6;
		int DeleteBook = 7;
//		int UploadAgain = 8;
//		int TryAgain = 9;
		int CopyToDevice = 10;
		int CopyToExtSD = 11;

	}

	private interface OptionsItemId {
		int Search = 0;
		int Rescan = 1;
		int UploadAgain = 2;
		int TryAgain = 3;
		int DeleteAll = 4;
		int ExternalView = 5;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {

		final int real_position = myadapter.my_numofcontentperpage * (myadapter.pageid - 1)
				+ ((AdapterView.AdapterContextMenuInfo) menuInfo).position;// for page turn setting

		final Book book = ((LibraryTree) getListAdapter().getItem(real_position)).getBook();

		if (book != null) {
			// Jacky fix quickly click many times crash issue
			if (!getTreeAdapter().getIndexOutOfBoundsException()) {
				createBookContextMenu(menu, book);
			}
			getTreeAdapter().setIndexOutOfBoundsException(false); // Jacky disable IndexOutOfBoundsException (TreeAdapter.java)
		}else if (Globals.rootStatus.equals(Globals.ROOT_ALL_IMAGES)){

			final String filePath = ((FileTree)((LibraryTree) getListAdapter().getItem(real_position))).getFile().getPath();

        	for (String type : Globals.searchImageType){
        		if (filePath.toLowerCase().indexOf(type) > -1){
        			createImageContextMenu(menu, filePath);
        			break;
        		}
        	}
		}
	}
	private void createImageContextMenu(ContextMenu menu, String path) {
    	try{
            if(!new File(path).exists()){
				UIMessageUtil.showErrorMessage(this, "fileNotFound", path);
				return;
    		}
		}catch(Exception e){
			return;
		}

		menu.setHeaderTitle(path);
		menu.add(0, ContextItemId.DeleteBook, 0, ZLResource.resource("library").getResource("deleteBook").getValue());

	}

	String pathPrefix = "/mnt/media_rw/extsd";
	private void createBookContextMenu(ContextMenu menu, Book book) {
    	try{
            if(!new File(book.getPath()).exists()){
				UIMessageUtil.showErrorMessage(this, "fileNotFound", book.getPath());
				return;
    		}
		}catch(Exception e){
			return;
		}
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, ContextItemId.OpenBook, 0, ZLResource.resource("library").getResource("openBook").getValue());
		menu.add(0, ContextItemId.ShowBookInfo, 0, ZLResource.resource("library").getResource("showBookInfo").getValue());

//		if (book.getPath().indexOf(Globals.extSD_path) != -1){
//			if (myCollection.canRemoveBook(book, true) && isSDCanWrite) {
//				menu.add(0, ContextItemId.DeleteBook, 0, resource.getResource("deleteBook").getValue());
//			}
//		}else{
			if (myCollection.canRemoveBook(book, true)) {
				menu.add(0, ContextItemId.DeleteBook, 0, ZLResource.resource("library").getResource("deleteBook").getValue());
			}
//		}


		if (book.getPath().indexOf(Globals.PATH_EXTERNALSD) != -1
				|| book.getPath().indexOf(pathPrefix) != -1) {

			String bookName = book.getPath().substring(book.getPath().lastIndexOf("/") + 1, book.getPath().length());
			File destFileCheck = new File(Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER+"/" + bookName);

			if (destFileCheck.exists()) {
				menu.add(0, ContextItemId.CopyToDevice, 0,
						"[ " + ZLResource.resource("library").getResource("copyFileExist").getValue() + " ] " + bookName);
			} else {
				menu.add(0, ContextItemId.CopyToDevice, 0, ZLResource.resource("library").getResource("copyToDevice").getValue());

			}
		}
//		else if (book.getPath().indexOf(Globals.intSD_path) != -1 ) {
//			String bookName = book.getPath().substring(book.getPath().lastIndexOf("/") + 1, book.getPath().length());
//			File destFileCheck = new File(Globals.extSD_path+"Books/" + bookName);
//
//			if (destFileCheck.exists()) {
//				menu.add(0, ContextItemId.CopyToExtSD, 0,
//						"[ " + resource.getResource("copyToExtSDExist").getValue() + " ] " + bookName);
//			} else {
//				menu.add(0, ContextItemId.CopyToExtSD, 0, resource.getResource("copyToExtSD").getValue());
//
//			}
//		}

		// if (BookUtil.fileByBook(book).getPhysicalFile() != null) {
		// menu.add(0, ContextItemId.ShareBook, 0,
		// resource.getResource("shareBook").getValue());
		// }
		// if (book.hasLabel(Book.FAVORITE_LABEL)) {
		// menu.add(0, ContextItemId.RemoveFromFavorites, 0,
		// resource.getResource("removeFromFavorites").getValue());
		// } else {
		// menu.add(0, ContextItemId.AddToFavorites, 0,
		// resource.getResource("addToFavorites").getValue());
		// }
		// if (book.hasLabel(Book.READ_LABEL)) {
		// menu.add(0, ContextItemId.MarkAsUnread, 0,
		// resource.getResource("markAsUnread").getValue());
		// } else {
		// menu.add(0, ContextItemId.MarkAsRead, 0,
		// resource.getResource("markAsRead").getValue());
		// }
		// if (book.hasLabel(Book.SYNC_DELETED_LABEL)) {
		// menu.add(0, ContextItemId.UploadAgain, 0,
		// resource.getResource("uploadAgain").getValue());
		// }
		// if (book.hasLabel(Book.SYNC_FAILURE_LABEL)) {
		// menu.add(0, ContextItemId.TryAgain, 0,
		// resource.getResource("tryAgain").getValue());
		// }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int real_position = myadapter.my_numofcontentperpage * (myadapter.pageid - 1)
				+ ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;// for page turn setting

		final Book book = ((LibraryTree) getTreeAdapter().getItem(real_position)).getBook();

		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}else {
			final String filePath = ((FileTree)((LibraryTree) getListAdapter().getItem(real_position))).getFile().getPath();

        	for (String type : Globals.searchImageType){
        		if (filePath.toLowerCase().indexOf(type) > -1){
        			return onContextImageSelected(item.getItemId(), filePath);
        		}
        	}

		}
		return super.onContextItemSelected(item);
	}

	private void syncAgain(Book book) {
		book.removeLabel(Book.SYNC_FAILURE_LABEL);
		book.removeLabel(Book.SYNC_DELETED_LABEL);
		book.addNewLabel(Book.SYNC_TOSYNC_LABEL);
		myCollection.saveBook(book);
	}
	private boolean onContextImageSelected(int itemId, String path) {

		switch (itemId) {
		case ContextItemId.DeleteBook:
			try {
				deleteBook(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			refreshCurrentTree();
			return true;
		}
		return false;
	}
	private boolean onContextItemSelected(int itemId, Book book) {
		String bookFileName=book.getPath().substring(book.getPath().lastIndexOf("/") + 1,book.getPath().length());
		File source=null;
		File dest=null;
		switch (itemId) {
		case ContextItemId.OpenBook:
			openBook(book);
			return true;
		case ContextItemId.ShowBookInfo:
			showBookInfo(book);
			return true;
		case ContextItemId.DeleteBook:
			tryToDeleteBook(book);
			return true;
		case ContextItemId.CopyToDevice:
			createDefaultBooksFolder(); // create default "Books" Folder

			source = new File(book.getPath());
			dest = new File(Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER+"/" + bookFileName);

			if (dest.exists()) {
    			Toast.makeText(mContext,"[ " + ZLResource.resource("library").getResource("copyFileExist").getValue()+ " ] " + bookFileName, Toast.LENGTH_SHORT).show();
			} else {
				try {
					copyBook(source, dest);
				} catch (IOException e) {
	    			Toast.makeText(mContext, ZLResource.resource("library").getResource("copyFail").getValue(), Toast.LENGTH_SHORT).show();
				}
			}

			return true;
		case ContextItemId.CopyToExtSD:

			source = new File(book.getPath());
			dest = new File(Globals.PATH_EXTERNALSD+"/"+Globals.BOOKS_FOLDER+"/" + bookFileName);

			File dest_path = new File(Globals.PATH_EXTERNALSD+"/"+Globals.BOOKS_FOLDER);
			final File newFile = new File(dest_path.getPath());
	        MediaFile mf = new MediaFile(getContentResolver(), newFile);
	        try {
				mf.mkdir();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (dest.exists()) {
				Toast.makeText(this, "[ " + ZLResource.resource("library").getResource("copyToExtSDExist").getValue() + " ] " + bookFileName,
						Toast.LENGTH_SHORT).show();
			} else {
				try {
					copyBook(source, dest);
				} catch (IOException e) {
					Toast.makeText(mContext, ZLResource.resource("library").getResource("copyFail").getValue(), Toast.LENGTH_SHORT).show();
				}
			}
			return true;

		// case ContextItemId.ShareBook:
		// FBUtil.shareBook(this, book);
		// return true;
		// case ContextItemId.AddToFavorites:
		// book.addNewLabel(Book.FAVORITE_LABEL);
		// myCollection.saveBook(book);
		// return true;
		// case ContextItemId.RemoveFromFavorites:
		// book.removeLabel(Book.FAVORITE_LABEL);
		// myCollection.saveBook(book);
		// if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
		// getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
		// }
		// return true;
		// case ContextItemId.MarkAsRead:
		// book.addNewLabel(Book.READ_LABEL);
		// myCollection.saveBook(book);
		// getListView().invalidateViews();
		// return true;
		// case ContextItemId.MarkAsUnread:
		// book.removeLabel(Book.READ_LABEL);
		// myCollection.saveBook(book);
		// getListView().invalidateViews();
		// return true;
		// case ContextItemId.UploadAgain:
		// case ContextItemId.TryAgain:
		// syncAgain(book);
		// if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
		// getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
		// }
		// return true;
		}
		return false;
	}
	public void copyBook(File src, File dst) throws IOException
	{
	    FileInputStream fileInputStream = new FileInputStream(src);
		FileChannel inChannel = fileInputStream.getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();

	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    }
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
		Toast.makeText(mContext, ZLResource.resource("library").getResource("copySuccess").getValue(), Toast.LENGTH_SHORT).show();
	}

	private void deleteBookDialog(final String path, boolean isImage) {
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        String dialogTag = "delete_confirm";
        String deleteConfirmMessage = isImage ? getResources().getString(R.string.toolbox_message_delete_confirm_image, 1 + "") : getResources().getString(R.string.toolbox_message_delete_confirm_book, 1 + "");
        AlertDialogFragment deleteConfirmDialogFragment = AlertDialogFragment.newInstance(deleteConfirmMessage, R.drawable.writing_ic_error,  dialogTag);

        deleteConfirmDialogFragment.setupPositiveButton(getString(android.R.string.yes));
        deleteConfirmDialogFragment.setupNegativeButton(getString(android.R.string.no));
        deleteConfirmDialogFragment.registerAlertDialogButtonClickListener(new AlertDialogButtonClickListener() {

            @Override
            public void onPositiveButtonClick(String fragmentTag) {
            	try {
					deleteBook(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }

            @Override
            public void onNegativeButtonClick(String fragmentTag) {
            }
        }, dialogTag);

        ft.replace(R.id.alert_dialog_container, deleteConfirmDialogFragment, dialogTag)
                .commit();
	}
	
	public boolean deleteBook(String path)
    throws IOException {
		String bookName = path.substring(path.lastIndexOf("/") + 1, path.length());
		String bookPath = path.substring(0, path.lastIndexOf("/"));

//        File file = new File(bookPath, bookName);
//        MediaFile mf = new MediaFile(getContentResolver(), file);
//        boolean result = mf.delete();
//        if(result){
//        	refreshCurrentTree();
//        }
		
		boolean result = false;
        File file = new File(bookPath, bookName);
        result = file.getCanonicalFile().delete();
        if(file.exists()){
        	result = getApplicationContext().deleteFile(file.getName());
        }
        
	    if(result){
	    	refreshCurrentTree();
	    }
        
        return result;
    }
	// Options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, OptionsItemId.Search, "localSearch", R.drawable.ic_menu_search);
		addMenuItem(menu, OptionsItemId.Rescan, "rescan", R.drawable.ic_menu_refresh);
		addMenuItem(menu, OptionsItemId.UploadAgain, "uploadAgain", -1);
		addMenuItem(menu, OptionsItemId.TryAgain, "tryAgain", -1);
		addMenuItem(menu, OptionsItemId.DeleteAll, "deleteAll", -1);
		// if (Build.VERSION.SDK_INT >= 9) {
		// addMenuItem(menu, OptionsItemId.ExternalView, "bookshelfView", -1);
		// }
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean enableUploadAgain = false;
		boolean enableTryAgain = false;
		boolean enableDeleteAll = false;
		final LibraryTree tree = getCurrentTree();
		if (tree instanceof SyncLabelTree) {
			final String label = ((SyncLabelTree) tree).Label;
			if (Book.SYNC_DELETED_LABEL.equals(label)) {
				enableUploadAgain = true;
				enableDeleteAll = true;
			} else if (Book.SYNC_FAILURE_LABEL.equals(label)) {
				enableTryAgain = true;
			}
		}

		final MenuItem rescanItem = menu.findItem(OptionsItemId.Rescan);
		myCollection.bindToService(this, new Runnable() {
            @Override
            public void run() {
				rescanItem.setEnabled(myCollection.status().IsComplete);
			}
		});
		rescanItem.setVisible(tree == myRootTree);
		menu.findItem(OptionsItemId.UploadAgain).setVisible(enableUploadAgain);
		menu.findItem(OptionsItemId.TryAgain).setVisible(enableTryAgain);
		menu.findItem(OptionsItemId.DeleteAll).setVisible(enableDeleteAll);

		return true;
	}

	private MenuItem addMenuItem(Menu menu, int id, String resourceKey, int iconId) {
		final String label = LibraryTree.resource().getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		if (iconId != -1) {
			item.setIcon(iconId);
		}
		return item;
	}

    @Override
    public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case OptionsItemId.Search:
			return onSearchRequested();
		case OptionsItemId.Rescan:
			if (myCollection.status().IsComplete) {
				myCollection.reset(true);
				openTree(myRootTree);
			}
			return true;
		case OptionsItemId.UploadAgain:
		case OptionsItemId.TryAgain:
			for (FBTree tree : getCurrentTree().subtrees()) {
				if (tree instanceof BookTree) {
					syncAgain(((BookTree) tree).Book);
				}
			}
			getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
			return true;
		case OptionsItemId.DeleteAll: {
			final List<Book> books = new LinkedList<Book>();
			for (FBTree tree : getCurrentTree().subtrees()) {
				if (tree instanceof BookTree) {
					books.add(((BookTree) tree).Book);
				}
			}
			tryToDeleteBooks(books);
			return true;
		}
		case OptionsItemId.ExternalView:
			runOrInstallExternalView(true);
			return true;
		default:
			return true;
		}
	}

	private void runOrInstallExternalView(boolean install) {
		try {
			startActivity(new Intent(FBReaderIntents.Action.EXTERNAL_LIBRARY));
			finish();
		} catch (ActivityNotFoundException e) {
			if (install) {
				PackageUtil.installFromMarket(this, "org.fbreader.plugin.library");
			}
		}
	}

	private void tryToDeleteBooks(final List<Book> books) {
		final int size = books.size();
		if (size == 0) {
			return;
		}
		final ZLResource dialogResource = ZLResource.resource("dialog");
		// final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource
				.getResource(size == 1 ? "deleteBookBox" : "deleteMultipleBookBox");
		final String title = size == 1 ? books.get(0).getTitle() : boxResource.getResource("title").getValue();
		final String message = boxResource.getResource("message").getValue(size).replaceAll("%s", String.valueOf(size));

		// Dialog
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		AlertDialogFragment overwriteHintDialogFragment;
		String overWriteHintDialogTag = "over_write_hint";

		overwriteHintDialogFragment = AlertDialogFragment.newInstance(title+"\n\n"+message, R.drawable.writing_ic_error, overWriteHintDialogTag);
		overwriteHintDialogFragment.setupPositiveButton(getString(android.R.string.yes));
		overwriteHintDialogFragment.setupNegativeButton(getString(android.R.string.no));

		overwriteHintDialogFragment.registerAlertDialogButtonClickListener(new AlertDialogButtonClickListener() {

			@Override
			public void onPositiveButtonClick(String fragmentTag) {
				final List<Book> myBooks = new ArrayList<Book>(books);

					if (getCurrentTree() instanceof FileTree) {
						for (Book book : myBooks) {
							getTreeAdapter().remove(new FileTree((FileTree) getCurrentTree(), BookUtil.fileByBook(book)));
							try {
								deleteBook(book.getPath());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						getListView().invalidateViews();
					} else {
						boolean doReplace = false;
						for (Book book : myBooks) {
							doReplace |= doReplace | getCurrentTree().onBookEvent(BookEvent.Removed, book);
							try {
								deleteBook(book.getPath());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						refreshCurrentTree();
					}
			}

			@Override
			public void onNegativeButtonClick(String fragmentTag) {}
		}, overWriteHintDialogTag);
		ft.replace(R.id.alert_dialog_container, overwriteHintDialogFragment, overWriteHintDialogTag)
				.commit();

//		new AlertDialog.Builder(this, R.style.AlertDialog_custom).setTitle(title).setMessage(message).setIcon(0)
//				.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(books))
//				.setNegativeButton(buttonResource.getResource("no").getValue(), null).create().show();
	}

	private void tryToDeleteBook(Book book) {
		tryToDeleteBooks(Collections.singletonList(book));
	}

	private void startBookSearch(final String pattern) {
		BookSearchPatternOption.setValue(pattern);

		final Thread searcher = new Thread("Library.searchBooks") {
            @Override
            public void run() {
				final SearchResultsTree oldSearchResults = myRootTree.getSearchResultsTree();

				if (oldSearchResults != null && pattern.equals(oldSearchResults.Pattern)) {
					onSearchEvent(true);
				} else if (myCollection.hasBooks(new Filter.ByPattern(pattern))) {
					if (oldSearchResults != null) {
						oldSearchResults.removeSelf();
					}
					myRootTree.createSearchResultsTree(pattern);
					onSearchEvent(true);
				} else {
					onSearchEvent(false);
				}
			}
		};
		searcher.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		searcher.start();
	}

	private void onSearchEvent(final boolean found) {
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
				if (found) {
					searchResultNotFoundMessage(false);
					openSearchResults();
				} else {
					searchResultNotFoundMessage(true);
//					UIMessageUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
				}
			}
		});
	}

    @Override
    public void onBookEvent(BookEvent event, Book book) {
		if (getCurrentTree().onBookEvent(event, book)) {
			getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEvent(RecentBookCoverCheckEvent event) {

	}

    @Override
    public void onBuildEvent(IBookCollection.Status status) {
		setProgressBarIndeterminateVisibility(!status.IsComplete);
	}

	@Override
	public void onPause() {
		changeEinkControlPermission(false);
		Intent myIntent = new Intent("ntx.eink_control.GLOBAL_REFRESH");
		myIntent.putExtra("updatemode", RefreshClass.UPDATE_MODE_GLOBAL_RESET);
		myIntent.putExtra("commandFromNtxApp", true);
		sendBroadcast(myIntent);
		super.onPause();

		myRootTree.fileFirstLevelTreeAllBooksSearchStop();
		searchRequest(false); // close search box

		Globals.restRootStatus(); // reset rootStatus to default.
	}
	
    private void changeEinkControlPermission(boolean isForNtxAppsOnly) {
        Intent changePermissionIntent = new Intent("ntx.eink_control.CHANGE_PERMISSION");
        changePermissionIntent.putExtra("isPermissionNtxApp", isForNtxAppsOnly);
        sendBroadcast(changePermissionIntent);
    }

	@Override
	protected void onResume() {
		changeEinkControlPermission(true);
		super.onResume();
		showLibrary(false);
		
		Intent saveRecentBookIntent = new Intent(LibraryActivity.this, SaveRecentBookListService.class);
		startService(saveRecentBookIntent);

		setScreenPortrait();

		if (myRootTree != null)
			handler.post(runnable);
		
		Globals.closeWaitDialog(this);
	}

	private void onPrevPage() {
		if (myadapter.pageid > 1) {
			myadapter.pageid--;
		} else {
			myadapter.pageid = myadapter.getTotalPage();
		}
		pageInfo.setText(myadapter.pageid + " of " + myadapter.getTotalPage());
		myadapter.notifyDataSetChanged();

	}

	private void onNextPage() {
		if (myadapter.pageid < myadapter.getTotalPage()) {
			myadapter.pageid++;
		} else {
			myadapter.pageid = 1;
		}
		pageInfo.setText(myadapter.pageid + " of " + myadapter.getTotalPage());
		myadapter.notifyDataSetChanged();
	}

	private void openBook(Book book) {
		Globals.openWaitDialog(this);
    	try{
            if(!new File(book.getPath()).exists()){
				UIMessageUtil.showErrorMessage(this, "fileNotFound", book.getPath());
				Globals.closeWaitDialog(this);
				return;
    		}
		}catch(Exception e){
			Globals.closeWaitDialog(this);
			return;
		}

		Intent saveRecentBookIntent = new Intent(LibraryActivity.this, SaveRecentBookListService.class);
		startService(saveRecentBookIntent);

		String myExtension = ZLFile.createFileByPath(book.getPath()).getExtension();
		String path = book.getPath();

		if (myExtension.equalsIgnoreCase("cbr") || myExtension.equalsIgnoreCase("cbz")) {
			try {
				Intent mintent = new Intent("android.intent.action.VIEW");
				mintent.setData(Uri.parse("file://" + path));
				mintent.setClassName(Globals.CBRCBZ_PACKAGE, Globals.CBRCBZ_CLASS);
				mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(mintent);
			} catch (Throwable e) {
				Toast.makeText(this, "Not find ComicViewer !", Toast.LENGTH_LONG).show();
			}

		} else if (myExtension.equalsIgnoreCase("pdf")) {
			try {
//				Intent mintent = new Intent("android.intent.action.VIEW");
//				mintent.setData(Uri.parse("file://" + path));
//				mintent.setClassName(Globals.PDF_PACKAGE, Globals.PDF_CLASS);
//				mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//				startActivity(mintent);

				Intent mintent = new Intent("android.intent.action.VIEW");
				mintent.setDataAndType(Uri.parse("file://" + path),"application/pdf");
                startActivity(mintent);

				myCollection.addToRecentlyOpened(book);
			} catch (Throwable e) {
				Toast.makeText(this, "Not find PDF Reader !", Toast.LENGTH_LONG).show();
			}
			// Daniel 20121218 open epub file using DLReader
		} else if (myExtension.equalsIgnoreCase("epub") || myExtension.equalsIgnoreCase("fb2")
				|| myExtension.equalsIgnoreCase("mobi") || myExtension.equalsIgnoreCase("prc")
				|| myExtension.equalsIgnoreCase("oeb") || myExtension.equalsIgnoreCase("txt")
				|| myExtension.equalsIgnoreCase("rtf")
				|| myExtension.equalsIgnoreCase("azw3")
		) {
			FBReader.openBookActivity(LibraryActivity.this, book, null);
		} else {
			Toast.makeText(this, "Not support \"" + myExtension.toUpperCase() + "\" format file", Toast.LENGTH_LONG)
					.show();
		}
	}

	private void addFavoriteList() {
		BookQuery BQ = new BookQuery(new Filter.ByLabel(Book.FAVORITE_LABEL), myCollection.size());// Jacky 20160408
		if (myCollection.hasBooks(new Filter.ByLabel(Book.FAVORITE_LABEL))) {
			for (int i = 0; i < myCollection.books(BQ).size(); i++) {
				FavoritesTree.addToFavoritesList(myCollection.books(BQ).get(i));
			}
		}
	}

    private void setScreenPortrait() {
    	if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
    	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	}
    }

	public Handler handler = new Handler();
	public Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (RefreshClass.isEinkHandWritingHardwareType())
				setTabVisibility(TAB_INDEX_READER);

			final Bundle bundle = getIntent().getExtras();
			if (bundle != null){
				
				getIntentValue(getIntent()); // get bookPath value;
				if (false == bookPath.equals("")){
					showLibrary(false);

					Handler handler = new Handler();
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							try{
								openBook(myCollection.getBookByFile(bookPath));
							}catch (Exception e){
							}
							
							setIntent(new Intent());
						}
					};

					handler.postDelayed(runnable,100);

		    	}else{
		    		
		    		if (Globals.rootStatus.equals(Globals.ROOT_ALL_IMAGES))	switchToAllImages();
					else if (Globals.rootStatus.equals(Globals.ROOT_EXT_SD))	switchToExtSDcard();
					else if (Globals.rootStatus.equals(Globals.ROOT_FAVORITES))	switchToFavorites();
					else if (Globals.rootStatus.equals(Globals.ROOT_RECENT))		switchToRecent();
					else switchToAllBooks();

		    		showLibrary(true);
					setView();
		    	}

				Globals.closeWaitDialog(LibraryActivity.this);
			}else{
				if (Globals.rootStatus.equals(Globals.ROOT_ALL_IMAGES))	switchToAllImages();
				else if (Globals.rootStatus.equals(Globals.ROOT_EXT_SD))	switchToExtSDcard();
				else if (Globals.rootStatus.equals(Globals.ROOT_FAVORITES))	switchToFavorites();
				else if (Globals.rootStatus.equals(Globals.ROOT_RECENT))		switchToRecent();
				else switchToAllBooks();
				
				showLibrary(true);
				setView();
				Globals.closeWaitDialog(LibraryActivity.this);
			}
		}
	};

	public void refreshCurrentTree(){
		int adapterPage=myadapter.pageid;
		openTree(getCurrentTree());
		myadapter.pageid = adapterPage;

		if (myadapter.pageid > myadapter.getTotalPage()) {
			myadapter.pageid = myadapter.getTotalPage();
		}

		pageInfo.setText(myadapter.pageid + " of " + myadapter.getTotalPage());
		myadapter.notifyDataSetChanged();
	}

	/* ------ Start Workaround :: Fix android 4.4 pressed front light key to back issue ------ */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return (android.os.Build.VERSION.SDK_INT == 19 && keyCode == 97) ? true : super.onKeyDown(keyCode, event);

	}
	/* ------ End   Workaround :: Fix android 4.4 pressed front light key to back issue ------ */

	@Override
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEvent(CallbackEvent event) {

		if (event.getMessage().equals(CallbackEvent.MORE_RECENT)) {
			Globals.rootStatus = Globals.ROOT_RECENT;
			openTree((LibraryTree) myRootTree.getSubtree(Globals.ROOT_RECENT));
		}else if (event.getMessage().equals(CallbackEvent.MORE_AUTHOR)){
			Globals.rootStatus = Globals.ROOT_BY_AUTHOR;
			openTree((LibraryTree) myRootTree.getSubtree(Globals.ROOT_BY_AUTHOR));
		}else if (event.getMessage().equals(CallbackEvent.MORE_TITLE)){
			Globals.rootStatus = Globals.ROOT_BY_TITLE;
			openTree((LibraryTree) myRootTree.getSubtree(Globals.ROOT_BY_TITLE));
		}else if (event.getMessage().equals(CallbackEvent.MORE_ALL_BOOKS)){
			switchToAllBooks();
		}else if (event.getMessage().equals(CallbackEvent.MORE_ALL_IMAGES)){
			switchToAllImages();
		}else if (event.getMessage().equals(CallbackEvent.MORE_FAVORITES)){
			switchToFavorites();
		}else if (event.getMessage().equals(CallbackEvent.MORE_EXT_SD_CARD)){
			switchToExtSDcard();
		}else if (event.getMessage().equals(CallbackEvent.MORE_ALL_BOOKS_REFRESH)){
			refreshPageAdapter();
			init(getIntent());
			
		}else if (event.getMessage().equals(CallbackEvent.MORE_ALL_BOOKS_SEARCH_DONE)){
			if (false == refreshButton.isEnabled()){
				refreshButton.setEnabled(true);
				refreshButton.setAlpha(1f);
			}
			refreshPageAdapter();
			init(getIntent());
			Globals.closeWaitDialog(this);

		}
	}

	private void openTreeAgain(final String root){
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try{
					openTree((LibraryTree) myRootTree.getSubtree(root));
					showLibrary(true);
				}catch (Exception e){
					 e.printStackTrace();
				}
			}
		};

		handler.postDelayed(runnable,1000);
	}

	private void switchToAllBooks(){
		Globals.rootStatus = Globals.ROOT_ALL_BOOKS;

		if (null != myRootTree){
			openTreeOnlyForAllBooks((LibraryTree) myRootTree.getSubtree(Globals.rootStatus));
			showLibrary(true);
		}
		
		searchAllBooks();
	}
	private void switchToAllImages(){
		Globals.rootStatus = Globals.ROOT_ALL_IMAGES;

		try{
			openTree((LibraryTree) myRootTree.getSubtree(Globals.rootStatus));
			showLibrary(true);
		}catch (Exception e){
			e.printStackTrace();
			openTreeAgain(Globals.rootStatus);
		}
	}

	private void switchToExtSDcard(){
		Globals.rootStatus = Globals.ROOT_EXT_SD;

		boolean isAvailable = false;
		for (String extSDState : Globals.extStateAvailable){
			if (Environment.getExternalExtSDStorageState().equals(extSDState)){
				isAvailable = true;
			}
		}
		
		try{
			openTree((LibraryTree) myRootTree.getSubtree(Globals.rootStatus));
			showLibrary(true);
		}catch (Exception e){
			openTreeAgain(Globals.rootStatus);
		}
		
		if (isAvailable){
			File[] the_Files = new File(Globals.PATH_EXTERNALSD).listFiles();
			if (the_Files.length <= 0) {
				Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("noFile").getValue(), Toast.LENGTH_LONG).show();
			}
		}else{
			
			Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("checkSDCard").getValue(), Toast.LENGTH_LONG).show();
		}
	}

	private void switchToFavorites(){
		if (FavoritesTree.inFavoritesBook.size()==0){
			Toast.makeText(LibraryActivity.this, ZLResource.resource("errorMessage").getResource("noFavorites").getValue(), Toast.LENGTH_LONG).show();
		}else{
			Globals.rootStatus = Globals.ROOT_FAVORITES;
			try{
				openTree((LibraryTree) myRootTree.getSubtree(Globals.rootStatus));
				showLibrary(true);
			}catch (Exception e){
				// ALog.debug(e.toString());
				openTreeAgain(Globals.rootStatus);
			}
		}
	}

	private void switchToRecent(){
		Globals.rootStatus = Globals.ROOT_RECENT;

		try{
			openTree((LibraryTree) myRootTree.getSubtree(Globals.rootStatus));
			showLibrary(true);
		}catch (Exception e){
			// ALog.debug(e.toString());
			openTreeAgain(Globals.rootStatus);
		}
	}

	/**
	 * Search Box
	 * @param b ; true = open search box ; false = close search box
	 */
	private void searchRequest(boolean b){

		searchBox.setVisibility(b ? View.VISIBLE : View.GONE);

		if (b){
			searchText.requestFocus();
			showInputMethod();
		}else{
			searchText.setText("");
			hideInputMethod(searchText);
		}
	}

    private void showInputMethod() {
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void hideInputMethod(View view) {
    	if ( null != view){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    	}
	}
    
    /**
	 * Show not found book message
	 * @param b ; true = open search result ; false = show not found message
	 */
    private void searchResultNotFoundMessage(boolean b){
    	mTvSearchNotFoundHint.setVisibility( b ? View.VISIBLE : View.GONE);
		if (currentView == GRID_VIEW) { // currentView== GRID_VIEW
			bookshelf_listview.setVisibility(View.GONE);
			bookshelf_gridview.setVisibility(b ? View.GONE : View.VISIBLE);
		} else {
			bookshelf_listview.setVisibility(b ? View.GONE : View.VISIBLE);
			bookshelf_gridview.setVisibility(View.GONE);
		}
		
		previousButton.setVisibility(b ? View.GONE : View.VISIBLE);
		nextButton.setVisibility(b ? View.GONE : View.VISIBLE);
		pageInfo.setVisibility(b ? View.GONE : View.VISIBLE);
		
    }
    
    private void refreshPageAdapter(){
		if (myadapter.pageid > myadapter.getTotalPage()) {
			myadapter.pageid = myadapter.getTotalPage();
		}

		pageInfo.setText(myadapter.pageid + " of " + myadapter.getTotalPage());
		myadapter.notifyDataSetChanged();
    }
    
    private void searchAllBooks(){
    	
    	if (null != myRootTree){
        	refreshButton.setEnabled(false);
    		refreshButton.setAlpha(0.1f);
    		
    		myRootTree.fileFirstLevelTreeAllBooks(); // Search all books.	
    	}
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		return gestureDetector.onTouchEvent(arg1);
	}
	
	@SuppressWarnings("deprecation")
	final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
		int FLYING_GESTURE_MIN_VELOCITY = 0;

		// public void onLongPress(MotionEvent e) {
		// Log.d("Dylan", "Longpress detected : " + origin_Position);
		// }

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1.getX() - e2.getX() > FLYING_GESTURE_HORIZONTAL_MIN_DISTANCE
					&& Math.abs(velocityX) > FLYING_GESTURE_MIN_VELOCITY) {
				onNextPage();
			} else if (e2.getX() - e1.getX() > FLYING_GESTURE_HORIZONTAL_MIN_DISTANCE
					&& Math.abs(velocityX) > FLYING_GESTURE_MIN_VELOCITY) {
				onPrevPage();
			}
			return false;
		}
	});
	
	long bookId = 0;
	String bookPath = "";
	
	private void getIntentValue(Intent intent){
		bookId = intent.getExtras().getLong(Globals.KEY_READER_BOOK_ID,0);
		bookPath = intent.getExtras().getString(Globals.KEY_READER_BOOK_PATH,"");
	}
}
