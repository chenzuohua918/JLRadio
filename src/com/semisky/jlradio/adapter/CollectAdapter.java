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
import com.semisky.jlradio.dao.DBConfiguration;
import com.semisky.jlradio.util.AppUtil;
import com.semisky.jlradio.util.Constants;
import com.semisky.jlradio.util.Logger;
import com.semisky.jlradio.util.RadioStatus;

public class CollectAdapter extends CursorAdapter implements OnClickListener {
	private OnCollectItemButtonClickListener onCollectItemButtonClickListener;

	public interface OnCollectItemButtonClickListener {
		void onDeleteButtonClick(View v, int frequency);
	}

	@SuppressWarnings("deprecation")
	public CollectAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public void setOnCollectItemButtonClickListener(
			OnCollectItemButtonClickListener onCollectItemButtonClickListener) {
		this.onCollectItemButtonClickListener = onCollectItemButtonClickListener;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.collect_item,
				parent, false);
		ViewHolder holder = new ViewHolder();
		holder.item_number = (TextView) view.findViewById(R.id.item_number);
		holder.item_frequency = (TextView) view
				.findViewById(R.id.item_frequency);
		holder.item_unit = (TextView) view.findViewById(R.id.item_unit);
		holder.item_delete = (ImageView) view.findViewById(R.id.item_delete);
		holder.item_delete.setOnClickListener(this);
		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.item_number.setText(String.valueOf(cursor.getPosition() + 1));
		int frequency = cursor
				.getInt(cursor
						.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY));
		if (RadioStatus.currentFrequency == frequency) {
			ColorStateList text_selected_color = context.getResources()
					.getColorStateList(
							R.color.radio_list_textcolor_selected_selector);
			holder.item_number.setTextColor(text_selected_color);
			holder.item_frequency.setTextColor(text_selected_color);
			holder.item_unit.setTextColor(text_selected_color);
			holder.item_delete
					.setImageResource(R.drawable.radio_listbtn_delete_selected_selector);
		} else {
			ColorStateList textcolor_normal_selector = context.getResources()
					.getColorStateList(
							R.color.radio_list_textcolor_unselected_selector);
			holder.item_number.setTextColor(textcolor_normal_selector);
			holder.item_frequency.setTextColor(textcolor_normal_selector);
			holder.item_unit.setTextColor(textcolor_normal_selector);
			holder.item_delete
					.setImageResource(R.drawable.radio_listbtn_delete_unselected_selector);
		}

		switch (cursor
				.getInt(cursor
						.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_TYPE))) {
		case Constants.TYPE_FM:
			holder.item_frequency.setText(AppUtil
					.formatFloatFrequency(frequency / Constants.FM_MULTIPLE));
			holder.item_unit.setText(R.string.mhz);
			break;
		case Constants.TYPE_AM:
			holder.item_frequency.setText(String.valueOf(frequency));
			holder.item_unit.setText(R.string.khz);
			break;
		default:
			break;
		}
		holder.item_delete.setTag(frequency);
	}

	static class ViewHolder {
		TextView item_number;
		TextView item_frequency;
		TextView item_unit;
		ImageView item_delete;
	}

	@Override
	public void onClick(View v) {
		Logger.logD("CollectAdapter-----------------------delete button click---tag = "
				+ v.getTag());
		if (onCollectItemButtonClickListener != null) {
			onCollectItemButtonClickListener.onDeleteButtonClick(v,
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
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					if (frequency <= cursor
							.getInt(cursor
									.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY))) {
						return cursor.getPosition();
					}
					if (cursor.isLast()) {
						return getCount() - 1;
					}
				}
			}
		} else if (AppUtil.inAMFrequencyRange(frequency)) {
			Cursor cursor = getCursor();
			if (cursor != null) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					int fre = cursor
							.getInt(cursor
									.getColumnIndex(DBConfiguration.TableCollectConfiguration.CHANNEL_FREQUENCY));
					if (AppUtil.inFMFrequencyRange(fre)) {
						continue;
					} else if (frequency <= fre) {
						return cursor.getPosition();
					}
				}
			}
		}
		return 0;
	}
}
