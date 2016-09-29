package com.uki.twitter.activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.uki.twitter.R;
import com.uki.twitter.adapter.RetweetsAdapter;
import com.uki.twitter.db.DatabaseHelper;
import com.uki.twitter.model.RetweetsCounter;

public class RetweetsActivity extends Activity {
	
	private static final String TAG = RetweetsActivity.class.getSimpleName();
	
	private DatabaseHelper mDatabaseHelper;
	private List<RetweetsCounter> mRetweets;
	
	private ListView retweetsList;
	private RetweetsAdapter retweetsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.retweets);
		
		mDatabaseHelper = new DatabaseHelper(RetweetsActivity.this);
		mRetweets = mDatabaseHelper.getAll();
		initListView();
	}
	
	
	private void initListView() {
		retweetsList = (ListView)findViewById(R.id.retweets_list);
		retweetsAdapter = new RetweetsAdapter(RetweetsActivity.this, mRetweets);
		retweetsList.setAdapter(retweetsAdapter);
	}

}
