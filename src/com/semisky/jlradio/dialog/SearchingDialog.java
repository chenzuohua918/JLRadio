package com.semisky.jlradio.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.semisky.jlradio.R;
import com.semisky.jlradio.model.ISearchAllAMModelImp;
import com.semisky.jlradio.model.ISearchAllFMModelImp;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.RadioStatus;

public class SearchingDialog extends Dialog implements
		android.view.View.OnClickListener {
	private TextView tv_title, tv_type, tv_frequency, tv_unit;
	private Button btn_cancel;
	private int searchType;// 搜索FM还是AM

	private OnSearchingDialogListener onSearchingDialogListener;

	public interface OnSearchingDialogListener {
		void onSearchingDialogDismiss();
	}

	public void setOnSearchingDialogListener(
			OnSearchingDialogListener onSearchingDialogListener) {
		this.onSearchingDialogListener = onSearchingDialogListener;
	}

	public SearchingDialog(Context context, int titleResId, int styleResId,
			int searchType) {
		super(context, styleResId);
		this.searchType = searchType;
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.dialog_searching);

		WindowManager.LayoutParams lParams = getWindow().getAttributes();
		lParams.width = Constants.DIALOG_WIDTH;
		lParams.height = Constants.DIALOG_HEIGHT;
		lParams.dimAmount = Constants.DIALOG_DIMAMOUNT;
		getWindow().setAttributes(lParams);
		getWindow().addFlags(LayoutParams.FLAG_DIM_BEHIND);

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_type = (TextView) findViewById(R.id.tv_type);
		tv_frequency = (TextView) findViewById(R.id.tv_frequency);
		tv_unit = (TextView) findViewById(R.id.tv_unit);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);

		tv_title.setText(titleResId);

		btn_cancel.setOnClickListener(this);
	}

	public void setType(int type) {
		tv_type.setText(type);
	}

	public void setFrequency(int frequency) {
		if (frequency == -1) {
			tv_frequency.setText("");
		} else {
			switch (searchType) {
			case Constants.TYPE_FM:
				tv_frequency.setText(AppUtil.formatFloatFrequency(frequency
						/ Constants.FM_MULTIPLE));
				break;
			case Constants.TYPE_AM:
				tv_frequency.setText(String.valueOf(frequency));
				break;
			default:
				break;
			}
		}
	}

	public void setFrequency(float frequency) {
		if (frequency == 0.0f) {
			tv_frequency.setText("");
		} else {
			tv_frequency.setText(String.valueOf(frequency));
		}
	}

	public void setUnit(int unit) {
		tv_unit.setText(unit);
	}

	@Override
	public void show() {
		super.show();
		// 开始搜索
		switch (searchType) {
		case Constants.TYPE_FM:
			ISearchAllFMModelImp.getInstance(getContext()).searchAllFM();
			break;
		case Constants.TYPE_AM:
			ISearchAllAMModelImp.getInstance(getContext()).searchAllAM();
			break;
		default:
			break;
		}
	}

	@Override
	public void dismiss() {
		if (onSearchingDialogListener != null) {
			onSearchingDialogListener.onSearchingDialogDismiss();
		}

		switch (searchType) {
		case Constants.TYPE_FM:
			RadioStatus.isSearchingFM = false;
			if (RadioStatus.isSearchingInterrupted) {// 没有在RadioInfoReceiver被置false，说明搜索所有FM被中断了
				Logger.logD("SearchingDialog-----------------------search all FM interrupted");
				ISearchAllFMModelImp.getInstance(getContext())
						.sendMsgToNotifyObserversSearchAllFMTimeOut();
			}
			break;
		case Constants.TYPE_AM:
			RadioStatus.isSearchingAM = false;
			if (RadioStatus.isSearchingInterrupted) {// 没有在RadioInfoReceiver被置false，说明搜索所有AM被中断了
				Logger.logD("SearchingDialog-----------------------search all AM interrupted");
				ISearchAllAMModelImp.getInstance(getContext())
						.sendMsgToNotifyObserversSearchAllAMInterrupt();
			}
			break;
		default:
			break;
		}

		super.dismiss();
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}
}
