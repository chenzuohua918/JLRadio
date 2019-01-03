package com.semisky.jlradio.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.semisky.jlradio.R;
import com.semisky.jlradio.util.Constants;

public class DialogManager {
	private static DialogManager instance;
	private Dialog mDialog;

	public static synchronized DialogManager getInstance() {
		if (instance == null) {
			instance = new DialogManager();
		}
		return instance;
	}

	/**
	 * 确认、取消双按钮弹框
	 * 
	 * @param context
	 * @param titleResId
	 *            标题
	 * @param messageResId
	 *            提示内容
	 * @param listener
	 *            回调接口
	 */
	public void showYesOrNoDialog(Context context, int titleResId,
			int messageResId, final OnYesOrNoDialogListener listener) {
		if (mDialog == null) {
			mDialog = new Dialog(context, R.style.SearchingDialog);
			// 点击框外消失
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mDialog = null;
					if (listener != null) {
						listener.onYesOrNoDialogDismiss();
					}
				}
			});
		} else if (mDialog.isShowing()) {
			return;
		}
		mDialog.setContentView(R.layout.dialog_yesorno);
		mDialog.show();
		TextView tv_title = (TextView) mDialog.findViewById(R.id.tv_title);
		tv_title.setText(titleResId);
		TextView tv_message = (TextView) mDialog.findViewById(R.id.tv_message);
		tv_message.setText(messageResId);
		Button btn_confirm = (Button) mDialog.findViewById(R.id.btn_confirm);
		btn_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				if (listener != null) {
					listener.onYesOrNoDialogConfirm(v);
				}
			}
		});
		Button btn_cancel = (Button) mDialog.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				if (listener != null) {
					listener.onYesOrNoDialogCancel(v);
				}
			}
		});
		WindowManager.LayoutParams lParams = mDialog.getWindow()
				.getAttributes();
		lParams.width = Constants.DIALOG_WIDTH;
		lParams.height = Constants.DIALOG_HEIGHT;
		lParams.dimAmount = Constants.DIALOG_DIMAMOUNT;
		mDialog.getWindow().setAttributes(lParams);
	}

	/**
	 * 确认单按钮弹框
	 * 
	 * @param context
	 * @param titleResId
	 *            标题
	 * @param messageResId
	 *            提示内容
	 */
	public void showYesDialog(Context context, int titleResId, int messageResId) {
		if (mDialog == null) {
			mDialog = new Dialog(context, R.style.SearchingDialog);
			// 点击框外消失
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mDialog = null;
				}
			});
		} else if (mDialog.isShowing()) {
			return;
		}
		mDialog.setContentView(R.layout.dialog_yes);
		mDialog.show();
		TextView tv_title = (TextView) mDialog.findViewById(R.id.tv_title);
		tv_title.setText(titleResId);
		TextView tv_message = (TextView) mDialog.findViewById(R.id.tv_message);
		tv_message.setText(messageResId);
		Button btn_confirm = (Button) mDialog.findViewById(R.id.btn_confirm);
		btn_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		WindowManager.LayoutParams lParams = mDialog.getWindow()
				.getAttributes();
		lParams.width = Constants.DIALOG_WIDTH;
		lParams.height = Constants.DIALOG_HEIGHT;
		lParams.dimAmount = Constants.DIALOG_DIMAMOUNT;
		mDialog.getWindow().setAttributes(lParams);
	}
}
