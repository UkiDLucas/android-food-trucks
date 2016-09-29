package com.uki.twitter.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uki.twitter.R;
import com.uki.twitter.model.RetweetsCounter;

public class RetweetsAdapter extends BaseAdapter {

	private List<RetweetsCounter> mItems;
	private LayoutInflater mInflater;

	static class ViewHolder {
		TextView header;
		TextView retweet;
	}

	public RetweetsAdapter(Context context, List<RetweetsCounter> items) {
		mItems = items;
		mInflater = LayoutInflater.from(context);
	}

	public List<RetweetsCounter> getItems() {
		return mItems;
	}

	public void setItems(List<RetweetsCounter> mItems) {
		this.mItems = mItems;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.retweets_list_item, null);
			holder = new ViewHolder();
			holder.header = (TextView) convertView.findViewById(R.id.header);
			holder.retweet = (TextView) convertView.findViewById(R.id.retweet);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		RetweetsCounter rt = mItems.get(position);

		holder.header.setText(rt.getHashTag());

		int count = rt.getCount();
		if (count > 1) {
			holder.retweet.setText("@" + rt.getUserName() + " - "
					+ rt.getCount() + " retweets");
		} else {
			holder.retweet.setText("@" + rt.getUserName() + " - "
					+ rt.getCount() + " retweet");
		}

		holder.header.setVisibility(View.GONE);
		if (position > 0) {
			String previousHashTag = mItems.get(position - 1).getHashTag();
			if (!previousHashTag.equals(rt.getHashTag())) {
				holder.header.setVisibility(View.VISIBLE);
			}
		} else {
			holder.header.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

}
