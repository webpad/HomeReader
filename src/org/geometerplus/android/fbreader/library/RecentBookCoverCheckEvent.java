package org.geometerplus.android.fbreader.library;

public class RecentBookCoverCheckEvent {
	private int checkCounter;
	private int countMax;
	
	public RecentBookCoverCheckEvent() {
		this.checkCounter = 0;
	}
	
	public void resetCounter() {
		this.checkCounter = 0;
	}
	
	public void setCounterMax(int max) {
		this.countMax = max;
	}
	
	public void countUp() {
		this.checkCounter++;
	}
	
	public int getCounter() {
		return this.checkCounter;
	}
	
	public boolean isExceedMax() {
		return checkCounter > countMax;
	}
}
