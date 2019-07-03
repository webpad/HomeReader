package org.geometerplus.android.fbreader.library;

import com.google.gson.annotations.SerializedName;
import com.ntx.config.Globals;

public class RecentlyBookData {
	@SerializedName(Globals.KEY_READER_RECENT_INDEX)
	private int recentIndex = 0;

	@SerializedName(Globals.KEY_READER_BOOK_ID)
	private long bookId = 0;

	@SerializedName(Globals.KEY_READER_BOOK_TITLE)
	private String bookTitle = "";
	
	@SerializedName(Globals.KEY_READER_RECENT_AUTHORS)
	private String bookAuthors = "";

	@SerializedName(Globals.KEY_READER_BOOK_PATH)
	private String bookPath = "";
	
	@SerializedName(Globals.KEY_READER_RECENT_SIZE)
	private long bookSize = 0;

	@SerializedName(Globals.KEY_READER_BOOK_ENCODING)
	private String bookEncoding = "";

	@SerializedName(Globals.KEY_READER_BOOK_LANGUAGE)
	private String bookLanguage = "";

	@SerializedName(Globals.KEY_READER_RECENT_HASCOVER)
	private boolean bookHasCover = false;

	@SerializedName(Globals.KEY_READER_RECENT_TYPE)
	private String bookType = "";

	public RecentlyBookData(int index) {
		this.recentIndex = index;
	}

	public RecentlyBookData(int index, long id, String title, String path, String encoding, String language) {
		this.recentIndex = index;
		this.bookId = id;
		this.bookTitle = title == null ? "" : title;
		this.bookPath = path == null ? "" : title;
		this.bookEncoding = encoding == null ? "" : encoding;
		this.bookLanguage = language == null ? "" : language;
	}

	public void setId(long id) {
		this.bookId = id;
	}

	public void setTitle(String title) {
		this.bookTitle = title;
	}

	public void setAuthors(String authors) {
		this.bookAuthors = authors;
	}

	public void setPath(String path) {
		this.bookPath = path;
	}

	public void setSize(long size) {
		this.bookSize = size;
	}

	public void setEncoding(String encoding) {
		this.bookEncoding = encoding;
	}

	public void setLanguage(String language) {
		this.bookLanguage = language;
	}

	public void setCover(boolean hasCover) {
		this.bookHasCover = hasCover;
	}

	public void setType(String type) {
		this.bookType = type;
	}

	public int getIndex() {
		return this.recentIndex;
	}

	public long getId() {
		return this.bookId;
	}

	public String getTitle() {
		return this.bookTitle;
	}

	public String getAuthors() {
		return this.bookAuthors;
	}

	public String getPath() {
		return this.bookPath;
	}

	public long getSize() {
		return this.bookSize;
	}

	public String getEncoding() {
		return this.bookEncoding;
	}

	public String getLanguage() {
		return this.bookLanguage;
	}

	public boolean hasCover() {
		return this.bookHasCover;
	}

	public String getType() {
		return this.bookType;
	}
}
