package com.dakbrown.weighttrackerapp;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "appDatabase.db";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";


    public static final String TABLE_WEIGHTS = "weights";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_WEIGHT = "weight";

    public static final String TABLE_GOALS = "goal_weights";
    public static final String COLUMN_GOAL_WEIGHT = "goal_weight";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT," +
                    COLUMN_PASSWORD + " TEXT" + ")";

    private static final String CREATE_WEIGHTS_TABLE =
            "CREATE TABLE " + TABLE_WEIGHTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER," +
                    COLUMN_DATE + " TEXT," +
                    COLUMN_WEIGHT + " INTEGER," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

    private static final String CREATE_GOALS_TABLE =
            "CREATE TABLE " + TABLE_GOALS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER," +
                    COLUMN_GOAL_WEIGHT + " INTEGER," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_WEIGHTS_TABLE);
        db.execSQL(CREATE_GOALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        onCreate(db);
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long newRowId = db.insert("users", null, values);
        db.close();
        return newRowId;
    }

    public void addWeightEntry(int userId, String date, int weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        db.insert(TABLE_WEIGHTS, null, values);
        db.close();
    }

    public void deleteWeightEntry(int userId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COLUMN_USER_ID + "=? AND " + COLUMN_DATE + "=?";
        String[] whereArgs = {String.valueOf(userId), date};
        db.delete(TABLE_WEIGHTS, where, whereArgs);
        db.close();
    }

    public Cursor getWeightsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_DATE, COLUMN_WEIGHT};
        String selection = COLUMN_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        return db.query(TABLE_WEIGHTS, columns, selection, selectionArgs, null, null, COLUMN_DATE + " DESC");
    }

    public int checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            db.close();
            return userId;
        }
        db.close();
        return -1;
    }

    public void updateWeightInDatabase(int userId, String date, int newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT, newWeight);
        String where = COLUMN_USER_ID + "=? AND " + COLUMN_DATE + "=?";
        String[] whereArgs = {String.valueOf(userId), date};
        db.update(TABLE_WEIGHTS, values, where, whereArgs);
        db.close();
    }

    public void updateGoalWeight(int userId, int goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_GOAL_WEIGHT, goalWeight);

        int id = getGoalId(userId);
        if (id != -1) {
            db.update(TABLE_GOALS, values, COLUMN_ID + "=?", new String[] {String.valueOf(id)});
        } else {
            db.insert(TABLE_GOALS, null, values);
        }
        db.close();
    }

    private int getGoalId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, new String[] {COLUMN_ID}, COLUMN_USER_ID + "=?", new String[] {String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        return -1;
    }

    public int getGoalWeightByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, new String[]{COLUMN_GOAL_WEIGHT}, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        int goalWeight = -1;
        if (cursor != null && cursor.moveToFirst()) {
            goalWeight = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return goalWeight;
    }
}