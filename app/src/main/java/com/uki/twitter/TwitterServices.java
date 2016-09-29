package com.uki.twitter;

import java.util.LinkedList;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.uki.twitter.activity.TwitterActivity;
import com.uki.twitter.activity.TwitterOAuthActivity;

public final class TwitterServices
{

	public static final String PREFS_NAME = "sns_prefs";

	public static final String TWITTER_FILE = "twitter_settings";

	private static final String TOKEN_SECRET = "token_secret";

	private static final String TOKEN = "token";

	private static final String ACCESS_TOKEN = "access_token";

	// Prefs settings
	public static final String TWITTER_CALLBACK_URL = "tw_callback_url";

	public static final String TWITTER_CONSUMER_KEY = "tw_consumer_key";

	public static final String TWITTER_CONSUMER_SECRET = "tw_consumer_secret";

	private Activity mActivity;

	public static String appName;

	private static String mMsg;
	private boolean mLocation;

	public static TwitterOAuthHandler twitterOAuthHandler;

	public TwitterServices(String appName)
	{
		TwitterServices.appName = appName;
	}

	public TwitterServices(Activity activity, String appName, TwitterShareSettings settings)
	{
		TwitterServices.appName = appName;
		saveSettings(settings, activity);
	}

	public void postToWallTwitter(String msg, final Activity activity, boolean location)
	{
		mActivity = activity;
		mMsg = msg;
		mLocation = location;

		twitterOAuthHandler = new TwitterOAuthHandler();

		SharedPreferences prefs = activity.getSharedPreferences(TWITTER_FILE, Activity.MODE_PRIVATE);
		final String token = prefs.getString(TOKEN, null);
		final String tokenSecret = prefs.getString(TOKEN_SECRET, null);
		if (token != null && tokenSecret != null)
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						postTweet(token, tokenSecret);
					} catch (Exception e)
					{
						// something wrong try to reauthorize
						mActivity.startActivity(new Intent(activity, TwitterOAuthActivity.class));
					}
				};
			}.start();
		} else
		{
			activity.startActivity(new Intent(activity, TwitterOAuthActivity.class));
		}
	}

	private void postTweet(String token, String tokenSecret) throws Exception
	{

		HttpPost post = new HttpPost("https://api.twitter.com/1.1/statuses/update.json");
		LinkedList<BasicNameValuePair> out = new LinkedList<BasicNameValuePair>();
		out.add(new BasicNameValuePair("status", mMsg));
		if (mLocation)
		{
			Location loc = getLocation(mActivity);
			if (loc != null)
			{
				out.add(new BasicNameValuePair("lat", Double.toString(loc.getLatitude())));
				out.add(new BasicNameValuePair("long", Double.toString(loc.getLongitude())));
			}
		}
		post.setEntity(new UrlEncodedFormEntity(out, HTTP.UTF_8));
		// Tweak further as needed for your app
		HttpParams params = new BasicHttpParams();
		// set this to false, or else you'll get an Expectation Failed: error
		HttpProtocolParams.setUseExpectContinue(params, false);
		post.setParams(params);
		// sign the request to authenticate
		SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String consumerKey = prefs.getString(TWITTER_CONSUMER_KEY, "");
		String consumerSecret = prefs.getString(TWITTER_CONSUMER_SECRET, "");
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		consumer.setTokenWithSecret(token, tokenSecret);
		consumer.sign(post);

		String response = new HttpHelper().doPost(post, mActivity);

		mActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(mActivity, "Twitter: " + mMsg, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void getUserProfile(String token, String tokenSecret, String screenName) throws Exception
	{

		HttpGet get = new HttpGet("http://api.twitter.com/1.1/users/show.json?screen_name=" + screenName);
		// Tweak further as needed for your app
		HttpParams params = new BasicHttpParams();
		// set this to false, or else you'll get an Expectation Failed: error
		HttpProtocolParams.setUseExpectContinue(params, false);
		get.setParams(params);
		// sign the request to authenticate
		SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String consumerKey = prefs.getString(TWITTER_CONSUMER_KEY, "");
		String consumerSecret = prefs.getString(TWITTER_CONSUMER_SECRET, "");
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		consumer.setTokenWithSecret(token, tokenSecret);
		consumer.sign(get);

		String response = new HttpHelper().getPage(get, mActivity);

		Log.i("", "");

	}

	public static void search(String message, Context context, String hashTag, String appString, TwitterShareSettings settings)
	{
		Intent intent = new Intent(context, TwitterActivity.class);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TITLE, hashTag);
		intent.putExtra(Intent.EXTRA_TEXT, message);
		if (appString != null)
		{
			intent.putExtra("APP_STRING", appString);
		}
		intent.putExtra("appName", settings.getApplicationName());
		context.startActivity(intent);

		TwitterServices.saveSettings(settings, context);
	}

	private Location getLocation(Context context)
	{
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = lm.getBestProvider(crit, true);
		return lm.getLastKnownLocation(provider);
	}

	public class TwitterOAuthHandler
	{
		public void handle(String token, String tokenSecret)
		{
			SharedPreferences prefs = mActivity.getSharedPreferences(TWITTER_FILE, Activity.MODE_PRIVATE);
			prefs.edit().putString(TOKEN, token).putString(TOKEN_SECRET, tokenSecret).commit();
			// try
			// {
			// postTweet(token, tokenSecret);
			// } catch (Exception e)
			// {
			// // TODO handle
			// }
		}
	}

	public static void saveSettings(TwitterShareSettings settings, Context context)
	{
		TwitterServices.appName = settings.getApplicationName();

		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
		prefs.edit().putString(TWITTER_CALLBACK_URL, settings.getTwitterCallbackUrl()).putString(TWITTER_CONSUMER_KEY, settings.getTwitterConsumerKey())
				.putString(TWITTER_CONSUMER_SECRET, settings.getTwitterConsumerSecret()).commit();
	}

}
