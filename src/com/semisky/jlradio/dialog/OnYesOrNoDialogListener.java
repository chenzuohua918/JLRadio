package com.semisky.jlradio.dialog;

import android.view.View;

public interface OnYesOrNoDialogListener {
	// 确认
	void onYesOrNoDialogConfirm(View v);

	// 取消
	void onYesOrNoDialogCancel(View v);
	
	// 关闭
	void onYesOrNoDialogDismiss();
}
