package com.semisky.jlradio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;

public class ISwitchFMAMModelImp implements ISwitchFMAMModel {
	private Context mContext;
	private static ISwitchFMAMModelImp instance;
	private List<ISwitchFMAMCallback> callbacks;

	public ISwitchFMAMModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<ISwitchFMAMCallback>();
	}

	public static ISwitchFMAMModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new ISwitchFMAMModelImp(context);
		}
		return instance;
	}

	@Override
	public void addISwitchFMAMCallback(ISwitchFMAMCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeISwitchFMAMCallback(ISwitchFMAMCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void switchRadioType(int radioType, boolean resetFragment) {
		for (ISwitchFMAMCallback callback : callbacks) {
			if (callback != null) {
				callback.onSwitchFMAMPrepare(resetFragment);
			}
		}

		switch (radioType) {
		case Constants.TYPE_FM:// 切换到FM
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 正在搜索上下一个强信号台的时候
				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:// 搜索的是FM，切换到FM，不做处理
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchFMToFMWhenSearchNearStrongRadio();
						}
					}
					break;
				case Constants.TYPE_AM:// 搜索的是AM，切换到FM，停止搜索
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchAMToFMWhenSearchNearStrongRadio();
						}
					}
					IRadioPlayModelImp.getInstance(mContext).playRadio(
							Constants.TYPE_FM,
							PreferencesUtil.getInstance()
									.getLatestFMRadioFrequency(mContext));
					break;
				default:
					break;
				}
			} else {// 只是普通的切换
				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchFMToFM();
						}
					}
					break;
				case Constants.TYPE_AM:
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchAMToFM();
						}
					}
					IRadioPlayModelImp.getInstance(mContext).playRadio(
							Constants.TYPE_FM,
							PreferencesUtil.getInstance()
									.getLatestFMRadioFrequency(mContext));
					break;
				default:
					break;
				}
			}
			break;
		case Constants.TYPE_AM:// 切换到AM
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 正在搜索上下一个强信号台的时候
				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:// 搜索的是FM，切换到AM，停止搜索
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchFMToAMWhenSearchNearStrongRadio();
						}
					}
					IRadioPlayModelImp.getInstance(mContext).playRadio(
							Constants.TYPE_AM,
							PreferencesUtil.getInstance()
									.getLatestAMRadioFrequency(mContext));
					break;
				case Constants.TYPE_AM:// 搜索的是AM，切换到AM，不做处理
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchAMToAMWhenSearchNearStrongRadio();
						}
					}
					break;
				default:
					break;
				}
			} else {// 只是普通的切换
				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchFMToAM();
						}
					}
					IRadioPlayModelImp.getInstance(mContext).playRadio(
							Constants.TYPE_AM,
							PreferencesUtil.getInstance()
									.getLatestAMRadioFrequency(mContext));
					break;
				case Constants.TYPE_AM:
					for (ISwitchFMAMCallback callback : callbacks) {
						if (callback != null) {
							callback.beginSwitchAMToAM();
						}
					}
					break;
				default:
					break;
				}
			}
			break;
		default:
			break;
		}
	}
}
