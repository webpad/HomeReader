package org.geometerplus.android.fbreader.library;

public class CallbackEvent {
	public final static String MORE_RECENT = "more_recent";
	public final static String MORE_FAVORITES = "more_favorites";
	public final static String MORE_AUTHOR = "more_author";
	public final static String MORE_TITLE = "more_title";
	public final static String MORE_ALL_BOOKS = "more_all_books";
	public final static String MORE_ALL_BOOKS_REFRESH = "more_all_books_refresh";
	public final static String MORE_ALL_BOOKS_SEARCH_DONE = "more_all_books_search_done";
	public final static String MORE_ALL_IMAGES = "more_all_images";
	public final static String MORE_EXT_SD_CARD = "more_ext_sd_card";

	private String message;

	public void setMessage(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}
}
