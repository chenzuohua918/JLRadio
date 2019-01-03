package com.semisky.jlradio.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.semisky.jlradio.dao.AMChannelDBManager;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;

public class ISearchAllAMModelImp implements ISearchAllAMModel {
	private Context mContext;
	private static ISearchAllAMModelImp instance;
	private MyHandler mHandler;
	private List<SearchAllAMListener> listeners;

	private static final int MSG_STATE_UNFINISH = 0x01;
	private static final int MSG_STATE_FINISH = 0x02;
	private static final int MSG_STATE_INTERRUPT = 0x03;
	private static final int MSG_STATE_TIMEOUT = 0x04;

	public ISearchAllAMModelImp(Context context) {
		this.mContext = context;
		mHandler = new MyHandler(this);
		listeners = new ArrayList<SearchAllAMListener>(3);// 不要申请过多
	}

	public static ISearchAllAMModelImp getInstance(Context context) {
		if (instance == null) {
			instance = new ISearchAllAMModelImp(context);
		}
		return instance;
	}

	@Override
	public void registerSearchAllAMListener(SearchAllAMListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void unregisterSearchAllAMListener(SearchAllAMListener listener) {
		listeners.remove(listener);
	}

	private static class MyHandler extends Handler {
		private static WeakReference<ISearchAllAMModelImp> mReference;

		public MyHandler(ISearchAllAMModelImp model) {
			mReference = new WeakReference<ISearchAllAMModelImp>(model);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}

			switch (msg.what) {
			case MSG_STATE_UNFINISH:// 进行中
				for (SearchAllAMListener listener : mReference.get().listeners) {
					if (listener != null) {
						listener.notifyObserversSearchAllAMUnFinish(msg.arg1);
					}
				}
				break;
			case MSG_STATE_FINISH:// 完成
				for (SearchAllAMListener listener : mReference.get().listeners) {
					if (listener != null) {
						listener.notifyObserversSearchAllAMFinish();
					}
				}
				break;
			case MSG_STATE_INTERRUPT:// 中断
				for (SearchAllAMListener listener : mReference.get().listeners) {
					if (listener != null) {
						listener.notifyObserversSearchAllAMInterrupt();
					}
				}
				break;
			case MSG_STATE_TIMEOUT:// 超时
				if (RadioStatus.isSearchingAM) {// 为true说明没有正常结束搜索，此时手动停止
					Logger.logD("Search AM timeout");
					RadioStatus.isSearchingAM = false;
					// dismiss dialog
					for (SearchAllAMListener listener : mReference.get().listeners) {
						if (listener != null) {
							listener.notifyObserversSearchAllAMTimeout();
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
	public void searchAllAM() {
		RadioStatus.isSearchingAM = true;
		// 如果RadioReceiver那没有置为false，则说明是用户中断了搜索
		RadioStatus.isSearchingInterrupted = true;
		// 清空列表
		AMChannelDBManager.getInstance(mContext).deleteAllAMChannels();
		// 通知观察者列表清空
		for (SearchAllAMListener listener : listeners) {
			if (listener != null) {
				listener.notifyObserversClearAMList();
			}
		}
		// 开始搜索
		ProtocolUtil.getInstance(mContext).searchAllAM();
		// 30秒后做超时处理
		mHandler.sendEmptyMessageDelayed(MSG_STATE_TIMEOUT,
				Constants.DURATION_SEARCH_TIME_OUT);
	}

	private void removeTimeoutMessage() {
		mHandler.removeMessages(MSG_STATE_TIMEOUT);
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllAMFinish() {
		removeTimeoutMessage();
		// 发送搜索结束消息
		mHandler.obtainMessage(MSG_STATE_FINISH).sendToTarget();
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllAMUnFinish(int frequency) {
		removeTimeoutMessage();
		// 发送搜索进行中消息
		Message msg = mHandler.obtainMessage(MSG_STATE_UNFINISH);
		msg.arg1 = frequency;
		msg.sendToTarget();
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllAMInterrupt() {
		removeTimeoutMessage();
		// 发送搜索中断消息
		mHandler.obtainMessage(MSG_STATE_INTERRUPT).sendToTarget();
	}

	@Override
	public void sendMsgToNotifyObserversSearchAllAMTimeOut() {
		removeTimeoutMessage();
		// 发送搜索超时消息
		mHandler.obtainMessage(MSG_STATE_TIMEOUT).sendToTarget();
	}

}
