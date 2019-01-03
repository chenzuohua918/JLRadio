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
import com.semisky.jlradio.adapter.AMAdapter;
import com.semisky.jlradio.adapter.AMAdapter.OnAMItemButtonClickListener;
import com.semisky.jlradio.dao.AMChannelDBManager;
import com.semisky.jlradio.dao.DBConfiguration;
import com.semisky.jlradio.dialog.DialogManager;
import com.semisky.jlradio.dialog.OnYesOrNoDialogListener;
import com.semisky.jlradio.dialog.SearchingDialog;
import com.semisky.jlradio.dialog.SearchingDialog.OnSearchingDialogListener;
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
import com.semisky.jlradio.model.ISearchAllAMModel;
import com.semisky.jlradio.model.ISearchAllAMModelImp;
import com.semisky.jlradio.model.ISearchNearStrongRadioCallback;
import com.semisky.jlradio.model.ISearchNearStrongRadioModel;
import com.semisky.jlradio.model.ISearchNearStrongRadioModelImp;
import com.semisky.jlradio.model.ISwitchFMAMModel;
import com.semisky.jlradio.model.ISwitchFMAMModelImp;
import com.semisky.jlradio.model.RadioInfoReceiver;
import com.semisky.jlradio.model.SearchAllAMListener;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;
import com.semisky.jlradio.util.SettingsUtil;
import com.semisky.jlradio.util.SortCursor;
import com.semisky.jlradio.util.Toaster;
import com.semisky.jlradio.view.CollectToast;
import com.semisky.jlradio.view.VerticalTextView;

public class AMFragment extends Fragment implements OnClickListener,
		OnItemClickListener, OnAMItemButtonClickListener,
		OnYesOrNoDialogListener, OnSearchingDialogListener, IRadioInfoCallback,
		ISearchNearStrongRadioCallback, IRadioAdjustmentCallback,
		IRadioPlayCallback, IRadioCollectCallback, SearchAllAMListener {
	private View amView;
	private TextView tv_empty;// 无FM频点Item时文字提示
	private ListView lv_am;
	private VerticalTextView btn_right;// 右侧刷新列表按钮
	private AMAdapter mAmAdapter;
	private Cursor mCursor;
	private SearchingDialog mSearchingDialog;// 搜索弹框
	private CollectToast mCollectToast;// 收藏吐司
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;// 搜索上下一个强信号台Model
	private ISwitchFMAMModel mISwitchFMAMModel;// 切换FM、AM频点Model
	private IRadioAdjustmentModel mIRadioAdjustmentModel;// 频点微调Model
	private IRadioPlayModel mIRadioPlayModel;// 频点播放Model
	private IRadioCollectModel mIRadioCollectModel;// 频点收藏Model
	private ISearchAllAMModel mISearchAllAMModel;// 搜索所有AM频道Model

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logD("AMFragment-----------------------onCreateView");
		amView = inflater.inflate(R.layout.fragment_list, container, false);
		initModel();
		setAM();
		initView();
		return amView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.logD("AMFragment-----------------------onResume");
		// 搜索时被盖住，再回来
		refreshListView();
		// 等出场动画结束后滚动到选中item
		lv_am.postDelayed(scrollRunnable, 800);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 应对系统字体语言等变化
		tv_empty.setText(R.string.no_channel);

		setRightBar();
	}

	private void setAM() {
		mISwitchFMAMModel.switchRadioType(Constants.TYPE_AM, false);
	}

	private void initModel() {
		RadioInfoReceiver.getInstance(getActivity())
				.addIRadioInfoCallback(this);

		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp
				.getInstance(getActivity());
		mISearchNearStrongRadioModel.addISearchNearStrongRadioCallback(this);

		mISwitchFMAMModel = ISwitchFMAMModelImp.getInstance(getActivity());

		mIRadioAdjustmentModel = IRadioAdjustmentModelImp
				.getInstance(getActivity());
		mIRadioAdjustmentModel.addIRadioAdjustmentCallback(this);

		mIRadioPlayModel = IRadioPlayModelImp.getInstance(getActivity());
		mIRadioPlayModel.addIRadioPlayCallback(this);

		mIRadioCollectModel = IRadioCollectModelImp.getInstance(getActivity());
		mIRadioCollectModel.addIRadioCollectCallback(this);

		mISearchAllAMModel = ISearchAllAMModelImp.getInstance(getActivity());
		mISearchAllAMModel.registerSearchAllAMListener(this);
	}

	private void initView() {
		tv_empty = (TextView) amView.findViewById(R.id.tv_empty);
		tv_empty.setText(R.string.no_channel);

		initListView();

		mCollectToast = (CollectToast) amView.findViewById(R.id.collectToast);

		btn_right = (VerticalTextView) amView.findViewById(R.id.btn_right);
		btn_right.setOnClickListener(this);
		setRightBar();
	}

	private void initListView() {
		lv_am = (ListView) amView.findViewById(R.id.lv_channel);
		mCursor = AMChannelDBManager.getInstance(getActivity())
				.getAMChannelsCursor();
		if (mCursor == null) {
			tv_empty.setVisibility(View.VISIBLE);
			lv_am.setVisibility(View.GONE);
		} else {
			mAmAdapter = new AMAdapter(getActivity(), new SortCursor(mCursor,
					DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY));
			mAmAdapter.setOnAMItemButtonClickListener(this);
			lv_am.setAdapter(mAmAdapter);
			lv_am.setOnItemClickListener(this);
			if (mCursor.getCount() <= 0) {
				tv_empty.setVisibility(View.VISIBLE);
				lv_am.setVisibility(View.GONE);
			} else {
				tv_empty.setVisibility(View.GONE);
				lv_am.setVisibility(View.VISIBLE);
			}
		}
	}

	/** 列表右侧按钮显示设置 */
	private void setRightBar() {
		String language = getResources().getConfiguration().locale
				.getLanguage();
		if (language.equals("en")) {// 当前系统语言为英文
			btn_right.setDirection(VerticalTextView.ORIENTATION_UP_TO_DOWN);// 文字竖排
		} else {// 当前系统语言为中文
			btn_right.setDirection(VerticalTextView.ORIENTATION_DEFAULT);// 文字标准排版
		}
		btn_right.setText(R.string.search_radio_vertical);
	}

	/**
	 * 设置选中位置
	 * 
	 * @param frequency
	 * @param smoothScrollToPlaying
	 *            是否滚动动画到当前播放
	 */
	private void setSelection(int frequency, boolean smoothScrollToPlaying) {
		if (RadioStatus.currentType == Constants.TYPE_AM) {
			if (mCursor.isClosed()) {
				mCursor = AMChannelDBManager.getInstance(getActivity())
						.getAMChannelsCursor();
			}
			if (mCursor == null) {
				return;
			}
			int position = mAmAdapter.getPosition(frequency);
			if (smoothScrollToPlaying) {
				lv_am.smoothScrollToPosition(position);
			} else {
				lv_am.setSelection(position);
			}
		}
	}

	/** 该刷新方法在数据量变化时无效 */
	@SuppressWarnings("deprecation")
	private void refreshListView() {
		if (lv_am != null && mAmAdapter != null) {
			mAmAdapter.getCursor().requery();
			mAmAdapter.notifyDataSetChanged();
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
		case R.id.btn_right:// 刷新列表
			/*
			 * 弹框：Dialog的显示有可能会在activity没创建完成之前就执行了，这时候context这个值是没意义的。
			 * activity的创建完成会使得window获得焦点，那么只要判断当前activity是否有焦点就可以了
			 */
			if (getActivity().hasWindowFocus()) {
				if (!SettingsUtil.getInstance().isRadioLatestOpened(
						getActivity())) {// 收音机没打开
					DialogManager.getInstance().showYesDialog(getActivity(),
							R.string.search_radio_horizontal,
							R.string.message_open_first);
				} else if (RadioStatus.isSearchingAM
						|| RadioStatus.isSearchingFM
						|| RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER) {// 正在搜索
					DialogManager.getInstance().showYesDialog(getActivity(),
							R.string.search_radio_horizontal,
							R.string.message_try_later);
				} else {
					DialogManager.getInstance().showYesOrNoDialog(
							getActivity(), R.string.search_radio_horizontal,
							R.string.message_search, this);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		itemClick(parent, view, position, id, true);
	}

	private void itemClick(AdapterView<?> parent, View view, int position,
			long id, boolean fromUser) {
		switch (parent.getId()) {
		case R.id.lv_channel:
			Cursor cursor = mAmAdapter.getCursor();
			if (cursor != null) {
				cursor.moveToPosition(position);
				int frequency = cursor
						.getInt(cursor
								.getColumnIndex(DBConfiguration.TableAMConfiguration.CHANNEL_FREQUENCY));
				Logger.logD("AMFragment-----------------------choose "
						+ getString(R.string.am) + ", frequency = " + frequency
						+ getString(R.string.khz));

				if (fromUser && RadioStatus.currentFrequency == frequency) {// 如果是用户点击并且点击的item频点就是正在播放的频点，不做任何操作
					Logger.logD("AMFragment-----------------------this frequency is playing");
				} else {
					mIRadioPlayModel.playRadio(Constants.TYPE_AM, frequency);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onCollectButtonClick(View v, int frequency) {
		// 点击音
		AppUtil.playClickSound(v);
		// 加入收藏与否
		mIRadioCollectModel.collectRadioOrNot(Constants.TYPE_AM, frequency);
	}

	@Override
	public void onYesOrNoDialogConfirm(View v) {// 确认
		mSearchingDialog = new SearchingDialog(getActivity(),
				R.string.searching, R.style.SearchingDialog, Constants.TYPE_AM);
		mSearchingDialog.setOnSearchingDialogListener(this);
		mSearchingDialog.setType(R.string.am);
		mSearchingDialog.setFrequency(Constants.AMMIN);// 初始AM频道值为最小值
		mSearchingDialog.setUnit(R.string.khz);
		mSearchingDialog.show();
		Logger.logD("AMFragment-----------------------begin search AM channels");
		// 开始搜索前，隐藏“无有效电台”
		tv_empty.setVisibility(View.GONE);
	}

	@Override
	public void onYesOrNoDialogCancel(View v) {// 取消
	}

	@Override
	public void onYesOrNoDialogDismiss() {// 消失
	}

	@Override
	public void onSearchingDialogDismiss() {// 取消搜索或者搜索结束
	}

	@Override
	public void onPause() {
		super.onPause();
		Logger.logD("AMFragment-----------------------onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Logger.logD("AMFragment-----------------------onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.logD("AMFragment-----------------------onDestroy");
		lv_am.removeCallbacks(scrollRunnable);
		if (mCursor != null) {
			// 如果close，快速切换Fragment时会报re-open already-close Object错误，所以干脆不关
			// mCursor.close();
		}
		removeModel();
	}

	private void removeModel() {
		mISearchAllAMModel.unregisterSearchAllAMListener(this);
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
				if (lv_am.getCount() > 0) {// 列表为空就不需要进行任何刷新
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
		if (lv_am.getCount() > 0) {// 列表为空就不需要进行任何刷新
			if (isFinish) {
				refreshListView();
				setSelection(frequency, true);
			}
		}
	}

	@Override
	public void onRadioAdjustmentStepPrevious() {
		if (lv_am.getCount() > 0) {// 列表为空就不需要进行任何刷新
			refreshListView();
			setSelection(RadioStatus.currentFrequency, true);
		}
	}

	@Override
	public void onRadioAdjustmentStepNext() {
		if (lv_am.getCount() > 0) {// 列表为空就不需要进行任何刷新
			refreshListView();
			setSelection(RadioStatus.currentFrequency, true);
		}
	}

	@Override
	public void onRadioPlay(int radioType, int frequency) {// 播放频点
		if (mAmAdapter != null) {
			mAmAdapter.notifyDataSetChanged();
			setSelection(frequency, true);
		}
	}

	@Override
	public void onRadioCollected(int radioType, int frequency) {// 收藏频点
		if (RadioStatus.currentFrequency == frequency) {
			mCollectToast
					.setImageResource(R.drawable.radio_listbtn_collected_selected);
		} else {
			mCollectToast
					.setImageResource(R.drawable.radio_listbtn_collected_normal);
		}
		mCollectToast.toast();
		refreshListView();
	}

	@Override
	public void onRadioUnCollected(int frequency) {// 取消收藏频点
		if (RadioStatus.currentFrequency == frequency) {
			mCollectToast
					.setImageResource(R.drawable.radio_listbtn_uncollect_selected);
		} else {
			mCollectToast
					.setImageResource(R.drawable.radio_listbtn_uncollect_normal);
		}
		mCollectToast.toast();
		refreshListView();
	}

	@Override
	public void onAllCollectRadioDelete() {// 删除所有收藏频点
	}

	@Override
	public void notifyObserversClearAMList() {
		initListView();
	}

	@Override
	public void notifyObserversSearchAllAMFinish() {
		initListView();
		if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
			mSearchingDialog.dismiss();
		}
		if (lv_am.getCount() > 0) {
			// 播放第一个AM强信号台
			itemClick(lv_am, lv_am.getChildAt(0), 0,
					lv_am.getItemIdAtPosition(0), false);
		} else {// 一个好台都没有
			Toaster.getInstance().makeText(getActivity(), R.string.search_fail);
			// 播放搜索之前那个频点
			mIRadioPlayModel.playRadio(RadioStatus.currentType,
					RadioStatus.currentFrequency);
		}
	}

	@Override
	public void notifyObserversSearchAllAMUnFinish(int frequency) {
		// 实时刷新扫描到的频道显示
		if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
			mSearchingDialog.setFrequency(frequency);
		}
	}

	@Override
	public void notifyObserversSearchAllAMInterrupt() {
		Toaster.getInstance()
				.makeText(getActivity(), R.string.search_interrupt);
		initListView();
		if (lv_am.getCount() > 0) {// 播放第一个AM强信号台
			itemClick(lv_am, lv_am.getChildAt(0), 0,
					lv_am.getItemIdAtPosition(0), false);
		} else {// 一个好台都没有
			// 播放搜索之前那个频点
			mIRadioPlayModel.playRadio(RadioStatus.currentType,
					RadioStatus.currentFrequency);
		}
	}

	@Override
	public void notifyObserversSearchAllAMTimeout() {
		if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
			mSearchingDialog.dismiss();
		}
		Toaster.getInstance().makeText(getActivity(), R.string.search_fail);
		// 播放搜索之前那个频点
		mIRadioPlayModel.playRadio(RadioStatus.currentType,
				RadioStatus.currentFrequency);
	}

}
