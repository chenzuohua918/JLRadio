package com.semisky.jlradio.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;

import com.semisky.jlradio.R;

public class Toaster {
	private static Toaster instance;
	private Toast mToast;
	private static boolean OPEN_TOAST = true;
	
	public static Toaster getInstance() {
		if (instance == null) {
			instance = new Toaster();
		}
		return instance;
	}

	public void makeText(Context context, String text) {
		if (OPEN_TOAST) {
			if (mToast != null) {
				mToast.cancel();
			}
			
			Button button = (Button) LayoutInflater.from(context).inflate(
					R.layout.layout_toast, null);
			button.setText(text);
			mToast = new Toast(context);
			mToast.setDuration(Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
			mToast.setView(button);
			mToast.show();
		}
	}

	public void makeText(Context context, int resId) {
		if (OPEN_TOAST) {
			if (mToast != null) {
				mToast.cancel();
			}
			
			Button button = (Button) LayoutInflater.from(context).inflate(
					R.layout.layout_toast, null);
			button.setText(resId);
			mToast = new Toast(context);
			mToast.setDuration(Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
			mToast.setView(button);
			mToast.show();
		}
	}
}
