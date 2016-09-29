package com.cyberwalkabout.foodtrucks.data;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class FoodTruck implements Serializable {

    public static final String TWITTER_NAME = "twitter_name";
    public static final String DESCRIPTION = "description";
    public static final String WEBSITE_URL = "website";
    public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phone";
    public static final String CITY = "city";
    public static final String FAVORITE = "favorite";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = TWITTER_NAME)
    private String twitterName;
    @DatabaseField(columnName = DESCRIPTION)
    private String description;
    @DatabaseField(columnName = WEBSITE_URL)
    private String website;
    @DatabaseField(columnName = EMAIL)
    private String email;
    @DatabaseField(columnName = PHONE_NUMBER)
    private String phone;
    @DatabaseField(columnName = CITY)
    private String city;
    @DatabaseField(columnName = FAVORITE)
    private boolean favorite;
    @DatabaseField(columnName = CREATED)
    private long created;
    @DatabaseField(columnName = UPDATED)
    private long updated;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTwitterName() {
        return twitterName;
    }

    public void setTwitterName(String twitterName) {
        this.twitterName = twitterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
