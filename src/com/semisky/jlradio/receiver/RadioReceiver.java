package com.semisky.jlradio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semisky.jlradio.R;
import com.semisky.jlradio.RadioActivity;
import com.semisky.jlradio.model.ISwitchFMAMModelImp;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.SettingsUtil;
import com.semisky.jlradio.util.Toaster;

/**
 * 静态广播接收器
 * 
 * @author Anter
 * 
 */
public class RadioReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Constants.ACTION_KEYEVENT_RADIO.equals(action)) {// 实体Radio按钮按下发来广播
			Logger.logD("receive broadcast:KEYCODE_RADIO----Radio实体按钮");
			if (!ProtocolUtil.getInstance(context).isBTTalking()) {// 是否在使用蓝牙打电话，打电话时不可切换
				// 如果RadioActivity没有启动，则做启动操作；如果已经启动了，则该按钮的功能为：切换FM和AM（对应各自最后收听的频点）
				boolean isForeground = AppUtil.isAppForeground(context,
						context.getPackageName());
				if (isForeground) {
					// 切换FM和AM
					if (!SettingsUtil.getInstance()
							.isRadioLatestOpened(context)) {
						Toaster.getInstance().makeText(context,
								R.string.message_open_first);
						return;
					}
					if (RadioStatus.isSearchingFM || RadioStatus.isSearchingAM) {// 如果正在搜索所有频道，不让切换频点类型
						return;
					}
					Logger.logD("RadioActivity is already foreground, switch FM and AM");
					switch (RadioStatus.currentType) {
					case Constants.TYPE_FM:
						ISwitchFMAMModelImp.getInstance(context)
								.switchRadioType(Constants.TYPE_AM, true);
						break;
					case Constants.TYPE_AM:
						ISwitchFMAMModelImp.getInstance(context)
								.switchRadioType(Constants.TYPE_FM, true);
						break;
					default:
						break;
					}
				} else {
					Logger.logD("RadioActivity is not foreground, start RadioActivity");
					// 跳转RadioActivity
					AppUtil.startActivity(context, RadioActivity.class);
				}
			} else {
				Logger.logE("BT calling, please try later!");
			}
		}
	}
}
