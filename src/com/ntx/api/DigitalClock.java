
package com.ntx.api;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import ntx.reader3.R;

public class DigitalClock extends TextView {
    private final static String TAG = "DigitalClock";
    private Calendar mCalendar;
    private String mFormat = "hh:mm";
    private Runnable mTicker;
    private Handler mHandler;
    private boolean mTickerStopped = false;
	private TextView txtv_ampm;
	private Context ctx;
	
    public DigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context) {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        this.ctx=context;
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();
        mTicker = new Runnable() {
            @Override
            public void run() {
                if (mTickerStopped)
                    return;
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                txtv_ampm=(TextView) ((Activity) ctx).findViewById(R.id.txtv_ampm);
                
     	       	int mHour = 0;
     	       	int mMinute = mCalendar.get(Calendar.MINUTE);

     	       	// check 24 hour or AM PM format
                if (DateFormat.is24HourFormat(ctx)){
                	mHour=mCalendar.get(Calendar.HOUR_OF_DAY);
               		txtv_ampm.setVisibility(View.GONE);               		
    				if (mHour<10)
                    {	
                    	if (mMinute<10)
                    	    setText("0"+mHour+":0"+mMinute);
                    	else
                    		setText("0"+mHour+":"+mMinute);            
                    }else{
                    	if (mMinute<10)
                    		setText(mHour+":0"+mMinute);
                    	else
                    		setText(mHour+":"+mMinute);
                    }                			
                }else{
                	mHour=mCalendar.get(Calendar.HOUR);
                	if (mCalendar.get(Calendar.AM_PM)==0){
                		txtv_ampm.setText("AM");
         	       	}else{
                    	if (mHour==0) mHour=12; // Jacky 20160425 When Calendar.HOUR 12:00 PM is show 0:00
         	       		txtv_ampm.setText("PM");
         	       	}
                    txtv_ampm.setVisibility(View.VISIBLE); 	
                    setText(DateFormat.format(mFormat, mCalendar.getTimeInMillis()));
                }

     	       	invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + 60000;
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    // jimmychung 20160411 fix E60Q62 issue 1017 synchronized system time.
    public void update() {
    	try {
    		mCalendar = Calendar.getInstance();
    		mHandler.post(mTicker);
    	} catch(Exception ex) {
//    		Log.e(TAG,ex.toString());
    	} 
    	
    }
    public void stop(){
    	mHandler.removeCallbacks(mTicker);
    }
    public void setFormat(String format) {
        mFormat = format;
    }
}
