package com.semisky.jlradio.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.semisky.jlradio.R;
import com.semisky.jlradio.dao.CollectChannelDBManager;
import com.semisky.jlradio.dao.DBConfiguration;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.RadioStatus;

public class FMAdapter extends CursorAdapter implements OnClickListener {
	private OnFMItemButtonClickListener onFMItemButtonClickListener;

	public interface OnFMItemButtonClickListener {
		void onCollectButtonClick(View v, int frequency);
	}

	@SuppressWarnings("deprecation")
	public FMAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public void setOnFMItemButtonClickListener(
			OnFMItemButtonClickListener onFMItemButtonClickListener) {
		this.onFMItemButtonClickListener = onFMItemButtonClickListener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.item_number.setText(String.valueOf(cursor.getPosition() + 1));
		int frequency = cursor
				.getInt(cursor
						.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY));
		holder.item_frequency.setText(AppUtil.formatFloatFrequency(frequency
				/ Constants.FM_MULTIPLE));
		holder.item_unit.setText(R.string.mhz);

		if (RadioStatus.currentFrequency == frequency) {// 当前选中
			ColorStateList text_selected_color = context.getResources()
					.getColorStateList(
							R.color.radio_list_textcolor_selected_selector);
			holder.item_number.setTextColor(text_selected_color);
			holder.item_frequency.setTextColor(text_selected_color);
			holder.item_unit.setTextColor(text_selected_color);
		} else {// 没选中
			ColorStateList textcolor_normal_selector = context.getResources()
					.getColorStateList(
							R.color.radio_list_textcolor_unselected_selector);
			holder.item_number.setTextColor(textcolor_normal_selector);
			holder.item_frequency.setTextColor(textcolor_normal_selector);
			holder.item_unit.setTextColor(textcolor_normal_selector);
		}

		if (CollectChannelDBManager.getInstance(context).isChannelInDB(
				frequency)) {// 收藏了
			if (RadioStatus.currentFrequency == frequency) {// 当前选中
				holder.btn_collect
						.setImageResource(R.drawable.radio_listbtn_collected_selected_selector);
			} else {// 没选中
				holder.btn_collect
						.setImageResource(R.drawable.radio_listbtn_collected_unselected_selector);
			}
		} else {// 没有收藏
			if (RadioStatus.currentFrequency == frequency) {// 当前选中
				holder.btn_collect
						.setImageResource(R.drawable.radio_listbtn_uncollected_selected_selector);
			} else {// 没选中
				holder.btn_collect
						.setImageResource(R.drawable.radio_listbtn_uncollected_unselected_selector);
			}
		}
		holder.btn_collect.setTag(frequency);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.fm_am_item,
				parent, false);
		ViewHolder holder = new ViewHolder();
		holder.item_number = (TextView) view.findViewById(R.id.item_number);
		holder.item_frequency = (TextView) view
				.findViewById(R.id.item_frequency);
		holder.item_unit = (TextView) view.findViewById(R.id.item_unit);
		holder.btn_collect = (ImageView) view.findViewById(R.id.btn_collect);
		holder.btn_collect.setOnClickListener(this);
		view.setTag(holder);
		return view;
	}

	static class ViewHolder {
		TextView item_number;
		TextView item_frequency;
		TextView item_unit;
		ImageView btn_collect;
	}

	@Override
	public void onClick(View v) {
		Logger.logD("FMAdapter-----------------------collect button click---tag = "
				+ v.getTag());
		if (onFMItemButtonClickListener != null) {
			onFMItemButtonClickListener.onCollectButtonClick(v,
					Integer.valueOf(v.getTag().toString()));
		}
	}

	/**
	 * 获取某频点的位置
	 * 
	 * @param frequency
	 * @return
	 */
	public int getPosition(int frequency) {
		if (AppUtil.inFMFrequencyRange(frequency)) {
			Cursor cursor = getCursor();
			if (cursor != null) {
				int pos = -1;
				cursor.moveToPosition(pos);
				while (cursor.moveToNext()) {
					pos++;
					if (frequency <= cursor
							.getInt(cursor
									.getColumnIndex(DBConfiguration.TableFMConfiguration.CHANNEL_FREQUENCY))) {
						return cursor.getPosition();
					}
					if (pos == getCount() - 1) {
						return pos;
					}
				}
			}
		}
		return 0;
	}
}
