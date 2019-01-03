package com.semisky.jlradio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.jlradio.R;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.SettingsUtil;
import com.semisky.jlradio.util.Toaster;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;

/**
 * 收音机搜索上下一个强信号台Model实现类
 * 
 * @author Anter
 * 
 */
public class ISearchNearStrongRadioModelImp implements
		ISearchNearStrongRadioModel {
	private Context mContext;
	private static ISearchNearStrongRadioModelImp instance;
	private List<ISearchNearStrongRadioCallback> callbacks;

	public ISearchNearStrongRadioModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<ISearchNearStrongRadioCallback>();
	}

	public static synchronized ISearchNearStrongRadioModelImp getInstance(
			Context context) {
		if (instance == null) {
			instance = new ISearchNearStrongRadioModelImp(context);
		}
		return instance;
	}

	@Override
	public void addISearchNearStrongRadioCallback(
			ISearchNearStrongRadioCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeISearchNearStrongRadioCallback(
			ISearchNearStrongRadioCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void searchPreviousStrongRadio() {
		if (RadioStatus.hasFocus && !RadioStatus.isSearchingFM
				&& !RadioStatus.isSearchingAM) {// 符合这些条件才能搜索下一个强信号台
			if (!SettingsUtil.getInstance().isRadioLatestOpened(mContext)) {// 开关没打开
				Toaster.getInstance().makeText(
						mContext.getApplicationContext(),
						R.string.message_open_first);
				return;
			}
			switch (RadioStatus.currentType) {
			case Constants.TYPE_FM:
				if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强FM台，停止
					Logger.logD("searching near strong FM radio, click previous strong radio button to stop");
					RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							RadioStatus.currentFrequency, Constants.TYPE_FM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									RadioStatus.currentFrequency);
						}
					}
					break;
				}

				if (RadioStatus.currentFrequency == Constants.FMMIN) {// 已是最小FM频点
					// 从最大FM频点开始
					RadioStatus.currentFrequency = Constants.FMMAX;
					RadioStatus.searchNearShowingFrequency = Constants.FMMAX;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							Constants.FMMAX, Constants.TYPE_FM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									Constants.FMMAX);
						}
					}
				} else {// 还没到最小FM频点，继续搜索
					RadioStatus.searchNearShowingFrequency = RadioStatus.currentFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.PREVIOUS;
					ProtocolUtil.getInstance(mContext).fm_searchPrev(
							RadioStatus.currentFrequency);
				}
				break;
			case Constants.TYPE_AM:
				if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强AM台，停止
					Logger.logD("searching near strong AM radio, click previous strong radio button to stop");
					RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							RadioStatus.currentFrequency, Constants.TYPE_AM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									RadioStatus.currentFrequency);
						}
					}
					break;
				}

				if (RadioStatus.currentFrequency == Constants.AMMIN) {// 已是最小AM频点
					// 从最大AM频点开始
					RadioStatus.currentFrequency = Constants.AMMAX;
					RadioStatus.searchNearShowingFrequency = Constants.AMMAX;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							Constants.AMMAX, Constants.TYPE_AM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									Constants.AMMAX);
						}
					}
				} else {// 还没到最小AM频点，继续搜索
					RadioStatus.searchNearShowingFrequency = RadioStatus.currentFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.PREVIOUS;
					ProtocolUtil.getInstance(mContext).am_searchPrev(
							RadioStatus.currentFrequency);
				}
				break;
			default:
				break;
			}
			// 搜索结果会被RadioReceiver接收到，同时发送给各个Fragment进行界面更新
		}
	}

	@Override
	public void searchNextStrongRadio() {
		if (RadioStatus.hasFocus && !RadioStatus.isSearchingFM
				&& !RadioStatus.isSearchingAM) {// 符合这些条件才能搜索下一个强信号台
			if (!SettingsUtil.getInstance().isRadioLatestOpened(mContext)) {// 开关没打开
				Toaster.getInstance().makeText(
						mContext.getApplicationContext(),
						R.string.message_open_first);
				return;
			}
			switch (RadioStatus.currentType) {
			case Constants.TYPE_FM:
				if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强FM台，停止
					Logger.logD("Searching near strong FM radio, click next strong radio button to stop");
					RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							RadioStatus.currentFrequency, Constants.TYPE_FM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									RadioStatus.currentFrequency);
						}
					}
					break;
				}

				if (RadioStatus.currentFrequency == Constants.FMMAX) {// 已是最大FM频点
					// 从最小FM频点开始
					RadioStatus.currentFrequency = Constants.FMMIN;
					RadioStatus.searchNearShowingFrequency = Constants.FMMIN;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							Constants.FMMIN, Constants.TYPE_FM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									Constants.FMMIN);
						}
					}
				} else {// 还没到最大FM频点，继续搜索
					RadioStatus.searchNearShowingFrequency = RadioStatus.currentFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEXT;
					ProtocolUtil.getInstance(mContext).fm_searchNext(
							RadioStatus.currentFrequency);
				}
				break;
			case Constants.TYPE_AM:
				if (RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 如果正在搜索上下一个强AM台，停止
					Logger.logD("Searching near strong AM radio, click next strong radio button to stop");
					RadioStatus.currentFrequency = RadioStatus.searchNearShowingFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							RadioStatus.currentFrequency, Constants.TYPE_AM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									RadioStatus.currentFrequency);
						}
					}
					break;
				}

				if (RadioStatus.currentFrequency == Constants.AMMAX) {// 已是最大AM频点
					// 从最小AM频点开始
					RadioStatus.currentFrequency = Constants.AMMIN;
					RadioStatus.searchNearShowingFrequency = Constants.AMMIN;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEITHER;
					PreferencesUtil.getInstance().saveLatestRadioInfo(mContext,
							Constants.AMMIN, Constants.TYPE_AM);
					// 通知RadioFragment和RadioActivity实时刷新频点显示
					for (ISearchNearStrongRadioCallback callback : callbacks) {
						if (callback != null) {
							callback.onSearchNearStrongRadioResult(true,
									Constants.AMMIN);
						}
					}
				} else {// 还没到最大AM频点，继续搜索
					RadioStatus.searchNearShowingFrequency = RadioStatus.currentFrequency;
					RadioStatus.searchNearState = SearchNearStrongChannel.NEXT;
					ProtocolUtil.getInstance(mContext).am_searchNext(
							RadioStatus.currentFrequency);
				}
				break;
			default:
				break;
			}
			// 搜索结果会被RadioReceiver接收到，同时发送给各个Fragment进行界面更新
		}
	}

}
