package com.semisky.jlradio.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.semisky.jlradio.dao.CollectChannelDBManager;

public class IRadioCollectModelImp implements IRadioCollectModel {
	private Context mContext;
	private static IRadioCollectModelImp instance;
	private List<IRadioCollectCallback> callbacks;

	public IRadioCollectModelImp(Context context) {
		this.mContext = context;
		callbacks = new ArrayList<IRadioCollectCallback>();
	}

	public static IRadioCollectModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new IRadioCollectModelImp(context);
		}
		return instance;
	}

	@Override
	public void addIRadioCollectCallback(IRadioCollectCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public void removeIRadioCollectCallback(IRadioCollectCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public void collectRadioOrNot(int radioType, int frequency) {
		if (CollectChannelDBManager.getInstance(mContext).isChannelInDB(
				frequency)) {// 已经收藏
			// 取消收藏
			disCollectRadio(frequency);
		} else {// 未收藏
			// 加入收藏
			collectRadio(radioType, frequency);
		}
	}

	@Override
	public void collectRadio(int radioType, int frequency) {
		CollectChannelDBManager.getInstance(mContext).insertCollectChannel(
				frequency, 0, radioType);
		for (IRadioCollectCallback callback : callbacks) {
			if (callback != null) {
				callback.onRadioCollected(radioType, frequency);
			}
		}
	}

	@Override
	public void disCollectRadio(int frequency) {
		CollectChannelDBManager.getInstance(mContext).deleteCollectChannel(
				frequency);
		for (IRadioCollectCallback callback : callbacks) {
			if (callback != null) {
				callback.onRadioUnCollected(frequency);
			}
		}
	}

	@Override
	public void deleteAllCollectRadio() {
		if (CollectChannelDBManager.getInstance(mContext).hasCollectChannels()) {
			CollectChannelDBManager.getInstance(mContext)
					.deleteAllCollectChannels();
			for (IRadioCollectCallback callback : callbacks) {
				if (callback != null) {
					callback.onAllCollectRadioDelete();
				}
			}
		}
	}

}
