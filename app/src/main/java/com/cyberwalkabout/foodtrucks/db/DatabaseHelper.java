package com.cyberwalkabout.foodtrucks.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.cyberwalkabout.foodtrucks.FoodtrucksApp;
import com.cyberwalkabout.foodtrucks.cities.City;
import com.cyberwalkabout.foodtrucks.data.FoodTruck;
import com.uki.common.util.CursorUtils;
import com.uki.common.util.DistanceUtils;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    public static final String NAME = "FOODTRUCKS_DB_HELPER";

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "foodtrucks.db";
    private static final int DATABASE_VERSION = 6;

    public static DatabaseHelper get(Context context) {
        DatabaseHelper dbHelper = (DatabaseHelper) context.getSystemService(NAME);
        if (dbHelper == null) {
            context = context.getApplicationContext();
            dbHelper = (DatabaseHelper) context.getSystemService(NAME);
            if (dbHelper == null) {
                FoodtrucksApp app = (FoodtrucksApp) context;
                app.initDatabaseHelper();
                dbHelper = (DatabaseHelper) context.getSystemService(NAME);
            }
        }
        if (dbHelper == null) {
            throw new IllegalStateException("EventsDbHelper not available");
        }
        return dbHelper;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource connectionSource) {
        try {
            Log.d(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, FoodTruck.class);
            TableUtils.createTable(connectionSource, City.class);

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int arg2, int arg3) {
        try {
            DbUtils.backupTable(db, DbUtils.getTable(FoodTruck.class));
            DbUtils.backupTable(db, DbUtils.getTable(City.class));
            TableUtils.dropTable(connectionSource, FoodTruck.class, true);
            TableUtils.dropTable(connectionSource, City.class, true);
            onCreate(db, connectionSource);
            DbUtils.restoreBackup(db, FoodTruck.class);
            DbUtils.restoreBackup(db, City.class);
            DbUtils.dropTable(db, DbUtils.getTable(FoodTruck.class), true);
            DbUtils.dropTable(db, DbUtils.getTable(City.class), true);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void updateFoodTruck(FoodTruck foodTruck) {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        FoodTruck truck = exists(foodTruck);
        if (truck != null) {
            truck.setEmail(foodTruck.getEmail());
            truck.setPhone(foodTruck.getPhone());
            truck.setTwitterName(foodTruck.getTwitterName());
            truck.setWebsite(foodTruck.getWebsite());
            truck.setDescription(foodTruck.getDescription());
            truck.setUpdated(System.currentTimeMillis());
            truck.setCity(foodTruck.getCity());
            dao.update(truck);
        } else {
            foodTruck.setCreated(System.currentTimeMillis());
            foodTruck.setUpdated(System.currentTimeMillis());
            dao.create(foodTruck);
        }
    }

    public void markAsFavorite(FoodTruck foodTruck) {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        FoodTruck truck = exists(foodTruck);
        truck.setFavorite(foodTruck.isFavorite());
        dao.update(truck);
    }

    public void createFoodTruck(FoodTruck foodTruck) {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        dao.create(foodTruck);
    }

    public void updateCities(List<City> cities) {
        RuntimeExceptionDao<City, Integer> dao = getRuntimeExceptionDao(City.class);
        for (City city : cities) {
            dao.createOrUpdate(city);
        }
    }

    public void updateCity(City city) {
        RuntimeExceptionDao<City, String> dao = getRuntimeExceptionDao(City.class);
        dao.createOrUpdate(city);
    }

    public boolean hasCityWithCoordinates(City city) {
        RuntimeExceptionDao<City, String> cityDao = getRuntimeExceptionDao(City.class);
        QueryBuilder<City, String> queryBuilder = cityDao.queryBuilder();
        Where<City, String> where = queryBuilder.where();
        try {
            where.eq("_id", city.getId());
            queryBuilder.setWhere(where);
            City result = cityDao.queryForFirst(queryBuilder.prepare());
            if (result != null) {
                return result.getLatitude() > 0;
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    public List<FoodTruck> getAllFoodtrucks() {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        return dao.queryForAll();
    }

    public List<City> getAllCities() {
        RuntimeExceptionDao<City, Integer> dao = getRuntimeExceptionDao(City.class);
        try {
            return dao.query(dao.queryBuilder().orderBy(City.NAME, true).prepare());
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public City getCityById(String id) {
        RuntimeExceptionDao<City, String> cityDao = getRuntimeExceptionDao(City.class);
        QueryBuilder<City, String> queryBuilder = cityDao.queryBuilder();
        Where<City, String> where = queryBuilder.where();
        try {
            where.eq("_id", id);
            queryBuilder.setWhere(where);
            return cityDao.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public City getCityByName(String name) {
        RuntimeExceptionDao<City, String> cityDao = getRuntimeExceptionDao(City.class);
        QueryBuilder<City, String> queryBuilder = cityDao.queryBuilder();
        Where<City, String> where = queryBuilder.where();
        try {
            where.eq("name", name);
            queryBuilder.setWhere(where);
            return cityDao.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public int getCitiesCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("select count(*) from cities", null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public List<City> selectNearbyCities(Location loc) {
        List<City> allCities = getAllCities();
        List<City> result = new ArrayList<City>();
        for (City city : allCities) {
            double distance = DistanceUtils.distance(loc.getLatitude(), loc.getLongitude(), city.getLatitude(), city.getLongitude());
            Log.d("distance", "= " + distance);
            if (distance <= 300) {
                result.add(city);
            }
        }
        return result;

    }

    public List<String> getAllTwitterNames() {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        List<String> twitterNames = new ArrayList<String>();
        try {
            QueryBuilder<FoodTruck, Integer> qb = dao.queryBuilder();
            qb.selectColumns(new String[]{FoodTruck.TWITTER_NAME});
            List<FoodTruck> result = dao.query(qb.prepare());
            if (result != null && result.size() > 0) {

                for (FoodTruck item : result) {
                    twitterNames.add(item.getTwitterName());
                }
                return twitterNames;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error occurred while retrieving food trucks", e);
        }
        return twitterNames;
    }

    public List<String> getTwitterNamesForCity(City city) {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        List<String> twitterNames = new ArrayList<String>();
        try {
            QueryBuilder<FoodTruck, Integer> qb = dao.queryBuilder();
            qb.selectColumns(new String[]{FoodTruck.TWITTER_NAME});
            qb.where().eq(FoodTruck.CITY, city.getName());
            List<FoodTruck> result = dao.query(qb.prepare());
            if (result != null && result.size() > 0) {

                for (FoodTruck item : result) {
                    twitterNames.add(item.getTwitterName());
                }
                return twitterNames;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error occurred while retrieving food trucks", e);
        }
        return twitterNames;
    }

    public List<String> getFavoriteTwitterNames() {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        List<String> twitterNames = new ArrayList<String>();
        try {
            QueryBuilder<FoodTruck, Integer> qb = dao.queryBuilder();
            qb.selectColumns(new String[]{FoodTruck.TWITTER_NAME});
            qb.where().eq(FoodTruck.FAVORITE, true);
            List<FoodTruck> result = dao.query(qb.prepare());
            if (result != null && result.size() > 0) {

                for (FoodTruck item : result) {
                    twitterNames.add(item.getTwitterName());
                }
                return twitterNames;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error occurred while retrieving food trucks", e);
        }
        return twitterNames;
    }

    public FoodTruck getFoodTruckByTwitterName(String twitterName) {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        try {
            QueryBuilder<FoodTruck, Integer> qb = dao.queryBuilder();
            qb.where().like(FoodTruck.TWITTER_NAME, twitterName);
            return dao.queryForFirst(qb.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "Error occurred while retrieving food trucks", e);
        }
        return null;
    }

    public FoodTruck exists(FoodTruck foodTruck) {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        try {
            QueryBuilder<FoodTruck, Integer> qb = dao.queryBuilder();
            qb.where().eq(FoodTruck.TWITTER_NAME, foodTruck.getTwitterName());
            List<FoodTruck> result = dao.query(qb.prepare());
            if (result != null && result.size() > 0) {
                return result.get(0);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error occurred while retrieving food trucks", e);
        }
        return null;
    }

    public void deleteExpiredRecords() {
        RuntimeExceptionDao<FoodTruck, Integer> dao = getRuntimeExceptionDao(FoodTruck.class);
        try {
            DeleteBuilder<FoodTruck, Integer> deleteDao = dao.deleteBuilder();
            deleteDao.where().le("updated", (System.currentTimeMillis() - 100000));
            dao.delete(deleteDao.prepare());
        } catch (SQLException e) {
            Log.e(TAG, "Error occurred while deleting expired food trucks", e);
        }
    }
}
