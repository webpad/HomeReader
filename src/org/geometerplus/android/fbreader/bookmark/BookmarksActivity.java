/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.bookmark;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.DeviceType;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.SearchDialogUtil;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import com.ntx.config.Globals;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import ntx.reader3.R;

public class BookmarksActivity extends Activity implements IBookCollection.Listener<Book> {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	private TabHost myTabHost;

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;
	private volatile Bookmark myBookmark;

	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();

	private volatile BookmarksAdapter myThisBookAdapter;
	private volatile BookmarksAdapter myAllBooksAdapter;
	private volatile BookmarksAdapter currentBooksAdapter;

	private volatile BookmarksAdapter mySearchResultsAdapter;

	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private final ZLStringOption myBookmarkSearchPatternOption =
		new ZLStringOption("BookmarkSearch", "Pattern", "");

	private ImageView bookmarkClose;
	private ImageView nextPage,prePage,firstPage,lastPage;
	private TextView pageInfo;
	public static final String THIS_BOOK_TAB = "this_book";
	public static final String ALL_BOOKS_TAB = "all_books";
	private static String currentTAB = THIS_BOOK_TAB;
	
	private void createTab(String tag, int id) {
		final String label = myResource.getResource(tag).getValue();
		myTabHost.addTab(myTabHost.newTabSpec(tag).setIndicator(label).setContent(id));
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.bookmarks);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

        myTabHost = (TabHost)findViewById(R.id.bookmarks_tabhost);
		myTabHost.setup();

		currentTAB = getCurrentTab().equals(THIS_BOOK_TAB) ? THIS_BOOK_TAB : ALL_BOOKS_TAB;

		if (getCurrentTab().equals(THIS_BOOK_TAB)){
			createTab("thisBook", R.id.bookmarks_this_book);
			createTab("allBooks", R.id.bookmarks_all_books);	
		}else{
			createTab("allBooks", R.id.bookmarks_all_books);
			createTab("thisBook", R.id.bookmarks_this_book);
		}
		
//		TextView x = (TextView) myTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
//		   x.setTextSize(24);
//		   
//		TextView x1 = (TextView) myTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
//		   x1.setTextSize(24);

		bookmarkClose = (ImageView) findViewById(R.id.bookmark_close);
		bookmarkClose.setOnClickListener(onBtnClickListener);
			
		myBook = FBReaderIntents.getBookExtra(getIntent(), myCollection);
		if (myBook == null) {
			finish();
		}
		myBookmark = FBReaderIntents.getBookmarkExtra(getIntent());
				
		nextPage = (ImageView) findViewById(R.id.btn_next_page);
		prePage = (ImageView) findViewById(R.id.btn_prev_page);
		lastPage = (ImageView) findViewById(R.id.btn_last_page);
		firstPage = (ImageView) findViewById(R.id.btn_first_page);
		pageInfo = (TextView) findViewById(R.id.tv_page_info);

		nextPage.setOnClickListener(onBtnClickListener);
		prePage.setOnClickListener(onBtnClickListener);
		lastPage.setOnClickListener(onBtnClickListener);
		firstPage.setOnClickListener(onBtnClickListener);

	}
	
	private View.OnClickListener onBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bookmark_close:
				finish();
				break;
				
			case R.id.btn_next_page:
				onNextPageThisBook(currentBooksAdapter);
				break;				
			case R.id.btn_prev_page:
				onPrevPageThisBook(currentBooksAdapter);
				break;				
			case R.id.btn_last_page:
				onLastPageThisBook(currentBooksAdapter);
				break;				
			case R.id.btn_first_page:
				onFirstPageThisBook(currentBooksAdapter);
				break;				

			}
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();

		Globals.closeWaitDialog(this);
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				if (myAllBooksAdapter != null) {
					return;
				}

				myThisBookAdapter =
					new BookmarksAdapter((ListView)findViewById(R.id.bookmarks_this_book), myBookmark != null);
				myAllBooksAdapter =
					new BookmarksAdapter((ListView)findViewById(R.id.bookmarks_all_books), false);
				myCollection.addListener(BookmarksActivity.this);

				loadBookmarks();
			}
		});

		OrientationUtil.setOrientation(this, getIntent());
		mHandler.postDelayed(mRunnable,2000);

	}

	private final Object myBookmarksLock = new Object();

	private void loadBookmarks() {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					for (BookmarkQuery query = new BookmarkQuery(myBook, 50); ; query = query.next()) {
						final List<Bookmark> thisBookBookmarks = myCollection.bookmarks(query);
						if (thisBookBookmarks.isEmpty()) {
							break;
						}
						myThisBookAdapter.addAll(thisBookBookmarks);
						myAllBooksAdapter.addAll(thisBookBookmarks);
					}
					for (BookmarkQuery query = new BookmarkQuery(50); ; query = query.next()) {
						final List<Bookmark> allBookmarks = myCollection.bookmarks(query);
						if (allBookmarks.isEmpty()) {
							break;
						}
						myAllBooksAdapter.addAll(allBookmarks);
					}

					refreshInfo();
				}
			}
		}).start();
	}

	private void updateBookmarks(final Book book) {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					final boolean flagThisBookTab = book.getId() == myBook.getId();
					final boolean flagSearchTab = mySearchResultsAdapter != null;

					final Map<String,Bookmark> oldBookmarks = new HashMap<String,Bookmark>();
					if (flagThisBookTab) {
						for (Bookmark b : myThisBookAdapter.bookmarks()) {
							oldBookmarks.put(b.Uid, b);
						}
					} else {
						for (Bookmark b : myAllBooksAdapter.bookmarks()) {
							if (b.BookId == book.getId()) {
								oldBookmarks.put(b.Uid, b);
							}
						}
					}
					final String pattern = myBookmarkSearchPatternOption.getValue().toLowerCase();

					for (BookmarkQuery query = new BookmarkQuery(book, 50); ; query = query.next()) {
						final List<Bookmark> loaded = myCollection.bookmarks(query);
						if (loaded.isEmpty()) {
							break;
						}
						for (Bookmark b : loaded) {
							final Bookmark old = oldBookmarks.remove(b.Uid);
							myAllBooksAdapter.replace(old, b);
							if (flagThisBookTab) {
								myThisBookAdapter.replace(old, b);
							}
							if (flagSearchTab && MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
								mySearchResultsAdapter.replace(old, b);
							}
						}
					}
					myAllBooksAdapter.removeAll(oldBookmarks.values());
					if (flagThisBookTab) {
						myThisBookAdapter.removeAll(oldBookmarks.values());
					}
					if (flagSearchTab) {
						mySearchResultsAdapter.removeAll(oldBookmarks.values());
					}
					
					refreshInfo();
				}
			}
		}).start();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);

		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		myBookmarkSearchPatternOption.setValue(pattern);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
		for (Bookmark b : myAllBooksAdapter.bookmarks()) {
			if (MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				bookmarks.add(b);
			}
		}
//		if (!bookmarks.isEmpty()) {
//			final ListView resultsView = (ListView)findViewById(R.id.bookmarks_search_results);
//			resultsView.setVisibility(View.VISIBLE);
//			if (mySearchResultsAdapter == null) {
//				mySearchResultsAdapter = new BookmarksAdapter(resultsView, false);
//			} else {
//				mySearchResultsAdapter.clear();
//			}
//			mySearchResultsAdapter.addAll(bookmarks);
//		} else {
//			UIMessageUtil.showErrorMessage(this, "bookmarkNotFound");
//		}
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(this, BookmarksActivity.class, myBookmarkSearchPatternOption.getValue(), null);
		}
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final String tag = myTabHost.getCurrentTabTag();
		final BookmarksAdapter adapter;
		if ("thisBook".equals(tag)) {
			adapter = myThisBookAdapter;
		} else if ("allBooks".equals(tag)) {
			adapter = myAllBooksAdapter;
		} else if ("search".equals(tag)) {
			adapter = mySearchResultsAdapter;
		} else {
			throw new RuntimeException("Unknown tab tag: " + tag);
		}

		final Bookmark bookmark = adapter.getItem(position);
		switch (item.getItemId()) {
			case OPEN_ITEM_ID:
				gotoBookmark(bookmark);
				return true;
			case EDIT_ITEM_ID:
				final Intent intent = new Intent(this, EditBookmarkActivity.class);
				FBReaderIntents.putBookmarkExtra(intent, bookmark);
				OrientationUtil.startActivity(this, intent);
				return true;
			case DELETE_ITEM_ID:
				myCollection.deleteBookmark(bookmark);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.BookId);
		if (book != null) {
			FBReader.openBookActivity(this, book, bookmark);
		} else {
			UIMessageUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
		private final List<Bookmark> myBookmarksList =
			Collections.synchronizedList(new LinkedList<Bookmark>());
		private volatile boolean myShowAddBookmarkItem;
		
		int my_numofcontentperpage = 6; // Numbers of Items show on a page
		int pageid = 1;// current page
	    
		BookmarksAdapter(ListView listView, boolean showAddBookmarkItem) {
			myShowAddBookmarkItem = showAddBookmarkItem;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}

		public List<Bookmark> bookmarks() {
			return Collections.unmodifiableList(myBookmarksList);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarksList) {
						for (Bookmark b : bookmarks) {
							final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
							if (position < 0) {
								myBookmarksList.add(- position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		private boolean areEqualsForView(Bookmark b0, Bookmark b1) {
			return
				b0.getStyleId() == b1.getStyleId() &&
				b0.getText().equals(b1.getText()) &&
				b0.getTimestamp(Bookmark.DateType.Latest).equals(b1.getTimestamp(Bookmark.DateType.Latest));
		}

		public void replace(final Bookmark old, final Bookmark b) {
			if (old != null && areEqualsForView(old, b)) {
				return;
			}
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarksList) {
						if (old != null) {
							myBookmarksList.remove(old);
						}
						final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
						if (position < 0) {
							myBookmarksList.add(- position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void removeAll(final Collection<Bookmark> bookmarks) {
			if (bookmarks.isEmpty()) {
				return;
			}
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarksList.removeAll(bookmarks);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarksList.clear();
					notifyDataSetChanged();
				}
			});
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if (getItem(position) != null) {
				menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("openBook").getValue());
				menu.add(0, EDIT_ITEM_ID, 0, myResource.getResource("editBookmark").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("deleteBookmark").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int real_position = my_numofcontentperpage * (pageid - 1) + position;// for page turn setting

			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
			final TextView textView = ViewUtil.findTextView(view, R.id.bookmark_item_text);
			final ImageView bookmarkStatus = ViewUtil.findImageView(view, R.id.bookmark_status);

			final TextView bookTitleView = ViewUtil.findTextView(view, R.id.bookmark_item_booktitle);
			final ImageView bookmarkTextEdit = ViewUtil.findImageView(view, R.id.bookmark_text_edit);
			final ImageView bookmarkItemDelete = ViewUtil.findImageView(view, R.id.bookmark_item_delete);
			final Bookmark bookmark = getItem(real_position);

			bookmarkTextEdit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
				
					final Intent intent = new Intent(BookmarksActivity.this, EditBookmarkActivity.class);
					FBReaderIntents.putBookmarkExtra(intent, bookmark);
					OrientationUtil.startActivity(BookmarksActivity.this, intent);
						
				}
			});

			bookmarkItemDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					deleteBookmarkDialog(bookmark);
				}
			});
			
			if (bookmark == null) {
				bookmarkStatus.setImageResource(R.drawable.btn_increase);
				textView.setText(myResource.getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
				bookmarkTextEdit.setVisibility(View.GONE);
				bookmarkItemDelete.setVisibility(View.GONE);
			} else {
				bookmarkStatus.setImageResource(R.drawable.calendar_ic_note);
				textView.setText(bookmark.getText());
				bookTitleView.setText(bookmark.BookTitle);
				bookTitleView.setVisibility(View.VISIBLE);
				bookmarkTextEdit.setVisibility(View.VISIBLE);
				bookmarkItemDelete.setVisibility(View.VISIBLE);
			}
			return view;
		}

		@Override
		public final boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public final boolean isEnabled(int position) {
			return true;
		}

		@Override
		public final long getItemId(int position) {
			final Bookmark item = getItem(position);
			return item != null ? item.getId() : -1;
		}

		@Override
		public final Bookmark getItem(int position) {
			if (myShowAddBookmarkItem) {
				--position;
			}
			return position >= 0 ? myBookmarksList.get(position) : null;
		}

		@Override
		public final int getCount() {
//			return myShowAddBookmarkItem ? myBookmarksList.size() + 1 : myBookmarksList.size();
			int totalItem = myShowAddBookmarkItem ? myBookmarksList.size() + 1 : myBookmarksList.size();

			if ( pageid == getTotalPage() ) {
				return totalItem - ((pageid - 1) * my_numofcontentperpage );
			}
			return my_numofcontentperpage;
		}
		   //calculate total page in this tree level
		public int getTotalPage() {
			int totalItem = myShowAddBookmarkItem ? myBookmarksList.size() + 1 : myBookmarksList.size();
			
			int totalpage = totalItem / my_numofcontentperpage;
			if ( totalpage % my_numofcontentperpage != 0 | totalpage / my_numofcontentperpage == 0)
				totalpage++;
			return totalpage;
		}
		
		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final int real_position = currentBooksAdapter.my_numofcontentperpage * (currentBooksAdapter.pageid - 1) + position;

			final Bookmark bookmark = getItem(real_position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			} else if (myShowAddBookmarkItem) {
				myShowAddBookmarkItem = false;
				myCollection.saveBookmark(myBookmark);
			}
		}
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		switch (event) {
			default:
				break;
			case BookmarkStyleChanged:
				runOnUiThread(new Runnable() {
					public void run() {
						myAllBooksAdapter.notifyDataSetChanged();
						myThisBookAdapter.notifyDataSetChanged();
						if (mySearchResultsAdapter != null) {
							mySearchResultsAdapter.notifyDataSetChanged();
						}
					}
				});
				break;
			case BookmarksUpdated:
				updateBookmarks(book);
				break;
		}
	}

	// method from IBookCollection.Listener
	public void onBuildEvent(IBookCollection.Status status) {
	}
	
	private void deleteBookmarkDialog(final Bookmark bookmark) {
	  String deleteConfirmMessage = getResources().getString(R.string.toolbox_message_delete_confirm_bookmark, "");
		new AlertDialog.Builder(this)
	    .setTitle(deleteConfirmMessage)
	    .setMessage(bookmark.getText().length() <= 100 ? bookmark.getText() : bookmark.getText().substring(0,50))
	    .setNegativeButton(getString(android.R.string.no),new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	
	        }
	    })
	    .setPositiveButton(getString(android.R.string.yes),new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	myCollection.deleteBookmark(bookmark);
	        }
	    }).show();
	}
	
	private void onPrevPageThisBook(BookmarksAdapter adapter) {
		if (adapter.pageid > 1) {
			adapter.pageid--;
		} else {
			adapter.pageid = adapter.getTotalPage();
		}
		pageInfo.setText(adapter.pageid + " of " + adapter.getTotalPage());
		adapter.notifyDataSetChanged();

	}

	private void onNextPageThisBook(BookmarksAdapter adapter) {
		if (adapter.pageid < adapter.getTotalPage()) {
			adapter.pageid++;
		} else {
			adapter.pageid = 1;
		}
		pageInfo.setText(adapter.pageid + " of " + adapter.getTotalPage());
		adapter.notifyDataSetChanged();
	}
	
	private void onLastPageThisBook(BookmarksAdapter adapter) {
		adapter.pageid = adapter.getTotalPage();
		
		pageInfo.setText(adapter.pageid + " of " + adapter.getTotalPage());
		adapter.notifyDataSetChanged();
	}
	
	private void onFirstPageThisBook(BookmarksAdapter adapter) {
		adapter.pageid = 1;
		
		pageInfo.setText(adapter.pageid + " of " + adapter.getTotalPage());
		adapter.notifyDataSetChanged();
	}
	
	public static void switchToThisBook(){
		currentTAB = THIS_BOOK_TAB;
	}
	public static void switchToAllBook(){
		currentTAB = ALL_BOOKS_TAB;
	}
	public static String getCurrentTab(){
		return currentTAB;
	}
	
	private void refreshInfo(){
		currentBooksAdapter = getCurrentTab().equals(THIS_BOOK_TAB) ? myThisBookAdapter : myAllBooksAdapter;
		pageInfo.setText(currentBooksAdapter.pageid + " of " + currentBooksAdapter.getTotalPage());
	}
	
	private Handler mHandler = new Handler();

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			updateBookmarks(myBook);
			currentBooksAdapter = getCurrentTab().equals(THIS_BOOK_TAB) ? myThisBookAdapter : myAllBooksAdapter;			
			onFirstPageThisBook(currentBooksAdapter);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		BookmarksActivity.switchToThisBook();
		deleteOpenBookmarkBook();
	}
	
	/**
	 * delete empty book for show bookmark
	 */
	private void deleteOpenBookmarkBook(){
		File file = new File(Globals.PATH_SDCARD+"/"+Globals.ASSET_BOOKMARK_FILE);
		if (file.exists()) {
			file.delete();
		}
	}
}
