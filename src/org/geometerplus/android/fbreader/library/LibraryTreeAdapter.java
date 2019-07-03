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

package org.geometerplus.android.fbreader.library;

import java.io.File;

import org.geometerplus.android.fbreader.covers.CoverManager;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.fbreader.tree.TreeAdapter;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.library.AuthorListTree;
import org.geometerplus.fbreader.library.AuthorTree;
import org.geometerplus.fbreader.library.FavoritesTree;
import org.geometerplus.fbreader.library.FileFirstLevelTree;
import org.geometerplus.fbreader.library.FileFirstLevelTree_AllBooks;
import org.geometerplus.fbreader.library.FileFirstLevelTree_AllImages;
import org.geometerplus.fbreader.library.FileFirstLevelTree_ExtSD;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.RecentBooksTree;
import org.geometerplus.fbreader.library.SyncTree;
import org.geometerplus.fbreader.library.TitleListTree;
import org.geometerplus.fbreader.library.TitleTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import com.ntx.api.RefreshClass;
import com.ntx.config.Globals;
import com.ntx.image.BitmapWorkerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask.Status;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ntx.reader3.R;

@SuppressLint("NewApi")
class LibraryTreeAdapter extends TreeAdapter {
	private CoverManager myCoverManager;
	private int myLayoutview;
	private Context mContext;
	private int IMAGE_TYPE=0x123;
	public BitmapWorkerTask[] asyncTask= new BitmapWorkerTask[my_numofcontentperpage];

	LibraryTreeAdapter(TreeActivity activity, int layoutview) {
		super(activity);
		mContext = (Context)activity;
		myLayoutview = layoutview;
	}
	public void setlayout(int layoutview) {
		myLayoutview = layoutview;

//		if( myLayoutview == ((LibraryActivity) mContext).LIST_VIEW ) {
//			//re-initial
////			myCoverWidth = -1;
////			myCoverHeight = -1;
//		}
	}
	
	@SuppressLint("InflateParams")
	private View createView(View convertView, ViewGroup parent, LibraryTree tree) {

		View current_view =null;
		String bookTilte="";
		try{
			if (tree.getBook()!=null){
				bookTilte=tree.getBook().getTitle();
			}else{
				bookTilte=tree.getName();
			}
			
		}catch(Exception e){}
		
		if ( myLayoutview == LibraryActivity.LIST_VIEW ) {
			current_view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
			final TextView nameView = ViewUtil.findTextView(current_view, R.id.library_tree_item_name);
			nameView.setText(bookTilte);
			final TextView summaryView = ViewUtil.findTextView(current_view, R.id.library_tree_item_childrenlist);
			
			if (tree.getName().equals(bookTilte)) {
				summaryView.setVisibility(View.GONE);
			}else {
				summaryView.setVisibility(View.VISIBLE);
				summaryView.setText(tree.getName());
			}
			
			RefreshClass.setListBookTitleSize(nameView,summaryView);
			
		} else if ( myLayoutview == LibraryActivity.GRID_VIEW ) {
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if ( convertView != null )
				current_view = convertView;
			else {
				current_view = inflater.inflate(R.layout.bookunit, null);
			}
//			((TextView)current_view.findViewById(R.id.book_title)).setText(tree.getName());
			final int index = tree.getName().lastIndexOf('.');
			final String myExtension = ((index > 0) ? tree.getName().substring(index).toLowerCase().intern() : "");
			boolean isShowTitle = false;
			
			for (String filenameType : Globals.searchBookTypeShowTitle){
				if (filenameType.equals(myExtension)){
					isShowTitle = true;
					break;
				}
			}
			
			// customer request change from filename to title name
			if (index > 0){
				((TextView)current_view.findViewById(R.id.book_title)).setText(isShowTitle ? bookTilte : tree.getName().substring(0,index));
			}else{
				((TextView)current_view.findViewById(R.id.book_title)).setText(tree.getName());
			}
			
			RefreshClass.setGridBookTitleSize(((TextView)current_view.findViewById(R.id.book_title))); // TODO
		}
		
		try{
			if (tree.getBook()!=null){
				
				if((new File(tree.getBook().getPath())).exists()){
					current_view.setVisibility(View.VISIBLE);
				}else{
					current_view.setVisibility(View.GONE);
				}
			}
		}catch(Exception e){}

		return current_view;
	}
	@SuppressWarnings("deprecation")
	public View getView(int position, View convertView, final ViewGroup parent) {

		final int real_position = my_numofcontentperpage * (pageid - 1) + position;// for page turn setting
		final LibraryTree tree = (LibraryTree)getItem(real_position);
		final View current_view = createView(convertView, parent, tree);

		InitStarButton(tree,real_position,current_view);

		if (getActivity().isTreeSelected(tree)) {
			current_view.setBackgroundColor(0xff555555);
		} else {
			current_view.setBackgroundColor(0);
		}
		if (myCoverManager == null) {
			current_view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			final int coverHeight = current_view.getMeasuredHeight();
			final TreeActivity<?> activity = getActivity();
			myCoverManager = new CoverManager(activity, activity.ImageSynchronizer, coverHeight * 15 / 32, coverHeight);
			current_view.requestLayout();
		}
		
		ImageView coverView, coverFrameView;
		RelativeLayout.LayoutParams linearParams = null;

		if ( myLayoutview == LibraryActivity.LIST_VIEW ) {
			coverView = ViewUtil.findImageView(current_view, R.id.library_tree_item_icon);
			int coverResourceId=getCoverResourceId(tree);
			int mWidth=60;
			int mHeight=80;
			
			if (coverResourceId==IMAGE_TYPE){
				coverView.setImageBitmap(resizeBitmapFromFile(((FileTree)tree).getFile().getPath(),mWidth,mHeight));
			}else if (!myCoverManager.trySetCoverImage(coverView, tree)){
				coverView.setImageResource(coverResourceId);
				coverView.setBackgroundResource(R.drawable.ic_list_library_book_format);
			}
			coverView.getLayoutParams().width = mWidth;
			coverView.getLayoutParams().height = mHeight;
			coverView.requestLayout();


		} else if ( myLayoutview == LibraryActivity.GRID_VIEW ) {
			// set image based on selected text
			coverView = ViewUtil.findImageView(current_view, R.id.book_cover);
			coverFrameView = ViewUtil.findImageView(current_view, R.id.book_cover_frame);
			
			int coverResourceId = getCoverResourceId(tree);

			int mWidth=205;
			int mHeight=150;
			
			if (coverResourceId==IMAGE_TYPE){
				coverView.setImageBitmap(null);		
				if( asyncTask[position]!=null && asyncTask[position].getStatus().equals(Status.RUNNING)){
					asyncTask[position].cancel(true);
				}
					
				asyncTask[position]=new BitmapWorkerTask(coverView, mWidth,mHeight);
				asyncTask[position].execute(((FileTree)tree).getFile().getPath());
				
			}else{
				if (!myCoverManager.trySetCoverImage(coverView, tree)){
					if (tree.getBook() != null){
						coverView.setImageResource(Globals.getCoverTypeTagResourceId(tree.getName()));
						coverView.setBackgroundResource(R.drawable.book_cover_default);
					}else{
						coverView.setImageResource(coverResourceId);
						coverView.setBackgroundResource(R.drawable.bg_book);
					}
				}
			}
			
			linearParams = (RelativeLayout.LayoutParams) coverView.getLayoutParams();
			linearParams.height = mWidth;
			linearParams.width = mHeight;

			coverView.setLayoutParams(linearParams);
			coverFrameView.setLayoutParams(linearParams);
		}
		return current_view;
	}
	
	/**
	 * Provide a cover for a book without a cover
	 * @param tree
	 * @return
	 */
	private int getCoverResourceId(LibraryTree tree) {
	    String myExtension = ZLFile.createFileByPath(tree.getTreeTitle().toString()).getExtension().toLowerCase();
		if (tree.getBook() != null) {
			
        	if ( myLayoutview == LibraryActivity.GRID_VIEW ) {
        		return Globals.getCoverTypeTagResourceId(tree.getBook().getPath());
        	}else{
        		return Globals.getCoverTypeListResourceId(tree.getBook().getPath());
        	}			

		}else if (tree instanceof FavoritesTree) {
			return R.drawable.icon_favorite;
		} else if (tree instanceof RecentBooksTree || tree instanceof SyncTree) {
			return R.drawable.ic_list_library_recent;
		} else if (tree instanceof AuthorListTree) {
			return R.drawable.ic_list_library_authors;
		} else if (tree instanceof AuthorTree) {
			return R.drawable.ic_list_library_author;
		} else if (tree instanceof TitleListTree) {
			return R.drawable.ic_list_library_by_title;
		} else if (tree instanceof TitleTree) {
			
			 if (((TitleTree)tree).getName().equalsIgnoreCase("A")) {
	              return R.drawable.ic_list_library_by_title_a;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("B")) {
	              return R.drawable.ic_list_library_by_title_b;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("C")) {
	              return R.drawable.ic_list_library_by_title_c;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("D")) {
	              return R.drawable.ic_list_library_by_title_d;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("E")) {
	              return R.drawable.ic_list_library_by_title_e;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("F")) {
	              return R.drawable.ic_list_library_by_title_f;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("G")) {
	              return R.drawable.ic_list_library_by_title_g;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("H")) {
	              return R.drawable.ic_list_library_by_title_h;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("I")) {
	              return R.drawable.ic_list_library_by_title_i;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("J")) {
	              return R.drawable.ic_list_library_by_title_j;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("K")) {
	              return R.drawable.ic_list_library_by_title_k;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("L")) {
	              return R.drawable.ic_list_library_by_title_l;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("M")) {
	              return R.drawable.ic_list_library_by_title_m;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("N")) {
	              return R.drawable.ic_list_library_by_title_n;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("O")) {
	              return R.drawable.ic_list_library_by_title_o;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("P")) {
	              return R.drawable.ic_list_library_by_title_p;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("Q")) {
	              return R.drawable.ic_list_library_by_title_q;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("R")) {
	              return R.drawable.ic_list_library_by_title_r;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("S")) {
	              return R.drawable.ic_list_library_by_title_s;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("T")) {
	              return R.drawable.ic_list_library_by_title_t;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("U")) {
	              return R.drawable.ic_list_library_by_title_u;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("V")) {
	              return R.drawable.ic_list_library_by_title_v;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("W")) {
	              return R.drawable.ic_list_library_by_title_w;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("X")) {
	              return R.drawable.ic_list_library_by_title_x;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("Y")) {
	              return R.drawable.ic_list_library_by_title_y;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("Z")) {
	              return R.drawable.ic_list_library_by_title_z;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("0")) {
	              return R.drawable.ic_list_library_by_title_0;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("1")) {
	              return R.drawable.ic_list_library_by_title_1;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("2")) {
	              return R.drawable.ic_list_library_by_title_2;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("3")) {
	              return R.drawable.ic_list_library_by_title_3;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("4")) {
	              return R.drawable.ic_list_library_by_title_4;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("5")) {
	              return R.drawable.ic_list_library_by_title_5;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("6")) {
	              return R.drawable.ic_list_library_by_title_6;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("7")) {
	              return R.drawable.ic_list_library_by_title_7;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("8")) {
	              return R.drawable.ic_list_library_by_title_8;
	          } else if (((TitleTree)tree).getName().equalsIgnoreCase("9")) {
	              return R.drawable.ic_list_library_by_title_9;
	          } else {
	              return R.drawable.ic_list_library_by_title_non;
	          }
		} else if (tree instanceof FileFirstLevelTree) {
			return R.drawable.ic_list_library_folder;
		} else if (tree instanceof FileFirstLevelTree_AllBooks) {
			return R.drawable.icon_library;
		} else if (tree instanceof FileFirstLevelTree_AllImages) {
			return R.drawable.icon_allimages;
		} else if (tree instanceof FileFirstLevelTree_ExtSD) {
			
//			if (LibraryActivity.isSDMount) 	return R.drawable.icon_microsd;
//			else 							return R.drawable.ic_list_library_permission_denied;
			
			return R.drawable.icon_microsd;
		} else if (tree instanceof FileTree) {
			final ZLFile file = ((FileTree)tree).getFile();
			if (file.isArchive()) {
				if (myExtension.equals("pdf"))
	            {
	                return R.drawable.ic_list_library_book_pdf;
	            }else {
				return R.drawable.ic_list_library_zip;
	            }
			} else if (file.isDirectory() && file.isReadable()) {
				return R.drawable.ic_list_library_folder;
			} else {
            	boolean isImage=false;
            	for (String type : Globals.searchImageType){
            		if (file.getPath().toLowerCase().indexOf(type) > -1){isImage=true;break;}
            	}
            	if (isImage){
            		return IMAGE_TYPE;
            	}else{
            		return R.drawable.ic_list_library_permission_denied;
            	}
			}	

		} else if (tree instanceof AuthorTree) {
			return R.drawable.ic_list_library_author;
		}
		return R.drawable.ic_list_library_books; 
	}

	// Jacky 20160331
	public boolean isBookInFavorites(Book book) {
		if (book == null) {
			return false;
		}
		
		for (String inFavoritesBook : FavoritesTree.inFavoritesBook) {
			if (book.getTitle().equals(inFavoritesBook)){
				return true;
			}
		}
		return false;
	}
	
	String pathPrefix = "/mnt/media_rw/extsd";
	private void InitStarButton( final LibraryTree tree, final int position, final View current_view ) {
		final LinearLayout ll_imv_extsd=(LinearLayout) current_view.findViewById(R.id.ll_imv_extsd);

		final Button favorateButton;
		final Book book = tree.getBook();
		favorateButton = (Button) current_view.findViewById(R.id.book_favorate);
		int coverResourceId=getCoverResourceId(tree);

			//show star when current item is book
			if( book == null ) {
				if (coverResourceId==IMAGE_TYPE){
//					if (((FileTree)tree).getFile().getPath().indexOf(Globals.extSD_path)!=-1){
					if (((FileTree)tree).getFile().getPath().substring(0,Globals.PATH_EXTERNALSD.length()).equals(Globals.PATH_EXTERNALSD)
							|| ((FileTree)tree).getFile().getPath().substring(0,pathPrefix.length()).equals(pathPrefix) ){
						ll_imv_extsd.setVisibility(View.VISIBLE);
					}else{
						ll_imv_extsd.setVisibility(View.INVISIBLE);
					}
					favorateButton.setVisibility(View.INVISIBLE);
				}else{
					favorateButton.setVisibility(View.INVISIBLE);
					ll_imv_extsd.setVisibility(View.INVISIBLE);
				}
			} else {
				// if file in ext_SD, remove star button.
				String filePath="";
				try{
					filePath=((FileTree)tree).getFile().getPath();
				}catch(Exception e){
					filePath=book.getPath();
				}
				
//				if (book.getPath().indexOf(Globals.extSD_path)!=-1){
				if (filePath.substring(0,Globals.PATH_EXTERNALSD.length()).equals(Globals.PATH_EXTERNALSD)
						|| filePath.substring(0,pathPrefix.length()).equals(pathPrefix) ){
					ll_imv_extsd.setVisibility(View.VISIBLE);
					favorateButton.setVisibility(View.INVISIBLE);
				}else{
					ll_imv_extsd.setVisibility(View.INVISIBLE);
					favorateButton.setVisibility(View.VISIBLE);
					if ( isBookInFavorites(book) ) {
						favorateButton.setBackgroundResource(R.drawable.reader_favorite_checked);
					} else {
						favorateButton.setBackgroundResource(R.drawable.reader_favorite_uncheck);
					}
				}
			}
            //set click star method
			favorateButton.setOnClickListener(new Button.OnClickListener() {
			    @Override
			    public void onClick(final View view) {

			        if (book != null) {

			        	if (isBookInFavorites(book)) {
			        	    favorateButton.setBackgroundResource(R.drawable.reader_favorite_uncheck);
			        	    book.removeLabel(Book.FAVORITE_LABEL);
			        	    LibraryActivity.myCollection.saveBook(book);
							if (tree.onBookEvent(BookEvent.Updated, book)) {
								((LibraryActivity)mContext).getTreeAdapter().replaceAll(((LibraryActivity)mContext).getCurrentTree().subtrees(), true);
							}
							FavoritesTree.removeFavoritesBook(book);
	    		        } else {
		    	        	favorateButton.setBackgroundResource(R.drawable.reader_favorite_checked);
		    	        	 book.addNewLabel(Book.FAVORITE_LABEL);
				        	 LibraryActivity.myCollection.saveBook(book); 
				        	 FavoritesTree.addToFavoritesList(book);
			            }
			        }
			    }
			});
	}
	  public static Bitmap resizeBitmapFromFile(String path,
		        int reqWidth, int reqHeight) { // BEST QUALITY MATCH
		  
		  	// First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(path, options);

		    // Calculate inSampleSize Raw height and width of image
		    final int height = options.outHeight;
		    final int width = options.outWidth;
		    options.inPreferredConfig = Bitmap.Config.RGB_565;
		    int inSampleSize = 1;

		    if (height > reqHeight) {
		        inSampleSize = Math.round((float)height / (float)reqHeight);
		    }

		    int expectedWidth = width / inSampleSize;

		    if (expectedWidth > reqWidth) {
		        //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
		        inSampleSize = Math.round((float)width / (float)reqWidth);
		    }
		    options.inSampleSize = inSampleSize;

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeFile(path, options);
	}
}
