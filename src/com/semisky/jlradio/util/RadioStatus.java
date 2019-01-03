package com.semisky.jlradio.util;

public class RadioStatus {
	public static boolean hasFocus = true;// 是否获取了AudioFocus焦点
	public static SearchNearStrongChannel searchNearState = SearchNearStrongChannel.NEITHER;// 搜索邻近强信号台状态（默认既不往前也不往后搜索）
	public static int currentType = Constants.TYPE_FM;// FM或者AM
	public static int currentFrequency = Constants.FMMIN;// 当前频道值
	public static int searchNearShowingFrequency = Constants.FMMIN;// 搜索上下一个强信号台时显示的频点
	public static boolean isSearchingFM = false;// 正在搜索FM
	public static boolean isSearchingAM = false;// 正在搜索AM
	public static boolean isSearchingInterrupted = false;// 搜索所有FM或AM是否被打断了

	public enum SearchNearStrongChannel {// 搜索邻近的一个强信号台
		PREVIOUS, // 上一个
		NEXT, // 下一个
		NEITHER // 都不是
	}
}
