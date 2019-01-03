package com.semisky.jlradio.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.semisky.jlradio.dao.FMChannelDBManager;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;

public class ISearchAllFMModelImp implements ISearchAllFMModel {
	private Context mContext;
	private static ISearchAllFMModelImp instance;
	private MyHandler mHandler;
	private List<SearchAllFMListener> listeners;

	private static final int MSG_STATE_UNFINISH = 0x01;
	private static final int MSG_STATE_FINISH = 0x02;
	private static final int MSG_STATE_INTERRUPT = 0x03;
	private static final int MSG_STATE_TIMEOUT = 0x04;

	public ISearchAllFMModelImp(Context context) {
		this.mContext = context;
		mHandler = new MyHandler(this);
		listeners = new ArrayList<SearchAllFMListener>(3);// 不要申请过多
	}

	public static ISearchAllFMModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new ISearchAllFMModelImp(context);
		}
		return instance;
	}

	@Override
	public void registerSearchAllFMListener(SearchAllFMListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void unregisterSearchAllFMListener(SearchAllFMListener listener) {
		listeners.remove(listener);
	}

	private static class MyHandler extends Handler {
		private static WeakReference<ISearchAllFMModelImp> mReference;

		public MyHandler(ISearchAllFMModelImp model) {
			mReference = new WeakReference<ISearchAllFMModelImp>(model);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}

			switch (msg.what) {
			case MSG_STATE_UNFINISH:// 进行中
				for (SearchAllFMListener listener : mReference.get().listeners) {
					if (listener != null) {
						listener.notifyObserversSearchAllFMUnFinish(msg.arg1);
					}
				}
				break;
			case MSG_STATE_FINISH:// 完成
				for (SearchAllFMListener listener : mReference.get().listeners) {
					if (listener != null) {
						listener.notifyObserversSearchAllFMFinish();
					}
				}
				break;
			case MSG_STATE_INTERRUPT:// 中断
				for (SearchAllFMListener listener : mReference.get().listeners) {
					if (listener != null) {
						listener.notifyObserversSearchAllFMInterrupt();
					}
				}
				break;
			case MSG_STATE_TIMEOUT:// 超时
				if (RadioStatus.isSearchingFM) {// 为true说明没有正常结束搜索，此时手动停止
					Logger.logD("Search FM timeout");
					RadioStatus.isSearchingFM = false;
					// dismiss dialog
					for (SearchAllFMListener listener : mReference.get().listeners) {
						if (listener != null) {
							listener.notifyObserversSearchAllFMTimeout();
						}
					}
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void searchAllFM() {
		RadioStatus.isSearchingFM = true;
		// 如果RadioReceiver那没有置为false，则说明是用户中断了搜索
		RadioStatus.isSearchingInterrupted = true;
		// 清空列表
		FMChannelDBManager.getInstance(mContext).deleteAllFMChannels();
		// 通知观察者列表清空
		for (SearchAllFMListener listener : listeners) {
			if (listener != null) {
				listener.notifyObserversClearFMList();
			}
		}
		// 开始搜索
		ProtocolUtil.getInstance(mContext).searchAllFM();
		// 30秒后做超时处理
		mHandler.sendEmptyMessageDelayed(MSG_STATE_TIMEOUT,
				Constants.DURATION_SEARCH_TIME_OUT);
	}

	private void removeTimeoutMessage() {
		mHandler.removeMessages(MSG_STATE_TIMEOUT);
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllFMFinish() {
		removeTimeoutMessage();
		// 发送搜索结束消息
		mHandler.obtainMessage(MSG_STATE_FINISH).sendToTarget();
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllFMUnFinish(int frequency) {
		removeTimeoutMessage();
		// 发送搜索进行中消息
		Message msg = mHandler.obtainMessage(MSG_STATE_UNFINISH);
		msg.arg1 = frequency;
		msg.sendToTarget();
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllFMInterrupt() {
		removeTimeoutMessage();
		// 发送搜索中断消息
		mHandler.obtainMessage(MSG_STATE_INTERRUPT).sendToTarget();
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllFMTimeOut() {
		removeTimeoutMessage();
		// 发送搜索超时消息
		mHandler.obtainMessage(MSG_STATE_TIMEOUT).sendToTarget();
	}
}
