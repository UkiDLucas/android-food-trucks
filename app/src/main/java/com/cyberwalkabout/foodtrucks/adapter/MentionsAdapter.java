package com.cyberwalkabout.foodtrucks.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uki.foodtrucks.R;
import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;

public class MentionsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<TwitterStatus> mItems;

    static class ViewHolder {
        TextView text;
    }

    public MentionsAdapter(Context context, List<TwitterStatus> items) {
        mItems = items;
        mInflater = LayoutInflater.from(context);
    }

    public List<TwitterStatus> getItems() {
        return mItems;
    }

    public void setItems(List<TwitterStatus> mItems) {
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
            convertView = mInflater.inflate(R.layout.mentions_list_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TwitterStatus s = mItems.get(position);
        holder.text.setText(Html.fromHtml("<b>" + s.getFromUser() + ":" + "</b>" + " " + s.getText()));
        return convertView;
    }
}
