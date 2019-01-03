package com.semisky.jlradio.fragment;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.semisky.jlradio.R;
import com.semisky.jlradio.adapter.CollectAdapter;
import com.semisky.jlradio.adapter.CollectAdapter.OnCollectItemButtonClickListener;
import com.semisky.jlradio.dao.CollectChannelDBManager;
import com.semisky.jlradio.dao.DBConfiguration;
import com.semisky.jlradio.dialog.DialogManager;
import com.semisky.jlradio.dialog.OnYesOrNoDialogListener;
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
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.view.VerticalTextView;

public class CollectFragment extends Fragment implements OnClickListener,
		OnItemClickListener, OnCollectItemButtonClickListener,
		OnYesOrNoDialogListener, IRadioInfoCallback,
		ISearchNearStrongRadioCallback, IRadioAdjustmentCallback,
		IRadioPlayCallback, IRadioCollectCallback {
	private View collectView;
	private TextView tv_empty;
	private ListView lv_collect;
	private VerticalTextView btn_right;
	private CollectAdapter mCollectAdapter;
	private Cursor mCursor;
	private int firstVisiblePostion = 0;
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;
	private IRadioAdjustmentModel mIRadioAdjustmentModel;
	private IRadioPlayModel mIRadioPlayModel;
	private IRadioCollectModel mIRadioCollectModel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logD("CollectFragment-----------------------onCreateView");
		collectView = inflater
				.inflate(R.layout.fragment_list, container, false);
		initModel();
		initView();
		return collectView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.logD("CollectFragment-----------------------onResume");
		// 搜索时被盖住，再回来
		refreshListView();
		// 等出场动画结束后滚动到选中item
		lv_collect.postDelayed(scrollRunnable, 800);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 应对系统字体语言等变化
		tv_empty.setText(R.string.no_collect);

		setRightBar();
	}

	private void initModel() {
		RadioInfoReceiver.getInstance(getActivity())
				.addIRadioInfoCallback(this);

		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp
				.getInstance(getActivity());
		mISearchNearStrongRadioModel.addISearchNearStrongRadioCallback(this);

		mIRadioAdjustmentModel = IRadioAdjustmentModelImp
				.getInstance(getActivity());
		mIRadioAdjustmentModel.addIRadioAdjustmentCallback(this);

		mIRadioPlayModel = IRadioPlayModelImp.getInstance(getActivity());
		mIRadioPlayModel.addIRadioPlayCallback(this);

		mIRadioCollectModel = IRadioCollectModelImp.getInstance(getActivity());
		mIRadioCollectModel.addIRadioCollectCallback(this);
	}

	private void initView() {
		tv_empty = (TextView) collectView.findViewById(R.id.tv_empty);
		tv_empty.setText(R.string.no_collect);

		initListView();

		btn_right = (VerticalTextView) collectView.findViewById(R.id.btn_right);
		btn_right.setOnClickListener(this);
		setRightBar();
	}

	private void initListView() {
		lv_collect = (ListView) collectView.findViewById(R.id.lv_channel);
		mCursor = CollectChannelDBManager.getInstance(getActivity())
				.getCollectChannelsCursor();
		if (mCursor == null || mCursor.getCount() <= 0) {
			tv_empty.setVisibility(View.VISIBLE);
			lv_collect.setVisibility(View.GONE);
		} else {
			tv_empty.setVisibility(View.GONE);
			lv_collect.setVisibility(View.VISIBLE);
			mCollectAdapter = new CollectAdapter(getActivity(), mCursor);
			mCollectAdapter.setOnCollectItemButtonClickListener(this);
			lv_collect.setAdapter(mCollectAdapter);
			lv_collect.setOnItemClickListener(this);
		}
	}

	/**
	 * 列表右侧按钮显示设置
	 */
	private void setRightBar() {
		String language = getResources().getConfiguration().locale
				.getLanguage();

		if (language.equals("en")) {// 当前系统语言为英文
			btn_right.setDirection(VerticalTextView.ORIENTATION_UP_TO_DOWN);// 文字竖排
		} else {// 当前系统语言为中文
			btn_right.setDirection(VerticalTextView.ORIENTATION_DEFAULT);// 文字标准排版
		}
		btn_right.setText(R.string.clear_list);
	}

	/**
	 * 设置选中位置
	 * 
	 * @param frequency
	 * @param smoothScrollToPlaying
	 *            是否滚动动画到当前播放
	 */
	private void setSelection(int frequency, boolean smoothScrollToPlaying) {
		if (mCollectAdapter != null && lv_collect.getCount() > 0) {
			if (mCursor.isClosed()) {
				mCursor = CollectChannelDBManager.getInstance(getActivity())
						.getCollectChannelsCursor();
			}
			if (mCursor == null) {
				return;
			}
			int position = mCollectAdapter.getPosition(frequency);
			if (smoothScrollToPlaying) {
				lv_collect.smoothScrollToPosition(position);
			} else {
				lv_collect.setSelection(position);
			}
			firstVisiblePostion = position;
		}
	}

	@SuppressWarnings("deprecation")
	private void refreshListView() {
		if (lv_collect != null && mCollectAdapter != null) {
			mCollectAdapter.getCursor().requery();
			mCollectAdapter.notifyDataSetChanged();
		}
	}

	private void refreshListViewWhenItemDelete(int frequency) {
		initListView();
		if (lv_collect.getCount() > 0 && firstVisiblePostion >= 0) {
			// 删除不需要滚动动画
			lv_collect.setSelection(firstVisiblePostion);
		}
	}

	private Runnable scrollRunnable = new Runnable() {

		@Override
		public void run() {
			setSelection(RadioStatus.currentFrequency, true);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_right:// 清空列表
			/*
			 * 弹框：Dialog的显示有可能会在activity没创建完成之前就执行了，这时候context这个值是没意义的。
			 * activity的创建完成会使得window获得焦点，那么只要判断当前activity是否有焦点就可以了
			 */
			if (getActivity().hasWindowFocus()) {
				if (tv_empty.getVisibility() == View.VISIBLE) {// 说明列表为空
					DialogManager.getInstance().showYesDialog(getActivity(),
							R.string.notice, R.string.no_collect);
				} else {
					DialogManager.getInstance().showYesOrNoDialog(
							getActivity(), R.string.notice,
							R.string.sure_to_clear, this);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onDeleteButtonClick(View v, int frequency) {
		// 点击音
		AppUtil.playClickSound(v);
		mIRadioCollectModel.disCollectRadio(frequency);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.lv_channel:
			Cursor cursor = mCollectAdapter.getCursor();
			if (cursor != null) {
				cursor.moveToPosition(position);
				int frequency = cursor
						.getInt(cursor
								.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY));
				int type = cursor
						.getInt(cursor
								.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_TYPE));
				Logger.logD("CollectFragment-----------------------choose "
						+ getString(type == Constants.TYPE_FM ? R.string.fm
								: R.string.am)
						+ "frequency = "
						+ frequency
						+ getString(type == Constants.TYPE_FM ? R.string.mhz
								: R.string.khz));

				if (RadioStatus.currentFrequency == frequency) {// 如果点击的item频点就是正在播放的频点，不做任何操作
					Logger.logD("CollectFragment-----------------------this frequency is playing");
				} else {
					mIRadioPlayModel.playRadio(type, frequency);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onYesOrNoDialogConfirm(View v) {
		mIRadioCollectModel.deleteAllCollectRadio();
	}

	@Override
	public void onYesOrNoDialogCancel(View v) {
	}

	@Override
	public void onYesOrNoDialogDismiss() {
	}

	@Override
	public void onPause() {
		super.onPause();
		Logger.logD("CollectFragment-----------------------onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Logger.logD("CollectFragment-----------------------onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.logD("CollectFragment-----------------------onDestroy");
		lv_collect.removeCallbacks(scrollRunnable);
		if (mCursor != null) {
			// 如果close，快速切换Fragment时会报re-open already-close Object错误，所以干脆不关
			// mCursor.close();
		}
		removeModel();
	}

	private void removeModel() {
		mIRadioCollectModel.removeIRadioCollectCallback(this);
		mIRadioPlayModel.removeIRadioPlayCallback(this);
		mIRadioAdjustmentModel.removeIRadioAdjustmentCallback(this);
		mISearchNearStrongRadioModel.removeISearchNearStrongRadioCallback(this);
		RadioInfoReceiver.getInstance(getActivity()).removeIRadioInfoCallback(
				this);
	}

	/**
	 * 搜索上下一个强信号台结果回调（不在主线程中）（情况一：无上一个强信号台时，返回最小频点，无下一个强信号台时，返回最大频点；情况二：在搜索过程中，
	 * 要求返回进行中的频点，包括信号强度为0的和强度大于0的结果频点）
	 */
	@Override
	public void onRadioInfoSearchNearStrongRadioResult(final boolean isFinish,
			final int frequency) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (lv_collect.getCount() > 0) {// 列表为空就不需要进行任何刷新
					if (isFinish) {
						refreshListView();
						setSelection(frequency, true);
					}
				}
			}
		});
	}

	/**
	 * 搜索上下一个强信号台准备阶段回调（情况一：正在搜索上下一个强信号台，再点就是中断返回；情况二：已是最小或最大频点，返回对应最大或最小频点）
	 */
	@Override
	public void onSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		if (lv_collect.getCount() > 0) {// 列表为空就不需要进行任何刷新
			if (isFinish) {
				refreshListView();
				setSelection(frequency, true);
			}
		}
	}

	@Override
	public void onRadioAdjustmentStepPrevious() {
		if (lv_collect.getCount() > 0) {// 列表为空就不需要进行任何刷新
			refreshListView();
			setSelection(RadioStatus.currentFrequency, true);
		}
	}

	@Override
	public void onRadioAdjustmentStepNext() {
		if (lv_collect.getCount() > 0) {// 列表为空就不需要进行任何刷新
			refreshListView();
			setSelection(RadioStatus.currentFrequency, true);
		}
	}

	@Override
	public void onRadioPlay(int radioType, int frequency) {// 播放频点
		if (mCollectAdapter != null) {
			mCollectAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onRadioCollected(int radioType, int frequency) {// 收藏频点
	}

	@Override
	public void onRadioUnCollected(int frequency) {// 取消收藏频点
		firstVisiblePostion = lv_collect.getFirstVisiblePosition();
		refreshListViewWhenItemDelete(frequency);
	}

	@Override
	public void onAllCollectRadioDelete() {// 删除所有收藏频点
		initListView();
	}

}
