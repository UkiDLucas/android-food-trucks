package com.cyberwalkabout.foodtrucks;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.cyberwalkabout.foodtrucks.adapter.StatusesAdapter;
import com.cyberwalkabout.foodtrucks.cities.City;
import com.cyberwalkabout.foodtrucks.data.FoodTruck;
import com.cyberwalkabout.foodtrucks.data.SpreadSheetFoodTrucksProvider;
import com.cyberwalkabout.foodtrucks.db.DatabaseHelper;
import com.cyberwalkabout.foodtrucks.overlays.TwitterMentionsOverlay;
import com.cyberwalkabout.foodtrucks.overlays.TwitterProfilesOverlay;
import com.cyberwalkabout.foodtrucks.twitter.TwitterClient;
import com.cyberwalkabout.foodtrucks.twitter.TwitterStatus;
import com.flurry.android.FlurryAgent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uki.common.util.ConvertUtils;
import com.uki.common.util.Sys;
import com.uki.sns.SNServices;
import com.uki.sns.ShareSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrucksScreen extends SherlockMapActivity implements OnClickListener {

    private static final long STATUS_EXPIRATION_TIMESTAMP = 24 * 3600000;// hours

    private static final int REFRESH_LIST = 0;
    private static final int REFRESH_MAP = 1;
    private static final int LOADING_LIST = 2;
    private static final int LOADING_MAP = 3;

    private List<TwitterStatus> profiles = new ArrayList<TwitterStatus>();
    private List<TwitterStatus> geoProfiles = new ArrayList<TwitterStatus>();
    private List<TwitterStatus> geoMentions = new ArrayList<TwitterStatus>();


    private List<TwitterStatus> filteredProfiles = new ArrayList<TwitterStatus>();
    private List<TwitterStatus> filteredGeoProfiles = new ArrayList<TwitterStatus>();
    private List<TwitterStatus> filteredGeoMentions = new ArrayList<TwitterStatus>();

    private Handler mHandler;

    private MapView mapView;
    private PullToRefreshListView statuses;

    private StatusesAdapter adapter;

    private Updater updaterTask;
    private ChangeViewModeTask changeViewModeTask;

    private SpreadSheetFoodTrucksProvider provider;

    private ViewMode currentViewMode = ViewMode.ALL;
    ;
    private City currentCity;

    private SharedPreferences prefs;

    private View disclosure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.foodtrucks_map);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bkg));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setLogo(R.drawable.transparent_img);
        getSupportActionBar().setCustomView(R.layout.custom_view);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        BugSenseHandler.initAndStartSession(TrucksScreen.this, "002b6265");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentCity = (City) getIntent().getSerializableExtra(Extras.EXTRA_CITY);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(currentCity.getName());
        provider = new SpreadSheetFoodTrucksProvider(getString(R.string.foodtrucks_spreadsheet_key), currentCity, this);
        init();
        mHandler.post(updaterTask);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updaterTask.deactivate();

        mHandler.removeCallbacks(updaterTask);
        mHandler.removeCallbacks(changeViewModeTask);

        BugSenseHandler.closeSession(TrucksScreen.this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showQuitConfirmationDalog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    protected void onResume() {
        super.onResume();
        FlurryAgent.logEvent("foodtrucks screen");
        if (currentViewMode == ViewMode.FAVORITES) {
            mHandler.post(changeViewModeTask);
        }
    }

    private void init() {
        updaterTask = new Updater();
        mHandler = new MyHandler();
        changeViewModeTask = new ChangeViewModeTask();

        initList();
        initMap();

        findViewById(R.id.btn_select_region).setOnClickListener(this);
        findViewById(R.id.btn_nearby_cities).setOnClickListener(this);
        findViewById(R.id.btn_favorites).setOnClickListener(this);
        findViewById(R.id.btn_share_this_app).setOnClickListener(this);
        findViewById(R.id.btn_about_us).setOnClickListener(this);
    }

    private void initList() {
        statuses = (PullToRefreshListView) findViewById(R.id.tweets_list);
        statuses.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                FlurryAgent.logEvent("refresh food trucks");
                mapView.getOverlays().clear();
                mHandler.post(updaterTask);
            }
        });
        statuses.setEmptyView(findViewById(android.R.id.empty));
        adapter = new StatusesAdapter(TrucksScreen.this, profiles);
        statuses.setAdapter(adapter);
        statuses.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                TwitterStatus profile = currentViewMode == ViewMode.FAVORITES ? filteredProfiles.get(position - 1) : profiles.get(position - 1);
                String screenName = "@" + profile.getFromUser();
                FoodTruck t = DatabaseHelper.get(TrucksScreen.this).getFoodTruckByTwitterName(screenName);
                Intent descriptionIntent = new Intent(TrucksScreen.this, DescriptionScreen.class);
                descriptionIntent.putExtra(Extras.EXTRA_FOODTRUCK, t);
                descriptionIntent.putExtra(Extras.EXTRA_IMAGE_URL, profile.getImageUrl());
                startActivity(descriptionIntent);
            }
        });
    }

    private void initMap() {
        disclosure = findViewById(R.id.no_geo_tweets_msg);
        disclosure.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideDisclosure();
                return false;
            }
        });
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        // TODO: zoom in when we have a lot of trucks
        // participating
        centerMapOnCurrentCity();
    }

    private void centerMapOnCurrentCity() {
        GeoPoint p = ConvertUtils.toGeoPoint(currentCity.getLatitude(), currentCity.getLongitude());
        mapView.getController().setZoom(12);
        mapView.getController().animateTo(p);
        mapView.getController().setCenter(p);
        mapView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            currentViewMode = ViewMode.ALL;
            ((TextView) findViewById(R.id.title)).setText(currentCity.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mHandler.post(changeViewModeTask);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_select_region) {
            FlurryAgent.logEvent("menu: select region");

            final List<City> cities = DatabaseHelper.get(TrucksScreen.this).getAllCities();
            final String[] items = new String[cities.size()];
            int currentCityPosition = 0;
            for (int i = 0; i < cities.size(); i++) {
                items[i] = cities.get(i).getName();
                if (cities.get(i).getId().equals(currentCity.getId())) {
                    currentCityPosition = i;
                }
            }
            new AlertDialog.Builder(this).setTitle("Select region:").setSingleChoiceItems(items, currentCityPosition, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    currentCity = cities.get(which);
                    prefs.edit().putInt("lastchecked", which).commit();
                    prefs.edit().putString("cityId", currentCity.getId()).commit();
                    provider.setCity(currentCity);
                    mHandler.post(updaterTask);
                    dialog.dismiss();
                    setTitle(currentCity.getName());
                    centerMapOnCurrentCity();

                    FlurryAgent.logEvent("change location: " + currentCity.getName());
                }
            }).show();
        } else if (view.getId() == R.id.btn_nearby_cities) {
            FlurryAgent.logEvent("menu: nearby cities");

            Location loc = Sys.getLatestKnownLocation(TrucksScreen.this);

            if (loc != null) {
                final List<City> cities = DatabaseHelper.get(TrucksScreen.this).selectNearbyCities(loc);

                if (cities.size() > 0) {
                    final String[] items = new String[cities.size()];
                    int currentCityPosition = 0;
                    for (int i = 0; i < cities.size(); i++) {
                        items[i] = cities.get(i).getName();
                        if (cities.get(i).getId().equals(currentCity.getId())) {
                            currentCityPosition = i;
                        }
                    }
                    new AlertDialog.Builder(this).setTitle("Select region:").setSingleChoiceItems(items, currentCityPosition, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            currentCity = cities.get(which);
                            prefs.edit().putInt("lastchecked", which).commit();
                            prefs.edit().putString("cityId", currentCity.getId()).commit();
                            provider.setCity(currentCity);
                            mHandler.post(updaterTask);
                            dialog.dismiss();
                            setTitle(currentCity.getName());
                            centerMapOnCurrentCity();
                        }
                    }).show();
                } else {
                    Toast.makeText(TrucksScreen.this, "We're sorry. We were unable to locate any cities around you at this time.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(TrucksScreen.this, "Your current location is unknown. It is not possible to show nearby cities.", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.btn_favorites) {
            currentViewMode = currentViewMode == ViewMode.ALL ? ViewMode.FAVORITES : ViewMode.ALL;
            ((TextView) findViewById(R.id.title)).setText(currentViewMode == ViewMode.FAVORITES ? "Favorites" : currentCity.getName());
            ((TextView) findViewById(android.R.id.empty)).setText(currentViewMode == ViewMode.FAVORITES ? getString(R.string.no_favorites_messsage) : "");
            if (currentViewMode == ViewMode.FAVORITES) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            getSupportActionBar().getCustomView().invalidate();
            mHandler.post(changeViewModeTask);
        } else if (view.getId() == R.id.btn_share_this_app) {
            SNServices.share("", getString(R.string.share_message), null, this, new ShareSettings(getString(R.string.twitter_callback_url), getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret), getString(R.string.facebook_callback_url), getString(R.string.facebook_app_id), getString(R.string.facebook_app_secret)));
        } else if (view.getId() == R.id.btn_about_us) {
            FlurryAgent.logEvent("menu: about us");
            startActivity(new Intent(TrucksScreen.this, AboutUsScreen.class));
        }
    }

    private class MyHandler extends Handler {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_LIST:
                    adapter.setItems(currentViewMode == ViewMode.FAVORITES ? filteredProfiles : profiles);
                    statuses.onRefreshComplete();
                    setSupportProgressBarIndeterminateVisibility(false);
                    break;
                case REFRESH_MAP:
                    setSupportProgressBarIndeterminateVisibility(false);
                    List<Overlay> mapOverlays = mapView.getOverlays();
                    mapOverlays.clear();

                    boolean hasGeoTweets = false;
                    if (currentViewMode == ViewMode.FAVORITES) {
                        hasGeoTweets = (filteredGeoMentions.size() > 0 || filteredGeoProfiles.size() > 0);
                    } else {
                        hasGeoTweets = (geoMentions.size() > 0 || geoProfiles.size() > 0);
                    }
                    if (hasGeoTweets) {
                        mapOverlays.add(new TwitterMentionsOverlay(mapView, TrucksScreen.this, currentViewMode == ViewMode.FAVORITES ? filteredGeoMentions : geoMentions));
                        mapOverlays.add(new TwitterProfilesOverlay(mapView, TrucksScreen.this, currentViewMode == ViewMode.FAVORITES ? filteredGeoProfiles : geoProfiles));
                    } else {
                        showDisclosure();
                    }
                    mapView.invalidate();
                    break;
                case LOADING_LIST:
                    setSupportProgressBarIndeterminateVisibility(true);
                    statuses.setRefreshing();
                    break;
                case LOADING_MAP:
                    mapView.getOverlays().clear();
                    setSupportProgressBarIndeterminateVisibility(true);
                    Toast.makeText(TrucksScreen.this, getString(R.string.fetching_map_details), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private class Updater implements Runnable {
        private AtomicBoolean isActive = new AtomicBoolean(true);

        public void deactivate() {
            isActive.set(false);
        }

        @Override
        public void run() {
            new Thread() {
                public void run() {
                    if (isActive.get()) {
                        mHandler.sendEmptyMessage(LOADING_LIST);
                        if (loadFoodTrucks()) {
                            loadTwitterProfiles();
                            mHandler.sendEmptyMessage(REFRESH_LIST);

                            mHandler.sendEmptyMessage(LOADING_MAP);

                            List<String> names = DatabaseHelper.get(TrucksScreen.this).getTwitterNamesForCity(currentCity);
                            List<String> tmp = new ArrayList<String>();
                            for (int i = 0; i < names.size(); i++) {
                                tmp.add(names.get(i));
                                if (tmp.size() == 10) {
                                    addMentions(TwitterClient.getTwitterMentions(TrucksScreen.this, tmp));
                                    tmp.clear();
                                }
                                if (i == names.size() - 1 & tmp.size() > 0) {
                                    addMentions(TwitterClient.getTwitterMentions(TrucksScreen.this, tmp));
                                }
                            }
                            mHandler.sendEmptyMessage(REFRESH_MAP);
                        } else {
                            mHandler.sendEmptyMessage(REFRESH_LIST);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(TrucksScreen.this, TrucksScreen.this.getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                }
            }.start();
        }
    }

    private class ChangeViewModeTask implements Runnable {

        @Override
        public void run() {
            new Thread() {
                public void run() {
                    if (currentViewMode == ViewMode.FAVORITES) {
                        List<String> favoritedTwitterNames = DatabaseHelper.get(TrucksScreen.this).getFavoriteTwitterNames();
                        filteredProfiles.clear();
                        filteredGeoProfiles.clear();
                        for (TwitterStatus profile : profiles) {
                            if (favoritedTwitterNames.contains("@" + profile.getFromUser())) {
                                filteredProfiles.add(profile);
                                if (profile.hasGeo()) {
                                    filteredGeoProfiles.add(profile);
                                }
                            }
                        }
                        mHandler.sendEmptyMessage(REFRESH_LIST);
                        filteredGeoMentions.clear();
                        for (TwitterStatus mention : geoMentions) {
                            if (favoritedTwitterNames.contains(mention.getFromUser())) {
                                filteredGeoMentions.add(mention);
                            }
                        }
                        mHandler.sendEmptyMessage(REFRESH_MAP);
                    } else {
                        mHandler.sendEmptyMessage(REFRESH_LIST);
                        mHandler.sendEmptyMessage(REFRESH_MAP);
                    }
                }
            }.start();
        }
    }

    private void addMentions(List<TwitterStatus> results) {
        geoMentions.clear();
        for (TwitterStatus s : results) {
            if (s.hasGeo() && !s.isExpired(STATUS_EXPIRATION_TIMESTAMP)) {
                geoMentions.add(s);
            }
        }
    }

    private boolean loadFoodTrucks() {
        return provider.load();
    }

    private void loadTwitterProfiles() {
        List<String> allTwitterNames = DatabaseHelper.get(TrucksScreen.this).getTwitterNamesForCity(currentCity);
        List<String> tmp = new ArrayList<String>();
        profiles.clear();
        for (int i = 0; i < allTwitterNames.size(); i++) {
            tmp.add(allTwitterNames.get(i));
            if (tmp.size() == 100) {
                profiles.addAll(TwitterClient.getTwitterStatuses(TrucksScreen.this, tmp));
                tmp.clear();
            }
            if (i == allTwitterNames.size() - 1 & tmp.size() > 0) {
                profiles.addAll(TwitterClient.getTwitterStatuses(TrucksScreen.this, tmp));
            }
        }
        geoProfiles.clear();
        for (TwitterStatus profile : profiles) {
            if (profile.hasGeo() && !profile.isExpired(STATUS_EXPIRATION_TIMESTAMP)) {
                geoProfiles.add(profile);
            }
        }
        Collections.sort(profiles, mDateComparator);
    }

    private final Comparator<TwitterStatus> mDateComparator = new Comparator<TwitterStatus>() {
        @Override
        public int compare(TwitterStatus a, TwitterStatus b) {
            Date d1 = new Date(a.getCreatedAt());
            Date d2 = new Date(b.getCreatedAt());
            return -d1.compareTo(d2);
        }
    };

    private void showQuitConfirmationDalog() {
        new Builder(this).setMessage(getString(R.string.quit_confirmation_msg)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    private void hideDisclosure() {
        AlphaAnimation a = new AlphaAnimation(1, 0);
        a.setDuration(500);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                disclosure.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                disclosure.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        disclosure.startAnimation(a);
    }

    private void showDisclosure() {
        AlphaAnimation a = new AlphaAnimation(0, 1);
        a.setDuration(500);
        disclosure.setVisibility(View.VISIBLE);
        disclosure.startAnimation(a);
    }
}