package com.uki.twitter.model;

import org.json.JSONObject;

public class Tweet {

    private long id;
    private String text;
    private String created_at;
    private String profile_image_url;
    private String from_user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    public String getFrom_user() {
        return from_user;
    }

    public void setFrom_user(String from_user) {
        this.from_user = from_user;
    }

    public static Tweet valueOf(JSONObject tweetJSON) {

        Tweet tweet = new Tweet();
        tweet.setId(tweetJSON.optLong("id"));
        tweet.setCreated_at(tweetJSON.optString("created_at"));
        tweet.setFrom_user(tweetJSON.optJSONObject("user").optString("screen_name"));
        tweet.setProfile_image_url(tweetJSON.optJSONObject("user").optString("profile_image_url"));
        tweet.setText(tweetJSON.optString("text"));

        return tweet;
    }
}
