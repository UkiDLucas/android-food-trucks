package com.uki.twitter.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.uki.twitter.R;
import com.uki.twitter.model.Tweet;

public class TweetsAdapter extends BaseAdapter
{

	private LayoutInflater mInflater;
	private SimpleDateFormat mFormat;
	private Pattern tagPattern;
	private Pattern linkPattern;
	private Pattern mentionPattern;
	private int tagsColor;
	private List<Tweet> mItems;

	static class ViewHolder
	{
		TextView time;
		TextView name;
		TextView text;
		ImageView img;

	}

	public TweetsAdapter(Context context, List<Tweet> items)
	{
		mItems = items;
		mInflater = LayoutInflater.from(context);
		mFormat = new SimpleDateFormat("hh:mm a, EEEEE");
		tagPattern = Pattern.compile("(?:(?<=\\s)|^)[#,@](\\w*[A-Za-z_]+\\w*)");
		linkPattern = Pattern.compile("http://\\S*|www\\..*\\s");
		mentionPattern = Pattern.compile("(?:(?<=\\s)|^)[@](\\w*[A-Za-z_]+\\w*)");
		tagsColor = context.getResources().getColor(R.color.hash_tags);
	}

	public List<String> getLinks(int position)
	{
		List<String> links = new ArrayList<String>();
		String text = mItems.get(position).getText();
		Matcher m = linkPattern.matcher(text);
		while (m.find())
		{
			links.add(text.substring(text.indexOf(m.group(0)), text.indexOf(m.group(0)) + m.group(0).length()));
		}
		return links;
	}

	public List<String> getMentions(int position)
	{
		List<String> mentions = new ArrayList<String>();
		String text = mItems.get(position).getText();
		Matcher m = mentionPattern.matcher(text);
		while (m.find())
		{
			mentions.add(text.substring(text.indexOf(m.group(0)), text.indexOf(m.group(0)) + m.group(0).length()));
		}
		return mentions;
	}

	public String getUserName(int position)
	{
		return mItems.get(position).getFrom_user();
	}

	public List<Tweet> getItems()
	{
		return mItems;
	}

	public void setItems(List<Tweet> mItems)
	{
		this.mItems = mItems;
	}

	@Override
	public int getCount()
	{
		return mItems.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.twiter_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.img = (ImageView) convertView.findViewById(R.id.img);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		final AQuery aq = new AQuery(convertView);
		Tweet tweet = mItems.get(position);

		holder.text.setText(Html.fromHtml(tweet.getText()));

		String text = holder.text.getText().toString();
		SpannableString spannable_text = new SpannableString(text);
		Date created_at = new Date(tweet.getCreated_at());

		holder.time.setText(mFormat.format(created_at));
		holder.name.setText(tweet.getFrom_user());

		aq.id(holder.img).image(tweet.getProfile_image_url());

		Matcher m = tagPattern.matcher(text);
		while (m.find())
		{
			spannable_text.setSpan(new ForegroundColorSpan(tagsColor), text.indexOf(m.group(0)), text.indexOf(m.group(0)) + m.group(0).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		}

		m = linkPattern.matcher(text);
		while (m.find())
		{
			spannable_text.setSpan(new ForegroundColorSpan(tagsColor), text.indexOf(m.group(0)), text.indexOf(m.group(0)) + m.group(0).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		}

		holder.text.setText(spannable_text);

		return convertView;
	}

}
