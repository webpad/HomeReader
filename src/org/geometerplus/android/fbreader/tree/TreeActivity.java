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

package org.geometerplus.android.fbreader.tree;

import java.util.*;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ntx.reader3.R;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;

import org.geometerplus.android.fbreader.library.CallbackEvent;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.library.MainPageMorePopupWindow;
import org.geometerplus.android.fbreader.library.RelativePopupWindow.HorizontalPosition;
import org.geometerplus.android.fbreader.library.RelativePopupWindow.VerticalPosition;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.android.util.OrientationUtil;

public abstract class TreeActivity<T extends FBTree> extends ListActivity {
	
	private static final String OPEN_TREE_ACTION = "android.fbreader.action.OPEN_TREE";

	public static final String TREE_KEY_KEY = "TreeKey";
	public static final String SELECTED_TREE_KEY_KEY = "SelectedTreeKey";
	public static final String HISTORY_KEY = "HistoryKey";

	public final AndroidImageSynchronizer ImageSynchronizer = new AndroidImageSynchronizer(this);

	private T myCurrentTree;
	// we store the key separately because
	// it will be changed in case of myCurrentTree.removeSelf() call
	private FBTree.Key myCurrentKey;
	private final List<FBTree.Key> myHistory =
		Collections.synchronizedList(new ArrayList<FBTree.Key>());

	final ZLResource resource_dialog = ZLResource.resource("dialog");
	final ZLResource resource_dialog_waitMessage = resource_dialog.getResource("waitMessage");
	
	private String msg=ZLResource.resource("dialog").getResource("waitMessage").getResource("loadingCatalogInfo").getValue();
	RelativeLayout txtv_tree_activity_title;
	TextView title_shelf_textView;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Globals.closeWaitDialog(this);
	}

	@Override
	protected void onDestroy() {
		ImageSynchronizer.clear();

		super.onDestroy();
	}

	public TreeAdapter getTreeAdapter() {
		return (TreeAdapter)super.getListAdapter();
	}

	public T getCurrentTree() {
		return myCurrentTree;
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		 //when we go to new level of tree, we need to re-init page id value
		((TreeAdapter) getListAdapter()).pageid = 1;
		
		OrientationUtil.setOrientation(this, intent);
		if (OPEN_TREE_ACTION.equals(intent.getAction())) {
			runOnUiThread(new Runnable() {
				public void run() {
					init(intent);
				}
			});
		} else {
			super.onNewIntent(intent);
		}
		
		//update ui
		TextView page_info = (TextView) findViewById(R.id.page_info);
		page_info.setText( getTreeAdapter().pageid + " of " + getTreeAdapter().getTotalPage());
		getTreeAdapter().notifyDataSetChanged();

	}

	protected abstract T getTreeByKey(FBTree.Key key);
	public abstract boolean isTreeSelected(FBTree tree);

	protected boolean isTreeInvisible(FBTree tree) {
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			LibraryActivity.removeCurrentTreeFromList(); // remove last tree record
			FBTree parent = null;
			synchronized (myHistory) {
				while (parent == null && !myHistory.isEmpty()) {
					parent = getTreeByKey(myHistory.remove(myHistory.size() - 1));
				}
			}
			if (parent == null && myCurrentTree != null) {
				parent = myCurrentTree.Parent;
			}
			if (parent != null && !isTreeInvisible(parent)) {
				if (parent.Level == 0 && RefreshClass.isEinkHandWritingHardwareType()) {
//					if(LibraryActivity.libraryBookshelf != null && LibraryActivity.libraryGrid != null) {
//						LibraryActivity.libraryBookshelf.setVisibility(View.VISIBLE);
//						LibraryActivity.libraryGrid.setVisibility(View.GONE);
//					}
				}else{
					openTree(parent, myCurrentTree, false);
				}
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	// TODO: change to protected
	public void openTree(final FBTree tree) {

		Globals.openWaitDialog(this);

		Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {
				openTree(tree, null, true);
			}
		};
		
		handler.postDelayed(runnable,100);		
	}

	// TODO: change to protected
	public void openTreeOnlyForAllBooks(final FBTree tree) {
		openTree(tree, null, true);
	}
	public void clearHistory() {
		runOnUiThread(new Runnable() {
			public void run() {
				myHistory.clear();
			}
		});
	}

	protected void onCurrentTreeChanged() {
	}

	private void openTree(final FBTree tree, final FBTree treeToSelect, final boolean storeInHistory) {
		switch (tree.getOpeningStatus()) {
			case WAIT_FOR_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				final String messageKey = tree.getOpeningStatusMessage();
				if (messageKey != null) {
					UIUtil.createExecutor(TreeActivity.this, messageKey).execute(
						new Runnable() {
							public void run() {
								tree.waitForOpening();
							}
						},
						new Runnable() {
							public void run() {
								openTreeInternal(tree, treeToSelect, storeInHistory);
							}
						}
					);
				} else {
					tree.waitForOpening();
					openTreeInternal(tree, treeToSelect, storeInHistory);
				}
				break;
			default:
				openTreeInternal(tree, treeToSelect, storeInHistory);
				break;
		}
		Globals.closeWaitDialog(this);
	}

//	private void setTitleAndSubtitle(Pair<String,String> pair) {
//		if (pair.Second != null) {
//			setTitle(pair.First + " - " + pair.Second);
//		} else {
//			setTitle(pair.First);
//		}
//	}
	
	public void init(Intent intent) {
		final FBTree.Key key = (FBTree.Key)intent.getSerializableExtra(TREE_KEY_KEY); //TreeKey
		final FBTree.Key selectedKey = (FBTree.Key)intent.getSerializableExtra(SELECTED_TREE_KEY_KEY);

		txtv_tree_activity_title = (RelativeLayout) findViewById(R.id.title_shelf);
		title_shelf_textView = (TextView) findViewById(R.id.title_shelf_textView);
		txtv_tree_activity_title.setOnClickListener(new ButtonClickListener());
		
		TextView txtv_tree_activity_path = (TextView) findViewById(R.id.path);
		txtv_tree_activity_path.setEllipsize(TruncateAt.MARQUEE.MIDDLE); // Jacky 20160408 if path too long , Middle text omitted

		title_shelf_textView.setTextColor(Color.BLACK);
		txtv_tree_activity_path.setTextColor(Color.BLACK);
		
		myCurrentTree = getTreeByKey(key);
		// not myCurrentKey = key
		// because key might be null
		myCurrentKey = myCurrentTree.getUniqueKey();
		final TreeAdapter adapter = getTreeAdapter();
		adapter.replaceAll(myCurrentTree.subtrees(), false);
		
		title_shelf_textView.setText(myCurrentTree.getName());

		 //terry add 20120531 : show title and folder path for fileTree
		if ( myCurrentTree.getUniqueKey().toString().contains("@FBReaderLibraryRoot :: fileTree") ) {
			txtv_tree_activity_path.setVisibility(View.INVISIBLE);
//			txtv_tree_activity_title.setText("Files");
			if ( myCurrentTree.getUniqueKey().toString().contains("@FBReaderLibraryRoot :: fileTree ::") ) {
				txtv_tree_activity_path.setVisibility(View.VISIBLE);
				txtv_tree_activity_path.setText(myCurrentTree.getTreeTitle().First);
			}
		} else if ( myCurrentTree.getUniqueKey().toString().contains("@FBReaderLibraryRoot :: extSD") ) {
			txtv_tree_activity_path.setVisibility(View.INVISIBLE);
			txtv_tree_activity_path.setVisibility(View.VISIBLE);
			if (myCurrentTree.getUniqueKey().toString().contains("@FBReaderLibraryRoot :: extSD ::") ) {
				txtv_tree_activity_path.setText(myCurrentTree.getTreeTitle().First);
			}else{
				txtv_tree_activity_path.setText(Globals.PATH_EXTERNALSD);
			}
		} else if ( myCurrentTree.getUniqueKey().toString().contains("@FBReaderLibraryRoot :: found") ) {
			txtv_tree_activity_path.setVisibility(View.INVISIBLE);
			title_shelf_textView.setText(myCurrentTree.getTreeTitle().First);

		} else{
			txtv_tree_activity_path.setVisibility(View.INVISIBLE);
		}
		//when current view is root view then we dont show page turn button
		TextView page_info = (TextView) findViewById(R.id.page_info);
		page_info.setTextColor(Color.BLACK);
		
		Button nextButton = (Button) findViewById(R.id.next_page_button);
		Button previousButton = (Button) findViewById(R.id.previous_page_button);
		//myCurrentTree.getUniqueKey().toString().equals("@FBReaderLibraryRoot") &&
		if ( ((TreeAdapter) getListAdapter()).getTotalPage() == 1 ) { //only one page
			page_info.setVisibility(View.INVISIBLE);
			nextButton.setVisibility(View.INVISIBLE);
			previousButton.setVisibility(View.INVISIBLE);
		} else {
			page_info.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.VISIBLE);
			previousButton.setVisibility(View.VISIBLE);
		}
		
//		setTitleAndSubtitle(myCurrentTree.getTreeTitle());
		final FBTree selectedTree =
			selectedKey != null ? getTreeByKey(selectedKey) : adapter.getFirstSelectedItem();
		final int index = adapter.getIndex(selectedTree);
		if (index != -1) {
			setSelection(index);
			getListView().post(new Runnable() {
				public void run() {
					setSelection(index);
				}
			});
		}

		myHistory.clear();
		
		// Jacky 20160706_fix keycode back history issue.
//		final ArrayList<FBTree.Key> history =
//			(ArrayList<FBTree.Key>)intent.getSerializableExtra(HISTORY_KEY);
//		if (history != null) {
//			myHistory.addAll(history);
//		}
		onCurrentTreeChanged();
	}

	private void openTreeInternal(FBTree tree, FBTree treeToSelect, boolean storeInHistory) {
		switch (tree.getOpeningStatus()) {
			case READY_TO_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				if (storeInHistory && !myCurrentKey.equals(tree.getUniqueKey())) {
					myHistory.add(myCurrentKey);
				}
				onNewIntent(new Intent(this, getClass())
					.setAction(OPEN_TREE_ACTION)
					.putExtra(TREE_KEY_KEY, tree.getUniqueKey())
					.putExtra(
						SELECTED_TREE_KEY_KEY,
						treeToSelect != null ? treeToSelect.getUniqueKey() : null
					)
					.putExtra(HISTORY_KEY, new ArrayList<FBTree.Key>(myHistory))
				);
				break;
			case CANNOT_OPEN:
				UIMessageUtil.showErrorMessage(TreeActivity.this, tree.getOpeningStatusMessage());
				break;
		}
	}
	
	private MainPageMorePopupWindow mainPageMorePopupWindow;

	private void showMainPageMorePopupWindow(String title) {
		
		txtv_tree_activity_title.setSelected(true);
		
		mainPageMorePopupWindow = MainPageMorePopupWindow.getInstance(this);
		mainPageMorePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				txtv_tree_activity_title.setSelected(false);
			}
		});
		mainPageMorePopupWindow.showOnAnchor(txtv_tree_activity_title, HorizontalPosition.ALIGN_LEFT, VerticalPosition.BELOW, 0, 0, 15, 15);
		mainPageMorePopupWindow.setTitle(title);
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEvent(CallbackEvent event) {
		
		if (event.getMessage().equals(CallbackEvent.MORE_RECENT)) {
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_RECENT).getValue());
		}else if (event.getMessage().equals(CallbackEvent.MORE_FAVORITES)){
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_FAVORITES).getValue());
		}else if (event.getMessage().equals(CallbackEvent.MORE_AUTHOR)){
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_BY_AUTHOR).getValue());
		}else if (event.getMessage().equals(CallbackEvent.MORE_TITLE)){
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_BY_TITLE).getValue());
		}else if (event.getMessage().equals(CallbackEvent.MORE_ALL_BOOKS)){
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_ALL_BOOKS).getValue());
		}else if (event.getMessage().equals(CallbackEvent.MORE_ALL_IMAGES)){
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_ALL_IMAGES).getValue());
		}else if (event.getMessage().equals(CallbackEvent.MORE_EXT_SD_CARD)){
			title_shelf_textView.setText(ZLResource.resource("library").getResource(Globals.ROOT_EXT_SD).getValue());
		}
	}
	
	public class ButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.title_shelf) {
				showMainPageMorePopupWindow(myCurrentTree.getName());
			} 
		}
	}
}
