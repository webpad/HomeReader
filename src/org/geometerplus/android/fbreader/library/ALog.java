package org.geometerplus.android.fbreader.library;

/**
 *  Artis' logcat switch for debug
 */

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ALog {
    static boolean isDebug = true;

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
    }

    public static void debug(Object... arr) {
        if(isDebug)
        {
        StackTraceElement call = Thread.currentThread().getStackTrace()[3];
        String className = call.getClassName();
        className = className.substring(className.lastIndexOf('.') + 1);
        /*android.util.Log.v("_DEBUG_", call.getLineNumber() + ": "
        + className + "." + call.getMethodName() + " "
        + java.util.Arrays.deepToString(arr));*/

        Log.d("_DEBUG_", call.getLineNumber() + ": "
        + className + "." + call.getMethodName() + " "
        + java.util.Arrays.deepToString(arr));
       
    }
    }
    
    public static void appendLog(String text)
    {       
       File logFile = new File("sdcard/log.txt");
       if (!logFile.exists())
       {
          try
          {
             logFile.createNewFile();
          } 
          catch (IOException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
       try
       {
          //BufferedWriter for performance, true to set append to file flag
          BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
          buf.append(text);
          buf.newLine();
          buf.close();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }

}
