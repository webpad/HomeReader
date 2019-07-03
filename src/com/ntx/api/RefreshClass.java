package com.ntx.api;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.TextView;

public class RefreshClass {

    static {
        try {
            System.loadLibrary("epd");
        } catch (Throwable e) {
            Log.e("RefreshClass", "Load epd library error !", e);
        }
    }

    public static final int EINK_STATIC_MODE_SET = 0x10000000;
    public static final int UPDATE_MODE_GLOBAL_RESET = 0x20000000;

    /* update mode for handwriting in eink */
    public static final int UPDATE_MODE_PARTIAL_DU =
            android.view.View.EINK_WAVEFORM_MODE_DU
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_PARTIAL_A2 =
                      android.view.View.EINK_WAVEFORM_MODE_A2
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_SET_PARTIAL_A2_MONO_WAIT =
            android.view.View.EINK_WAVEFORM_MODE_A2
            |EINK_STATIC_MODE_SET
            |android.view.View.EINK_MONOCHROME_MODE_MONOCHROME
            |android.view.View.EINK_WAIT_MODE_WAIT
          | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_SET_FULL_A2_MONO_WAIT =
            android.view.View.EINK_WAVEFORM_MODE_A2
            |EINK_STATIC_MODE_SET
            |android.view.View.EINK_MONOCHROME_MODE_MONOCHROME
            |android.view.View.EINK_WAIT_MODE_WAIT
          | android.view.View.EINK_UPDATE_MODE_FULL;

    public static final int UPDATE_MODE_PARTIAL_GC4 =
            android.view.View.EINK_WAVEFORM_MODE_GC4
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_PARTIAL_DU_WITH_DITHER =
                      android.view.View.EINK_WAVEFORM_MODE_DU
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL
                    | android.view.View.EINK_DITHER_MODE_DITHER;

    public static final int UPDATE_MODE_FULL_DU_WITH_DITHER =
            android.view.View.EINK_WAVEFORM_MODE_DU
          | android.view.View.EINK_UPDATE_MODE_FULL
          | android.view.View.EINK_DITHER_MODE_DITHER;

    public static final int UPDATE_MODE_PARTIAL_A2_WITH_DITHER =
                      android.view.View.EINK_WAVEFORM_MODE_A2
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL
                    | android.view.View.EINK_DITHER_MODE_DITHER;

    public static final int UPDATE_MODE_PARTIAL_GC4_WITH_DITHER =
            android.view.View.EINK_WAVEFORM_MODE_GC4
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL
                    | android.view.View.EINK_DITHER_MODE_DITHER;

    public static final int UPDATE_MODE_PARTIAL_DU_WITH_MONO =
            android.view.View.EINK_WAVEFORM_MODE_DU
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL
                    | android.view.View.EINK_DITHER_MODE_DITHER
                    | android.view.View.EINK_MONOCHROME_MODE_MONOCHROME;

    public static final int UPDATE_MODE_PARTIAL_A2_WITH_MONO_DITHER =
            android.view.View.EINK_WAVEFORM_MODE_A2
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL
                    | android.view.View.EINK_DITHER_MODE_DITHER
                    | android.view.View.EINK_MONOCHROME_MODE_MONOCHROME;

    public static final int UPDATE_MODE_PARTIAL_GC4_WITH_MONO =
            android.view.View.EINK_WAVEFORM_MODE_GC4
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL
                    | android.view.View.EINK_DITHER_MODE_DITHER
                    | android.view.View.EINK_MONOCHROME_MODE_MONOCHROME;

    public static final int UPDATE_MODE_PARTIAL_GC16 =
                      android.view.View.EINK_WAVEFORM_MODE_GC16
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_PARTIAL_AUTO =
                      android.view.View.EINK_WAVEFORM_MODE_AUTO
                    | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_FULL_GC16 =
                      android.view.View.EINK_WAVEFORM_MODE_GC16
                    | android.view.View.EINK_UPDATE_MODE_FULL;

    public static final int UPDATE_MODE_FULL_GLR16_WAIT =
            android.view.View.EINK_WAVEFORM_MODE_GLR16
            |android.view.View.EINK_WAIT_MODE_WAIT
          | android.view.View.EINK_UPDATE_MODE_FULL;

    public static final int UPDATE_MODE_PARTIAL_GL16 =
            android.view.View.EINK_WAVEFORM_MODE_GL16
          | android.view.View.EINK_UPDATE_MODE_PARTIAL;

    public static final int UPDATE_MODE_FULL_A2 =
                      android.view.View.EINK_WAVEFORM_MODE_A2
                    | android.view.View.EINK_UPDATE_MODE_FULL
                    | android.view.View.EINK_MONOCHROME_MODE_MONOCHROME;

    public static final int UPDATE_MODE_FULL_DU =
                      android.view.View.EINK_WAVEFORM_MODE_DU
                    | android.view.View.EINK_UPDATE_MODE_FULL
                    | android.view.View.EINK_MONOCHROME_MODE_MONOCHROME;

    public static final int UPDATE_MODE_SCREEN = UPDATE_MODE_FULL_GC16;

    private static native int FullRefresh();
    private static native int PartialRefresh(int left, int top, int right, int bottom);
    private static native int RegionalRefresh(int left, int top, int right, int bottom, int waveformMode, int updateMode);
    private static native int SetFBColor(int left, int top, int right, int bottom, int rgb, int microsecond);
    private static native int SetFBColorArray(int left, int top, int right, int bottom, int[] rgbArray, int microsecond);

    public static boolean isEinkHardwareType() {
        return SystemProperties.get("ro.product.hardwareType", "").equals("E60Q20")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60Q30")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60Q50")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60Q60")
                || SystemProperties.get("ro.product.hardwareType", "").equals("ED0Q00")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60QD0")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E70Q30")
                || SystemProperties.get("ro.product.hardwareType", "").equals("EA0Q00")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60QR0")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60QR2");
    }

    public static boolean isEinkHandWritingHardwareType() {
        return SystemProperties.get("ro.product.hardwareType", "").equals("E60Q60")
                || SystemProperties.get("ro.product.hardwareType", "").equals("ED0Q00")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E70Q30")
                || SystemProperties.get("ro.product.hardwareType", "").equals("EA0Q00")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60QR0")
                || SystemProperties.get("ro.product.hardwareType", "").equals("E60QR2");
    }

    public static boolean isHasNavigationBar() {
        return SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60)
        		|| SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QR0)
        		|| SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QR2)
        		|| SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E70Q30);
    }

    public static boolean isEinkUsingLargerUI() {
        return SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60)
                || SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QR0)
                || SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QR2)
                || SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E70Q30)
        	|| SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_EA0Q00);
    }

    public static boolean isEink68HandWritingHardwareType() { // 6.8 inch like E60Q62
        return isEinkHandWritingHardwareType() && isEinkUsingLargerUI();
    }

    public static boolean isHasPhysicalKey() {
        return (!isHasPhysicalBackKey() && !isHasPhysicalHomeKey());
    }

    public static boolean isHasPhysicalBackKey() {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
    }

    public static boolean isHasPhysicalHomeKey() {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
    }
    
	public static boolean hasExternalSDCard() {
		return !SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_EA0Q00);
	}
	
    public static boolean setFullRefresh() {
        int value = FullRefresh();
        if (value != 1) {
            Log.e("RefreshClass", "fullRefresh failed with error = " + value);
            return (false);
        } else {
            return (true);
        }
    }

    public static boolean setPartialRefresh(int left, int top, int right, int bottom) {
        int value = PartialRefresh(left, top, right, bottom);
        if (value != 1) {
                Log.e("ScreenHelper", "patrialRefresh failed with error = " + value);
            return (false);
        } else {
            return (true);
        }
    }

    public static final String TYPE_E60Q20 = "E60Q20";
    public static final String TYPE_E60Q30 = "E60Q30";
    public static final String TYPE_E60Q50 = "E60Q50";
    public static final String TYPE_E60Q60 = "E60Q60";
    public static final String TYPE_E60QR0 = "E60QR0";
    public static final String TYPE_E60QR2 = "E60QR2";
    public static final String TYPE_E60QD0 = "E60QD0";
    public static final String TYPE_E60QH0 = "E60QH0";
    public static final String TYPE_E70Q30 = "E70Q30";
    public static final String TYPE_ED0Q00 = "ED0Q00";
    public static final String TYPE_EA0Q00 = "EA0Q00";

    public static boolean isExternalExtSDStorage() {
        if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QH0)) return true;
        if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60)) return true;
        if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_ED0Q00)) return true;
        if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QR0)) return true;
        if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60QR2)) return true;
        return false;
    }

    public static String getHardwareType(){
    	return SystemProperties.get("ro.product.hardwareType", "");
    }

    public static void setGridBookTitleSize(TextView tv){
    	tv.setTextColor(Color.BLACK);

    	if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60)){ tv.setTextSize(24); tv.setTypeface(Typeface.DEFAULT_BOLD); }
    	else if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_ED0Q00)){ tv.setTextSize(18); } // "W" 2.5mm

    }

    public static void setGridBookCoverTitleSize(TextView tv){
    	tv.setTextColor(Color.BLACK);

    	if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60))	 { tv.setTextSize(18); }
    	else if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_ED0Q00)){ tv.setTextSize(14); }
    }

    public static void setListBookTitleSize(TextView tv_title,TextView tv_filename){ // 3mm , 2mm
    	tv_title.setTextColor(Color.BLACK);
    	tv_filename.setTextColor(Color.BLACK);

    	if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_E60Q60))	 { tv_title.setTextSize(26); tv_filename.setTextSize(20); }
    	else if (SystemProperties.get("ro.product.hardwareType", "").equals(TYPE_ED0Q00)){ tv_title.setTextSize(24); tv_filename.setTextSize(16); }
    }
}
