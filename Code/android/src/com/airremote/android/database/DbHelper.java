package com.airremote.android.database;

import android.database.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbHelper {
    public static final String TABLE_TEST_CODE = "TbTestCode";
    private static final String DATABASE_NAME = "AirRemote";
    private static final String TABLE_DB_VERSION = "TbDBVersion";
    private static final String TABLE_BUTTON_ICON = "TbBtnIcon";
    private static final String TABLE_DEVICE = "TbDevice";
    private static final int    DATABASE_VERSION = 1;
    private static final String TAG = "DbHelper";

    private Context mCtx;

    private static final String CREATE_DB_VERSION_TABLE =
            "CREATE TABLE " + TABLE_DB_VERSION + " ("
             + "version INTEGER NOT NULL)";

    private static final String CREATE_BUTTON_ICON_TABLE =
            "CREATE TABLE " + TABLE_BUTTON_ICON + " ("
                        + "Id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "TypeName VARCHAR(50), "
                        + "FileName VARCHAR(50), "
                        + "Custom CHAR(1))";

    private static final String CREATE_DEVICE_TABLE = "" +
    		"CREATE TABLE " + TABLE_DEVICE + " ("
    		    + "Id INTEGER PRIMARY KEY AUTOINCREMENT, "
    		    + "Name VARCHAR(50), "
                + "Type INTEGER)";

    private static final String CREATE_TEST_CODE_TABLE = "" +
            "CREATE TABLE " + TABLE_TEST_CODE + " ("
                + "KeyIdx INTEGER PRIMARY KEY, "
                + "Code VARCHER(512))";

    private SQLiteDatabase mDatabase;

    public DbHelper(Context ctx) {
        mCtx = ctx;
        try {
            mDatabase = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
            Cursor cursor = mDatabase.query("sqlite_master",
                            new String[] {"name"},
                            "type='table' and name='" + TABLE_DB_VERSION + "'",
                            null, null, null, null);
            int numRows = cursor.getCount();
            if (numRows < 1) {
                CreateDatabase(mDatabase);
            } else {
                int version = 0;
                Cursor c = mDatabase.query(true, TABLE_DB_VERSION, new String[] { "version" },
                        null, null, null, null, null, null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    version = c.getInt(0);
                }
                c.close();
                if (version != DATABASE_VERSION) {
                    Log.e(TAG, "database version mismatch");
                }
            }
            cursor.close();
        } catch (SQLException e) {
            Log.d(TAG, "SQLite exception: " + e.getLocalizedMessage());
        } finally {
            mDatabase.close();
        }
    }

    private void CreateDatabase(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_DB_VERSION_TABLE);
            ContentValues value = new ContentValues();
            value.put("version", DATABASE_VERSION);
            db.insert(TABLE_DB_VERSION, null, value);
            db.execSQL(CREATE_BUTTON_ICON_TABLE);
            db.execSQL(CREATE_TEST_CODE_TABLE);
        } catch (SQLException e) {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        }
    }

    public void close() {
        mDatabase.close();
    }

    public void addButtonIconEntry(String typeName, String fileName) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("typeName", typeName);
        initialValues.put("fileName", fileName);
        mDatabase = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        mDatabase.insert(TABLE_BUTTON_ICON, null, initialValues);
        mDatabase.close();
    }

    public List<ButtonIconEntry> fetchAllButtonIconRows() {
        ArrayList<ButtonIconEntry> result = new ArrayList<ButtonIconEntry>();
        try {
            mDatabase = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
            Cursor c;
            c = mDatabase.query(TABLE_BUTTON_ICON,
                    new String[] {"id", "fileName"}, null, null, null, null, null);

            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; i++) {
                ButtonIconEntry row = new ButtonIconEntry();
                row.id = c.getInt(0);
                row.fileName = c.getString(1);

                result.add(row);
                c.moveToNext();
            }
            c.close();
        } catch (SQLException e) {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        } finally {
            mDatabase.close();
        }

        return result;
    }

    public List<DeviceEntry> fetchAllDeviceRows() {
        ArrayList<DeviceEntry> result = new ArrayList<DeviceEntry>();
        try {
            mDatabase = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
            Cursor c;
            c = mDatabase.query(TABLE_DEVICE,
                    new String[] { "Id", "DeviceType", "DeviceName" }, null, null, null, null, null);
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; i++) {
                DeviceEntry row = new DeviceEntry();
                row.DeviceType = c.getString(1);
                row.DeviceName = c.getString(2);

                result.add(row);
                c.moveToNext();
            }
            c.close();
        } catch (SQLException e) {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        } finally {
            mDatabase.close();
        }

        return result;
    }

    public String ReadTestIRCode(int key) {
        String result = null;
        try {
            mDatabase = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
            Cursor c;
            c = mDatabase.query(TABLE_TEST_CODE,
                    new String[] { "KeyIdx", "Code" }, "KeyIdx=" + key , null, null, null, null);
            int numRows = c.getCount();
            if (numRows > 0) {
                c.moveToFirst();
                result = c.getString(1);
            }
            c.close();
        } catch (SQLException e) {
            Log.d(TAG,"SQLite exception: " + e.getLocalizedMessage());
        } finally {
            mDatabase.close();
        }
        return result;
    }

    public void UpdateTestIRCode(int key, String value) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("KeyIdx", key);
        initialValues.put("Code", value);
        mDatabase = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        mDatabase.replace(TABLE_TEST_CODE, null, initialValues);
        mDatabase.close();
    }
}
