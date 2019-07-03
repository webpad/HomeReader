
package org.geometerplus.android.fbreader.library;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import com.ntx.config.Globals;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class OpenBookActivity extends Activity {

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private Intent mIntent;
	
	Intent saveRecentBookIntent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIntent = getIntent();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if (myCollection != null) {
			myCollection.unbind();
		}

		getIntentValue(mIntent);
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				openBook(myCollection.getBookByFile(bookPath));
			}
		});
		
		saveRecentBookIntent = new Intent(OpenBookActivity.this, SaveRecentBookListService.class);
		startService(saveRecentBookIntent);

	}

	@Override
	public void onNewIntent(Intent intent) {

		setIntent(intent);
		mIntent = intent;
		saveRecentBookIntent = new Intent(OpenBookActivity.this, SaveRecentBookListService.class);
		startService(saveRecentBookIntent);

		getIntentValue(mIntent);
		openBook(myCollection.getBookByFile(bookPath));
		
		super.onNewIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		Globals.closeWaitDialog(this);
		if (mIntent == null) {
			// when open book press back key to finish.
			finish();
		} else {
			mIntent = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myCollection != null) {
			myCollection.unbind();
		}
	}

	@SuppressLint("DefaultLocale")
	private void openBook(Book book) {

		String myExtension = ZLFile.createFileByPath(book.getPath()).getExtension();
		String path = book.getPath();

		if (myExtension.equalsIgnoreCase("cbr") || myExtension.equalsIgnoreCase("cbz")) {
			try {
				Intent mintent = new Intent("android.intent.action.VIEW");
				mintent.setData(Uri.parse("file://" + path));
				mintent.setClassName("net.androidcomics.acv", "net.robotmedia.acv.ui.ComicViewerActivity");
				mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(mintent);
			} catch (Throwable e) {
				Toast.makeText(this, "Not find ComicViewer !", Toast.LENGTH_LONG).show();
			}

		} else if (myExtension.equalsIgnoreCase("pdf")) {
			try {
				Intent mintent = new Intent("android.intent.action.VIEW");
				mintent.setDataAndType(Uri.parse("file://" + path), "application/pdf");
				startActivity(mintent);

				myCollection.addToRecentlyOpened(book);
			} catch (Throwable e) {
				Toast.makeText(this, "Not find PDF Reader !", Toast.LENGTH_LONG).show();
			}
		} else if (myExtension.equalsIgnoreCase("epub") || myExtension.equalsIgnoreCase("fb2")
				|| myExtension.equalsIgnoreCase("mobi") || myExtension.equalsIgnoreCase("prc")
				|| myExtension.equalsIgnoreCase("oeb") || myExtension.equalsIgnoreCase("txt")
				|| myExtension.equalsIgnoreCase("rtf") || myExtension.equalsIgnoreCase("azw3")
		) {
			if (LibraryActivity.isAlive){
				FBReader.openBookActivity(OpenBookActivity.this, book, null);
				
			}else{	
				Intent mIntent = new Intent(this,LibraryActivity.class);				
				mIntent.putExtra(Globals.KEY_READER_BOOK_PATH, book.getPath());
				startActivity(mIntent);	    
			}
		} else {
			Toast.makeText(this, "Not support \"" + myExtension.toUpperCase() + "\" format file", Toast.LENGTH_LONG)
					.show();
		}
	}
	
	long bookId = 0;
	String bookPath = "";
	String bookTitle= "";
	String bookEncoding = "";
	String bookLanguage = "";
	
	private void getIntentValue(Intent intent){
		bookId = intent.getExtras().getLong(Globals.KEY_READER_BOOK_ID,0);
		bookPath = intent.getExtras().getString(Globals.KEY_READER_BOOK_PATH,"");
		bookTitle = intent.getExtras().getString(Globals.KEY_READER_BOOK_TITLE,"");
		bookEncoding = intent.getExtras().getString(Globals.KEY_READER_BOOK_ENCODING,"");
		bookLanguage = intent.getExtras().getString(Globals.KEY_READER_BOOK_LANGUAGE,"");
	}
}
