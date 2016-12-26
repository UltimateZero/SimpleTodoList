package com.uz.simpletodolist.core;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    public static final String TAG = "DBManager";
    // Database Info
    private static final String DATABASE_NAME = "aast-todo-db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TASKS = "tasks";

    // Tasks Table Columns
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_CREATED_AT = "createdAt";
    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_BODY = "body";
    private static final String KEY_TASK_DONE = "done";

    private static DatabaseManager instance;

    public static synchronized DatabaseManager getInstance(Context context) {
        if(instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }


    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS +
                "(" +
                KEY_TASK_ID + " INTEGER PRIMARY KEY ," + // Define a primary key
                KEY_TASK_CREATED_AT + " TEXT ," +
                KEY_TASK_TITLE + " TEXT ," +
                KEY_TASK_BODY + " TEXT ," +
                KEY_TASK_DONE + " INTEGER " +
                ")";

        db.execSQL(CREATE_TASKS_TABLE);
        Log.d(TAG, "The database is created for the FIRST time");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
