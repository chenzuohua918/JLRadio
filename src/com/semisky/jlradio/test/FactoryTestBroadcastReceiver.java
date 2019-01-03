package com.semisky.jlradio.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.jlradio.model.IRadioAdjustmentModelImp;
import com.semisky.jlradio.model.IRadioPlayModelImp;
import com.semisky.jlradio.model.ISearchAllAMModelImp;
import com.semisky.jlradio.model.ISearchAllFMModelImp;
import com.semisky.jlradio.model.ISearchNearStrongRadioModelImp;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.RadioStatus;

/**
 * 自动化产线测试广播接收器
 * 
 * @author Anter
 * 
 */
public class FactoryTestBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (FactoryTestConstants.SEMISKEY_RADIOCOLLECT.equals(action)) {// 设定电台收藏

		} else if (FactoryTestConstants.SEMISKEY_HOME.equals(action)) {// 主页

		} else if (FactoryTestConstants.SEMISKEY_BACK.equals(action)) {// 返回

		} else if (FactoryTestConstants.SEMISKEY_RADIODOWN.equals(action)) {// 步进下一频道（往右）
			IRadioAdjustmentModelImp.getInstance(context).adjustmentStepNext();
		} else if (FactoryTestConstants.SEMISKEY_RADIOUP.equals(action)) {// 步进上一频道（往左）
			IRadioAdjustmentModelImp.getInstance(context)
					.adjustmentStepPrevious();
		} else if (FactoryTestConstants.SEMISKEY_RADIONEXT.equals(action)) {// 搜索下一台
			ISearchNearStrongRadioModelImp.getInstance(context)
					.searchNextStrongRadio();
		} else if (FactoryTestConstants.SEMISKEY_RADIOPREVIOUS.equals(action)) {// 搜索上一台
			ISearchNearStrongRadioModelImp.getInstance(context)
					.searchPreviousStrongRadio();
		} else if (FactoryTestConstants.SEMISKEY_RADIOAM.equals(action)) {// 播放AM频点
			int frequency = intent.getIntExtra("AM_CHANNEL", Constants.AMMIN);
			IRadioPlayModelImp.getInstance(context).playRadio(
					Constants.TYPE_AM, frequency);
		} else if (FactoryTestConstants.SEMISKEY_RADIOFM.equals(action)) {// 播放FM频点
			int frequency = intent.getIntExtra("FM_CHANNEL", Constants.FMMIN);
			IRadioPlayModelImp.getInstance(context).playRadio(
					Constants.TYPE_FM, frequency);
		} else if (FactoryTestConstants.SEMISKEY_RADIOSEARCH.equals(action)) {// 自动搜索电台
			switch (RadioStatus.currentType) {
			case Constants.TYPE_FM:
				ISearchAllFMModelImp.getInstance(context).searchAllFM();
				break;
			case Constants.TYPE_AM:
				ISearchAllAMModelImp.getInstance(context).searchAllAM();
				break;
			default:
				break;
			}
		}
	}

}
