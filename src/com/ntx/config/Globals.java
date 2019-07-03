package com.ntx.config;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import ntx.reader3.R;

import org.geometerplus.zlibrary.core.resources.ZLResource;

@SuppressLint("SdCardPath")
public class Globals {
	public static final String HOME_PACKAGE="com.example.android.home";
	public static final String HOME_CLASS="com.example.android.home.HomeActivity";

	public static final String SETTINGS_PACKAGE="com.android.settings";
	public static final String SETTINGS_CLASS="com.android.settings.Settings";

	public static final String nTools_PACKAGE="ntx.tools";
	public static final String nTools_CLASS="ntx.tools.MainActivity";

	public static final String READER_PACKAGE="ntx.reader3";
	public static final String READER_CLASS="org.geometerplus.android.fbreader.library.LibraryActivity";

	public static final String NOTE_PACKAGE="ntx.note2";
	public static final String NOTE_CLASS="ntx.note.bookshelf.NtxLauncherActivity";
	public static final String NOTE_EDITOR_CLASS="ntx.note.NoteWriterActivity";
	public static final String NOTE_HOME_LAUNCHER_CLASS = "ntx.note.bookshelf.NtxHomeActivity";

	public static final String CALENDAR_PACKAGE	= "com.simplemobiletools.calendar";
	public static final String CALENDAR_CLASS	= "com.simplemobiletools.calendar.activities.MainActivity";

	public static final String PDF_PACKAGE="com.artifex.mupdfdemo";
	public static final String PDF_CLASS="com.artifex.mupdfdemo.MuPDFActivity";

	public static final String CBRCBZ_PACKAGE="net.androidcomics.acv";
	public static final String CBRCBZ_CLASS="net.robotmedia.acv.ui.ComicViewerActivity";

	public static final String CALCULATOR_PACKAGE="com.visionobjects.calculator";
	public static final String CALCULATOR_CLASS="com.visionobjects.calculator.activity.MainActivity";

	public static final String PAINTER_PACKAGE="ntx.painter";
	public static final String PAINTER_CLASS="ntx.painter.MainActivity";

	public static final String LAUNCHER_PACKAGE="com.android.launcher";
	public static final String LAUNCHER_CLASS="com.android.launcher2.Launchery";

	public static final String UPGRADE_OTA_PACKAGE="com.netronix.fw_upgrade_ota";
	public static final String UPGRADE_OTA_CLASS="com.netronix.fw_upgrade_ota.Upgrade_ota";

	public static final String SAVE_RECENT_BOOK_FINISHED_NOTIFICATION = "org.geometerplus.android.fbreader.library.SaveRecentBookListService";

	public static final Uri BOOKSTORE_URI = Uri.parse("http://www.ezread.com.tw/webapp_Q70J02/index3.html#home");

	public final static String PATH_SDCARD_ABSOLUTE = "/mnt/media_rw/sdcard1";
    public final static String PATH_SDCARD = Environment.getExternalStorageDirectory().getPath();
    
	public final static String PATH_EXTERNALSD_ABSOLUTE = "/mnt/media_rw/extsd";
    public final static String PATH_EXTERNALSD = Environment.getExternalExtSDStorageDirectory().getPath();
    
    public final static String BOOKS_FOLDER = "Books";
    public final static String DOWNLOAD_FOLDER = Environment.DIRECTORY_DOWNLOADS;
    
	public static final String KEY_READER_RECENT_INDEX = "recentIndex";
	public static final String KEY_READER_RECENT_AUTHORS = "bookAuthors";
	public static final String KEY_READER_RECENT_SIZE = "bookSize";
	public static final String KEY_READER_RECENT_HASCOVER = "bookHasCover";
	public static final String KEY_READER_RECENT_TYPE = "bookType";

	public static final String KEY_READER_MAIN_PAGE = "reader_main_page";
    public final static String KEY_READER_BOOK_ID = "bookId";
    public final static String KEY_READER_BOOK_PATH = "bookPath";
    public final static String KEY_READER_BOOK_TITLE= "bookTitle";
    public final static String KEY_READER_BOOK_ENCODING = "bookEncoding";
    public final static String KEY_READER_BOOK_LANGUAGE = "bookLanguage";
    public final static String KEY_READER_BOOKMARK_TAB = "bookmarktab";

	public static final String ROOT_FOUND = "found";
	public static final String ROOT_FAVORITES = "favorites";
	public static final String ROOT_RECENT = "recent";
	public static final String ROOT_BY_AUTHOR = "byAuthor";
	public static final String ROOT_BY_TITLE = "byTitle";
	public static final String ROOT_BY_SERIES = "bySeries";
	public static final String ROOT_BY_TAG = "byTag";
	public static final String ROOT_FILE_TREE = "fileTree";
	public static final String ROOT_ALL_BOOKS = "allBooks";
	public static final String ROOT_EXT_SD = "extSD";
	public static final String ROOT_ALL_IMAGES = "allImages";

	public static final String ROOT_EXTERNAL_VIEW = "bookshelfView";
	public static final String ROOT_SYNC = "sync";
	public static final String ROOT_FILE = "fileTree";
	public static final String rootStatusDefault = ROOT_ALL_BOOKS;

    public static String rootStatus = rootStatusDefault;

	public static String assetDemoBook_path="demo/Guide"; // default path = file:///android_asset/demo/Guide
	public static final String ASSET_BOOKMARK_PATH="demo/bookmark";
	public static final String ASSET_BOOKMARK_FILE="bookmark.txt";

//	public static String[] searchBookType= new String[]{".epub",".rtf",".txt",".mobi",".fb2",".prc",".oeb",".pdf"}; //auto list files
	public static String[] searchBookType= new String[]{".epub",".rtf",".txt",".mobi",".pdf",".azw3"}; //auto list files. Please double confirm 'FileTypeCollection'
	public static String[] searchBookTypeShowTitle= new String[]{".epub",".mobi"};    		   // show title name

	public static String[] searchImageType= new String[]{".png",".jpg",".jpeg",".bmp"}; //auto list files

	public static String[] extStateAvailable = new String[]{Environment.MEDIA_MOUNTED	// 6.8 & 13.3 inch note ExtSD State
															,Environment.MEDIA_REMOVED	// Good eReader ExtSD State
															};

	/**
	 *  Change filename from English to local.
	 *  values-zh-rTW ==> <string name="user_guide_book_filename">6.8吋電子筆記本.epub</string>
	 */
	public static String[] supportLanguageUserGuideFileName = new String[] {
			"6.8吋電子筆記本.epub", 		// langCode = "zh_TW"
			"6.8吋电子笔记本.epub",		// langCode = "zh_CN"
			"6.8inch_notebook.epub"		// langCode = "en_US"
			};

	public static String[] pageString= {
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("eachpage").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each2page").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each3page").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each4page").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each5page").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each6page").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each7page").getValue(),
			ZLResource.resource("Preferences").getResource("appearance").getResource("screenRefresh").getResource("each8page").getValue()
		};
	public static float fullScreenWidth=0;
	public static float fullScreenHeight=0;

	public static int RECENT_BOOK_MAIN_PAGE_MAX_SIZE = 7;	// in Main Page recently book max size.
	public static int RECENT_BOOK_LIBRARY_MAX_SIZE = 12;	// in library recently book max size.

	public static boolean isBuildEventFired = false;
	public final static float SWIPE_DISTANCE_THRESHOLD_BY_CM = 4.0f; // centimeter
    public static final String TYPE_E60Q20   = "E60Q20";
    public static final String TYPE_E60Q30   = "E60Q30";
    public static final String TYPE_E60Q50   = "E60Q50";
    public static final String TYPE_E60Q60   = "E60Q60";
    public static final String TYPE_E60QR0   = "E60QR0";
    public static final String TYPE_E60QR2   = "E60QR2";
    public static final String TYPE_E60QD0   = "E60QD0";
    public static final String TYPE_E60QH0   = "E60QH0";
    public static final String TYPE_E70Q30   = "E70Q30";
    public static final String TYPE_EA0Q00   = "EA0Q00";
    public static final String TYPE_ED0Q00   = "ED0Q00";
    
	/**
	 * Because ntx devices can't get metrics.xdpi, so use the each device to set the length first.
	 * @param cm  centimeter
	 * @param metrics DisplayMetrics
	 * @return pixel
	 */
	public static int getCMtoPixel(float cm, DisplayMetrics metrics){
		if (      SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60)){ return (int) (cm * 104);
		}else if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E70Q30)){ return (int) (cm * 118);
		}else if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_EA0Q00)){ return (int) (cm * 90);
		}else if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_ED0Q00)){ return (int) (cm * 60);
		}else{
			if (metrics != null){
				return Math.round( (cm/2.54f) * metrics.xdpi); // cm/2.54f = Inch
			}else{
				return (int) (cm * 90);
			}
		}
	}

	public static void restRootStatus(){
		rootStatus = rootStatusDefault;
	}
	
	// HomerReader Grid view icon
	public static final int BOOK_COVER_TAG_EPUB = R.drawable.book_cover_tag_epub;
	public static final int BOOK_COVER_TAG_MOBI = R.drawable.book_cover_tag_mobi;
	public static final int BOOK_COVER_TAG_PDF = R.drawable.book_cover_tag_pdf;
	public static final int BOOK_COVER_TAG_RTF = R.drawable.book_cover_tag_rtf;
	public static final int BOOK_COVER_TAG_TXT = R.drawable.book_cover_tag_txt;
	public static final int BOOK_COVER_TAG_DJVU = R.drawable.book_cover_tag_djvu;
	public static final int BOOK_COVER_TAG_AZW3 = R.drawable.book_cover_tag_azw3;
	public static final int BOOK_COVER_TAG_FB2 = R.drawable.book_cover_tag_fb2;
	public static final int BOOK_COVER_TAG_OTHER = R.drawable.book_cover_tag_other;
	public static final int BOOK_COVER_TAG_DEFAULT = R.drawable.book_cover_default;

	// HomerReader List view icon
	public static final int BOOK_COVER_LIST_EPUB = R.drawable.reader_list_book_cover_epub;
	public static final int BOOK_COVER_LIST_MOBI = R.drawable.reader_list_book_cover_mobi;
	public static final int BOOK_COVER_LIST_PDF = R.drawable.reader_list_book_cover_pdf;
	public static final int BOOK_COVER_LIST_RTF = R.drawable.reader_list_book_cover_rtf;
	public static final int BOOK_COVER_LIST_TXT = R.drawable.reader_list_book_cover_txt;
	public static final int BOOK_COVER_LIST_DJVU = R.drawable.reader_list_book_cover_djvu;
	public static final int BOOK_COVER_LIST_AZW3 = R.drawable.reader_list_book_cover_azw3;
	public static final int BOOK_COVER_LIST_FB2 = R.drawable.reader_list_book_cover_fb2;
	public static final int BOOK_COVER_LIST_OTHER = R.drawable.reader_list_book_cover_other;

	// HomerReader book cover format
    public static final String COVER_FORMAT_EPUB   = "epub";
    public static final String COVER_FORMAT_MOBI   = "mobi";
    public static final String COVER_FORMAT_PDF   = "pdf";
    public static final String COVER_FORMAT_RTF   = "rtf";
    public static final String COVER_FORMAT_TXT   = "txt";
    public static final String COVER_FORMAT_DJVU   = "djvu";
    public static final String COVER_FORMAT_AZW3   = "azw3";
    public static final String COVER_FORMAT_FB2   = "fb2";

	/**
	 * HomerReader A book with a cover,Display format Grid view
	 * @param tree
	 * @return
	 */
	public static int getCoverTypeTagResourceId(String path) {
		
		final int index = path.lastIndexOf('.');
		final String myExtension = ((index > 0) ? path.substring(index).toLowerCase().intern() : "");
			
		if (myExtension.equals("."+COVER_FORMAT_EPUB)) 		  {	return BOOK_COVER_TAG_EPUB;            	                
        } else if (myExtension.equals("."+COVER_FORMAT_MOBI)) {	return BOOK_COVER_TAG_MOBI;
        } else if (myExtension.equals("."+COVER_FORMAT_PDF))  {	return BOOK_COVER_TAG_PDF;
        } else if (myExtension.equals("."+COVER_FORMAT_RTF))  {	return BOOK_COVER_TAG_RTF;
        } else if (myExtension.equals("."+COVER_FORMAT_TXT))  {	return BOOK_COVER_TAG_TXT;
        } else if (myExtension.equals("."+COVER_FORMAT_DJVU)) {	return BOOK_COVER_TAG_DJVU;
        } else if (myExtension.equals("."+COVER_FORMAT_AZW3)) {	return BOOK_COVER_TAG_AZW3;
        } else if (myExtension.equals("."+COVER_FORMAT_FB2))  {	return BOOK_COVER_TAG_FB2;}
	
		return BOOK_COVER_TAG_OTHER;
		
	}
	
	/**
	 * HomerReader A book with a cover,Display format List view
	 * @param tree
	 * @return
	 */
	public static int getCoverTypeListResourceId(String path) {
		
		final int index = path.lastIndexOf('.');
		final String myExtension = ((index > 0) ? path.substring(index).toLowerCase().intern() : "");
			
		if (myExtension.equals("."+COVER_FORMAT_EPUB)) 		  {	return BOOK_COVER_LIST_EPUB;            	                
        } else if (myExtension.equals("."+COVER_FORMAT_MOBI)) {	return BOOK_COVER_LIST_MOBI;
        } else if (myExtension.equals("."+COVER_FORMAT_PDF))  {	return BOOK_COVER_LIST_PDF;
        } else if (myExtension.equals("."+COVER_FORMAT_RTF))  {	return BOOK_COVER_LIST_RTF;
        } else if (myExtension.equals("."+COVER_FORMAT_TXT))  {	return BOOK_COVER_LIST_TXT;
        } else if (myExtension.equals("."+COVER_FORMAT_DJVU)) {	return BOOK_COVER_LIST_DJVU;
        } else if (myExtension.equals("."+COVER_FORMAT_AZW3)) {	return BOOK_COVER_LIST_AZW3;
        } else if (myExtension.equals("."+COVER_FORMAT_FB2))  {	return BOOK_COVER_LIST_FB2;}
	
		return BOOK_COVER_LIST_OTHER;
		
	}
	
	public static void closeWaitDialog(final Context c) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent();
                i.setComponent(new ComponentName("ntx.tools", "ntx.tools.OverlayService"));
                c.stopService(i);
            }
        }, 1000);
    }
	
	public static void closeWaitDialog(final Context c, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent();
                i.setComponent(new ComponentName("ntx.tools", "ntx.tools.OverlayService"));
                c.stopService(i);
            }
        }, delay);
    }

    public static void openWaitDialog(final Context c) {
        Intent i = new Intent();
        i.setComponent(new ComponentName("ntx.tools", "ntx.tools.OverlayService"));
        c.startService(i);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
