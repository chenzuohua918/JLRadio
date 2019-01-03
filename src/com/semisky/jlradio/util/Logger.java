package com.semisky.jlradio.util;

import android.util.Log;

public class Logger {
	public static final String TAG = "JLRadio";
	public static final boolean DEBUG = true;

	public static void logD(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void logI(String msg) {
		if (DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void logE(String msg) {
		if (DEBUG) {
			Log.e(TAG, msg);
		}
	}

}
