package com.cyberwalkabout.foodtrucks;

import android.content.Context;
import android.content.Intent;

import com.cyberwalkabout.foodtrucks.cities.City;
import com.uki.twitter.TwitterShareSettings;

public class FoodTrucksService {
	
	public static final String EXTRA_APP_NAME = "app_name";
	public static final String EXTRA_FOODTRUCKS_SPREADSHEET_KEY = "f_key";
	public static final String EXTRA_FAKE_TWEETS_SPREADSHEET_KEY = "t_key";
	public static final String EXTRA_CITY = "c";
	public static final String EXTRA_FILTER = "f";
	public static final String EXTRA_TWITTER_SETTINGS = "twitter_settings";
    public static final String EXTRA_BEARER_TOKEN = "EXTRA_BEARER_TOKEN";

	public static void showMap(Context context, String appName, String foodtrucksSpreadsheetKey, String tweeetsSpreadsheetKey, TwitterShareSettings settings, City city, boolean filter) {
		Intent intent = new Intent(context, TrucksScreen.class);
		intent.putExtra(EXTRA_APP_NAME, appName);
		intent.putExtra(EXTRA_FOODTRUCKS_SPREADSHEET_KEY, foodtrucksSpreadsheetKey);
		intent.putExtra(EXTRA_FAKE_TWEETS_SPREADSHEET_KEY, tweeetsSpreadsheetKey);
		intent.putExtra(EXTRA_CITY, city);
		intent.putExtra(EXTRA_FILTER, filter);
		intent.putExtra(EXTRA_TWITTER_SETTINGS, settings);
		context.startActivity(intent);
	}
	
}
