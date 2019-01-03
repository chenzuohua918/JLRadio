package com.semisky.jlradio.dao;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * ContentProvider属性常量
 * 
 * @author Anter
 * @date 2016-10-26
 * 
 */
public class DBConfiguration {
	public static final String AUTHORITY = "com.semisky.jlradio.dao.database";
	public static final String DATABASE_NAME = "channel.db";
	public static final int DATABASE_VERSION = 1;

	/*
	 * 收藏电台数据库常量
	 */
	public static class TableCollectConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "collect_channels";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ DBConfiguration.AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.semisky.collectChannelList";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.semisky.collectChannelItem";
		public static final String CHANNEL_FREQUENCY = "frequency";
		public static final String CHANNEL_TYPE = "type";
		public static final String CHANNEL_SIGNAL = "signal";
		public static final String DEFAULT_SORT_ORDER = "type Asc, frequency Asc";// 优先按类型排序（先FM再AM），再按频道值排序（由小到大）
	}

	/*
	 * FM电台数据库常量
	 */
	public static class TableFMConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "fm_channels";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ DBConfiguration.AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.semisky.fmChannelList";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.semisky.fmChannelItem";
		public static final String CHANNEL_FREQUENCY = "frequency";
		public static final String CHANNEL_SIGNAL = "signal";
		public static final String DEFAULT_SORT_ORDER = "signal Desc";
		public static final int CHANNEL_LIMIT = 20;// 查询数据条数限制
	}

	/*
	 * AM电台数据库常量
	 */
	public static class TableAMConfiguration implements BaseColumns {
		public static final String TABLE_NAME = "am_channels";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ DBConfiguration.AUTHORITY + "/" + TABLE_NAME);
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.semisky.amChannelList";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.semisky.amChannelItem";
		public static final String CHANNEL_FREQUENCY = "frequency";
		public static final String CHANNEL_SIGNAL = "signal";
		public static final String DEFAULT_SORT_ORDER = "signal Desc";
		public static final int CHANNEL_LIMIT = 20;// 查询数据条数限制
	}
}
