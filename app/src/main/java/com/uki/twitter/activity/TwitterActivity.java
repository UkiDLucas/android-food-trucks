package com.uki.twitter.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.uki.twitter.R;
import com.uki.twitter.TwitterServices;
import com.uki.twitter.adapter.TweetsAdapter;
import com.uki.twitter.client.JsonTwitterClient;
import com.uki.twitter.client.TwitterClient;
import com.uki.twitter.model.Tweet;

public class TwitterActivity extends Activity implements OnClickListener {

    public static final String TAG = TwitterActivity.class.getSimpleName();

    private List<Tweet> mTweets = new ArrayList<Tweet>();
    private TweetsAdapter mTweetsAdapter;

    private Intent mIntent;

    private String mTag;
    private String mAppString;

    private ImageButton btnRefresh;
    private ListView mTweetsList;
    private LinearLayout lpost;
    private EditText message;
    private TextView status;
    private Button btnShare;
    private View footer;
    private ProgressDialog mDialog;

    private int defaultColor = 0;
    private int twitterColor = 0;

    private boolean flag = true;
    private boolean isFetching = true;

    private int length = 0;
    private int visibleThreshold = 3;

    private TwitterClient client;

    private boolean hasMoreItems = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweets_list);
        mIntent = getIntent();
        if (mIntent.getComponent().getPackageName().equals(getPackageName())) {
            client = new JsonTwitterClient();
            if (getIntent().hasExtra("APP_STRING")) {
                mAppString = getIntent().getStringExtra("APP_STRING");
            } else {
                mAppString = getString(R.string.twitter_app_string);
            }
            mTag = mIntent.getStringExtra(Intent.EXTRA_TITLE);
            init();
            initListView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.retweets) {
            startActivity(new Intent(TwitterActivity.this, RetweetsActivity.class));
        } else {
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.post) {
            lpost.setVisibility(lpost.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        } else if (v.getId() == R.id.refresh) {
            mDialog.show();
            mTweets.clear();
            new TweetsLoader().execute();
        } else if (v.getId() == R.id.btnSend) {
            new TwitterServices(mIntent.getStringExtra("appName")).postToWallTwitter(message.getText().toString() + " " + mTag + " " + mAppString, TwitterActivity.this, false);
            lpost.setVisibility(View.GONE);
        } else {
        }

    }

    private void init() {
        defaultColor = getResources().getColor(R.color.share_status_default);
        twitterColor = getResources().getColor(R.color.share_status_twitter);

        ((TextView) findViewById(R.id.hashTag)).setText(mTag);
        ((TextView) findViewById(R.id.appString)).setText(mTag + " " + mAppString);

        lpost = (LinearLayout) findViewById(R.id.lpost);

        ((ImageButton) findViewById(R.id.post)).setOnClickListener(this);

        btnRefresh = (ImageButton) findViewById(R.id.refresh);
        btnRefresh.setOnClickListener(this);

        message = (EditText) findViewById(R.id.message);
        message.setText(mIntent.getStringExtra(Intent.EXTRA_TEXT));
        message.addTextChangedListener(textWatcher);

        status = (TextView) findViewById(R.id.status);

        btnShare = (Button) findViewById(R.id.btnSend);
        btnShare.setOnClickListener(this);

        mDialog = new ProgressDialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setMessage(getString(R.string.message_loading));
        mDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        updateTextStatus();
        mDialog.show();
        new TweetsLoader().execute();
    }

    private void initListView() {

        footer = getLayoutInflater().inflate(R.layout.footer, null);
        footer.setVisibility(View.GONE);

        mTweetsList = (ListView) TwitterActivity.this.findViewById(R.id.tweets_list);
        mTweetsList.addFooterView(footer, null, true);
        mTweetsAdapter = new TweetsAdapter(TwitterActivity.this, mTweets);
        mTweetsList.setAdapter(mTweetsAdapter);

        registerForContextMenu(mTweetsList);

        mTweetsList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                message.setText("RT @" + mTweets.get(position).getFrom_user()+" "+mTweets.get(position).getText());
                updateTextStatus();
                lpost.setVisibility(View.VISIBLE);
                return false;
            }
        });

    }

    private class TweetsLoader extends AsyncTask {
        protected Object doInBackground(Object... params) {
            isFetching = true;

            List<Tweet> results = client.fetchTweets(TwitterActivity.this, mTag);
            if (results.size() > 0) {
                mTweets.addAll(results);
            } else {
                hasMoreItems = false;
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(Object result) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mTweetsAdapter.setItems(mTweets);
            mTweetsAdapter.notifyDataSetChanged();
            footer.setVisibility(View.GONE);
            isFetching = false;
            if (flag && mTweets.size() > 0) {
                Toast.makeText(TwitterActivity.this, "Use long click for more item options", Toast.LENGTH_SHORT).show();
                flag = false;
            }
            if (flag && mTweets.size() == 0) {
                Toast.makeText(TwitterActivity.this, "Be the first to twett adout the " + mTag, Toast.LENGTH_SHORT).show();
                lpost.setVisibility(View.VISIBLE);
                flag = false;
            }
        }
    }

    private void updateTextStatus() {
        length = message.getText().toString().length() + 1 + mTag.length() + 1 + mAppString.length();
        if (length > 140) {
            status.setText("-" + (length - 140));
            status.setTextColor(twitterColor);
            btnShare.setEnabled(false);
        } else {
            status.setText("+" + (140 - length));
            status.setTextColor(defaultColor);
            btnShare.setEnabled(true);
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateTextStatus();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

}