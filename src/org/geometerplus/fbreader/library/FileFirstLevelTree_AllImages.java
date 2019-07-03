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
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import android.annotation.SuppressLint;
import com.ntx.config.Globals;

@SuppressLint("DefaultLocale")
public class FileFirstLevelTree_AllImages extends FirstLevelTree {
	private ArrayList<String> resultFiles = new ArrayList<String>();

	FileFirstLevelTree_AllImages(RootTree root) {
		super(root, Globals.ROOT_ALL_IMAGES);
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
		clear();

		resultFiles.clear();

		searchFiles(new File(Globals.PATH_SDCARD));

		Collections.sort(resultFiles, new SortIgnoreCase());

		for (String rb :resultFiles){
			addChild(rb, rb.substring(rb.lastIndexOf("/") + 1, rb.length()), "");
		}
		
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

	// scan all the specific type files. ex:*.note
	public void searchFiles(File file) {
		File[] the_Files = file.listFiles();
		if (the_Files == null)
			return;

		// search all dirs and files
		for (File tempF : the_Files) {
			if (tempF.isDirectory()) {
				searchFiles(tempF);
			} else {
				final int index = tempF.getPath().lastIndexOf('.');
				final String myExtension = ((index > 0) ? tempF.getPath().substring(index).toLowerCase().intern() : "");				
				
				// compare file. if the key is matched, return value > -1.
				for (String fileType : Globals.searchImageType){
					if (myExtension.equals(fileType) && tempF.getName().substring(0,1).equals(".")==false){
						// add matched path into array
						resultFiles.add(tempF.getPath());
						break;
					}
				}
			}
		}
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

}
