package com.cyberwalkabout.foodtrucks.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.uki.common.util.CursorUtils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Maria Dzyokh
 */
public class DbUtils {

    private static final String TAG = DbUtils.class.getSimpleName();

    public static void backupTable(SQLiteDatabase db, String tableName) {
        try {
            db.execSQL("alter table " + tableName + " rename to bak_" + tableName);
        } catch (SQLException e) {
            Log.w(TAG, "Couldn't backup table '" + tableName + "'");
        }
    }

    public static void restoreBackup(SQLiteDatabase db, String tableName) {
        try {
            db.execSQL("insert into " + tableName + " select * from bak_" + tableName);
        } catch (SQLException e) {
            Log.w(TAG, "Couldn't restore backup from table 'bak_" + tableName + "' to '" + tableName + "'");
        }
    }

    public static void dropTable(SQLiteDatabase db, String tableName, boolean isBackup) {
        try {
            db.execSQL("drop table " + (isBackup ? "bak_" + tableName : tableName));
        } catch (SQLException e) {
            Log.w(TAG, "Couldn't drop table '" + tableName + "'");
        }
    }

    public static String getTable(Class clazz) {
        DatabaseTable annotation = (DatabaseTable) clazz.getAnnotation(DatabaseTable.class);
        return annotation.tableName();
    }

    public static int count(SQLiteDatabase db, String table) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from " + table, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public static int count(SQLiteDatabase db, String table, String id) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from " + table + " where id=" + id, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public static int count(SQLiteDatabase db, String table, String column, String value) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from " + table + " where " + column + "=" + value, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return count;
    }

    public static void restoreBackup(SQLiteDatabase db, Class entityClass) {
        String tableName = getTable(entityClass);
        try {
            Cursor cursor = db.rawQuery("select * from bak_" + tableName, null);

            try {
                if (cursor.getCount() > 0) {
                    ArrayList<String> columns = DbUtils.getColumns(entityClass);

                    while (cursor.moveToNext()) {
                        ContentValues values = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor, values);

                        if (!columns.isEmpty()) {

                            Set<String> toRemove = new HashSet<String>();

                            Iterator i = values.valueSet().iterator();
                            while (i.hasNext()) {
                                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) i.next();
                                if (!columns.contains(entry.getKey())) {
                                    toRemove.add(entry.getKey());
                                }
                            }

                            if (!toRemove.isEmpty()) {
                                for (String removedColumn : toRemove) {
                                    values.remove(removedColumn);
                                }
                            }
                        }

                        db.insert(tableName, null, values);
                    }
                }
            } finally {
                cursor.close();
            }

        } catch (SQLException e) {
            Log.w(TAG, "Couldn't restore backup from table 'bak_" + tableName + "' to '" + tableName + "'");
        }
    }

    public static ArrayList<String> getColumns(Class clazz) {
        ArrayList<String> list = new ArrayList<String>();
        getColumns(clazz, list);
        return list;
    }

    private static void getColumns(Class clazz, List<String> columns) {
        if (clazz.getSuperclass() != Object.class) {
            getColumns(clazz.getSuperclass(), columns);
        }

        for (Field field : clazz.getDeclaredFields()) {
            DatabaseField annotation = field.getAnnotation(DatabaseField.class);
            if (annotation != null) {
                columns.add(annotation.columnName());
            }
        }
    }
}
