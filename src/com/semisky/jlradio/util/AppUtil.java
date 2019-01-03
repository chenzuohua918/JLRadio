package com.semisky.jlradio.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.SoundEffectConstants;
import android.view.View;

public class AppUtil {
	/**
	 * 发送广播给SystemUI更新标题
	 * 
	 * @param context
	 * @param firstRes
	 * @param secondRes
	 * @param thirdRes
	 */
	public static void updateSystemUITitle(Context context, int firstRes,
			int secondRes, int thirdRes) {
		Intent intent = new Intent(Constants.ACTION_UPDATE_SYSTEMUI_TITLE);
		intent.putExtra(Constants.KEY_FIRST_PAGER, context.getString(firstRes));
		intent.putExtra(Constants.KEY_SECOND_PAGER,
				context.getString(secondRes));
		intent.putExtra(Constants.KEY_THIRD_PAGER, context.getString(thirdRes));
		context.sendBroadcast(intent);
	}

	/**
	 * 发送广播给SystemUI更新标题
	 * 
	 * @param context
	 * @param firstRes
	 * @param secondRes
	 */
	public static void updateSystemUITitle(Context context, int firstRes,
			int secondRes) {
		Intent intent = new Intent(Constants.ACTION_UPDATE_SYSTEMUI_TITLE);
		intent.putExtra(Constants.KEY_FIRST_PAGER, context.getString(firstRes));
		intent.putExtra(Constants.KEY_SECOND_PAGER,
				context.getString(secondRes));
		intent.putExtra(Constants.KEY_THIRD_PAGER, "");
		context.sendBroadcast(intent);
	}

	/**
	 * 发送广播给SystemUI更新标题
	 * 
	 * @param context
	 * @param firstText
	 * @param secondText
	 * @param thirdText
	 */
	public static void updateSystemUITitle(Context context, String firstText,
			String secondText, String thirdText) {
		Intent intent = new Intent(Constants.ACTION_UPDATE_SYSTEMUI_TITLE);
		intent.putExtra(Constants.KEY_FIRST_PAGER, firstText);
		intent.putExtra(Constants.KEY_SECOND_PAGER, secondText);
		intent.putExtra(Constants.KEY_THIRD_PAGER, thirdText);
		context.sendBroadcast(intent);
	}

	/**
	 * 发送广播给SystemUI更新标题
	 * 
	 * @param context
	 * @param firstText
	 * @param secondText
	 */
	public static void updateSystemUITitle(Context context, String firstText,
			String secondText) {
		Intent intent = new Intent(Constants.ACTION_UPDATE_SYSTEMUI_TITLE);
		intent.putExtra(Constants.KEY_FIRST_PAGER, firstText);
		intent.putExtra(Constants.KEY_SECOND_PAGER, secondText);
		intent.putExtra(Constants.KEY_THIRD_PAGER, "");
		context.sendBroadcast(intent);
	}

	/**
	 * 判断某个应用是否在前台
	 * 
	 * @param context
	 * @param packageName
	 *            应用包名
	 * @return
	 */
	public static boolean isAppForeground(Context context, String packageName) {
		if (context == null || TextUtils.isEmpty(packageName)) {
			return false;
		}
		List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
		if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
			ComponentName cpn = runningTaskInfos.get(0).topActivity;
			if (packageName.equals(cpn.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断某个界面是否在前台
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isActivityForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className)) {
			return false;
		}
		List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
		if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
			ComponentName cpn = runningTaskInfos.get(0).topActivity;
			if (className.equals(cpn.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否蓝牙或者多媒体在前台
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isBTOrMultimediaForeground(Context context) {
		return isAppForeground(context, Constants.PKG_BT)
				|| isAppForeground(context, Constants.PKG_MULTIMEDIA);
	}

	/**
	 * 跳转应用（进入主Activity）
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void startActvity(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return;
		}
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(
				packageName);
		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Logger.logE("Unknow packageName!");
		}
	}

	/**
	 * 跳转应用（进入对应Activity）
	 * 
	 * @param context
	 * @param packageName
	 * @param className
	 */
	public static void startActivity(Context context, String packageName,
			String className) {
		if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
			return;
		}
		Intent intent = new Intent();
		intent.setClassName(packageName, className);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 跳转Activity
	 * 
	 * @param context
	 * @param cls
	 */
	public static void startActivity(Context context, Class<?> cls) {
		Intent intent = new Intent(context, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 点击音
	 * 
	 * @param view
	 */
	public static void playClickSound(View view) {
		view.playSoundEffect(SoundEffectConstants.CLICK);
	}

	/**
	 * 频点是否在规定范围内
	 * 
	 * @param type
	 * @param frequency
	 * @return
	 */
	public static boolean inFrequencyRange(int frequency) {
		return inFMFrequencyRange(frequency) || inAMFrequencyRange(frequency);
	}

	/**
	 * 频点是否在当前类型频点范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inCurrentFrequencyRange(int frequency) {
		switch (RadioStatus.currentType) {
		case Constants.TYPE_FM:
			return inFMFrequencyRange(frequency);
		case Constants.TYPE_AM:
			return inAMFrequencyRange(frequency);
		default:
			break;
		}
		return false;
	}

	/**
	 * 频点是否在FM规定范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inFMFrequencyRange(int frequency) {
		return frequency >= Constants.FMMIN && frequency <= Constants.FMMAX;
	}

	/**
	 * 频点是否在AM规定范围内
	 * 
	 * @param frequency
	 * @return
	 */
	public static boolean inAMFrequencyRange(int frequency) {
		return frequency >= Constants.AMMIN && frequency <= Constants.AMMAX;
	}

	/**
	 * 保留一位小数
	 * 
	 * @param frequency
	 * @return
	 */
	public static String formatFloatFrequency(float frequency) {
		return new DecimalFormat(".0").format(frequency);
	}

	/**
	 * 四舍五入取整
	 * 
	 * @param f
	 * @return
	 */
	public static int roundOff(float f) {
		return new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP)
				.intValue();
	}

}
