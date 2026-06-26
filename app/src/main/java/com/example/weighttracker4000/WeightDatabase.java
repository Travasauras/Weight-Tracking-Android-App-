package com.example.weighttracker4000;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeightDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 2;


// set up the database and create the tables
    public WeightDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final class UserTable {
        private static final String TABLE_NAME = "users";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_USERNAME = "username";
        private static final String COLUMN_PASSWORD = "password";
    }

    private static final class WeightTable {
        private static final String TABLE_NAME = "weights";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_USER_ID = "user_id";
        private static final String COLUMN_DATE = "date";
        private static final String COLUMN_WEIGHT = "weight";
    }

    private static final class GoalTable {
        private static final String TABLE_NAME = "goals";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_USER_ID = "user_id";
        private static final String COLUMN_TARGET_WEIGHT = "target_weight";
    }

    private static final class NotificationTable {
        private static final String TABLE_NAME = "notifications";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_USER_ID = "user_id";
        private static final String COLUMN_ENABLED = "enabled";
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create table for user login information
        db.execSQL("create table " + UserTable.TABLE_NAME + " (" +
                UserTable.COLUMN_ID + " integer primary key autoincrement, " +
                UserTable.COLUMN_USERNAME + " text not null unique, " +
                UserTable.COLUMN_PASSWORD + " text not null);");

        // Create table for daily weight entries
        db.execSQL("create table " + WeightTable.TABLE_NAME + " (" +
                WeightTable.COLUMN_ID + " integer primary key autoincrement, " +
                WeightTable.COLUMN_USER_ID + " integer not null, " +
                WeightTable.COLUMN_DATE + " text not null, " +
                WeightTable.COLUMN_WEIGHT + " real not null);");

        // Create table for each user's goal weight
        db.execSQL("create table " + GoalTable.TABLE_NAME + " (" +
                GoalTable.COLUMN_ID + " integer primary key autoincrement, " +
                GoalTable.COLUMN_USER_ID + " integer not null, " +
                GoalTable.COLUMN_TARGET_WEIGHT + " real not null);");

        // Create table for SMS notification settings
        db.execSQL("create table " + NotificationTable.TABLE_NAME + " (" +
                NotificationTable.COLUMN_ID + " integer primary key autoincrement, " +
                NotificationTable.COLUMN_USER_ID + " integer not null, " +
                NotificationTable.COLUMN_ENABLED + " integer not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Recreate all tables when the database version changes
        db.execSQL("drop table if exists " + UserTable.TABLE_NAME);
        db.execSQL("drop table if exists " + WeightTable.TABLE_NAME);
        db.execSQL("drop table if exists " + GoalTable.TABLE_NAME);
        db.execSQL("drop table if exists " + NotificationTable.TABLE_NAME);
        onCreate(db);
    }

    // Add a new user account
    public long addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserTable.COLUMN_USERNAME, username);
        values.put(UserTable.COLUMN_PASSWORD, password);

        return db.insert(UserTable.TABLE_NAME, null, values);
    }

    // Check login credentials and return the matching user ID
    public int checkLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + UserTable.COLUMN_ID +
                        " FROM " + UserTable.TABLE_NAME +
                        " WHERE " + UserTable.COLUMN_USERNAME + " = ? AND " +
                        UserTable.COLUMN_PASSWORD + " = ?",
                new String[]{username, password}
        );

        int userID = -1;

        if (cursor.moveToFirst()) {
            userID = cursor.getInt(0);
        }

        cursor.close();
        return userID;
    }

    // Add a new weight entry for the current user
    public long addWeight(int userId, String date, double weight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WeightTable.COLUMN_USER_ID, userId);
        values.put(WeightTable.COLUMN_DATE, date);
        values.put(WeightTable.COLUMN_WEIGHT, weight);

        return db.insert(WeightTable.TABLE_NAME, null, values);
    }

    // Get all weight entries for the current user
    public Cursor getWeights(int userID) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + WeightTable.TABLE_NAME +
                        " WHERE " + WeightTable.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userID)}
        );
    }

    // Update an existing weight entry
    public int updateWeight(int weightID, String date, double weight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WeightTable.COLUMN_DATE, date);
        values.put(WeightTable.COLUMN_WEIGHT, weight);

        return db.update(
                WeightTable.TABLE_NAME,
                values,
                WeightTable.COLUMN_ID + " = ?",
                new String[]{String.valueOf(weightID)}
        );
    }

    // Delete a selected weight entry
    public int deleteWeight(int weightId) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                WeightTable.TABLE_NAME,
                WeightTable.COLUMN_ID + " = ?",
                new String[]{String.valueOf(weightId)}
        );
    }

    // Save or update the user's goal weight
    public long setGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "select * from " + GoalTable.TABLE_NAME +
                        " where " + GoalTable.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        ContentValues values = new ContentValues();
        values.put(GoalTable.COLUMN_USER_ID, userId);
        values.put(GoalTable.COLUMN_TARGET_WEIGHT, goalWeight);

        long result;

        // Update the goal if it already exists, otherwise insert a new goal
        if (cursor.moveToFirst()) {
            result = db.update(
                    GoalTable.TABLE_NAME,
                    values,
                    GoalTable.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
        } else {
            result = db.insert(
                    GoalTable.TABLE_NAME,
                    null,
                    values
            );
        }

        cursor.close();
        return result;
    }

    // Get the saved goal weight for the current user
    public double getGoalWeight(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "select " + GoalTable.COLUMN_TARGET_WEIGHT +
                        " from " + GoalTable.TABLE_NAME +
                        " where " + GoalTable.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        double goalWeight = 0;

        if (cursor.moveToFirst()) {
            goalWeight = cursor.getDouble(0);
        }

        cursor.close();
        return goalWeight;
    }

    // Check whether the user has already chosen SMS notification settings
    public boolean hasNotificationSettings(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "select * from " + NotificationTable.TABLE_NAME +
                        " where " + NotificationTable.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        boolean exists = cursor.moveToFirst();

        cursor.close();
        return exists;
    }

    // Save the user's SMS notification settings
    public void saveNotificationSettings(int userId, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotificationTable.COLUMN_USER_ID, userId);
        values.put(NotificationTable.COLUMN_ENABLED, enabled ? 1 : 0);

        Cursor cursor = db.rawQuery(
                "select * from " + NotificationTable.TABLE_NAME +
                        " where " + NotificationTable.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        // Update existing notification settings instead of creating duplicates
        if (cursor.moveToFirst()) {
            db.update(
                    NotificationTable.TABLE_NAME,
                    values,
                    NotificationTable.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
        } else {
            db.insert(NotificationTable.TABLE_NAME, null, values);
        }

        cursor.close();
    }

    // Check whether SMS notifications are enabled for the current user
    public boolean notificationsEnabled(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "select " + NotificationTable.COLUMN_ENABLED +
                        " from " + NotificationTable.TABLE_NAME +
                        " where " + NotificationTable.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        boolean enabled = false;

        if (cursor.moveToFirst()) {
            enabled = cursor.getInt(0) == 1;
        }

        cursor.close();
        return enabled;
    }
}