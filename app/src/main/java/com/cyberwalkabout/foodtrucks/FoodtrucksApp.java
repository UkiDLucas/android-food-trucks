package com.cyberwalkabout.foodtrucks;

import android.app.Application;
import com.cyberwalkabout.foodtrucks.db.DatabaseHelper;


/**
 * @author Maria Dzyokh
 */
public class FoodtrucksApp extends Application {

    private volatile DatabaseHelper databaseHelper;

    @Override
    public Object getSystemService(String name) {
        if (DatabaseHelper.NAME.equals(name)) {
            return databaseHelper;
        } else {
            return super.getSystemService(name);
        }
    }

    public void initDatabaseHelper() {
        databaseHelper = new DatabaseHelper(this);
    }
}
