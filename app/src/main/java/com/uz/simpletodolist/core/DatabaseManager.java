package com.uz.simpletodolist.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.uz.simpletodolist.model.Task;
import com.uz.simpletodolist.model.User;
import com.uz.simpletodolist.utils.UtilsDateTime;

import java.util.ArrayList;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    public static final String TAG = "DBManager";
    // Database Info
    private static final String DATABASE_NAME = "aast-todo-db";
    private static final int DATABASE_VERSION = 7;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_TASKS = "tasks";

    // Users Table Columns
    private static final String KEY_USERS_ID = "id";
    private static final String KEY_USERS_EMAIL = "email";
    private static final String KEY_USERS_PASSWORD = "password";

    // Tasks Table Columns
    private static final String KEY_TASK_LOCALID = "localid";
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_USER_ID = "user_id";
    private static final String KEY_TASK_CREATED_AT = "createdAt";
    private static final String KEY_TASK_SYNCED_AT = "syncAt";
    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_BODY = "body";
    private static final String KEY_TASK_DONE = "done";
    private static final String KEY_TASK_SYNCED = "synced";
    private static final String KEY_TASK_DELETED = "deleted";

    private static DatabaseManager instance;

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }


    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        Log.d(TAG, "The database is created for the FIRST time");
    }

    private void createTables(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_USERS_EMAIL + " TEXT UNIQUE ," +
                KEY_USERS_PASSWORD + " TEXT " +
                ")";

        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS +
                "(" +
                KEY_TASK_LOCALID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a local primary key
                KEY_TASK_ID + " INTEGER," + //ID on server
                KEY_TASK_USER_ID + " INTEGER ," +
                KEY_TASK_CREATED_AT + " TEXT ," +
                KEY_TASK_SYNCED_AT + " TEXT ," +
                KEY_TASK_TITLE + " TEXT ," +
                KEY_TASK_BODY + " TEXT ," +
                KEY_TASK_DONE + " INTEGER, " +
                KEY_TASK_SYNCED + " INTEGER, " +
                KEY_TASK_DELETED + " INTEGER, " +
                 " FOREIGN KEY("+KEY_TASK_USER_ID+") REFERENCES "+TABLE_USERS+"("+KEY_USERS_ID+")" +
                ")";


        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            createTables(db);
            Log.d(TAG, "The database is updated from v: " + oldVersion + " to v: " + newVersion);
        }
    }

    public User insertUser(String email, String password) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        User user = null;
        int rowId = -1;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USERS_EMAIL, email);
            values.put(KEY_USERS_PASSWORD, password);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            rowId = (int)db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
            Log.d(TAG, "Successful insertion for user: " + email);
            user = new User();
            user.setId(rowId);
            user.setEmail(email);

        } catch (Exception e) {
            Log.e(TAG, "Error while trying to add user to database", e);
        } finally {
            db.endTransaction();
        }
        return user;
    }

    public User getUser(String email, String password) {
        User user = null;
        String QUERY =
                String.format("SELECT * FROM %s WHERE %s = '%s' AND %s = '%s'", TABLE_USERS, KEY_USERS_EMAIL, email, KEY_USERS_PASSWORD, password);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(KEY_USERS_ID)));
                user.setEmail(email);
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get user from database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return user;
    }



    public ArrayList<Task> getAllTasks(int userId) {
        ArrayList<Task> tasks = new ArrayList<>();
        String QUERY =
                String.format("SELECT * FROM %s WHERE %s = %s AND %s == 0", TABLE_TASKS, KEY_TASK_USER_ID, userId, KEY_TASK_DELETED);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    int localId = cursor.getInt(cursor.getColumnIndex(KEY_TASK_LOCALID));
                    int id = cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID));
                    String createdAt = cursor.getString(cursor.getColumnIndex(KEY_TASK_CREATED_AT));
                    String syncedAt = cursor.getString(cursor.getColumnIndex(KEY_TASK_SYNCED_AT));
                    String title = cursor.getString(cursor.getColumnIndex(KEY_TASK_TITLE));
                    String body = cursor.getString(cursor.getColumnIndex(KEY_TASK_BODY));
                    boolean done = cursor.getInt(cursor.getColumnIndex(KEY_TASK_DONE)) != 0;
                    boolean synced = cursor.getInt(cursor.getColumnIndex(KEY_TASK_SYNCED)) != 0;

                    task.setLocalId(localId);
                    task.setId(id);
                    task.setTitle(title);
                    task.setBody(body);
                    task.setCreatedAt(createdAt);
                    task.setSyncedAt(syncedAt);
                    task.setDone(done);
                    task.setSynced(synced);

                    tasks.add(task);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get tasks from database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    public ArrayList<Task> getAllTasksIncludeDeleted(int userId) {
        ArrayList<Task> tasks = new ArrayList<>();
        String QUERY =
                String.format("SELECT * FROM %s WHERE %s = %s", TABLE_TASKS, KEY_TASK_USER_ID, userId);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    int localId = cursor.getInt(cursor.getColumnIndex(KEY_TASK_LOCALID));
                    int id = cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID));
                    String createdAt = cursor.getString(cursor.getColumnIndex(KEY_TASK_CREATED_AT));
                    String syncedAt = cursor.getString(cursor.getColumnIndex(KEY_TASK_SYNCED_AT));
                    String title = cursor.getString(cursor.getColumnIndex(KEY_TASK_TITLE));
                    String body = cursor.getString(cursor.getColumnIndex(KEY_TASK_BODY));
                    boolean done = cursor.getInt(cursor.getColumnIndex(KEY_TASK_DONE)) != 0;
                    boolean synced = cursor.getInt(cursor.getColumnIndex(KEY_TASK_SYNCED)) != 0;
                    boolean deleted = cursor.getInt(cursor.getColumnIndex(KEY_TASK_DELETED)) != 0;

                    task.setLocalId(localId);
                    task.setId(id);
                    task.setTitle(title);
                    task.setBody(body);
                    task.setCreatedAt(createdAt);
                    task.setSyncedAt(syncedAt);
                    task.setDone(done);
                    task.setSynced(synced);
                    task.setDeleted(deleted);
                    tasks.add(task);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get tasks from database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    public Task insertTask(String title, String body, int userId) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        long rowId;
        Task task = null;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            String currentTime = UtilsDateTime.getISO8601String();
            ContentValues values = new ContentValues();
            values.put(KEY_TASK_USER_ID, userId);
            values.put(KEY_TASK_CREATED_AT, currentTime);
            values.put(KEY_TASK_TITLE, title);
            values.put(KEY_TASK_BODY, body);
            values.put(KEY_TASK_DONE, false);
            values.put(KEY_TASK_SYNCED, false);
            values.put(KEY_TASK_DELETED, false);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            rowId = db.insertOrThrow(TABLE_TASKS, null, values);
            db.setTransactionSuccessful();
            task = new Task();
            task.setLocalId((int)rowId);
            task.setTitle(title);
            task.setBody(body);
            task.setCreatedAt(currentTime);
            Log.d(TAG, "Successful insertion for task: " + task.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to add task to database", e);
        } finally {
            db.endTransaction();
        }
        return task;
    }

    public Task insertTask(Task existingTask, int userId) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        long rowId;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            String currentTime = UtilsDateTime.getISO8601String();
            ContentValues values = new ContentValues();
            values.put(KEY_TASK_USER_ID, userId);
            values.put(KEY_TASK_ID, existingTask.getId());
            values.put(KEY_TASK_CREATED_AT, existingTask.getCreatedAt());
            values.put(KEY_TASK_TITLE, existingTask.getTitle());
            values.put(KEY_TASK_BODY, existingTask.getBody());
            values.put(KEY_TASK_DONE, existingTask.isDone());
            values.put(KEY_TASK_SYNCED, existingTask.isSynced());
            values.put(KEY_TASK_DELETED, false);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            rowId = db.insertOrThrow(TABLE_TASKS, null, values);
            existingTask.setLocalId((int)rowId);
            db.setTransactionSuccessful();

            Log.d(TAG, "Successful insertion for task: " + existingTask.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to add task to database", e);
        } finally {
            db.endTransaction();
        }
        return existingTask;
    }


    public long updateTask(Task task) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        long rowId = -1;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TASK_ID, task.getId());
            values.put(KEY_TASK_SYNCED_AT, task.getCreatedAt());
            values.put(KEY_TASK_TITLE, task.getTitle());
            values.put(KEY_TASK_BODY, task.getBody());
            values.put(KEY_TASK_DONE, task.isDone());
            values.put(KEY_TASK_SYNCED, task.isSynced());
            values.put(KEY_TASK_SYNCED_AT, task.getSyncedAt());

            rowId = db.update(TABLE_TASKS, values, KEY_TASK_LOCALID + " = ?",
                    new String[]{String.valueOf(task.getLocalId())});
            db.setTransactionSuccessful();
            Log.d(TAG, "Successful update for task: " + task.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to update task in database", e);
        } finally {
            db.endTransaction();
        }
        return rowId;
    }

    public long deleteTask(Task task) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        long rowId = -1;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TASK_DELETED, true);

            rowId = db.update(TABLE_TASKS, values, KEY_TASK_LOCALID + " = ?",
                    new String[]{String.valueOf(task.getLocalId())});
            db.setTransactionSuccessful();
            Log.d(TAG, "Successful soft deleted task: " + task.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to delete task from database", e);
        } finally {
            db.endTransaction();
        }
        return rowId;
    }
}
