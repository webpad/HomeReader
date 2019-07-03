package org.geometerplus.android.fbreader.library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ntx.config.Globals;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;

public class SaveRecentBookListService extends Service implements IBookCollection.Listener<Book> {

	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);
	private final BookCollectionShadow myCollection = new BookCollectionShadow();

	private int mRecentBookListUpdateCount;
	private List<RecentlyBookData> mRecentlyBookList;
	private List<RecentlyBookData> mRecentlyBookList_old = new ArrayList<RecentlyBookData>();
	private EventBus mEventBus;
	private RecentBookCoverCheckEvent mRecentBookCoverCheckEvent;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mEventBus = EventBus.getDefault();
		mEventBus.register(SaveRecentBookListService.this);
		mRecentBookCoverCheckEvent = new RecentBookCoverCheckEvent();

		if (myCollection != null) {
			myCollection.removeListener(this);
			myCollection.unbind();
		}

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				myCollection.addListener(SaveRecentBookListService.this);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (myCollection != null) {
			myCollection.removeListener(this);
			myCollection.unbind();
		}
		mEventBus.unregister(this);
	}

	@Override
	public void onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.Opened || event == BookEvent.Removed) {
			saveRecentlyBookInfo();
		}
	}

	@Override
	public void onBuildEvent(Status status) {
	}

	private void saveRecentlyBookInfo() {
//		List<Book> recentB = myCollection.recentlyOpenedBooks(Globals.RECENT_BOOK_LIST_SIZE + 1);
		List<Book> recentB = myCollection.recentlyOpenedBooks(Globals.RECENT_BOOK_LIBRARY_MAX_SIZE); // if remove file at computer, 

		mRecentBookListUpdateCount = recentB.size();
		mRecentBookCoverCheckEvent.setCounterMax(mRecentBookListUpdateCount);
		if (mRecentlyBookList == null) {
			mRecentlyBookList = new ArrayList<RecentlyBookData>();
		} else {
			mRecentlyBookList_old.clear();
			mRecentlyBookList_old.addAll(mRecentlyBookList);
		}
		mRecentlyBookList.clear();
		mRecentBookCoverCheckEvent.resetCounter();
		for (int i = 0; i < recentB.size(); i++) {
			Book b = recentB.get(i);
			BookUtil.reloadInfoFromFile(b, PluginCollection.Instance(Paths.systemInfo(this)));
			final RecentlyBookData recentlyBookData = new RecentlyBookData(i);
			recentlyBookData.setId(b.getId());
			recentlyBookData.setTitle(b.getTitle());
			recentlyBookData.setAuthors(b.authorsString(", "));
			recentlyBookData.setPath(b.getPath());
			recentlyBookData.setEncoding(b.getEncodingNoDetection());
			recentlyBookData.setLanguage(b.getLanguage());
			String myExtension = ZLFile.createFileByPath(b.getPath()).getExtension();
			recentlyBookData.setType(myExtension);
			File bookFile = new File(b.getPath());
			recentlyBookData.setSize(bookFile.length());

			boolean isContinu = false;
			for (RecentlyBookData oldBookData : mRecentlyBookList_old) {
				if (oldBookData.getIndex() == recentlyBookData.getIndex()) {
					if (oldBookData.getId() == b.getId() && oldBookData.getTitle().equals(b.getTitle())
							&& oldBookData.getPath().equals(b.getPath())) {
						isContinu = true; // exactly same book. No need to save the cover.
						recentlyBookData.setCover(oldBookData.hasCover());
						mRecentlyBookList.add(recentlyBookData);
						mRecentBookCoverCheckEvent.countUp();
						mEventBus.post(mRecentBookCoverCheckEvent);
					}
				}
			}
			if (isContinu) // exactly same book. No need to save the cover, go next.
				continue;

			if (myExtension.equalsIgnoreCase("pdf") || myExtension.equalsIgnoreCase("txt") || myExtension.equalsIgnoreCase("rtf")  ) {
				recentlyBookData.setCover(false);
				mRecentlyBookList.add(recentlyBookData);
				mRecentBookCoverCheckEvent.countUp();
				mEventBus.post(mRecentBookCoverCheckEvent);
				continue;
			}

			final String bFileName = "Cover_" + recentlyBookData.getIndex() + ".png";
			final ZLImage bImage = CoverUtil.getCover(b, PluginCollection.Instance(Paths.systemInfo(this)));
			if (bImage instanceof ZLImageProxy) {
				((ZLImageProxy) bImage).startSynchronization(myImageSynchronizer, new Runnable() {
					@Override
					public void run() {
						boolean hasCoverFile = saveBookImageToFile(bImage, bFileName);
						recentlyBookData.setCover(hasCoverFile);
						mRecentlyBookList.add(recentlyBookData);
					}
				});
			} else {
				recentlyBookData.setCover(saveBookImageToFile(bImage, bFileName));
				mRecentlyBookList.add(recentlyBookData);
			}
		}
	}

	private boolean saveBookImageToFile(ZLImage bImage, String fileName) {
		if (bImage == null) {
			mRecentBookCoverCheckEvent.countUp();
			mEventBus.post(mRecentBookCoverCheckEvent);
			return false;
		}

		ZLAndroidImageData bImageData = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(bImage);
		if (bImageData == null) {
			mRecentBookCoverCheckEvent.countUp();
			mEventBus.post(mRecentBookCoverCheckEvent);
			return false;
		}

		Bitmap bCoverBitmap = bImageData.getFullSizeBitmap();
		if (bCoverBitmap == null) {
			mRecentBookCoverCheckEvent.countUp();
			mEventBus.post(mRecentBookCoverCheckEvent);
			return false;
		}

		new saveBitmapToFileAsyncTask().execute(bCoverBitmap, fileName);

		return true;
	}

	private class saveBitmapToFileAsyncTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			Bitmap coverBitmap = (Bitmap) params[0];
			String fileName = (String) params[1];
			try {
				FileOutputStream out = openFileOutput(fileName, MODE_PRIVATE);
				coverBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mRecentBookCoverCheckEvent.countUp();
			mEventBus.post(mRecentBookCoverCheckEvent);
		}
	}

	@Subscribe(threadMode = ThreadMode.BACKGROUND)
	public void onEvent(RecentBookCoverCheckEvent event) {
		if (event.getCounter() == mRecentBookListUpdateCount) {
			Gson gson = new Gson();
			Type listType = new TypeToken<List<RecentlyBookData>>() {
			}.getType();
			String jsonStr = gson.toJson(mRecentlyBookList, listType);
			OutputStreamWriter jsonFileWriter = null;
			try {
				FileOutputStream fOut = openFileOutput("recentBooks.json", MODE_PRIVATE);
				jsonFileWriter = new OutputStreamWriter(fOut);
				jsonFileWriter.write(jsonStr);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (jsonFileWriter != null) {
					try {
						jsonFileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mRecentBookCoverCheckEvent.countUp();
				mEventBus.post(mRecentBookCoverCheckEvent);
				publishResults();
			}
		}
	}

	private void publishResults() {
		Intent intent = new Intent(Globals.SAVE_RECENT_BOOK_FINISHED_NOTIFICATION);
		sendBroadcast(intent);
	}

}
