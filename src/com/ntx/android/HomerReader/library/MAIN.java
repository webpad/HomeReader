
package com.ntx.android.HomerReader.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.library.OpenBookActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import ntx.reader3.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

@SuppressLint("DefaultLocale")
@SuppressWarnings("deprecation")
public class MAIN extends Activity implements IBookCollection.Listener<Book> {
	
	private final BookCollectionShadow myCollection = new BookCollectionShadow();

    private PopupWindow popup_menu;
    private GridView mGridView;
    private ViewFlipper mViewFlipper;
    private AbsoluteLayout mLayout;
    private int titleIndex = 0;
//    private ImageView imgv_cover_bg_1, imgv_cover_bg_2, imgv_cover_bg_3;
    private ImageView imgv_cover_1, imgv_cover_2, imgv_cover_3;
    private TextView txtv_cover_1, txtv_cover_2, txtv_cover_3;
    private TextView txtv_version, txtv_battery;
    private ImageView imgv_battery_notice;
//    public static Book recent_book_1, recent_book_2, recent_book_3;
    private Book[] recentBook=new Book[3];
    public static final String ROOT_RECENT = "recent";
    
	private boolean isSDMount=false;

    public GridView ntx_main_gridview;

    private int display_width;
    private float sp2pxl;

    private int popup_menu_width;
    private int popup_menu_height = 200;

    int[] menu_image_array = {
            R.drawable.icon_library,
            R.drawable.button_menu_lastread,
            R.drawable.button_menu_setting,
            R.drawable.page_right
    };

    int[] menu_image_array2 = {
            R.drawable.button_menu_find,
            R.drawable.button_menu_painter,
            R.drawable.button_menu_calculator,
            R.drawable.button_menu_default,
    };

    String[] menu_name_array = new String[4];//{ "Library", "Last read", "Settings", "More"};
    String[] menu_name_array2 = new String[3];//{ "Search", "Painter", "Calculator", "Reserved"};

    private LinearLayout ll_imv_extsd1, ll_imv_extsd2, ll_imv_extsd3;
	private void initInstantKeys() {
        imgv_cover_1 = (ImageView) findViewById(R.id.imgv_cover01);
        imgv_cover_2 = (ImageView) findViewById(R.id.imgv_cover02);
        imgv_cover_3 = (ImageView) findViewById(R.id.imgv_cover03);
//        imgv_cover_bg_1 = (ImageView) findViewById(R.id.homescreen_book_1);
//        imgv_cover_bg_2 = (ImageView) findViewById(R.id.homescreen_book_2);
//        imgv_cover_bg_3 = (ImageView) findViewById(R.id.homescreen_book_3);

        txtv_cover_1 = (TextView) findViewById(R.id.txtv_cover01);
        txtv_cover_2 = (TextView) findViewById(R.id.txtv_cover02);
        txtv_cover_3 = (TextView) findViewById(R.id.txtv_cover03);
        txtv_version = (TextView) findViewById(R.id.txtv_version);
        txtv_battery = (TextView) findViewById(R.id.txtv_battery);
		imgv_battery_notice = (ImageView) findViewById(R.id.imgv_battery_notice);

		ll_imv_extsd1 = (LinearLayout) findViewById(R.id.ll_imv_extsd1);
		ll_imv_extsd2 = (LinearLayout) findViewById(R.id.ll_imv_extsd2);
		ll_imv_extsd3 = (LinearLayout) findViewById(R.id.ll_imv_extsd3);

        imgv_cover_1.setOnClickListener(new ButtonClickListener());
        imgv_cover_2.setOnClickListener(new ButtonClickListener());
        imgv_cover_3.setOnClickListener(new ButtonClickListener());

        ntx_main_gridview = (GridView) findViewById(R.id.ntx_main_gridview);
        
	    menu_name_array[0]=ZLResource.resource("menu").getResource("library").getValue();
	    menu_name_array[1]=ZLResource.resource("menu").getResource("lastRead").getValue();
	    menu_name_array[2]=ZLResource.resource("menu").getResource("settings").getValue();
	    menu_name_array[3]=ZLResource.resource("menu").getResource("more").getValue();
	    		
	    menu_name_array2[0]=ZLResource.resource("menu").getResource("search").getValue();
	    menu_name_array2[1]=ZLResource.resource("menu").getResource("painter").getValue();
	    menu_name_array2[2]=ZLResource.resource("menu").getResource("calculator").getValue();

	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ntx_main);

        //artis
        Display display = getWindowManager().getDefaultDisplay();
        display_width = display.getWidth();  // deprecated
        popup_menu_width = display_width;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        sp2pxl = dm.scaledDensity;

        initInstantKeys();
        initGridViewMenu();
        initPopupMenu();

        try {
            txtv_version.setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        myCollection.bindToService(this, new Runnable() {
			public void run() {
				myCollection.addListener(MAIN.this);
		        checkUpdate();
			}
		});
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        checkUpdate();
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String pattern = intent.getStringExtra(SearchManager.QUERY);
            try {
                Bundle mBundle = new Bundle();
                mBundle.putString("SEARCH_STRING", pattern);
                mBundle.putInt("SEARCHABLE", 1);
                Intent mIntent = new Intent();
                mIntent.setComponent(new ComponentName(Globals.READER_PACKAGE,
                		Globals.READER_CLASS));
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mIntent.putExtras(mBundle);
                startActivity(mIntent);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSearchRequested() {
        startSearch("", true, null, false);
        return true;
    }

    public class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.imgv_cover01) {
                if (recentBook[0] != null) {
                    openBook(recentBook[0]);
                }
            } else if (v.getId() == R.id.imgv_cover02) {
                if (recentBook[1] != null) {
                    openBook(recentBook[1]);
                }
            } else if (v.getId() == R.id.imgv_cover03) {
                if (recentBook[2] != null) {
                    openBook(recentBook[2]);
                }
            }
        }
    }

    private void initGridViewMenu() {
        ntx_main_gridview.setNumColumns(menu_name_array.length);
        ntx_main_gridview.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_menu));
        ntx_main_gridview.setGravity(Gravity.CENTER);
        ntx_main_gridview.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
        ntx_main_gridview.setOnItemClickListener(mItemClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
	private void initPopupMenu() {
        mGridView = new GridView(getApplicationContext());
        mGridView.setNumColumns(menu_name_array2.length);
        mGridView.setBackgroundColor(Color.argb(0, 0, 0, 0));
        mGridView.setAdapter(getMenuAdapter(menu_name_array2, menu_image_array2));
        mGridView.setGravity(Gravity.CENTER);
        mGridView.setOnItemClickListener(mItemClickListener);

        mLayout = new AbsoluteLayout(getApplicationContext());
        mLayout.setLayoutParams(new LayoutParams(
                (int) (popup_menu_width),
                (int) (popup_menu_height * sp2pxl))
        );

        mLayout.addView(mGridView, new AbsoluteLayout.LayoutParams(
                display_width,
                (int) ((popup_menu_height * sp2pxl)),
                0,
                (int) (10 * sp2pxl))
        );
        mLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_popup_menu));

        mViewFlipper = new ViewFlipper(this);
        mViewFlipper.addView(mLayout);

        popup_menu = new PopupWindow(mViewFlipper, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popup_menu.setFocusable(true);
        popup_menu.setOutsideTouchable(true);
        popup_menu.setBackgroundDrawable(new BitmapDrawable());
        popup_menu.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popup_menu.dismiss();
                    return true;
                }

                return false;
            }
        });
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            TextView txtview = ((TextView) arg1.findViewById(R.id.item_text));
            String mString = txtview.getText().toString();
            if (mString.equalsIgnoreCase("back")) {
//            	ntx_main_gridview.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
//            	Toast.makeText(MAIN.this, "back", Toast.LENGTH_LONG).show();
                onChangeItem(0);
            } else if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("settings").getValue())) {
                SwitchToSettings();
            } else if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("library").getValue())) {
                SwitchToHomerReaderLibrary();
            } else if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("search").getValue())) {
            	onSearchRequested();
            } else if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("lastRead").getValue())) {
            	if (recentBook[0] != null) {
            		openBook(recentBook[0]);
            	}
//            } else if (mString.equalsIgnoreCase("book store")) {
//                SwitchToBookstore();
            } else if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("calculator").getValue())) {
                SwitchToCalculator();
            } else if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("painter").getValue())) {
                SwitchToPainter();
//            } else if (mString.equalsIgnoreCase("note")) {
//                SwitchToNote();
            } else if (mString.equalsIgnoreCase("")) {
//                SwitchToDefaultLauncher();
                SwitchToSampleLauncher();
            }

            if (popup_menu.isShowing())
            	popup_menu.dismiss();

            if (mString.equalsIgnoreCase(ZLResource.resource("menu").getResource("more").getValue())) {
                if (popup_menu.isShowing())
                    popup_menu.dismiss();
                else {
                    popup_menu.showAtLocation(
                    		findViewById(R.id.ntx_main),
                            Gravity.TOP,
                            0,
                            0
                            );
                }
            }
        }
    };

    private SimpleAdapter getMenuAdapter(String[] menuNameArray, int[] imageResourceArray) {
        ArrayList<HashMap<String, Object>> mArrayList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < menuNameArray.length; i++) {
            HashMap<String, Object> mHashMap = new HashMap<String, Object>();
            mHashMap.put("itemImage", imageResourceArray[i]);
            mHashMap.put("itemText", menuNameArray[i]);
            mArrayList.add(mHashMap);
        }
        SimpleAdapter simperAdapter = new SimpleAdapter(
                this,
                mArrayList,
                R.layout.item_menu,
                new String[] { "itemImage", "itemText" },
                new int[] { R.id.item_image, R.id.item_text });
        return simperAdapter;
    }

    private void onChangeItem(int item_page_number) {
        titleIndex = item_page_number;
        switch (titleIndex) {
            case 0:
                ntx_main_gridview.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
                break;
            case 1:
                ntx_main_gridview.setAdapter(getMenuAdapter(menu_name_array2, menu_image_array2));
                break;
        }
    }

	public static final String TREE_KEY_KEY = "TreeKey";
	public static final String SELECTED_TREE_KEY_KEY = "SelectedTreeKey";
	public static final String HISTORY_KEY = "HistoryKey";
	
	public static boolean getExternalSdCardPath() {
		
        if(new File(Globals.PATH_EXTERNALSD).isDirectory()){
            if ( ZLFile.createFileByPath(Globals.PATH_EXTERNALSD).isReadable()) return true;	  
        }
        
        return false;
	}
    private void checkUpdate() {
		final PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(this));
		final ArrayList<Book> recentBookAvailable = new ArrayList<Book>();
		for (int i=0; i<12;i++){
			try{
	            if(new File(myCollection.getRecentBook(i).getPath()).exists()){
					recentBookAvailable.add(myCollection.getRecentBook(i));
					if (recentBookAvailable.size()>=3) break;
				}
			}catch(Exception e){}
		}
		
		for (int i=0;i<3;i++){
			recentBook[i]=recentBookAvailable.size()>i ? recentBookAvailable.get(i) : null;
		}

		setupCover(recentBook[0], pluginCollection, ll_imv_extsd1, imgv_cover_1, txtv_cover_1);
		setupCover(recentBook[1], pluginCollection, ll_imv_extsd2, imgv_cover_2, txtv_cover_2);
		setupCover(recentBook[2], pluginCollection, ll_imv_extsd3, imgv_cover_3, txtv_cover_3);

    }
    
	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);
	
	private void setupCover(Book book, PluginCollection pluginCollection, final LinearLayout ll_imv_extsd,
			final ImageView coverView_recent, final TextView coverText_recent) {
		RefreshClass.setGridBookTitleSize(coverText_recent);
		if (book == null) {
			ll_imv_extsd.setVisibility(View.INVISIBLE);
			coverView_recent.setVisibility(View.INVISIBLE);
			coverText_recent.setVisibility(View.INVISIBLE);
			return;
		} else {
			// if file in ext_SD, remove star button.
			if (book.getPath().indexOf(Globals.PATH_EXTERNALSD) != -1)
				ll_imv_extsd.setVisibility(View.VISIBLE);
			else
				ll_imv_extsd.setVisibility(View.INVISIBLE);

			coverView_recent.setVisibility(View.VISIBLE);
			coverText_recent.setVisibility(View.VISIBLE);
		}
		
		final Object oldBook = coverView_recent.getTag();
		if (oldBook instanceof Book && book.getId() == ((Book)oldBook).getId()) {
			return;
		}
		coverView_recent.setTag(book);
		coverView_recent.setImageDrawable(null);
		coverText_recent.setText(book.getTitle());
		coverView_recent.setBackgroundResource(R.drawable.ntx_main_book_bg);
		
//		coverBackgroundView_recent.setVisibility(View.VISIBLE);

		final ZLImage image = CoverUtil.getCover(book, pluginCollection);
		if (image == null) {
			coverText_recent.setVisibility(View.VISIBLE);
			return;
		}

		if (image instanceof ZLImageProxy) {
			((ZLImageProxy)image).startSynchronization(myImageSynchronizer, new Runnable() {
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							setCover(coverView_recent, image,coverText_recent);
						}
					});
				}
			});
		} else {
			setCover(coverView_recent, image,coverText_recent);
		}
	}

	private void setCover(ImageView coverView, ZLImage image,TextView coverText) {
		final ZLAndroidImageData data =
			((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
		
		if (data == null) {
			return;
		}

		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		final Bitmap coverBitmap = data.getFullSizeBitmap();

		if (coverBitmap == null) {
			return;
		}

		coverView.setVisibility(View.VISIBLE);
		coverView.setImageBitmap(coverBitmap);
		coverText.setText("");
	}

    private void SwitchToHomerReaderLibrary() {
        try {
        	ComponentName componentName = new ComponentName(Globals.READER_PACKAGE,Globals.READER_CLASS); 
            
            startActivity(new Intent().setComponent(componentName)
            .setAction(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } catch (Throwable e) {
            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
        }
    }

    private void SwitchToSettings() {
        try {
            Intent mIntent = new Intent();
            ComponentName componentName = new ComponentName(Globals.SETTINGS_PACKAGE,Globals.SETTINGS_CLASS);
            mIntent.setComponent(componentName);
            startActivity(mIntent);
        } catch (Throwable e) {
            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
        }
    }

//    private void SwitchToUpgradeOTA() {
//        try {
//            Intent mIntent = new Intent();
//            ComponentName comp = new ComponentName("com.netronix.fw_upgrade_ota",
//                    "com.netronix.fw_upgrade_ota.Upgrade_ota");
//            mIntent.setComponent(comp);
//            startActivity(mIntent);
//        } catch (Throwable e) {
//            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void SwitchToDefaultLauncher() {
//        try {
//            Intent mIntent = new Intent();
//            ComponentName componentName = null;
//            componentName = new ComponentName("com.android.launcher",
//                    "com.android.launcher2.Launcher");
//
//            mIntent.setComponent(componentName);
//            startActivity(mIntent);
//        } catch (Throwable e) {
//            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
//        }
//    }

    private void SwitchToSampleLauncher() {
        try {
            Intent mIntent = new Intent();
            ComponentName componentName = null;
            componentName = new ComponentName(Globals.HOME_PACKAGE,
            		Globals.HOME_CLASS);
            mIntent.setComponent(componentName);
            startActivity(mIntent);
        } catch (Throwable e) {
            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
        }
    }

    private void SwitchToPainter() {
        try {
            Intent mIntent = new Intent();
            ComponentName componentName = null;
            componentName = new ComponentName(Globals.PAINTER_PACKAGE,
            		Globals.PAINTER_CLASS);
            mIntent.setComponent(componentName);
            startActivity(mIntent);
        } catch (Throwable e) {
            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
        }
    }

//    private void SwitchToNote() {
//        try {
//            Intent mIntent = new Intent();
//            ComponentName componentName = null;
//            componentName = new ComponentName(Globals.NOTE_PACKAGE,
//            		Globals.NOTE_CLASS);
////                    "ntx.note.NoteWriterActivity");
//            mIntent.setComponent(componentName);
//            startActivity(mIntent);
//        } catch (Throwable e) {
//            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
//        }
//    }

    // Switch To Bookstore Page
//    private void SwitchToBookstore()
//    {
//        Intent intent = new Intent(Intent.ACTION_VIEW, Globals.BOOKSTORE_URI);
//        startActivity(intent);
//    }

    private void SwitchToCalculator() {
        try {
            Intent mIntent = new Intent();
            ComponentName componentName = null;
            componentName = new ComponentName(Globals.CALCULATOR_PACKAGE,
            		Globals.CALCULATOR_CLASS);
            mIntent.setComponent(componentName);
            startActivity(mIntent);
        } catch (Throwable e) {
            Toast.makeText(this, "APP NOT FIND !", Toast.LENGTH_LONG).show();
        }
    }

    public class BatteryStatus
    {
        public int iLevel;
        public int iStatus;
        BatteryStatus()
        {
            iStatus = iLevel = 0;
        }
    }

    private BatteryStatus bsMsg = new BatteryStatus();

    private BroadcastReceiver batteryStatusReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED))
            {
                changeBatteryMgr(intent);
            }
        }
    };

    private void changeBatteryMgr(Intent intent)
    {
        bsMsg.iLevel = intent.getIntExtra("level", 0);
        bsMsg.iStatus = intent.getIntExtra("status", 0);
        showBatteryInfo();
    }

    private void showBatteryInfo()
    {
        txtv_battery.setText(bsMsg.iLevel + "%");

        switch (bsMsg.iStatus) {
        case BatteryManager.BATTERY_STATUS_CHARGING:
            imgv_battery_notice.setImageResource(R.drawable.battery_charging);
            break;
        case BatteryManager.BATTERY_STATUS_UNKNOWN:
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
        case BatteryManager.BATTERY_STATUS_FULL:
            if (bsMsg.iLevel <= 15) {
                imgv_battery_notice.setImageResource(R.drawable.battery_worrying);
            } else {
                imgv_battery_notice.setImageResource(R.drawable.battery_none);
            }
            break;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Register Battery Receiver
        registerReceiver(batteryStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        checkUpdate();
        isSDMount=getExternalSdCardPath(); // set internal & external storage path
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(batteryStatusReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void onBookEvent(BookEvent event, Book book) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBuildEvent(Status status) {
		// TODO Auto-generated method stub
		
	}
    private void openBook(Book book) { 
    	try{
			if(!isSDMount && book.getPath().indexOf(Globals.PATH_EXTERNALSD) != -1 ){
				UIMessageUtil.showErrorMessage(this, "fileNotFound", book.getPath());
				return;
			}
		}catch(Exception e){
//			Toast.makeText(mContext,"_"+resource_errorMessage.getResource("fileNotFound"+).getValue(), 3000).show();
			return;
		}
	    String myExtension = ZLFile.createFileByPath(book.getPath()).getExtension();
	    String path		   = book.getPath();

    	if ( myExtension.equalsIgnoreCase("cbr")
          || myExtension.equalsIgnoreCase("cbz")) {
            try {
                Intent mintent = new Intent("android.intent.action.VIEW");
                mintent.setData(Uri.parse("file://" + path));
                mintent.setClassName("net.androidcomics.acv",
                        "net.robotmedia.acv.ui.ComicViewerActivity");
                mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(mintent);
            } catch (Throwable e) {
                Toast.makeText(this, "Not find ComicViewer !", Toast.LENGTH_LONG).show();
            }

        } else if (myExtension.equalsIgnoreCase("pdf")) {
            try {
//                Intent mintent = new Intent("android.intent.action.VIEW");
//                mintent.setData(Uri.parse("file://" + path));
//                mintent.setClassName("com.artifex.mupdfdemo",
//                        "com.artifex.mupdfdemo.MuPDFActivity");
//                mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                        | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				Intent mintent = new Intent("android.intent.action.VIEW");
				mintent.setDataAndType(Uri.parse("file://" + path),"application/pdf");				
                startActivity(mintent);
                
                myCollection.addToRecentlyOpened(book);
            } catch (Throwable e) {
                Toast.makeText(this, "Not find PDF Reader !", Toast.LENGTH_LONG).show();
            }
            // Daniel 20121218 open epub file using DLReader
        } else if (myExtension.equalsIgnoreCase("epub")
        		|| myExtension.equalsIgnoreCase("fb2")
        		|| myExtension.equalsIgnoreCase("mobi")
        		|| myExtension.equalsIgnoreCase("prc")
        		|| myExtension.equalsIgnoreCase("oeb")
        		|| myExtension.equalsIgnoreCase("txt")
        		|| myExtension.equalsIgnoreCase("rtf")
        		|| myExtension.equalsIgnoreCase("azw3")
        		) {
			if (LibraryActivity.isAlive){
				FBReader.openBookActivity(this, book, null);
				
			}else{	
				Intent mIntent = new Intent(this,LibraryActivity.class);				
				mIntent.putExtra(Globals.KEY_READER_BOOK_PATH, book.getPath());
				startActivity(mIntent);	    
			}
        }else{
            Toast.makeText(this, "Not support \""+myExtension.toUpperCase()+"\" format file", Toast.LENGTH_LONG).show();
        }
    }

}
