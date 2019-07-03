package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.bookmark.BookmarksActivity;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.NativeFormatPlugin;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;

import com.ntx.config.Globals;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ntx.reader3.R;

public class OpenBookAsyncTask extends AsyncTask<Void, Integer, Boolean> {

	private Activity mActivity;
	private FBReaderApp myFbReaderApp;
	private Book myBook;
	private Bookmark myBookmark;
	private final Dialog mDialog;
	private boolean isOpenBookFail = false;

	private Button btnInterrupt;
	private ImageView mFailImage;
	private ProgressBar mBarProgress;
	private TextView mTvProgress;
	private TextView tvProgressMessage;
	private String mProgressMessage;
	private int mProgressMaxValue = 100;

	/**
	 * 
	 * @param act
	 * @param fbReaderApp
	 * @param book
	 * @param bookmark
	 * @param showOpenBookFailMessage
	 *            :: open book fail , only show dialog
	 */
	public OpenBookAsyncTask(final Activity act, FBReaderApp fbReaderApp, Book book, Bookmark bookmark,
			boolean showOpenBookFailMessage) {
		this.mActivity = act;
		this.myFbReaderApp = fbReaderApp;
		this.myBook = book;
		this.myBookmark = bookmark;
		this.isOpenBookFail = showOpenBookFailMessage;
		this.mProgressMessage = myBook.getTitle() + "\n" + myBook.getPath();

		if (myBook.getPath().contains(Globals.PATH_SDCARD) ){
			mProgressMessage = mProgressMessage.replace(Globals.PATH_SDCARD,mActivity.getString(R.string.message_device_internal)+"    ");
		}else if (myBook.getPath().contains(Globals.PATH_SDCARD_ABSOLUTE)){
			mProgressMessage = mProgressMessage.replace(Globals.PATH_SDCARD_ABSOLUTE,mActivity.getString(R.string.message_device_internal)+"    ");
		}else if (myBook.getPath().contains(Globals.PATH_EXTERNALSD)){
			mProgressMessage = mProgressMessage.replace(Globals.PATH_EXTERNALSD, mActivity.getString(R.string.message_device_external)+"    ");
		}else if (myBook.getPath().contains(Globals.PATH_EXTERNALSD_ABSOLUTE)){
			mProgressMessage = mProgressMessage.replace(Globals.PATH_EXTERNALSD_ABSOLUTE, mActivity.getString(R.string.message_device_external)+"    ");
		}
		
		mDialog = new Dialog(mActivity, R.style.AlertDialog_custom);
		mDialog.setContentView(R.layout.dialog_interruptible_progressing);
		mDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
		mDialog.setCanceledOnTouchOutside(false);

		mFailImage = (ImageView) mDialog.findViewById(R.id.iv_fail);
		tvProgressMessage = (TextView) mDialog.findViewById(R.id.tv_progress_message);
		tvProgressMessage.setText(mProgressMessage);

		mBarProgress = (ProgressBar) mDialog.findViewById(R.id.progressbar_progress);
		mBarProgress.setMax(mProgressMaxValue);
		mBarProgress.setProgress(0);

		mTvProgress = (TextView) mDialog.findViewById(R.id.tv_progress);
		mTvProgress.setText("0%");

		btnInterrupt = (Button) mDialog.findViewById(R.id.btn_interrupt);
		btnInterrupt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NativeFormatPlugin.stopReading();
				mDialog.dismiss();
				mActivity.finish();
			}
		});

		mDialog.show();

	}

	@Override
	protected Boolean doInBackground(Void... params) {

		if (isOpenBookFail)
			return false;

		NativeFormatPlugin.callBack(new NativeFormatPlugin.Callback() {

			@Override
			public void onProgress(int progress) {
				publishProgress(progress);
			}

			@Override
			public void onClose() {
				myFbReaderApp.isSameBookEncryption = true;
				isOpenBookFail = true;
			}
		});
		
		if (LibraryActivity.isAlive){
			myFbReaderApp.openBookInternal(myBook, myBookmark, false);
			
		}else{	
			Intent mIntent = new Intent(mActivity,LibraryActivity.class);				
			mIntent.putExtra(Globals.KEY_READER_BOOK_PATH, myBook.getPath());
			mActivity.startActivity(mIntent);	    
		}
		return true;

	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		mBarProgress.setProgress(progress[0]);
		mTvProgress.setText(progress[0]+"%");
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (isOpenBookFail) {
			tvProgressMessage.setText(mActivity.getResources().getString(R.string.message_failed_open));
			tvProgressMessage.setGravity(Gravity.CENTER);
			mFailImage.setVisibility(View.VISIBLE);
			hideView();
		} else if (result) {
			mDialog.dismiss();
			if (BookmarksActivity.getCurrentTab().equals(BookmarksActivity.ALL_BOOKS_TAB)){
				FBReaderApp.Instance().closeWindow();
				
				myFbReaderApp.runAction(ActionCode.SHOW_BOOKMARKS);
			}
		}

		AndroidFontUtil.clearFontCache();
	}

	/**
	 * only show open book fail message, hide progress bar & progress text
	 */
	private void hideView() {
		mBarProgress.setVisibility(View.GONE);
		mTvProgress.setVisibility(View.GONE);
	}
}
