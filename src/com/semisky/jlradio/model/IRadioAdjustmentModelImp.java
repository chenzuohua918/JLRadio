package com.semisky.jlradio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;

/**
 * 频点微调Model实现类
 * 
 * @author Anter
 * 
 */
public class IRadioAdjustmentModelImp implements IRadioAdjustmentModel {
	private Context mContext;
	private static IRadioAdjustmentModelImp instance;
	private List<IRadioAdjustmentCallback> callbacks;

	public IRadioAdjustmentModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<IRadioAdjustmentCallback>();
	}

	public static IRadioAdjustmentModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new IRadioAdjustmentModelImp(context);
		}
		return instance;
	}

	@Override
	public void addIRadioAdjustmentCallback(IRadioAdjustmentCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeIRadioAdjustmentCallback(IRadioAdjustmentCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void adjustmentStepPrevious() {// 步退一步
		if (RadioStatus.hasFocus && !RadioStatus.isSearchingFM
				&& !RadioStatus.isSearchingAM) {
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果是正在搜索上下一个强信号台
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				// 记录当前滚动到的频点
				RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
			}
			switch (RadioStatus.currentType) {
			case Constants.TYPE_FM:
				if (RadioStatus.currentFrequency <= Constants.FMMIN) {
					// 循环
					RadioStatus.currentFrequency = Constants.FMMAX;
				} else {
					// 步退0.1
					RadioStatus.currentFrequency -= Constants.FM_STEP;
				}
				// search当前频率
				ProtocolUtil.getInstance(mContext).fmSearch(
						RadioStatus.currentFrequency);
				break;
			case Constants.TYPE_AM:
				if (RadioStatus.currentFrequency <= Constants.AMMIN) {
					// 循环
					RadioStatus.currentFrequency = Constants.AMMAX;
				} else {
					// 步退9
					RadioStatus.currentFrequency -= Constants.AM_STEP;
				}
				// search当前频率
				ProtocolUtil.getInstance(mContext).amSearch(
						RadioStatus.currentFrequency);
				break;
			default:
				break;
			}
			// 保存
			PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
					RadioStatus.currentFrequency, RadioStatus.currentType);
			// 通知Activity,Fragment更新UI
			for (IRadioAdjustmentCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioAdjustmentStepPrevious();
				}
			}
		}
	}

	@Override
	public void adjustmentStepNext() {// 步进一步
		if (RadioStatus.hasFocus && !RadioStatus.isSearchingFM
				&& !RadioStatus.isSearchingAM) {
			if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果是正在搜索上下一个强信号台
				// 如果正在搜索上下一个强信号台时，播放频点会中断搜索，而且不会发出65535结束码，所以要自己重置变量
				RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
				// 记录当前滚动到的频点
				RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
			}
			switch (RadioStatus.currentType) {
			case Constants.TYPE_FM:
				if (RadioStatus.currentFrequency >= Constants.FMMAX) {
					// 循环
					RadioStatus.currentFrequency = Constants.FMMIN;
				} else {
					// 步进0.1
					RadioStatus.currentFrequency += Constants.FM_STEP;
				}
				// search当前频率
				ProtocolUtil.getInstance(mContext).fmSearch(
						RadioStatus.currentFrequency);
				break;
			case Constants.TYPE_AM:
				if (RadioStatus.currentFrequency >= Constants.AMMAX) {
					// 循环
					RadioStatus.currentFrequency = Constants.AMMIN;
				} else {
					// 步进9
					RadioStatus.currentFrequency += Constants.AM_STEP;
				}
				// search当前频率
				ProtocolUtil.getInstance(mContext).amSearch(
						RadioStatus.currentFrequency);
				break;
			default:
				break;
			}
			// 保存
			PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
					RadioStatus.currentFrequency, RadioStatus.currentType);
			// 通知Activity,Fragment更新UI
			for (IRadioAdjustmentCallback callback : callbacks) {
				if (callback != null) {
					callback.onRadioAdjustmentStepNext();
				}
			}
		}
	}

}
