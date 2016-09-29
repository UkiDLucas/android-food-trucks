package com.cyberwalkabout.foodtrucks;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.cyberwalkabout.foodtrucks.adapter.MentionsAdapter;
import com.cyberwalkabout.foodtrucks.data.FoodTruck;
import com.cyberwalkabout.foodtrucks.db.DatabaseHelper;
import com.cyberwalkabout.foodtrucks.twitter.TwitterClient;
import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.uki.sns.SNServices;
import com.uki.sns.ShareSettings;
import com.flurry.android.FlurryAgent;

public class DescriptionScreen extends SherlockActivity implements OnClickListener {

    private AQuery aq;

    private List<TwitterStatus> mentions = new ArrayList<TwitterStatus>();

    private int defaultColor = 0;
    private int twitterColor = 0;
    private int length = 0;

    private String mAppString;

    private LinearLayout lPost;
    private TextView txtStatus;
    private EditText message;
    private Button btnSend;

    private FoodTruck foodTruck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodtruck_description);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bkg));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.transparent_img);
        getSupportActionBar().setCustomView(R.layout.custom_view);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        foodTruck = (FoodTruck) getIntent().getSerializableExtra(Extras.EXTRA_FOODTRUCK);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(foodTruck.getTwitterName());

        defaultColor = getResources().getColor(R.color.share_status_default);
        twitterColor = getResources().getColor(R.color.share_status_twitter);

        mAppString = getString(R.string.app_string);

        aq = new AQuery(this);

        initViews();
        updateTextStatus();
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    private void initViews() {
        lPost = (LinearLayout) findViewById(R.id.lpost);
        btnSend = (Button) findViewById(R.id.btnSend);
        findViewById(R.id.btnSend).setOnClickListener(this);

        txtStatus = (TextView) findViewById(R.id.status);
        ((TextView) findViewById(R.id.appString)).setText(getString(R.string.app_string));

        message = (EditText) findViewById(R.id.message);
        message.setText(foodTruck.getTwitterName());
        message.addTextChangedListener(textWatcher);

        CheckBox favorite = (CheckBox) findViewById(R.id.favorite);
        favorite.setChecked(foodTruck.isFavorite());
        favorite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                foodTruck.setFavorite(((CheckBox) v).isChecked());
                DatabaseHelper.get(DescriptionScreen.this).markAsFavorite(foodTruck);

            }
        });

        aq.id(R.id.txtDescription).text(foodTruck.getDescription());
        aq.id(R.id.twitter_image).image(getIntent().getStringExtra(Extras.EXTRA_IMAGE_URL));
        new MentionsLoader().execute();
    }

    private class MentionsLoader extends AsyncTask {
        protected Object doInBackground(Object... params) {
            mentions = TwitterClient.getTwitterMentions(DescriptionScreen.this, foodTruck.getTwitterName());
            return 1;
        }

        protected void onPostExecute(Object result) {
            aq.id(R.id.mentions).adapter(new MentionsAdapter(DescriptionScreen.this, mentions));
            aq.id(R.id.mentions).itemClicked(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                    message.setText("RT @" + mentions.get(position).getFromUser() + " " + mentions.get(position).getText());
                    lPost.setVisibility(View.VISIBLE);

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSend) {
            FlurryAgent.logEvent("foodtrucks: send tweet");
            SNServices.saveSettings(new ShareSettings(getString(R.string.twitter_callback_url), getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret), getString(R.string.facebook_callback_url), getString(R.string.facebook_app_id), getString(R.string.facebook_app_secret)), DescriptionScreen.this);
            new SNServices(getString(R.string.app_name)).postToWallTwitter(message.getText().toString() + " " + mAppString, DescriptionScreen.this);
            lPost.setVisibility(View.GONE);
        }

    }

    private void updateTextStatus() {
        length = message.length() + mAppString.length() + 1;
        if (length > 140) {
            txtStatus.setText("-" + (length - 140));
            txtStatus.setTextColor(twitterColor);
            btnSend.setEnabled(false);
        } else {
            txtStatus.setText("+" + (140 - length));
            txtStatus.setTextColor(defaultColor);
            btnSend.setEnabled(true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.description_screen_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_tweet) {
            FlurryAgent.logEvent("foodtrucks: write tweet");
            if (lPost.getVisibility() == View.VISIBLE) {
                lPost.setVisibility(View.GONE);
            } else {
                lPost.setVisibility(View.VISIBLE);
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
