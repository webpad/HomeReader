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

import java.io.File;
import java.util.*;

import android.util.Log;
import android.widget.BaseAdapter;

import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.tree.FBTree;

import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;

public abstract class TreeAdapter extends BaseAdapter {
	
	/**
	 *  Jacky fix quickly click Exception issue
	 *  modify LibraryActivity.java & TreeAdapter.java & TreeActivity.java
	 *  1.isIndexOutOfBoundsException
	 *  2.setIndexOutOfBoundsException(t)
	 *  3.getIndexOutOfBoundsException()
	 */
	private boolean isIndexOutOfBoundsException=false;
	public void setIndexOutOfBoundsException(boolean t){ isIndexOutOfBoundsException=t; }
	public boolean getIndexOutOfBoundsException(){ return isIndexOutOfBoundsException; }
	
	public int my_numofcontentperpage = 8; // Numbers of Items show on a page
	private final static int HARDWARE_TYPE_EINK_HAND_WRITE_PAGE_COUNT = 8;
	private final static int HARDWARE_TYPE_EINK_READER_PAGE_COUNT = 6;

	//pageid
	public int pageid = 1;// current page
	
	private final TreeActivity myActivity;
	private final List<FBTree> myItems;

	protected TreeAdapter(TreeActivity activity) {
		myActivity = activity;
		myItems = Collections.synchronizedList(new ArrayList<FBTree>());
		activity.setListAdapter(this);
		
		if (RefreshClass.isEinkHandWritingHardwareType()) {
			my_numofcontentperpage = HARDWARE_TYPE_EINK_HAND_WRITE_PAGE_COUNT;
		}else{
			my_numofcontentperpage = HARDWARE_TYPE_EINK_READER_PAGE_COUNT;
		}
	}

	protected TreeActivity getActivity() {
		return myActivity;
	}

	public void remove(final FBTree item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.remove(item);
				notifyDataSetChanged();
			}
		});
	}

	public void add(final FBTree item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.add(item);
				notifyDataSetChanged();
			}
		});
	}

	public void add(final int index, final FBTree item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.add(index, item);
				notifyDataSetChanged();
			}
		});
	}

	public void replaceAll(final Collection<FBTree> items, final boolean invalidateViews) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				synchronized (myItems) {
					myItems.clear();
					myItems.addAll(items);
				}
				
				// Jacky 20160511, if removed sd card, hide book form sd card
				try{
					for (int i=0;i<myItems.size();i++){
			            if(!new File(((LibraryTree) myItems.get(i)).getBook().getPath()).exists()){
			            	myItems.remove(i);
			    		}
					}
				}catch(Exception e){
//					Log.d("JK_org.geometerplus.android.fbreader.tree.TreeAdapter", e.toString());
				}
				
				notifyDataSetChanged();
				if (invalidateViews) {
					myActivity.getListView().invalidateViews();
				}
			}
		});
	}

	public int getCount() {
		if ( pageid == getTotalPage() ) {
			return myItems.size() - ((pageid - 1) * my_numofcontentperpage );
		}
		return my_numofcontentperpage;
	}
	
	public int getTotalItem() {
		return myItems.size();
	}
	
	public FBTree getItem(int position) {
		try{
			return myItems.get(position);
		}catch(IndexOutOfBoundsException e){
			setIndexOutOfBoundsException(true); // Jacky enable IndexOutOfBoundsException
			return myItems.get(myItems.size()-1);
		}
//		return myItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getIndex(FBTree item) {
		return myItems.indexOf(item);
	}

	public FBTree getFirstSelectedItem() {
		synchronized (myItems) {
			for (FBTree t : myItems) {
				if (myActivity.isTreeSelected(t)) {
					return t;
				}
			}
		}
		return null;
	}
	
    //calculate total page in this tree level
	public int getTotalPage() {
		int totalpage = myItems.size() / my_numofcontentperpage;
		if ( myItems.size() % my_numofcontentperpage != 0 | myItems.size() / my_numofcontentperpage == 0)
			totalpage++;
		return totalpage;
	}
}
