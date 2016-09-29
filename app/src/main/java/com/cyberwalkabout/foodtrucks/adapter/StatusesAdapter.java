package com.cyberwalkabout.foodtrucks.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.uki.foodtrucks.R;
import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;

public class StatusesAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private SimpleDateFormat mFormat;
    private List<TwitterStatus> mItems;

    static class ViewHolder {
        TextView time;
        TextView name;
        TextView text;
        ImageView img;
    }

    public StatusesAdapter(Context context, List<TwitterStatus> items) {
        mItems = items;
        mInflater = LayoutInflater.from(context);
        mFormat = new SimpleDateFormat("hh:mm a, EEEEE");
    }

    public void setItems(List<TwitterStatus> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
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
            convertView = mInflater.inflate(R.layout.twiter_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.img = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AQuery aq = new AQuery(convertView);
        TwitterStatus status = mItems.get(position);

        holder.text.setText(Html.fromHtml(status.getText()));

        Date created_at = new Date(status.getCreatedAt());
        holder.time.setText(mFormat.format(created_at));
        holder.name.setText(status.getFromUser());

        aq.id(holder.img).image(status.getImageUrl());

        return convertView;
    }

}
