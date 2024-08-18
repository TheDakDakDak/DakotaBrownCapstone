package com.dakbrown.weighttrackerapp;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "appDatabase.db";
    public static final int DATABASE_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PREFERRED_NAME = "preferred_name";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_GENDER = "gender";


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
                    COLUMN_PASSWORD + " TEXT," +
                    COLUMN_PREFERRED_NAME + " TEXT," +
                    COLUMN_HEIGHT + " INTEGER," +
                    COLUMN_AGE + " INTEGER," +
                    COLUMN_GENDER + " TEXT" + ")";


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
    //For use in Create Account for adding a new user.
    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long newRowId = db.insert("users", null, values);
        db.close();
        return newRowId;
    }
    //For use in GridActivity for adding a weight to the list.
    public void addWeightEntry(int userId, String date, int weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        db.insert(TABLE_WEIGHTS, null, values);
        db.close();
    }
    //For use in GridActivity to get the list of weights from the database.
    public Cursor getWeightsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_DATE, COLUMN_WEIGHT};
        String selection = COLUMN_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        return db.query(TABLE_WEIGHTS, columns, selection, selectionArgs, null, null, COLUMN_DATE + " DESC");
    }
    //For use in MainActivity to make sure the login credentials are correct.
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
    //For use in GridActivity to delete all weight entries.
    public void deleteAllWeightEntries(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COLUMN_USER_ID + "=?";
        String[] whereArgs = {String.valueOf(userId)};
        db.delete(TABLE_WEIGHTS, where, whereArgs);
        db.close();
    }
    //For use in GridActivity to change the goal weight.
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
    }
    //For use in GridActivity to get the goal ID from the database.
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
    //For use in GridActivity to get the goal weight by user ID.
    public int getGoalWeightByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int goalWeight = -1;

        try {
            cursor = db.query(TABLE_GOALS, new String[]{COLUMN_GOAL_WEIGHT}, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                goalWeight = cursor.getInt(cursor.getColumnIndex(COLUMN_GOAL_WEIGHT));
            } else {
                Log.d("DatabaseHelper", "No goal weight found for user ID: " + userId);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while fetching goal weight", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return goalWeight;
    }
    //For use in UserProfile to update their information.
    public void updateUserProfile(int userId, String preferredName, int age, int height, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PREFERRED_NAME, preferredName);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_GENDER, gender);
        String where = COLUMN_ID + "=?";
        String[] whereArgs = {String.valueOf(userId)};
        db.update(TABLE_USERS, values, where, whereArgs);
        db.close();
    }
    //For use in WeightLossCalculator to fill in the user's data for them.
    public Cursor getUserProfile(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PREFERRED_NAME, COLUMN_AGE, COLUMN_HEIGHT, COLUMN_GENDER};
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        return db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }
    //For use in GridActivity to calculate the change in weight from the last entry.
    public int getMostRecentWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_WEIGHT};
        String selection = COLUMN_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = COLUMN_DATE + " DESC";
        Cursor cursor = db.query(TABLE_WEIGHTS, columns, selection, selectionArgs, null, null, orderBy, "1");

        if (cursor != null && cursor.moveToFirst()) {
            int weight = cursor.getInt(cursor.getColumnIndex(COLUMN_WEIGHT));
            cursor.close();
            return weight;
        }
        if (cursor != null) {
            cursor.close();
        }
        return -1; // or any other default value if no weight is found
    }
}