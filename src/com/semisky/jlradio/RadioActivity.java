package com.semisky.jlradio;

import java.lang.ref.WeakReference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.semisky.jlradio.fragment.AMFragment;
import com.semisky.jlradio.fragment.CollectFragment;
import com.semisky.jlradio.fragment.FMFragment;
import com.semisky.jlradio.fragment.RadioFragment;
import com.semisky.jlradio.model.IRadioAdjustmentCallback;
import com.semisky.jlradio.model.IRadioAdjustmentModel;
import com.semisky.jlradio.model.IRadioAdjustmentModelImp;
import com.semisky.jlradio.model.IRadioInfoCallback;
import com.semisky.jlradio.model.IRadioPlayCallback;
import com.semisky.jlradio.model.IRadioPlayModel;
import com.semisky.jlradio.model.IRadioPlayModelImp;
import com.semisky.jlradio.model.IRadioSwitchCallback;
import com.semisky.jlradio.model.IRadioSwitchModel;
import com.semisky.jlradio.model.IRadioSwitchModelImp;
import com.semisky.jlradio.model.ISearchAllAMModel;
import com.semisky.jlradio.model.ISearchAllAMModelImp;
import com.semisky.jlradio.model.ISearchAllFMModel;
import com.semisky.jlradio.model.ISearchAllFMModelImp;
import com.semisky.jlradio.model.ISearchNearStrongRadioCallback;
import com.semisky.jlradio.model.ISearchNearStrongRadioModel;
import com.semisky.jlradio.model.ISearchNearStrongRadioModelImp;
import com.semisky.jlradio.model.ISwitchFMAMCallback;
import com.semisky.jlradio.model.ISwitchFMAMModel;
import com.semisky.jlradio.model.ISwitchFMAMModelImp;
import com.semisky.jlradio.model.RadioInfoReceiver;
import com.semisky.jlradio.model.RadioVolumeModel;
import com.semisky.jlradio.model.SearchAllAMListener;
import com.semisky.jlradio.model.SearchAllFMListener;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.PreferencesUtil;
import com.semisky.jlradio.util.ProtocolUtil;
import com.semisky.jlradio.util.RadioStatus;
import com.semisky.jlradio.util.RadioStatus.SearchNearStrongChannel;
import com.semisky.jlradio.util.SettingsUtil;

public class RadioActivity extends FragmentActivity implements
		IRadioSwitchCallback, IRadioInfoCallback,
		ISearchNearStrongRadioCallback, ISwitchFMAMCallback,
		IRadioAdjustmentCallback, IRadioPlayCallback, SearchAllFMListener,
		SearchAllAMListener {
	private FragmentManager mFragmentManager;
	private FragmentTransaction mTransaction;
	private RadioFragment mRadioFragment;
	private FMFragment mFmFragment;
	private AMFragment mAmFragment;
	private CollectFragment mCollectFragment;
	private RadioGroup rg_left;// 左侧选项卡
	private RadioButton rb_radio, rb_fm, rb_am, rb_collect;
	private CheckBox cb_switch;// 开关
	private android.view.BottomBar mBottomBar;
	private AudioManager mAudioManager;
	private IRadioSwitchModel mIRadioSwitchModel;// 收音机开关Model
	private ISearchNearStrongRadioModel mISearchNearStrongRadioModel;// 搜索上下一个强信号台Model
	private RadioInfoReceiver mRadioInfoReceiver;// 频点信息接收Model
	private ISwitchFMAMModel mISwitchFMAMModel;// 切换FM、AM频点Model
	private IRadioAdjustmentModel mIRadioAdjustmentModel;// 频点微调Model
	private IRadioPlayModel mIRadioPlayModel;// 频点播放Model
	private RadioVolumeModel mRadioVolumeModel;// 收音机声音操作Model
	private ISearchAllFMModel mISearchAllFMModel;// 搜索所有FM频道Model
	private ISearchAllAMModel mISearchAllAMModel;// 搜索所有AM频道Model

	private final RadioHandler mRadioHandler = new RadioHandler(this);
	private boolean firstResume = true;// 是否是第一次执行onResume

	private static final int KEY_PREV = 8;// 上一个强信号台
	private static final int KEY_NEXT = 9;// 下一个强信号台
	private static final int KEY_SEEK_INC = 10; // TUNE右旋
	private static final int KEY_SEEK_DEC = 11;// TUNE左旋
	private static final int KEY_POWER = 19;// Power按键

	private static class RadioHandler extends Handler {
		private static WeakReference<RadioActivity> mReference;

		public RadioHandler(RadioActivity activity) {
			mReference = new WeakReference<RadioActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mReference.get() == null) {
				return;
			}

			switch (msg.what) {
			case Constants.START_PLAY:
				mReference.get().startPlay();
				break;
			case Constants.STOP_PLAY:
				mReference.get().stopPlay();
				break;
			case Constants.MSG_RADIO_PREVIOUS:// 搜索上一个强信号台
				mReference.get().mISearchNearStrongRadioModel
						.searchPreviousStrongRadio();
				break;
			case Constants.MSG_RADIO_NEXT:// 搜索下一个强信号台
				mReference.get().mISearchNearStrongRadioModel
						.searchNextStrongRadio();
				break;
			case Constants.MSG_SEEK_DEC:// TUNE旋钮左旋（向小微调）
				mReference.get().mIRadioAdjustmentModel
						.adjustmentStepPrevious();
				break;
			case Constants.MSG_SEEK_INC:// TUNE旋钮右旋（向大微调）
				mReference.get().mIRadioAdjustmentModel.adjustmentStepNext();
				break;
			case Constants.MSG_SEARCH_NEAR_STRONG_RADIO:
				if ((Boolean) msg.obj) {// 播放且显示
					mReference.get().mIRadioPlayModel.playRadio(
							RadioStatus.currentType, msg.arg1);
					if (RadioStatus.hasFocus) {
						mReference.get().mRadioVolumeModel.fadeUpVolume();
					}
				} else {// 只显示不播放
					mReference.get().onRadioPlay(RadioStatus.currentType,
							msg.arg1);
				}
				break;
			case Constants.MSG_SWITCH_FRAGMENT:// 切换Fragment
				mReference.get().switchFragment(msg.arg1);
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * 在应用被切换到后台的时候，Activity可能被回收，
		 * 在回收之前都会执行FragmentActivity中的onSaveInstanceState方法保存所有Fragment的状态
		 * ；重新启动该activity时系统会恢复之前被回收的Activity
		 * ，这个时候FragmentActivity在onCreate里面也会做Fragment的恢复
		 * ，从而导致重叠或getActivity()为null的情况
		 * ；这里的解决方法是在恢复Fragment之前把保存Bundle里面的数据给清除，
		 * 赶在Activity恢复其之前所绑定的Fragment之前清除所有存储在savedInstanceState中的信息。
		 */
		if (savedInstanceState != null) {
			savedInstanceState.putParcelable("android:support:fragments", null);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radio);
		Logger.logD("RadioActivity-----------------------onCreate");

		initModel();
		loadData();
		initView();
		setShowingFragment();
		register();
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (fragment instanceof RadioFragment) {
			mRadioFragment = (RadioFragment) fragment;
		} else if (fragment instanceof FMFragment) {
			mFmFragment = (FMFragment) fragment;
		} else if (fragment instanceof AMFragment) {
			mAmFragment = (AMFragment) fragment;
		} else if (fragment instanceof CollectFragment) {
			mCollectFragment = (CollectFragment) fragment;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Logger.logD("RadioActivity-----------------------onNewIntent");
		setIntent(intent);
	}

	/**
	 * 如果跳转到一个加了属性<item name="android:windowIsTranslucent">true</item>的Activity，
	 * 返回后不会执行onRestart()和onStart()，只会执行onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Logger.logD("RadioActivity-----------------------onResume");
		if (!firstResume) {// 非第一次就执行，即第一次不执行
			/*
			 * 这段代码本来放在onNewIntent中执行，但是有种特殊情况onNewIntent不执行（在收音机界面播放，关屏关功放，蓝牙通话之后
			 * ，回到收音机界面，只执行了onResume方法，没有执行onNewIntent方法），onCreate会申请一次音频焦点。
			 */

			// 进了收音机页，如果没有音频焦点，就要把音频焦点申请回来
			if (!RadioStatus.hasFocus) {
				requestAudioFocus();
			}
			if (isOpened()
					&& RadioStatus.searchNearState == SearchNearStrongChannel.NEITHER
					&& !RadioStatus.isSearchingFM && !RadioStatus.isSearchingAM) {// 防止在搜台时倒车，回来后把声音拉回来
				if (mRadioVolumeModel.getCurrentVolumeRatio() < 1.0f) {// 如果只是切至后台播放，回来之后不要再重新操作声音
					mRadioHandler.removeMessages(Constants.START_PLAY);
					mRadioHandler.sendEmptyMessage(Constants.START_PLAY);
				}
			}
			// 更新SystemUI标题显示
			AppUtil.updateSystemUITitle(this, getString(R.string.app_name),
					((RadioButton) findViewById(rg_left
							.getCheckedRadioButtonId())).getText().toString());
		}
		firstResume = false;// 不是第一次了
		// 重置BottomBar的显示
		updateBottomBarContentByPreferences();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Logger.logD("RadioActivity-----------------------onConfigurationChanged");
		// 应对系统语言变化
		rb_radio.setText(R.string.current);
		rb_fm.setText(R.string.fm);
		rb_am.setText(R.string.am);
		rb_collect.setText(R.string.collect);
		cb_switch.setText(isOpened() ? R.string.close_radio
				: R.string.open_radio);
	}

	/** 初始化一些全局数据 */
	private void loadData() {
		// 获取最大音量
		mRadioVolumeModel.setMaxRadioVolume(ProtocolUtil.getInstance(this)
				.getMaxRadioVolume());

		RadioStatus.currentType = PreferencesUtil.getInstance()
				.getLatestRadioType(this);
		RadioStatus.currentFrequency = PreferencesUtil.getInstance()
				.getLatestRadioFrequency(this);
	}

	/** 初始化Model层 */
	private void initModel() {
		mRadioInfoReceiver = RadioInfoReceiver.getInstance(this);
		mRadioInfoReceiver.registerRadioInfoReceiver();
		mRadioInfoReceiver.addIRadioInfoCallback(this);

		mIRadioSwitchModel = IRadioSwitchModelImp.getInstance(this);
		mIRadioSwitchModel.addIRadioSwitchCallback(this);

		mISearchNearStrongRadioModel = ISearchNearStrongRadioModelImp
				.getInstance(this);
		mISearchNearStrongRadioModel.addISearchNearStrongRadioCallback(this);

		mISwitchFMAMModel = ISwitchFMAMModelImp.getInstance(this);
		mISwitchFMAMModel.addISwitchFMAMCallback(this);

		mIRadioAdjustmentModel = IRadioAdjustmentModelImp.getInstance(this);
		mIRadioAdjustmentModel.addIRadioAdjustmentCallback(this);

		mIRadioPlayModel = IRadioPlayModelImp.getInstance(this);
		mIRadioPlayModel.addIRadioPlayCallback(this);

		mRadioVolumeModel = RadioVolumeModel.getInstance(this);

		mISearchAllFMModel = ISearchAllFMModelImp.getInstance(this);
		mISearchAllFMModel.registerSearchAllFMListener(this);

		mISearchAllAMModel = ISearchAllAMModelImp.getInstance(this);
		mISearchAllAMModel.registerSearchAllAMListener(this);
	}

	private void initView() {
		rg_left = (RadioGroup) findViewById(R.id.rg_left);
		rb_radio = (RadioButton) findViewById(R.id.rb_radio);
		rb_fm = (RadioButton) findViewById(R.id.rb_fm);
		rb_am = (RadioButton) findViewById(R.id.rb_am);
		rb_collect = (RadioButton) findViewById(R.id.rb_collect);
		cb_switch = (CheckBox) findViewById(R.id.cb_switch);

		initBottomBar();

		// 只为点击音
		rb_radio.setOnClickListener(onClickListener);
		rb_fm.setOnClickListener(onClickListener);
		rb_am.setOnClickListener(onClickListener);
		rb_collect.setOnClickListener(onClickListener);

		rg_left.setOnCheckedChangeListener(radioGroupCheckedChangeListener);
		cb_switch
				.setOnCheckedChangeListener(compoundButtonCheckedChangeListener);
		cb_switch.setOnClickListener(onClickListener);// 只为点击音

		// 进了收音机页，就要把音频焦点抢回来
		requestAudioFocus();
		if (SettingsUtil.getInstance().isRadioLatestOpened(this)) {// 最后是打开的
			cb_switch.setChecked(true);
			cb_switch.setText(R.string.close_radio);
		} else {// 最后是关闭的
			cb_switch.setChecked(false);
			cb_switch.setText(R.string.open_radio);
		}
	}

	/** 初始化BottomBar */
	private void initBottomBar() {
		mBottomBar = (android.view.BottomBar) findViewById(R.id.bottomBar);
		// 隐藏右侧返回按钮
		// mBottomBar.setBackBtnVisible(false);
		// 自定义左侧LinearLayout的点击事件
		mBottomBar.setCustomLeftLinearFunc(true);
		mBottomBar
				.setOnBottomBarListener(new android.view.BottomBar.OnBottomBarListener() {

					@Override
					public void onLeftLinearClick(View view) {// 左边整个LinearLayout点击事件（需先设置mBottomBar.setCustomLeftLinearFunc(true);才有效）
						Logger.logD("BottomBar-----------------------onLeftLinearClick");
						// 不做处理
					}

					@Override
					public void onIconClick(View view) {// 左边图标点击事件
						Logger.logD("BottomBar-----------------------onIconClick");
					}

					@Override
					public void onMenuClick(View view) {// Home按钮点击事件（需先设置mBottomBar.setCustomMenuFunc(true);才有效，否则默认为Home键功能）
						Logger.logD("BottomBar-----------------------onMenuClick");
					}

					@Override
					public void onBackClick(View view) {// 返回按钮点击事件（需先设置mBottomBar.setCustomBackFunc(true);才有效，否则默认为Back键功能）
						Logger.logD("BottomBar-----------------------onBackClick");
					}
				});
	}

	/** 更新底部栏显示 */
	private void updateBottomBarContent() {
		if (RadioStatus.hasFocus && !AppUtil.isBTOrMultimediaForeground(this)) {// 有音频焦点并且不是蓝牙应用或者多媒体应用在最顶层时可以刷新底部显示
			setBottomBarPlayType(RadioStatus.currentType);
			setBottomBarFrequency(RadioStatus.currentType,
					RadioStatus.currentFrequency);
		}
	}

	/**
	 * 更新底部栏显示
	 * 
	 * @param frequency
	 */
	private void updateBottomBarContent(int frequency) {
		if (RadioStatus.hasFocus && !AppUtil.isBTOrMultimediaForeground(this)) {// 有音频焦点并且不是蓝牙应用或者多媒体应用在最顶层时可以刷新底部显示
			setBottomBarPlayType(RadioStatus.currentType);
			setBottomBarFrequency(RadioStatus.currentType, frequency);
		}
	}

	/** 读取储存状态更新底部显示 */
	private void updateBottomBarContentByPreferences() {
		if (RadioStatus.hasFocus && !AppUtil.isBTOrMultimediaForeground(this)) {// 有音频焦点并且不是蓝牙应用或者多媒体应用在最顶层时可以刷新底部显示
			int type = PreferencesUtil.getInstance().getLatestRadioType(this);
			setBottomBarPlayType(type);
			setBottomBarFrequency(type, PreferencesUtil.getInstance()
					.getLatestRadioFrequency(this));
		}
	}

	/**
	 * 搜索所有FM或AM时更新底部显示
	 * 
	 * @param frequency
	 */
	private void updateBottomBarContentWhenSearchingAll(int frequency) {
		if (RadioStatus.hasFocus && !AppUtil.isBTOrMultimediaForeground(this)) {// 有音频焦点并且不是蓝牙应用或者多媒体应用在最顶层时可以刷新底部显示
			if (RadioStatus.isSearchingFM) {
				setBottomBarPlayType(Constants.TYPE_FM);
				setBottomBarFrequency(Constants.TYPE_FM, frequency);
			} else if (RadioStatus.isSearchingAM) {
				setBottomBarPlayType(Constants.TYPE_AM);
				setBottomBarFrequency(Constants.TYPE_AM, frequency);
			}
		}
	}

	/** 保存底部栏信息 */
	private void saveBottomBarInfo() {
		mBottomBar.setIcon(android.view.BottomBar.ICON_RADIO);
		mBottomBar.setPackageName(getPackageName());
		mBottomBar.setClassName(getComponentName().getClassName());
	}

	/**
	 * 设置底部类型显示
	 * 
	 * @param type
	 */
	private void setBottomBarPlayType(int type) {
		switch (type) {
		case Constants.TYPE_FM:
			mBottomBar.setFirstText(getString(R.string.fm));
			mBottomBar.setThirdText(getString(R.string.mhz));
			break;
		case Constants.TYPE_AM:
			mBottomBar.setFirstText(getString(R.string.am));
			mBottomBar.setThirdText(getString(R.string.khz));
			break;
		default:
			break;
		}
	}

	/**
	 * 设置底部频点显示
	 * 
	 * @param type
	 * @param frequency
	 */
	private void setBottomBarFrequency(int type, int frequency) {
		switch (type) {
		case Constants.TYPE_FM:
			saveBottomBarInfo();
			if (AppUtil.inCurrentFrequencyRange(frequency)) {
				mBottomBar.setSecondText(AppUtil.formatFloatFrequency(frequency
						/ Constants.FM_MULTIPLE));
			}
			break;
		case Constants.TYPE_AM:
			saveBottomBarInfo();
			if (AppUtil.inCurrentFrequencyRange(frequency)) {
				mBottomBar.setSecondText(String.valueOf(frequency));
			}
			break;
		default:
			break;
		}
	}

	/** 注册 */
	private void register() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SHUTDOWN);
		registerReceiver(mReceiver, filter);
	}

	/** 设置显示记忆的Fragment */
	private void setShowingFragment() {
		int position = PreferencesUtil.getInstance()
				.getCheckedRadioButtonPosition(this);
		switch (position) {
		case Constants.POSITION_RADIO_FRAGMENT:
			rg_left.check(R.id.rb_radio);
			break;
		case Constants.POSITION_FM_FRAGMENT:
			rg_left.check(R.id.rb_fm);
			break;
		case Constants.POSITION_AM_FRAGMENT:
			rg_left.check(R.id.rb_am);
			break;
		case Constants.POSITION_COLLECT_FRAGMENT:
			rg_left.check(R.id.rb_collect);
			break;
		default:
			rg_left.check(R.id.rb_radio);
			break;
		}
	}

	/**
	 * 切换Fragment
	 * 
	 * @param buttonId
	 */
	private void switchFragment(int buttonId) {
		if (mFragmentManager == null) {
			mFragmentManager = getSupportFragmentManager();
		}
		// 开启Fragment事务
		mTransaction = mFragmentManager.beginTransaction();

		switch (buttonId) {
		case R.id.rb_radio:
			if (mRadioFragment == null) {
				mRadioFragment = new RadioFragment();
			}
			mTransaction.replace(R.id.id_container, mRadioFragment);
			AppUtil.updateSystemUITitle(this, R.string.app_name,
					R.string.current);
			PreferencesUtil.getInstance().setCheckedRadioButtonPosition(this,
					Constants.POSITION_RADIO_FRAGMENT);
			break;
		case R.id.rb_fm:
			if (mFmFragment == null) {
				mFmFragment = new FMFragment();
			}
			mTransaction.replace(R.id.id_container, mFmFragment);
			AppUtil.updateSystemUITitle(this, R.string.app_name, R.string.fm);
			PreferencesUtil.getInstance().setCheckedRadioButtonPosition(this,
					Constants.POSITION_FM_FRAGMENT);
			break;
		case R.id.rb_am:
			if (mAmFragment == null) {
				mAmFragment = new AMFragment();
			}
			mTransaction.replace(R.id.id_container, mAmFragment);
			AppUtil.updateSystemUITitle(this, R.string.app_name, R.string.am);
			PreferencesUtil.getInstance().setCheckedRadioButtonPosition(this,
					Constants.POSITION_AM_FRAGMENT);
			break;
		case R.id.rb_collect:
			if (mCollectFragment == null) {
				mCollectFragment = new CollectFragment();
			}
			mTransaction.replace(R.id.id_container, mCollectFragment);
			AppUtil.updateSystemUITitle(this, R.string.app_name,
					R.string.collect);
			PreferencesUtil.getInstance().setCheckedRadioButtonPosition(this,
					Constants.POSITION_COLLECT_FRAGMENT);
			break;
		default:
			break;
		}
		// 事务提交
		// 因为RadioReceiver中有发来切换电台类型的请求事件，而且有可能是在onSaveInstanceState之后接收到，导致异常，所以使用commitAllowingStateLoss
		mTransaction.commitAllowingStateLoss();
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		}
	};

	/** 左侧选项卡监听 */
	private RadioGroup.OnCheckedChangeListener radioGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (group.getId()) {
			case R.id.rg_left:
				// 应对选项卡切换过快发生Tab和Fragment不对应的问题
				mRadioHandler.removeMessages(Constants.MSG_SWITCH_FRAGMENT);
				mRadioHandler.sendMessageDelayed(mRadioHandler.obtainMessage(
						Constants.MSG_SWITCH_FRAGMENT, checkedId, 0), 10);
				break;
			default:
				break;
			}
		}
	};

	/** 开关监听 */
	private CompoundButton.OnCheckedChangeListener compoundButtonCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.cb_switch:// 开关
				mIRadioSwitchModel.switchOnOff(isChecked);
				break;
			default:
				break;
			}
		}
	};

	/** 开始播放 */
	private void startPlay() {
		// 防止点开收音机应用时，默认声音过高而产生砰的一声，先将声音设为0
		ProtocolUtil.getInstance(this).setVolumn(
				android.media.AudioSystem.STREAM_RADIO, 0, 0);
		mRadioVolumeModel.setCurrentVolumeRatio(mRadioVolumeModel
				.getLowestVolume());
		/*
		 * 有可能刚点击返回键，执行onDestroy，声音渐变降低，此时再次打开应用，需要删除之前的关闭声音消息。
		 * 而重新创建Activity后RadioHandler操作不了之前的消息队列，所以需要将声音操作独立出去。
		 */
		mRadioVolumeModel.removeMessages(Constants.FADE_DOWM);
		mRadioVolumeModel.removeMessages(Constants.CLOSE_RADIO_VOLUME);
		mIRadioPlayModel.playRadio(RadioStatus.currentType,
				RadioStatus.currentFrequency);
		mRadioVolumeModel.fadeUpVolume();
	}

	/** 停止播放 */
	private void stopPlay() {
		mRadioHandler.removeMessages(Constants.START_PLAY);
		mRadioVolumeModel.fadeDownVolume();
		// 音量降为0后发送closeRadioVol请求
		int fade_down_duration = (int) ((1f / RadioVolumeModel.volume_step_sub) * RadioVolumeModel.fade_down_delayMillis);
		radioOff(fade_down_duration);
	}

	/** 音频焦点监听器 */
	private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:// 暂时失去AudioFocus，但是可以继续播放（如导航时），不过要降低音量。
				Logger.logD("RadioActivity-----------------------AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
				// RadioStatus.hasFocus = false;
				// 计算导航混音比例（默认7）
				int ratio = Settings.System.getInt(getContentResolver(),
						"semisky_car_navmixing", 7);
				mRadioVolumeModel
						.setLowestVolume((88 - (ratio - 1) * 8) / 100f);
				mRadioVolumeModel.fadeDownVolume();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去了AudioFocus,但很快会重新得到焦点（如来电时），必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源。
				Logger.logD("RadioActivity-----------------------AUDIOFOCUS_LOSS_TRANSIENT");
				RadioStatus.hasFocus = false;
				mRadioVolumeModel.setLowestVolume(0);
				mRadioHandler.removeMessages(Constants.STOP_PLAY);
				mRadioHandler.sendEmptyMessage(Constants.STOP_PLAY);
				break;
			case AudioManager.AUDIOFOCUS_LOSS:// 失去AudioFocus，并将会持续很长的时间（如播放音乐或视频时）
				Logger.logD("RadioActivity-----------------------AUDIOFOCUS_LOSS");
				// 为了应对易连抢完音频焦点就走，当失去永久焦点时，先判断当前界面是否还是收音机界面，如果是，重新抢回来。
				if (AppUtil.isActivityForeground(RadioActivity.this,
						RadioActivity.this.getComponentName().getClassName())) {
					Logger.logD("RadioActivity-----------------------当前还在收音机界面，被抢走永久焦点，重新申请回来！");
					requestAudioFocus();
					return;
				}
				RadioStatus.hasFocus = false;
				mRadioVolumeModel.setLowestVolume(0);
				mRadioHandler.removeMessages(Constants.STOP_PLAY);
				mRadioHandler.sendEmptyMessage(Constants.STOP_PLAY);
				// 一失去永久焦点就释放音频焦点
				abandonAudioFocus();
				break;
			case AudioManager.AUDIOFOCUS_GAIN:// 获得AudioFocus
				Logger.logD("RadioActivity-----------------------AUDIOFOCUS_GAIN");
				RadioStatus.hasFocus = true;
				if (!isOpened()
						|| RadioStatus.searchNearState != SearchNearStrongChannel.NEITHER
						|| RadioStatus.isSearchingFM
						|| RadioStatus.isSearchingAM) {// 如果没有打开，或者正在搜索
					return;
				}
				if (mRadioVolumeModel.getLowestVolume() > 0) {// 说明是AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK，拉高声音即可
					mRadioVolumeModel.fadeUpVolume();
				} else {
					mRadioHandler.removeMessages(Constants.START_PLAY);
					mRadioHandler.sendEmptyMessage(Constants.START_PLAY);
				}
				break;
			default:
				break;
			}
		}
	};

	/** 申请音频焦点 */
	private int requestAudioFocus() {
		Logger.logD("RadioActivity-----------------------requestAudioFocus");
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		}
		int audioFocusState = mAudioManager.requestAudioFocus(
				mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);// 获取音频永久焦点
		RadioStatus.hasFocus = true;
		// 注册实体按键监听器
		ProtocolUtil.getInstance(this).registerKeyPressListener(
				mIKeyPressInterface);
		return audioFocusState;
	}

	/** 释放音频焦点 */
	private void abandonAudioFocus() {
		Logger.logD("RadioActivity-----------------------abandonAudioFocus");
		if (mAudioManager != null) {
			mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		}
		RadioStatus.hasFocus = false;
		// 释放实体按键监听器
		ProtocolUtil.getInstance(this).unregisterKeyPressListener(
				mIKeyPressInterface);
	}

	/**
	 * 是否打开了收音机
	 * 
	 * @return
	 */
	public boolean isOpened() {
		return SettingsUtil.getInstance().isRadioLatestOpened(this);
	}

	/**
	 * 收音机声音打开
	 * 
	 * @param delayMillis
	 *            延迟时长
	 */
	private void radioOn(long delayMillis) {
		mRadioVolumeModel.removeMessages(Constants.CLOSE_RADIO_VOLUME);
		mRadioVolumeModel.removeMessages(Constants.OPEN_RADIO_VOLUME);
		mRadioVolumeModel.sendEmptyMessageDelayed(Constants.OPEN_RADIO_VOLUME,
				delayMillis);
	}

	/**
	 * 收音机声音关闭
	 * 
	 * @param delayMillis
	 *            延迟时长
	 */
	private void radioOff(long delayMillis) {
		mRadioVolumeModel.removeMessages(Constants.OPEN_RADIO_VOLUME);
		mRadioVolumeModel.removeMessages(Constants.CLOSE_RADIO_VOLUME);
		mRadioVolumeModel.sendEmptyMessageDelayed(Constants.CLOSE_RADIO_VOLUME,
				delayMillis);
	}

	/**
	 * 当搜索上下一个强信号台时发送消息
	 * 
	 * @param isFinish
	 *            是否手动播放某个频点（搜索到上下一个强信号台会自动播放）
	 * @param frequency
	 *            显示频点
	 */
	private void sendSearchNearStrongRadioResultMessage(boolean isFinish,
			int frequency) {
		mRadioHandler.obtainMessage(Constants.MSG_SEARCH_NEAR_STRONG_RADIO,
				frequency, 0, isFinish).sendToTarget();
	}

	/** 广播接收器 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Logger.logD("RadioActivity-----------------------receiver broadcast, action = "
					+ action);
			if (Intent.ACTION_SHUTDOWN.equals(action)) {// 关机广播
				mRadioHandler.sendEmptyMessage(Constants.STOP_PLAY);
			}
		}
	};

	/** 实体按钮前后台监听 */
	private android.os.IKeyPressInterface mIKeyPressInterface = new android.os.IKeyPressInterface.Stub() {

		public void onKeyPressed(int keyCode, int mode) {
			switch (keyCode) {
			case KEY_PREV:// 按键上一曲
				Logger.logD("KEY_PREV------------------------上一个强信号台, keyCode = "
						+ keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_DEC);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_INC);
				mRadioHandler.sendEmptyMessage(Constants.MSG_RADIO_PREVIOUS);
				break;
			case KEY_NEXT:// 按键下一曲
				Logger.logD("KEY_NEXT------------------------下一个强信号台, keyCode = "
						+ keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_DEC);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_INC);
				mRadioHandler.sendEmptyMessage(Constants.MSG_RADIO_NEXT);
				break;
			case KEY_SEEK_DEC:// TUNE左旋
				Logger.logD("KEY_SEEK_DEC------------------------TUNE左旋, keyCode = "
						+ keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_DEC);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_INC);
				mRadioHandler.sendEmptyMessage(Constants.MSG_SEEK_DEC);
				break;
			case KEY_SEEK_INC:// TUNE右旋
				Logger.logD("KEY_SEEK_INC------------------------TUNE右旋, keyCode = "
						+ keyCode + " mode = " + mode);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_PREVIOUS);
				mRadioHandler.removeMessages(Constants.MSG_RADIO_NEXT);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_DEC);
				mRadioHandler.removeMessages(Constants.MSG_SEEK_INC);
				mRadioHandler.sendEmptyMessage(Constants.MSG_SEEK_INC);
				break;
			case KEY_POWER:// Power按键
				if (RadioStatus.isSearchingFM || RadioStatus.isSearchingAM) {// 如果正在自动搜索所有电台，则不响应TUNE按键
					return;
				}

				if (isOpened()) {
					Logger.logD("KEYCODE_F5-----------------------旋钮里的Enter按键, keyCode = "
							+ keyCode + "---close Radio");
					runOnUiThread(new Runnable() {
						public void run() {
							// 关闭收音机
							cb_switch.setChecked(false);
						}
					});
				} else {
					Logger.logD("KEYCODE_F5-----------------------旋钮里的Enter按键, keyCode = "
							+ keyCode + "---open radio");
					runOnUiThread(new Runnable() {
						public void run() {
							// 关闭收音机
							cb_switch.setChecked(true);
						}
					});
				}
				break;
			default:
				break;
			}
		}

		public String onGetAppInfo() {// 返回各应用名字标记
			return "radio";
		}

	};

	@Override
	protected void onPause() {
		super.onPause();
		Logger.logD("RadioActivity-----------------------onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Logger.logD("RadioActivity-----------------------onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.logD("RadioActivity-----------------------onDestroy");
		mRadioHandler.removeCallbacksAndMessages(null);
		// 停止播放，声音渐渐关闭
		mRadioHandler.sendEmptyMessage(Constants.STOP_PLAY);
		// 释放音频焦点
		abandonAudioFocus();
		unregister();
		removeModel();
	}

	/** 注销 */
	private void unregister() {
		unregisterReceiver(mReceiver);
	}

	/** 注销Model回调 */
	private void removeModel() {
		mISearchAllFMModel.unregisterSearchAllFMListener(this);
		mISearchAllAMModel.unregisterSearchAllAMListener(this);
		mIRadioPlayModel.removeIRadioPlayCallback(this);
		mIRadioAdjustmentModel.removeIRadioAdjustmentCallback(this);
		mISwitchFMAMModel.removeISwitchFMAMCallback(this);
		mISearchNearStrongRadioModel.removeISearchNearStrongRadioCallback(this);
		mIRadioSwitchModel.removeIRadioSwitchCallback(this);
		mRadioInfoReceiver.removeIRadioInfoCallback(this);
		mRadioInfoReceiver.unregisterRadioInfoReceiver();
	}

	@Override
	public void onRadioSwitchOn() {// 开关打开回调
		mRadioHandler.removeMessages(Constants.MSG_SEARCH_NEAR_STRONG_RADIO);
		mRadioHandler.removeMessages(Constants.START_PLAY);
		mRadioHandler.sendEmptyMessage(Constants.START_PLAY);
		cb_switch.setText(R.string.close_radio);
	}

	@Override
	public void onRadioSwitchOff() {// 开关关闭回调
		mRadioHandler.removeMessages(Constants.MSG_SEARCH_NEAR_STRONG_RADIO);
		mRadioVolumeModel.setLowestVolume(0);
		mRadioHandler.removeMessages(Constants.STOP_PLAY);
		mRadioHandler.sendEmptyMessage(Constants.STOP_PLAY);
		cb_switch.setText(R.string.open_radio);
	}

	/**
	 * 搜索上下一个强信号台结果回调（不在主线程中）（情况一：无上一个强信号台时，返回最小频点，无下一个强信号台时，返回最大频点；情况二：在搜索过程中，
	 * 要求返回进行中的频点，包括信号强度为0的和强度大于0的结果频点）
	 */
	@Override
	public void onRadioInfoSearchNearStrongRadioResult(boolean isFinish,
			int frequency) {
		sendSearchNearStrongRadioResultMessage(isFinish, frequency);
	}

	/**
	 * 搜索上下一个强信号台准备阶段回调（情况一：正在搜索上下一个强信号台，再点就是中断返回；情况二：已是最小或最大频点，返回对应最大或最小频点）
	 */
	@Override
	public void onSearchNearStrongRadioResult(boolean isFinish, int frequency) {
		sendSearchNearStrongRadioResultMessage(isFinish, frequency);
	}

	@Override
	public void onSwitchFMAMPrepare(boolean resetFragment) {// 准备开始切换回调
		if (resetFragment) {
			// 先回到RadioFragment界面
			rg_left.check(R.id.rb_radio);
		}
	}

	@Override
	public void beginSwitchFMToFM() {// FM切换到FM回调
	}

	@Override
	public void beginSwitchFMToAM() {// FM切换到AM回调
	}

	@Override
	public void beginSwitchAMToFM() {// AM切换到FM回调
	}

	@Override
	public void beginSwitchAMToAM() {// AM切换到AM回调
	}

	@Override
	public void beginSwitchFMToFMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，FM切换到FM回调
	}

	@Override
	public void beginSwitchFMToAMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，FM切换到AM回调
		// 停止动态刷新底部显示
		mRadioHandler.removeMessages(Constants.MSG_SEARCH_NEAR_STRONG_RADIO);
	}

	@Override
	public void beginSwitchAMToFMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，AM切换到FM回调
		// 停止动态刷新底部显示
		mRadioHandler.removeMessages(Constants.MSG_SEARCH_NEAR_STRONG_RADIO);
	}

	@Override
	public void beginSwitchAMToAMWhenSearchNearStrongRadio() {// 搜索上下一个强信号台时，AM切换到AM回调
	}

	@Override
	public void onRadioAdjustmentStepPrevious() {// 向上微调频点回调
		// 刷新底部显示
		updateBottomBarContent();
	}

	@Override
	public void onRadioAdjustmentStepNext() {// 向下微调频点回调
		// 刷新底部显示
		updateBottomBarContent();
	}

	@Override
	public void onRadioPlay(int radioType, int frequency) {// 播放某个频点回调
		// 刷新底部显示
		updateBottomBarContent(frequency);
	}

	@Override
	public void notifyObserversClearFMList() {
	}

	@Override
	public void notifyObserversSearchAllFMFinish() {
		updateBottomBarContentByPreferences();
		if (RadioStatus.hasFocus) {
			// 声音拉高
			mRadioVolumeModel.fadeUpVolume();
		}
	}

	@Override
	public void notifyObserversSearchAllFMUnFinish(int frequency) {
		updateBottomBarContentWhenSearchingAll(frequency);
	}

	@Override
	public void notifyObserversSearchAllFMInterrupt() {
		updateBottomBarContentByPreferences();
		if (RadioStatus.hasFocus) {
			// 声音拉高
			mRadioVolumeModel.fadeUpVolume();
		}
	}

	@Override
	public void notifyObserversSearchAllFMTimeout() {
		updateBottomBarContentByPreferences();
		if (RadioStatus.hasFocus) {
			// 声音拉高
			mRadioVolumeModel.fadeUpVolume();
		}
	}

	@Override
	public void notifyObserversClearAMList() {
	}

	@Override
	public void notifyObserversSearchAllAMFinish() {
		updateBottomBarContentByPreferences();
		if (RadioStatus.hasFocus) {
			// 声音拉高
			mRadioVolumeModel.fadeUpVolume();
		}
	}

	@Override
	public void notifyObserversSearchAllAMUnFinish(int frequency) {
		updateBottomBarContentWhenSearchingAll(frequency);
	}

	@Override
	public void notifyObserversSearchAllAMInterrupt() {
		updateBottomBarContentByPreferences();
		if (RadioStatus.hasFocus) {
			// 声音拉高
			mRadioVolumeModel.fadeUpVolume();
		}
	}

	@Override
	public void notifyObserversSearchAllAMTimeout() {
		updateBottomBarContentByPreferences();
		if (RadioStatus.hasFocus) {
			// 声音拉高
			mRadioVolumeModel.fadeUpVolume();
		}
	}
}
