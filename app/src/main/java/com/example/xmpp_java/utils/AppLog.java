package com.example.xmpp_java.utils;

import android.util.Log;

public class AppLog {
	public static String TAG = "com.example.xmpp_java";
	
	public static void i(String msg) {
		Log.i(TAG, msg);
	}
	
	public static void d(String msg) {
		Log.d(TAG, msg);
	}
	
	public static void d(String msg, Throwable tr) {
		Log.d(TAG, msg, tr);
	}
	
	public static void e(String msg) {
		Log.e(TAG, msg);
	}
	
	public static void e(String msg, Throwable tr) {
		Log.e(TAG, msg, tr);
	}
}