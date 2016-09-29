package com.uki.twitter.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.uki.twitter.model.RetweetsCounter;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String TAG = DatabaseHelper.class.getSimpleName();
	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "twitter.sqlite";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;

	// the DAO object we use to access the retweets table
	private Dao<RetweetsCounter, Integer> retweetsDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public DatabaseHelper(Context context, String dbName) {
		super(context, dbName, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, RetweetsCounter.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {

	}

	/**
	 * Returns the Database Access Object (DAO) for our Album class. It will
	 * create it or just give the cached value.
	 */
	public Dao<RetweetsCounter, Integer> getRetweetsDataDao()
			throws SQLException {
		if (retweetsDao == null) {
			retweetsDao = getDao(RetweetsCounter.class);
		}
		return retweetsDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		retweetsDao = null;
	}

	public List<RetweetsCounter> getAll() {

		try {
			QueryBuilder<RetweetsCounter, Integer> qb = getRetweetsDataDao()
					.queryBuilder()
					.orderBy(RetweetsCounter.EVENT_HASHTAG, true);
			return getRetweetsDataDao().query(qb.prepare());
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return new ArrayList<RetweetsCounter>();

	}

	public int insertRow(String hashTag, String userName) {
		RetweetsCounter row = new RetweetsCounter();
		row.setHashTag(hashTag);
		row.setUserName(userName);
		try {
			retweetsDao = getRetweetsDataDao();
			List<RetweetsCounter> items = retweetsDao
					.query(retweetsDao
							.queryBuilder()
							.where()
							.eq(RetweetsCounter.EVENT_HASHTAG, row.getHashTag())
							.and()
							.eq(RetweetsCounter.USER_NAME, row.getUserName())
							.prepare());

			if (items.size() > 0) {
				row.setId(items.get(0).getId());
				row.setCount(items.get(0).getCount() + 1);
				return retweetsDao.update(row);
			} else {
				row.setCount(1);
				return retweetsDao.create(row);
			}

		} catch (SQLException e) {
			Log.e(TAG, e.getMessage(), e);
			return -1;
		}

	}

}
