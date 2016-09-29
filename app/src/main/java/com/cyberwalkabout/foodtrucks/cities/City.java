package com.cyberwalkabout.foodtrucks.cities;

import java.io.Serializable;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cities")
public class City implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2928894470352253767L;
    public static final String NAME = "name";
    public static final String LATITUDE = "latitude";
    public static final String LONGUTIDE = "longitude";

    @DatabaseField(columnName = BaseColumns._ID, id = true)
    private String id;
    @DatabaseField(columnName = NAME)
    private String name;
    @DatabaseField(columnName = LATITUDE)
    private double latitude;
    @DatabaseField(columnName = LONGUTIDE)
    private double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
