package com.semisky.jlradio.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.semisky.jlradio.bean.Channel;

/**
 * 收藏电台数据库管理类
 * 
 * @author Anter
 * 
 */
public class CollectChannelDBManager {
	private static CollectChannelDBManager instance;
	private Context mContext;

	public static synchronized CollectChannelDBManager getInstance(
			Context context) {
		if (instance == null) {
			instance = new CollectChannelDBManager(context);
		}
		return instance;
	}

	public CollectChannelDBManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 是否收藏了频道
	 * 
	 * @return
	 */
	public boolean hasCollectChannels() {
		boolean result = false;
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					DBConfiguration.TableCollectConfiguration.CONTENT_URI,
					null, null, null, null);
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
	public Channel queryCollectChannel(int channelFrequency) {
		Channel channel = null;
		Cursor cursor = null;
		try {
			cursor = mContext
					.getContentResolver()
					.query(ContentUris.withAppendedId(
							DBConfiguration.TableCollectConfiguration.CONTENT_URI,
							ChannelContentProvider.COLLECT_ITEM_CODE),
							null,
							DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY
									+ "=?",
							new String[] { String.valueOf(channelFrequency) },
							null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					channel = new Channel();
					channel.setChannelFrequency(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY)));
					channel.setChannelSignal(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_SIGNAL)));
					channel.setChannelType(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_TYPE)));
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
		return queryCollectChannel(channelFrequency) != null;
	}

	/**
	 * 查询频道数据
	 * 
	 * @return
	 */
	public Cursor getCollectChannelsCursor() {
		return mContext.getContentResolver().query(
				DBConfiguration.TableCollectConfiguration.CONTENT_URI, null,
				null, null, null);
	}

	/**
	 * 查询频道数据
	 * 
	 * @return
	 */
	public List<Channel> queryCollectChannels() {
		List<Channel> channels = null;
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(
					DBConfiguration.TableCollectConfiguration.CONTENT_URI,
					null, null, null, null);
			if (cursor != null) {
				channels = new ArrayList<Channel>();
				while (cursor.moveToNext()) {
					Channel channel = new Channel();
					channel.setChannelFrequency(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY)));
					channel.setChannelSignal(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_SIGNAL)));
					channel.setChannelType(cursor.getInt(cursor
							.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_TYPE)));
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
	public boolean updateCollectChannel(Channel channel) {
		ContentValues values = new ContentValues();
		values.put(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY,
				String.valueOf(channel.getChannelFrequency()));
		values.put(DBConfiguration.TableCollectConfiguration.CHANNEL_SIGNAL,
				channel.getChannelSignal());
		values.put(DBConfiguration.TableCollectConfiguration.CHANNEL_TYPE,
				channel.getChannelType());
		Uri updateUri = ContentUris.withAppendedId(
				DBConfiguration.TableCollectConfiguration.CONTENT_URI,
				ChannelContentProvider.COLLECT_ITEM_CODE);
		int result = mContext.getContentResolver().update(
				updateUri,
				values,
				DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY
						+ "=?",
				new String[] { String.valueOf(channel.getChannelFrequency()) });
		return result != -1;
	}

	/**
	 * 插入频道信息
	 * 
	 * @param channelFrequency
	 * @param channelType
	 * @param channelSignal
	 */
	public void insertCollectChannel(int channelFrequency, int channelSignal,
			int channelType) {
		if (isChannelInDB(channelFrequency)) {// 如果已经插入，则只更新
			Channel channel = new Channel();
			channel.setChannelFrequency(channelFrequency);
			channel.setChannelSignal(channelSignal);
			channel.setChannelType(channelType);
			updateCollectChannel(channel);
		} else {
			ContentValues values = new ContentValues();
			values.put(
					DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY,
					String.valueOf(channelFrequency));
			values.put(
					DBConfiguration.TableCollectConfiguration.CHANNEL_SIGNAL,
					channelSignal);
			values.put(DBConfiguration.TableCollectConfiguration.CHANNEL_TYPE,
					channelType);
			mContext.getContentResolver().insert(
					DBConfiguration.TableCollectConfiguration.CONTENT_URI,
					values);
		}
	}

	/**
	 * 插入频道信息
	 * 
	 * @param channel
	 */
	public void insertCollectChannel(Channel channel) {
		insertCollectChannel(channel.getChannelFrequency(),
				channel.getChannelSignal(), channel.getChannelType());
	}

	/**
	 * 删除所有频道
	 */
	public void deleteAllCollectChannels() {
		mContext.getContentResolver().delete(
				DBConfiguration.TableCollectConfiguration.CONTENT_URI, null,
				null);
	}

	/**
	 * 根据频率删除频道
	 * 
	 * @param frequency
	 */
	public void deleteCollectChannel(int frequency) {
		mContext.getContentResolver().delete(
				ContentUris.withAppendedId(
						DBConfiguration.TableCollectConfiguration.CONTENT_URI,
						ChannelContentProvider.COLLECT_ITEM_CODE),
				DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY
						+ "=?", new String[] { String.valueOf(frequency) });
	}
}
