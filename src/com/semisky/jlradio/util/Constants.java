package com.semisky.jlradio.util;

/**
 * 全局常量
 * 
 * @author Anter
 * @date 2016-10-26
 * 
 */
public class Constants {
	// Fragment位置标识
	public static final int POSITION_RADIO_FRAGMENT = 0;
	public static final int POSITION_FM_FRAGMENT = 1;
	public static final int POSITION_AM_FRAGMENT = 2;
	public static final int POSITION_COLLECT_FRAGMENT = 3;
	// 频道类型
	public static final int TYPE_FM = 0;
	public static final int TYPE_AM = 1;

	// FM倍数
	public static final float FM_MULTIPLE = 100f;
	// FM步进
	public static final int FM_STEP = 10;
	// AM步进
	public static final int AM_STEP = 9;

	// FM/AM频率范围
	public static final int FMMAX = 10800;
	public static final int FMMIN = 8750;
	public static final int AMMAX = 1629;
	public static final int AMMIN = 531;
	// 默认频道
	public static final int DEFAULT_FM_FREQUENCY = FMMIN;
	public static final int DEFAULT_AM_FREQUENCY = AMMIN;
	// 搜索超时时间
	public static final int DURATION_SEARCH_TIME_OUT = 30000;
	// 搜索结束码
	public static final int SEARCH_OVER_CODE = 65535;

	// 弹框宽高
	public static final int DIALOG_WIDTH = 650;
	public static final int DIALOG_HEIGHT = 393;
	public static final float DIALOG_DIMAMOUNT = 0.9f;

	// handler msg
	public static final int START_PLAY = 0;
	public static final int STOP_PLAY = 1;
	public static final int OPEN_RADIO_VOLUME = 2;
	public static final int CLOSE_RADIO_VOLUME = 3;
	public static final int FADE_DOWM = 4;
	public static final int FADE_UP = 5;
	public static final int MSG_RADIO_PREVIOUS = 6;
	public static final int MSG_RADIO_NEXT = 7;
	public static final int MSG_SEEK_DEC = 8;
	public static final int MSG_SEEK_INC = 9;
	public static final int MSG_SEARCH_NEAR_STRONG_RADIO = 10;
	public static final int MSG_SWITCH_FRAGMENT = 11;

	// SystemUI key
	public static final String KEY_FIRST_PAGER = "FirstPage";
	public static final String KEY_SECOND_PAGER = "SecondPage";
	public static final String KEY_THIRD_PAGER = "thirdPage";

	// 广播action
	
	// 发送广播给SystemUI更新标题
	public static final String ACTION_UPDATE_SYSTEMUI_TITLE = "com.semisky.nl.currentPage";
	// 实体Radio按钮按下发来广播
	public static final String ACTION_KEYEVENT_RADIO = "com.semisky.keyevent.RADIO";
	
	// 应用包名
	public static final String PKG_BT = "com.semisky.bydbluetooth.bluetooth";// 蓝牙
	public static final String PKG_MULTIMEDIA = "com.semisky.jlmultimedia";// 多媒体
}
