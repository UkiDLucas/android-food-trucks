package com.cyberwalkabout.foodtrucks.twitter;

import android.util.Log;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * @author Maria Dzyokh
 */

@DatabaseTable(tableName = "twitter_statuses")
public class TwitterStatus {

    private static final String TAG = TwitterStatus.class.getSimpleName();
    @DatabaseField(columnName = "_id", id = true)
    private long id;
    @DatabaseField(columnName = "lat")
    private double latitude;
    @DatabaseField(columnName = "lng")
    private double longitude;
    @DatabaseField(columnName = "created_at")
    private String createdAt;
    @DatabaseField(columnName = "text")
    private String text;
    @DatabaseField(columnName = "from_user")
    private String fromUser;
    @DatabaseField(columnName = "image_url")
    private String imageUrl;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasGeo() {
        return getLatitude() != 0.0 && getLongitude() != 0.0;
    }

    public boolean isExpired(long timestamp) {
        Date date = new Date(getCreatedAt());
        return System.currentTimeMillis() - date.getTime() > timestamp;
    }

    public static TwitterStatus fromJSON(JSONObject status) {
        try {
            TwitterStatus s = new TwitterStatus();
            if (!status.getString("geo").equals("null")) {
                s.setLatitude(status.getJSONObject("geo")
                        .getJSONArray("coordinates").getDouble(0));
                s.setLongitude(status.getJSONObject("geo")
                        .getJSONArray("coordinates").getDouble(1));
            }
            s.setFromUser(status.getJSONObject("user").getString("screen_name"));
            s.setCreatedAt(status.getString("created_at"));
            s.setText(status.getString("text"));
            s.setId(status.getLong("id"));
            return s;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getFromUser() {
        return fromUser;
    }

}
