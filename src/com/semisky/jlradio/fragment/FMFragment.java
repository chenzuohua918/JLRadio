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
import com.semisky.jlradio.adapter.FMAdapter;
import com.semisky.jlradio.adapter.FMAdapter.OnFMItemButtonClickListener;
import com.semisky.jlradio.dao.DBConfiguration;
import com.semisky.jlradio.dao.FMChannelDBManager;
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
import com.semisky.jlradio.model.ISearchAllFMModel;
import com.semisky.jlradio.model.ISearchAllFMModelImp;
import com.semisky.jlradio.model.ISearchNearStrongRadioCallback;
import com.semisky.jlradio.model.ISearchNearStrongRadioModel;
import com.semisky.jlradio.model.ISearchNearStrongRadioModelImp;
import com.semisky.jlradio.model.ISwitchFMAMModel;
import com.semisky.jlradio.model.ISwitchFMAMModelImp;
import com.semisky.jlradio.model.RadioInfoReceiver;
import com.semisky.jlradio.model.SearchAllFMListener;
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

public class FMFragment extends Fragment implements OnClickListener,
		OnItemClickListener, OnFMItemButtonClickListener,
		OnYesOrNoDialogListener, OnSearchingDialogListener, IRadioInfoCallback,
		ISearchNearStrongRadioCallback, IRadioAdjustmentCallback,
		IRadioPlayCallback, IRadioCollectCallback, SearchAllFMListener {
	private View fmView;
	private TextView tv_empty;// 无FM频点Item时文字提示
	private ListView lv_fm;// FM频点列表
	private VerticalTextView btn_right;// 右侧刷新列表按钮
	private FMAdapter mFmAdapter;
	private Cursor mCursor;
	private SearchingDialog mSearchingDialog;// 搜索弹框
	private CollectToast mCollectToast;// 收藏吐司
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;// 搜索上下一个强信号台Model
	private ISwitchFMAMModel mISwitchFMAMModel;// 切换FM、AM频点Model
	private IRadioAdjustmentModel mIRadioAdjustmentModel;// 频点微调Model
	private IRadioPlayModel mIRadioPlayModel;// 频点播放Model
	private IRadioCollectModel mIRadioCollectModel;// 频点收藏Model
	private ISearchAllFMModel mISearchAllFMModel;// 搜索所有FM频道Model

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.logD("FMFragment-----------------------onCreateView");
		fmView = inflater.inflate(R.layout.fragment_list, container, false);
		initModel();
		setFM();
		initView();
		return fmView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.logD("FMFragment-----------------------onResume");
		// 搜索时被盖住，再回来
		refreshListView();
		// 等出场动画结束后滚动到选中item
		lv_fm.postDelayed(scrollRunnable, 800);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 应对系统字体语言等变化
		tv_empty.setText(R.string.no_channel);

		setRightBar();
	}

	/** 选择播放FM */
	private void setFM() {
		mISwitchFMAMModel.switchRadioType(Constants.TYPE_FM, false);
	}

	/** 初始化Model */
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

		mISearchAllFMModel = ISearchAllFMModelImp.getInstance(getActivity());
		mISearchAllFMModel.registerSearchAllFMListener(this);
	}

	private void initView() {
		tv_empty = (TextView) fmView.findViewById(R.id.tv_empty);
		tv_empty.setText(R.string.no_channel);

		initListView();

		mCollectToast = (CollectToast) fmView.findViewById(R.id.collectToast);

		btn_right = (VerticalTextView) fmView.findViewById(R.id.btn_right);
		btn_right.setOnClickListener(this);
		setRightBar();
	}

	private void initListView() {
		lv_fm = (ListView) fmView.findViewById(R.id.lv_channel);
		mCursor = FMChannelDBManager.getInstance(getActivity())
				.getFMChannelsCursor();
		if (mCursor == null) {
			tv_empty.setVisibility(View.VISIBLE);
			lv_fm.setVisibility(View.GONE);
		} else {
			mFmAdapter = new FMAdapter(getActivity(), new SortCursor(mCursor,
					DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY));
			mFmAdapter.setOnFMItemButtonClickListener(this);
			lv_fm.setAdapter(mFmAdapter);
			lv_fm.setOnItemClickListener(this);
			if (mFmAdapter.getCount() <= 0) {
				tv_empty.setVisibility(View.VISIBLE);
				lv_fm.setVisibility(View.GONE);
			} else {
				tv_empty.setVisibility(View.GONE);
				lv_fm.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 设置选中位置
	 * 
	 * @param frequency
	 * @param smoothScrollToPlaying
	 *            是否滚动动画到当前播放
	 */
	private void setSelection(int frequency, boolean smoothScrollToPlaying) {
		if (RadioStatus.currentType == Constants.TYPE_FM) {
			if (mCursor.isClosed()) {
				mCursor = FMChannelDBManager.getInstance(getActivity())
						.getFMChannelsCursor();
			}
			if (mCursor == null) {
				return;
			}
			int position = mFmAdapter.getPosition(frequency);
			if (smoothScrollToPlaying) {
				lv_fm.smoothScrollToPosition(position);
			} else {
				lv_fm.setSelection(position);
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

	/** 该刷新方法在数据量变化时无效 */
	@SuppressWarnings("deprecation")
	private void refreshListView() {
		if (lv_fm != null && mFmAdapter != null) {
			mFmAdapter.getCursor().requery();
			mFmAdapter.notifyDataSetChanged();
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
			Cursor cursor = mFmAdapter.getCursor();
			if (cursor != null) {
				cursor.moveToPosition(position);
				int frequency = cursor
						.getInt(cursor
								.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY));
				Logger.logD("FMFragment-----------------------choose "
						+ getString(R.string.fm) + ", frequency = " + frequency
						+ getString(R.string.mhz));

				if (fromUser && RadioStatus.currentFrequency == frequency) {// 如果是用户点击并且点击的item频点就是正在播放的频点，不做任何操作
					Logger.logD("FMFragment-----------------------this frequency is playing");
				} else {
					mIRadioPlayModel.playRadio(Constants.TYPE_FM, frequency);
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
		mIRadioCollectModel.collectRadioOrNot(Constants.TYPE_FM, frequency);
	}

	@Override
	public void onYesOrNoDialogConfirm(View v) {// 确认
		mSearchingDialog = new SearchingDialog(getActivity(),
				R.string.searching, R.style.SearchingDialog, Constants.TYPE_FM);
		mSearchingDialog.setOnSearchingDialogListener(this);
		mSearchingDialog.setType(R.string.fm);
		mSearchingDialog
				.setFrequency(Float.parseFloat(AppUtil
						.formatFloatFrequency(Constants.FMMIN
								/ Constants.FM_MULTIPLE)));// 初始FM频道值为最小FM值
		mSearchingDialog.setUnit(R.string.mhz);
		mSearchingDialog.show();
		Logger.logD("FMFragment-----------------------begin search FM channels");
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
		Logger.logD("FMFragment-----------------------onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Logger.logD("FMFragment-----------------------onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.logD("FMFragment-----------------------onDestroy");
		lv_fm.removeCallbacks(scrollRunnable);
		if (mCursor != null) {
			// 如果close，快速切换Fragment时会报re-open already-close Object错误，所以干脆不关
			// mCursor.close();
		}
		removeModel();
	}

	private void removeModel() {
		mISearchAllFMModel.unregisterSearchAllFMListener(this);
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
				if (lv_fm.getCount() > 0) {// 列表为空就不需要进行任何刷新
					if (isFinish) {
						refreshListView();
						setSelection(frequency, true);
					}
				}
			}
		});
	}

	/** 搜索上下一个强信号台准备阶段回调（情况一：正在搜索上下一个强信号台，再点就是中断返回；情况二：已是最小或最大频点，返回对应最大或最小频点） */
	@Override
	public void onSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		if (lv_fm.getCount() > 0) {// 列表为空就不需要进行任何刷新
			if (isFinish) {
				refreshListView();
				setSelection(frequency, true);
			}
		}
	}

	@Override
	public void onRadioAdjustmentStepPrevious() {
		if (lv_fm.getCount() > 0) {// 列表为空就不需要进行任何刷新
			refreshListView();
			setSelection(RadioStatus.currentFrequency, true);
		}
	}

	@Override
	public void onRadioAdjustmentStepNext() {
		if (lv_fm.getCount() > 0) {// 列表为空就不需要进行任何刷新
			refreshListView();
			setSelection(RadioStatus.currentFrequency, true);
		}
	}

	@Override
	public void onRadioPlay(int radioType, int frequency) {// 播放频点
		if (mFmAdapter != null) {
			mFmAdapter.notifyDataSetChanged();
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
	public void notifyObserversClearFMList() {
		initListView();
	}

	@Override
	public void notifyObserversSearchAllFMFinish() {
		initListView();
		if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
			mSearchingDialog.dismiss();
		}
		if (lv_fm.getCount() > 0) {
			// 播放第一个FM强信号台
			itemClick(lv_fm, lv_fm.getChildAt(0), 0,
					lv_fm.getItemIdAtPosition(0), false);
		} else {// 一个好台都没有
			Toaster.getInstance().makeText(getActivity(), R.string.search_fail);
			// 播放搜索之前那个频点
			mIRadioPlayModel.playRadio(RadioStatus.currentType,
					RadioStatus.currentFrequency);
		}
	}

	@Override
	public void notifyObserversSearchAllFMUnFinish(int frequency) {
		// 实时刷新扫描到的频道显示
		if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
			mSearchingDialog.setFrequency(Float.parseFloat(AppUtil
					.formatFloatFrequency(frequency / Constants.FM_MULTIPLE)));
		}
	}

	@Override
	public void notifyObserversSearchAllFMInterrupt() {
		Toaster.getInstance()
				.makeText(getActivity(), R.string.search_interrupt);
		initListView();
		if (lv_fm.getCount() > 0) {// 播放第一个FM强信号台
			itemClick(lv_fm, lv_fm.getChildAt(0), 0,
					lv_fm.getItemIdAtPosition(0), false);
		} else {// 一个好台都没有
			// 播放搜索之前那个频点
			mIRadioPlayModel.playRadio(RadioStatus.currentType,
					RadioStatus.currentFrequency);
		}
	}

	@Override
	public void notifyObserversSearchAllFMTimeout() {
		if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
			mSearchingDialog.dismiss();
		}
		Toaster.getInstance().makeText(getActivity(), R.string.search_fail);
		// 播放搜索之前那个频点
		mIRadioPlayModel.playRadio(RadioStatus.currentType,
				RadioStatus.currentFrequency);
	}
}
