package com.semisky.jlradio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.SettingsUtil;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;

/**
 * 收音机开关Model实现类
 * 
 * @author Anter
 * 
 */
public class IRadioSwitchModelImp implements IRadioSwitchModel {
	private Context mContext;
	private static IRadioSwitchModelImp instance;
	private List<IRadioSwitchCallback> callbacks;

	public IRadioSwitchModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<IRadioSwitchCallback>();
	}

	public static synchronized IRadioSwitchModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new IRadioSwitchModelImp(context);
		}
		return instance;
	}

	@Override
	public void addIRadioSwitchCallback(IRadioSwitchCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeIRadioSwitchCallback(IRadioSwitchCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void switchOnOff(boolean on_off) {
		if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 正在搜索上下一个强信号台
			Logger.logD("searching near strong radio, click switcher to "
					+ (on_off ? "open" : "close"));
			switch (RadioStatus.currentType) {
			case Constants.TYPE_FM:
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
				PreferencesUtil.getInstance().setLatestRadioFrequency(mContext,
						RadioStatus.currentFrequency, Constants.TYPE_FM);
				ProtocolUtil.getInstance(mContext).fmSearch(
						RadioStatus.currentFrequency);
				break;
			case Constants.TYPE_AM:
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
				PreferencesUtil.getInstance().setLatestRadioFrequency(mContext,
						RadioStatus.currentFrequency, Constants.TYPE_AM);
				ProtocolUtil.getInstance(mContext).amSearch(
						RadioStatus.currentFrequency);
				break;
			default:
				break;
			}
		}

		if (on_off) {// 打开
			Logger.logD("switch on Radio");
			// 保存开关状态（只在主动操作开关时记住）
			SettingsUtil.getInstance().setRadioPlayState(mContext, true);
			for (IRadioSwitchCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioSwitchOn();
				}
			}
		} else {// 关闭
			Logger.logD("switch off Radio");
			// 保存开关状态（只在主动操作开关时记住）
			SettingsUtil.getInstance().setRadioPlayState(mContext, false);
			for (IRadioSwitchCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioSwitchOff();
				}
			}
		}
	}

}
