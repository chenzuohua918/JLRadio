package com.semisky.jlradio.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.semisky.jlradio.bean.Channel;
import com.semisky.jlradio.util.Constants;

/**
 * FM电台数据库管理类
 * 
 * @author Anter
 * 
 */
public class FMChannelDBManager {
	private static FMChannelDBManager instance;
	private Context mContext;

	public static synchronized FMChannelDBManager getInstance(Context context) {
		if (instance == null) {
			instance = new FMChannelDBManager(context);
		}
		return instance;
	}

	public FMChannelDBManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 是否数据库中有该频道
	 * 
	 * @return
	 */
	public boolean hasFMChannels() {
		boolean result = false;
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					DBConfiguration.TableFMConfiguration.CONTENT_URI, null,
					null, null, null);
			if (cursor != null) {
				result = cursor.getCount() != 0;
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 查询频道数据
	 * 
	 * @param channelFrequency
	 * @return
	 */
	public Channel queryFMChannel(int channelFrequency) {
		Channel channel = null;
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					ContentUris.withAppendedId(
							DBConfiguration.TableFMConfiguration.CONTENT_URI,
							ChannelContentProvider.FM_ITEM_CODE),
					null,
					DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY
							+ "=?",
					new String[] { String.valueOf(channelFrequency) }, null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					channel = new Channel();
					channel.setChannelFrequency(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY)));
					channel.setChannelSignal(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL)));
					channel.setChannelType(Constants.TYPE_FM);
				}
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return channel;
	}

	/**
	 * 是否已经收藏了该频道
	 * 
	 * @param channelFrequency
	 * @return
	 */
	public boolean isChannelInDB(int channelFrequency) {
		return queryFMChannel(channelFrequency) != null;
	}

	/**
	 * 查询频道数据
	 * 
	 * @return
	 */
	public Cursor getFMChannelsCursor() {
		return mContext.getContentResolver().query(
				DBConfiguration.TableFMConfiguration.CONTENT_URI, null, null,
				null, null);
	}

	/**
	 * 查询频道数据
	 * 
	 * @return
	 */
	public List<Channel> queryFMChannels() {
		List<Channel> channels = null;
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					DBConfiguration.TableFMConfiguration.CONTENT_URI, null,
					null, null, null);
			if (cursor != null) {
				channels = new ArrayList<Channel>();
				while (cursor.moveToNext()) {
					Channel channel = new Channel();
					channel.setChannelFrequency(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY)));
					channel.setChannelSignal(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL)));
					channel.setChannelType(Constants.TYPE_FM);
					channels.add(channel);
				}
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return channels;
	}

	/**
	 * 更新频道信息
	 * 
	 * @param channel
	 * @return
	 */
	public boolean updateFMChannel(Channel channel) {
		ContentValues values = new ContentValues();
		values.put(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY,
				String.valueOf(channel.getChannelFrequency()));
		values.put(DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL,
				channel.getChannelSignal());
		int result = mContext.getContentResolver().update(
				ContentUris.withAppendedId(
						DBConfiguration.TableFMConfiguration.CONTENT_URI,
						ChannelContentProvider.FM_ITEM_CODE), values,
				DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY + "=?",
				new String[] { String.valueOf(channel.getChannelFrequency()) });
		return result != -1;
	}

	/**
	 * 插入频道信息
	 * 
	 * @param channelFrequency
	 * @param channelSignal
	 */
	public void insertFMChannel(int channelFrequency, int channelSignal) {
		if (isChannelInDB(channelFrequency)) {// 如果已经插入，则只更新
			Channel channel = new Channel();
			channel.setChannelFrequency(channelFrequency);
			channel.setChannelSignal(channelSignal);
			updateFMChannel(channel);
		} else {// 如果不在数据库，则插入
			ContentValues values = new ContentValues();
			values.put(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY,
					String.valueOf(channelFrequency));
			values.put(DBConfiguration.TableFMConfiguration.CHANNEL_SIGNAL,
					channelSignal);
			mContext.getContentResolver().insert(
					DBConfiguration.TableFMConfiguration.CONTENT_URI, values);
		}
	}

	/**
	 * 插入频道信息
	 * 
	 * @param channel
	 */
	public void insertFMChannel(Channel channel) {
		insertFMChannel(channel.getChannelFrequency(),
				channel.getChannelSignal());
	}

	/**
	 * 删除所有频道
	 */
	public void deleteAllFMChannels() {
		mContext.getContentResolver().delete(
				DBConfiguration.TableFMConfiguration.CONTENT_URI, null, null);
	}

	/**
	 * 根据频率删除频道
	 * 
	 * @param frequency
	 */
	public void deleteFMChannel(int frequency) {
		mContext.getContentResolver().delete(
				ContentUris.withAppendedId(
						DBConfiguration.TableFMConfiguration.CONTENT_URI,
						ChannelContentProvider.FM_ITEM_CODE),
				DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY + "=?",
				new String[] { String.valueOf(frequency) });
	}
}
