package com.cyberwalkabout.foodtrucks.all;

import java.util.List;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.cyberwalkabout.foodtrucks.FoodTrucksService;
import com.cyberwalkabout.foodtrucks.R;
import com.cyberwalkabout.foodtrucks.cities.City;
import com.cyberwalkabout.foodtrucks.cities.SpreadSheetCitiesProvider;
import com.cyberwalkabout.foodtrucks.db.DatabaseHelper;
import com.uki.twitter.TwitterShareSettings;
import com.uki.common.net.NetworkUtils;
import com.uki.common.util.DistanceUtils;
import com.uki.common.util.LocationUtils;
import com.flurry.android.FlurryAgent;

public class SplashScreen extends Activity
{
	private static final int SPLASH_TIME = 3000;

	private Handler mHandler;
	private Runnable mStartApp;
	private DatabaseHelper helper;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		helper = new DatabaseHelper(this);
		mHandler = new Handler();
		mStartApp = new Runnable()
		{

			@Override
			public void run()
			{
				if (loadCities() || helper.getCitiesCount() > 0)
				{
					Location loc = LocationUtils.getLatestKnownLocation(SplashScreen.this);
					FoodTrucksService.showMap(SplashScreen.this, getString(R.string.app_name), getString(R.string.foodtrucks_spreadsheet_key), getString(R.string.fake_tweets_spreadsheet_key),
							getTwitterShareSettings(), getClosestCity(loc), true, getString(R.string.flurry_app_key));
				} else
				{
					Toast.makeText(SplashScreen.this, "We were unable to synchronize data at this time, please open this application later.", Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		};
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (!NetworkUtils.isNetworkAvailable(SplashScreen.this))
		{
			NetworkUtils.createNetworkisabledAlert(SplashScreen.this);

		} else if (!LocationUtils.ifAnyLocationProviderEnabled(SplashScreen.this))
		{
			LocationUtils.createLocationProviderDisabledAlert(SplashScreen.this);
		} else
		{
			mHandler.postDelayed(mStartApp, SPLASH_TIME);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
	}

	@Override
	public void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	private boolean loadCities()
	{
		SpreadSheetCitiesProvider citiesProvider = new SpreadSheetCitiesProvider(getString(R.string.foodtrucks_spreadsheet_key), helper);
		return citiesProvider.load();
	}

	private City getClosestCity(Location loc)
	{
		List<City> cities = helper.getAllCities();
		if (loc != null)
		{
			int index = 0;
			double minDistance = DistanceUtils.distance(loc.getLatitude(), loc.getLongitude(), cities.get(0).getLatitude(), cities.get(0).getLongitude());
			for (int i = 1; i < cities.size(); i++)
			{
				double distance = DistanceUtils.distance(loc.getLatitude(), loc.getLongitude(), cities.get(i).getLatitude(), cities.get(i).getLongitude());
				if (distance < minDistance)
				{
					minDistance = distance;
					index = i;
				}
			}
			return cities.get(index);
		}

		return cities.get(0);

	}
	
	private TwitterShareSettings getTwitterShareSettings() {
		return new TwitterShareSettings(getString(R.string.twitter_callback_url), getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
	}

}