package org.androidpn.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by daktak on 2/20/16.
 */
public class PNNotificationDataSource {
    final static ArrayList<HashMap<String, ?>> notifications = new ArrayList<HashMap<String, ?>>();

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_MESSAGE,
            MySQLiteHelper.COLUMN_URI,
            MySQLiteHelper.COLUMN_DTTM};

    public PNNotificationDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public PNNotification createNotification(String title, String message, String uri) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title);
        values.put(MySQLiteHelper.COLUMN_MESSAGE, message);
        values.put(MySQLiteHelper.COLUMN_URI, uri);
        values.put(MySQLiteHelper.COLUMN_DTTM, getDateTime());
        long insertId = database.insert(MySQLiteHelper.TABLE_NOTIFICATIONS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        PNNotification newNotification = cursorTonotification(cursor);
        cursor.close();
        return newNotification;
    }

    public void deleteNotification(PNNotification notification) {
        long id = notification.getId();
        System.out.println("Notification deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_NOTIFICATIONS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteAllNotifications() {
        System.out.println("Delete all notifications");
        database.delete(MySQLiteHelper.TABLE_NOTIFICATIONS, null, null);
    }
    public ArrayList<HashMap<String, ?>> getAllNotifications() {
        //List<PNNotification> notifications = new ArrayList<PNNotification>();
        HashMap<String, Object> row;

        Cursor cursor = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PNNotification notification = cursorTonotification(cursor);
            row  = new HashMap<String, Object>();
            row.put("title", notification.getTitle());
            row.put("message", notification.getMessage());
            row.put("uri", notification.getUri());
            row.put("dttm", notification.getDttm());
            notifications.add(row);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return notifications;
    }

    public Cursor fetchAllNotifications(String dttmFormat) {

        allColumns[4] = "strftime('"+dttmFormat+"',"+
                MySQLiteHelper.COLUMN_DTTM+") as "+
                MySQLiteHelper.COLUMN_DTTM;
        Cursor mCursor = database.query(MySQLiteHelper.TABLE_NOTIFICATIONS,
                allColumns, null, null, null, null, MySQLiteHelper.COLUMN_ID + " DESC");

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public PNNotification cursorTonotification(Cursor cursor) {
        PNNotification notification = new PNNotification();
        notification.setId(cursor.getLong(0));
        notification.setTitle(cursor.getString(1));
        notification.setMessage(cursor.getString(2));
        notification.setUri(cursor.getString(3));
        notification.setDttm(cursor.getString(4));
        return notification;
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}

