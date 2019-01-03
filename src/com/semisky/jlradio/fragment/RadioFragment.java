package com.semisky.jlradio.fragment;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.semisky.jlradio.R;
import com.semisky.jlradio.dao.CollectChannelDBManager;
import com.semisky.jlradio.model.IRadioAdjustmentCallback;
import com.semisky.jlradio.model.IRadioAdjustmentModel;
import com.semisky.jlradio.model.IRadioAdjustmentModelImp;
import com.semisky.jlradio.model.IRadioCollectCallback;
import com.semisky.jlradio.model.IRadioCollectModel;
import com.semisky.jlradio.model.IRadioCollectModelImp;
import com.semisky.jlradio.model.IRadioInfoCallback;
import com.semisky.jlradio.model.IRadioPlayCallback;
import com.semisky.jlradio.model.IRadioPlayModel;
import com.semisky.jlradio.model.IRadioPlayModelImp;
import com.semisky.jlradio.model.ISearchNearStrongRadioCallback;
import com.semisky.jlradio.model.ISearchNearStrongRadioModel;
import com.semisky.jlradio.model.ISearchNearStrongRadioModelImp;
import com.semisky.jlradio.model.RadioInfoReceiver;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.PreferencesUtil.PreferencesManager;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.view.CollectStar;

public class RadioFragment extends Fragment implements OnTouchListener,
		OnClickListener, IRadioAdjustmentCallback, IRadioCollectCallback,
		ISearchNearStrongRadioCallback, IRadioInfoCallback, IRadioPlayCallback {
	private View radioView;
	private CollectStar iv_collected;
	private TextView tv_type, tv_frequecy, tv_unit;
	private ImageButton btn_last_radio, btn_sub_channel, btn_collect,
			btn_plus_channel, btn_next_radio;
	private CollectChannelDBManager mChannelDBManager;
	private PreferencesManager mPreferencesManager = PreferencesUtil
			.getInstance();
	private long frequency_adjust_delayMillis = 150;
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;
	private IRadioAdjustmentModel mIRadioAdjustmentModel;
	private IRadioCollectModel mIRadioCollectModel;
	private IRadioPlayModel mIRadioPlayModel;
	private final RadioHandler mRadioHandler = new RadioHandler(this);
	private long btnSubTouchDownTime, btnPlusTouchDownTime;// 手指按下步退步进按钮的时间

	private static final int MSG_SUB = 0;
	private static final int MSG_PLUS = 1;
	private static final int MSG_REMOVE_SUB_PLUS = 2;

	private static class RadioHandler extends Handler {
		private static WeakReference<RadioFragment> mReference;

		public RadioHandler(RadioFragment fragment) {
			mReference = new WeakReference<RadioFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SUB:
				mReference.get().mIRadioAdjustmentModel
						.adjustmentStepPrevious();
				mReference.get().mRadioHandler.sendEmptyMessageDelayed(MSG_SUB,
						mReference.get().frequency_adjust_delayMillis);
				break;
			case MSG_PLUS:
				mReference.get().mIRadioAdjustmentModel.adjustmentStepNext();
				mReference.get().mRadioHandler
						.sendEmptyMessageDelayed(MSG_PLUS,
								mReference.get().frequency_adjust_delayMillis);
				break;
			case MSG_REMOVE_SUB_PLUS:
				mReference.get().mRadioHandler.removeMessages(MSG_SUB);
				mReference.get().mRadioHandler.removeMessages(MSG_PLUS);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logD("RadioFragment-----------------------onCreateView");
		radioView = inflater.inflate(R.layout.fargment_radio, container, false);
		initTools();
		initModels();
		initView();
		setListener();
		return radioView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.logD("RadioFragment-----------------------onResume");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 应对系统字体语言等变化
	}

	private void initTools() {
		mChannelDBManager = CollectChannelDBManager.getInstance(getActivity());
	}

	private void initModels() {
		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp
				.getInstance(getActivity());
		mISearchNearStrongRadioModel.addISearchNearStrongRadioCallback(this);

		mIRadioAdjustmentModel = IRadioAdjustmentModelImp
				.getInstance(getActivity());
		mIRadioAdjustmentModel.addIRadioAdjustmentCallback(this);

		mIRadioCollectModel = IRadioCollectModelImp.getInstance(getActivity());
		mIRadioCollectModel.addIRadioCollectCallback(this);

		RadioInfoReceiver.getInstance(getActivity())
				.addIRadioInfoCallback(this);

		mIRadioPlayModel = IRadioPlayModelImp.getInstance(getActivity());
		mIRadioPlayModel.addIRadioPlayCallback(this);
	}

	private void initView() {
		iv_collected = (CollectStar) radioView.findViewById(R.id.iv_collected);
		tv_type = (TextView) radioView.findViewById(R.id.tv_type);
		tv_frequecy = (TextView) radioView.findViewById(R.id.tv_frequecy);
		tv_unit = (TextView) radioView.findViewById(R.id.tv_unit);
		btn_last_radio = (ImageButton) radioView
				.findViewById(R.id.btn_last_radio);
		btn_sub_channel = (ImageButton) radioView
				.findViewById(R.id.btn_sub_channel);
		btn_collect = (ImageButton) radioView.findViewById(R.id.btn_collect);
		btn_plus_channel = (ImageButton) radioView
				.findViewById(R.id.btn_plus_channel);
		btn_next_radio = (ImageButton) radioView
				.findViewById(R.id.btn_next_radio);

		RadioStatus.currentType = mPreferencesManager
				.getLatestRadioType(getActivity());
		RadioStatus.currentFrequency = mPreferencesManager
				.getLatestRadioFrequency(getActivity());
		updateDisplay(RadioStatus.currentFrequency);
	}

	private void setListener() {
		btn_last_radio.setOnClickListener(this);
		btn_sub_channel.setOnTouchListener(this);
		btn_collect.setOnClickListener(this);
		btn_plus_channel.setOnTouchListener(this);
		btn_next_radio.setOnClickListener(this);
	}

	/**
	 * 更新显示
	 * 
	 * @param frequency
	 *            显示频点
	 */
	public void updateDisplay(int frequency) {
		if (!AppUtil.inCurrentFrequencyRange(frequency)) {// 不是当前类型电台频点范围内
			return;
		}
		switch (RadioStatus.currentType) {
		case Constants.TYPE_FM:
			tv_type.setText(R.string.fm);// 更新类型
			tv_frequecy.setText(AppUtil.formatFloatFrequency(frequency
					/ Constants.FM_MULTIPLE));// 更新频率
			tv_unit.setText(R.string.mhz);// 更新单位
			break;
		case Constants.TYPE_AM:
			tv_type.setText(R.string.am);// 更新类型
			tv_frequecy.setText(String.valueOf(frequency));// 更新频点
			tv_unit.setText(R.string.khz);// 更新单位
			break;
		default:
			break;
		}

		if (mChannelDBManager.isChannelInDB(frequency)) {// 已经收藏
			iv_collected.show();
			btn_collect.setImageResource(R.drawable.btn_collected_selector);
		} else {
			iv_collected.dismiss();
			btn_collect.setImageResource(R.drawable.btn_uncollect_selector);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_last_radio:// 搜索上一个强信号台
			mISearchNearStrongRadioModel.searchPreviousStrongRadio();
			break;
		case R.id.btn_collect:// 收藏按钮
			mIRadioCollectModel.collectRadioOrNot(RadioStatus.currentType,
					RadioStatus.currentFrequency);
			break;
		case R.id.btn_next_radio:// 搜索下一个强信号台
			mISearchNearStrongRadioModel.searchNextStrongRadio();
			break;
		default:
			break;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			switch (v.getId()) {
			case R.id.btn_sub_channel:
				// 记录步退按钮按下的时间
				btnSubTouchDownTime = System.currentTimeMillis();

				if (!mRadioHandler.hasMessages(MSG_SUB)) {
					mRadioHandler.sendEmptyMessage(MSG_SUB);
				}
				break;
			case R.id.btn_plus_channel:
				// 记录步退按钮按下的时间
				btnPlusTouchDownTime = System.currentTimeMillis();

				if (!mRadioHandler.hasMessages(MSG_PLUS)) {
					mRadioHandler.sendEmptyMessage(MSG_PLUS);
				}
				break;
			default:
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
			switch (v.getId()) {
			case R.id.btn_sub_channel:
				// 如果时间很短（类似click），则播放按键音；如果时间很长（类似longClick），则不播放按键音
				if ((System.currentTimeMillis() - btnSubTouchDownTime) < frequency_adjust_delayMillis) {
					AppUtil.playClickSound(btn_sub_channel);
				}
				// 停止步退步进
				mRadioHandler.sendEmptyMessage(MSG_REMOVE_SUB_PLUS);
				break;
			case R.id.btn_plus_channel:
				// 如果时间很短（类似click），则播放按键音；如果时间很长（类似longClick），则不播放按键音
				if ((System.currentTimeMillis() - btnPlusTouchDownTime) < frequency_adjust_delayMillis) {
					AppUtil.playClickSound(btn_plus_channel);
				}
				// 停止步退步进
				mRadioHandler.sendEmptyMessage(MSG_REMOVE_SUB_PLUS);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Logger.logD("RadioFragment-----------------------onPause");
		mRadioHandler.sendEmptyMessage(MSG_REMOVE_SUB_PLUS);
	}

	@Override
	public void onStop() {
		super.onStop();
		Logger.logD("RadioFragment-----------------------onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.logD("RadioFragment-----------------------onDestroy");
		mRadioHandler.removeCallbacksAndMessages(null);
		removeModel();
	}

	private void removeModel() {
		mIRadioPlayModel.removeIRadioPlayCallback(this);

		RadioInfoReceiver.getInstance(getActivity()).removeIRadioInfoCallback(
				this);

		mISearchNearStrongRadioModel.removeISearchNearStrongRadioCallback(this);

		mIRadioCollectModel.removeIRadioCollectCallback(this);

		mIRadioAdjustmentModel.removeIRadioAdjustmentCallback(this);
	}

	@Override
	public void onRadioAdjustmentStepPrevious() {
		updateDisplay(RadioStatus.currentFrequency);
	}

	@Override
	public void onRadioAdjustmentStepNext() {
		updateDisplay(RadioStatus.currentFrequency);
	}

	@Override
	public void onRadioCollected(int radioType, int frequency) {// 收藏频点
		iv_collected.animateShow();
		btn_collect.setImageResource(R.drawable.btn_collected_selector);
	}

	@Override
	public void onRadioUnCollected(int frequency) {// 取消收藏频点
		iv_collected.animateDismiss();
		btn_collect.setImageResource(R.drawable.btn_uncollect_selector);
	}

	@Override
	public void onAllCollectRadioDelete() {// 删除所有收藏频点
	}

	@Override
	public void onSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		updateDisplay(frequency);
	}

	@Override
	public void onRadioInfoSearchNearStrongRadioResult(boolean isFinish,
			final int frequency) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updateDisplay(frequency);
			}
		});
	}

	@Override
	public void onRadioPlay(int radioType, int frequency) {
		updateDisplay(frequency);
	}

}
