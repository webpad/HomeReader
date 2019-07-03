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

package org.geometerplus.fbreader.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.fbreader.util.Pair;
import org.geometerplus.android.fbreader.library.ALog;
import org.geometerplus.android.fbreader.library.CallbackEvent;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.greenrobot.eventbus.EventBus;

import com.ntx.config.Globals;
import android.annotation.SuppressLint;
import android.os.AsyncTask;

@SuppressLint("DefaultLocale")
public class FileFirstLevelTree_AllBooks extends FirstLevelTree {
	private ArrayList<String> resultFiles = new ArrayList<String>();
	private EventBus mEventBus;
	
	FileFirstLevelTree_AllBooks(RootTree root) {
		super(root, Globals.ROOT_ALL_BOOKS);
		mEventBus = EventBus.getDefault();
	}
	
	@Override
	public Pair<String,String> getTreeTitle() {
		return new Pair(getName(), null);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}
	
	@Override
	public void waitForOpening() {
		/*
		ArrayList<String> tempBooks = new ArrayList<String>();
		clear();
		resultFiles.clear();
		// scan all the specific type files. ex:*.epub
		searchFiles(new File(Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER));
		
		// UI for [Random] Book
		tempBooks.clear();
		tempBooks.addAll(resultFiles);
		Collections.shuffle(tempBooks);
		// limit 30 books
		*/	
	}

	public void sortByFileName(){
		clear();
		Collections.sort(resultFiles, new SortIgnoreCase());
		
		// Add Grid Item
		for (String rb :resultFiles){
			addChild(rb, rb.substring(rb.lastIndexOf("/") + 1, rb.length()), "");
		}
		callEvent(CallbackEvent.MORE_ALL_BOOKS_REFRESH);
	}
	
	public void stop(){
		if (asyncSearchAll != null) asyncSearchAll.cancel(true);
	}
	
	AsyncSearchAllFiles asyncSearchAll;
	public void waitForOpeningAll(){

		if( asyncSearchAll != null && asyncSearchAll.getStatus().equals(AsyncTask.Status.RUNNING)){
			return;
		}
		
		asyncSearchAll =  (AsyncSearchAllFiles) new AsyncSearchAllFiles();
		asyncSearchAll.execute();	
		
	}

	private void addChild(String path, String title, String summary) {
		final ZLFile file = ZLFile.createFileByPath(path);
		if (file != null) {
			new FileTree(this, file, title, summary);
		}
	}

	private void addChild(String path, String resourceKey) {
		final ZLResource resource = resource().getResource(resourceKey);
		addChild(path, resource.getValue(), resource.getResource("summary").getValue());
	}
	
	public class SortIgnoreCase implements Comparator<Object> {
	    public int compare(Object o1, Object o2) {
	    	String s1 = (String) o1;
	        String s2 = (String) o2;
	        
	        final int indexNameS1 = s1.lastIndexOf('/');
	        final String filenameS1 = ((indexNameS1 > 1) ? s1.substring(indexNameS1).toLowerCase().intern() : "");				
	
	        final int indexNameS2 = s2.lastIndexOf('/');
	        final String filenameS2 = ((indexNameS2 > 1) ? s2.substring(indexNameS2).toLowerCase().intern() : "");	
	
	        return filenameS1.toLowerCase().compareTo(filenameS2.toLowerCase());
	    }
	}

	// ===== AsyncTask Search =====
	class AsyncSearchAllFiles extends AsyncTask<Integer, Long, Boolean> {
		
		public AsyncSearchAllFiles() {
		}

		@Override
		protected Boolean doInBackground(Integer... params) {	
			clear();
			resultFiles.clear();
			// scan all the specific type files. ex:*.epub
			search(new File(Globals.PATH_SDCARD+"/"+Globals.DOWNLOAD_FOLDER));
			search(new File(Globals.PATH_SDCARD+"/"+Globals.BOOKS_FOLDER));
			
			callEvent(CallbackEvent.MORE_ALL_BOOKS_REFRESH);
			/* Sort by file name
			clear();
			Collections.sort(resultFiles, new SortIgnoreCase());
			
			// Add Grid Item
			for (String rb :resultFiles){
				addChild(rb, rb.substring(rb.lastIndexOf("/") + 1, rb.length()), "");
			}
			*/
			return true;
		}

		@Override
		protected void onProgressUpdate(Long... progress) {
		}

		@Override
		protected void onPostExecute(Boolean result) {
			callEvent(CallbackEvent.MORE_ALL_BOOKS_SEARCH_DONE);
		}
		
		private void search(File file) {
			File[] the_Files = file.listFiles();
			if (the_Files == null)
				return;

			// search all dirs and files
			for (File tempF : the_Files) {
				if (tempF.isDirectory()) {		
					search(tempF);
				} else {
					final int index = tempF.getPath().lastIndexOf('.');
					final String myExtension = ((index > 0) ? tempF.getPath().substring(index).toLowerCase().intern() : "");				
					
					// compare file. if the key is matched
					for (String fileType : Globals.searchBookType){
						
						if (isCancelled())
							return;
						
						if (myExtension.equals(fileType) && tempF.getName().substring(0,1).equals(".")==false){
							Book tempB = Collection.getBookByFile(tempF.getPath());
							if (tempB != null) {
								// If the book has no cover or no title, the book file may be broken.
								if (CoverUtil.getCover(tempB, PluginCollection) != null && !tempB.isTitleEmpty()) {
									// add matched path into array
									addChild(tempF.getPath(), tempF.getPath().substring(tempF.getPath().lastIndexOf("/") + 1, tempF.getPath().length()), "");
									resultFiles.add(tempF.getPath());
									
									// resultFiles.size() == 8, Show page 1 first
									if (resultFiles.size() == 8 || resultFiles.size() % 40 == 0) {
										callEvent(CallbackEvent.MORE_ALL_BOOKS_REFRESH);
									}
									
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	private void callEvent(String event){
		CallbackEvent callbackEvent = new CallbackEvent();
		callbackEvent.setMessage(event);
		mEventBus.post(callbackEvent);
	}
}
