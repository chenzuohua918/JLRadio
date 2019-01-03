package com.semisky.jlradio.model;

import java.util.ArrayList;
import java.util.List;

import com.semisky.jlradio.R;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;
import com.semisky.jlradio.util.Toaster;

import android.content.Context;

public class IRadioPlayModelImp implements IRadioPlayModel {
	private Context mContext;
	private static IRadioPlayModelImp instance;
	private List<IRadioPlayCallback> callbacks;

	public IRadioPlayModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<IRadioPlayCallback>();
	}

	public static IRadioPlayModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new IRadioPlayModelImp(context);
		}
		return instance;
	}

	@Override
	public void addIRadioPlayCallback(IRadioPlayCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeIRadioPlayCallback(IRadioPlayCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void playRadio(int targetType, int frequency) {
		switch (targetType) {
		case Constants.TYPE_FM:
			if (!AppUtil.inFMFrequencyRange(frequency)) {
				Toaster.getInstance().makeText(mContext,
						R.string.invalid_fm_frequency);
				return;
			}
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;

				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, Constants.TYPE_FM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(frequency);
					break;
				case Constants.TYPE_AM:
					PreferencesUtil.getInstance().setLatestRadioType(mContext,
							Constants.TYPE_FM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.searchNearShowingFrequency,
							Constants.TYPE_AM);
					RadioStatus.currentType = Constants.TYPE_FM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(
							RadioStatus.currentFrequency);
					break;
				default:
					break;
				}
			} else {
				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, Constants.TYPE_FM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(frequency);
					break;
				case Constants.TYPE_AM:
					PreferencesUtil.getInstance().setLatestRadioType(mContext,
							Constants.TYPE_FM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.currentFrequency,
							Constants.TYPE_AM);
					RadioStatus.currentType = Constants.TYPE_FM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).fmSearch(
							RadioStatus.currentFrequency);
					break;
				default:
					break;
				}
			}
			for (IRadioPlayCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioPlay(Constants.TYPE_FM, frequency);
				}
			}
			break;
		case Constants.TYPE_AM:
			if (!AppUtil.inAMFrequencyRange(frequency)) {
				Toaster.getInstance().makeText(mContext,
						R.string.invalid_am_frequency);
				return;
			}
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;

				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:
					PreferencesUtil.getInstance().setLatestRadioType(mContext,
							Constants.TYPE_AM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.searchNearShowingFrequency,
							Constants.TYPE_FM);
					RadioStatus.currentType = Constants.TYPE_AM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(
							RadioStatus.currentFrequency);
					break;
				case Constants.TYPE_AM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, Constants.TYPE_AM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(frequency);
					break;
				default:
					break;
				}
			} else {
				switch (RadioStatus.currentType) {
				case Constants.TYPE_FM:
					PreferencesUtil.getInstance().setLatestRadioType(mContext,
							Constants.TYPE_AM);
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, RadioStatus.currentFrequency,
							Constants.TYPE_FM);
					RadioStatus.currentType = Constants.TYPE_AM;
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(
							RadioStatus.currentFrequency);
					break;
				case Constants.TYPE_AM:
					PreferencesUtil.getInstance().setLatestRadioFrequency(
							mContext, frequency, Constants.TYPE_AM);
					RadioStatus.currentFrequency = frequency;
					ProtocolUtil.getInstance(mContext).amSearch(frequency);
					break;
				default:
					break;
				}
			}
			for (IRadioPlayCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioPlay(Constants.TYPE_AM, frequency);
				}
			}
			break;
		default:
			break;
		}
	}

}
