package com.cyberwalkabout.foodtrucks.data;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.cyberwalkabout.foodtrucks.cities.City;
import com.cyberwalkabout.foodtrucks.db.DatabaseHelper;
import com.uki.common.http.HttpClientConfig;
import com.uki.common.http.HttpUtils;
import com.cyberwalkabout.gss.AtomPublicSpreadsheetsClient;
import com.cyberwalkabout.gss.Entry;
import com.cyberwalkabout.gss.Worksheet;
import org.apache.http.client.HttpClient;

public class SpreadSheetFoodTrucksProvider {

    private static final String TAG = SpreadSheetFoodTrucksProvider.class.getSimpleName();

    private static final String KEY_TWITTER_NAME = "twitter";
    private static final String KEY_DESCRIPTION = "descriptionoffood";
    private static final String KEY_WEBSITE = "website";
    private static final String KEY_EMAIL = "e-mail";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_KITCHEN_ADDRESS = "kitchenaddress";
    private static final String KEY_HASHTAG = "hashtag";
    private static final String KEY_FACEBOOK = "fb";

    private AtomPublicSpreadsheetsClient spreadsheetsClient;
    private String spreadsheetKey;
    private City city;
    private DatabaseHelper helper;

    public void setCity(com.cyberwalkabout.foodtrucks.cities.City city) {
        this.city = city;

    }

    public SpreadSheetFoodTrucksProvider(String spreadsheetKey, City city, Context ctx) {
        this.spreadsheetsClient = new AtomPublicSpreadsheetsClient();
        this.spreadsheetKey = spreadsheetKey;
        this.city = city;
        this.helper = DatabaseHelper.get(ctx);
    }

    public boolean load() {
        boolean successful = true;
        try {
            HttpClient httpClient = HttpUtils.createHttpClient(HttpClientConfig.createConfig(10000));
            Worksheet worksheet = spreadsheetsClient.getWorksheet(spreadsheetKey, city.getId(), httpClient);
            if (worksheet != null && worksheet.getEntries() != null) {
                for (Entry entry : worksheet.getEntries()) {
                    FoodTruck foodTruck = new FoodTruck();
                    Map<String, String> values = getContentAsMap(entry.getContent());
                    if (values.containsKey(KEY_DESCRIPTION)) {
                        foodTruck.setDescription(values.get(KEY_DESCRIPTION));
                    }
                    if (values.containsKey(KEY_TWITTER_NAME)) {
                        foodTruck.setTwitterName(values.get(KEY_TWITTER_NAME));
                    }
                    foodTruck.setCity(city.getName());
                    helper.updateFoodTruck(foodTruck);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            successful = false;
            BugSenseHandler.sendException(e);
        }
        return successful;
    }

    public Map<String, String> getContentAsMap(String content) {
        Map<String, String> map = new HashMap<String, String>();
        if (content != null && content.length() > 0) {
            StringBuilder sb = new StringBuilder();

//            cut off description as it contains arbitary text and affects at
//            the result of parsing
            int description_start = content.indexOf(KEY_DESCRIPTION + ": ");
            int description_end = content.indexOf(KEY_WEBSITE + ": ");
            if (description_end == -1) {
                description_end = content.indexOf(KEY_EMAIL + ": ");
            }
            if (description_end == -1) {
                description_end = content.indexOf(KEY_PHONE + ": ");
            }
            if (description_end == -1) {
                description_end = content.indexOf(KEY_KITCHEN_ADDRESS + ": ");
            }
            if (description_end == -1) {
                description_end = content.indexOf(KEY_HASHTAG + ": ");
            }
            if (description_end == -1) {
                description_end = content.indexOf(KEY_FACEBOOK + ": ");
            }
            if (description_end == -1) {
                description_end = content.length() - 1;
            }
            if (description_start > -1) {
                map.put(KEY_DESCRIPTION, content.substring(description_start + 19, description_end - 2));
                sb.setLength(0);
                sb.append(content.substring(0, description_start)).append(content.substring(description_end, content.length()));
            }
            String[] array = sb.toString().split(", |: ");
            for (int i = 0; i < array.length; i += 2) {
                if (i + 1 < array.length) {
                    map.put(array[i], array[i + 1]);
                }
            }
        }
        return map;
    }
}
