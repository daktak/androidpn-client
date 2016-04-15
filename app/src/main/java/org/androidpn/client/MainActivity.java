package org.androidpn.client;

import org.androidpn.client.SerivceManager.Constants;
import org.androidpn.client.SerivceManager.LogUtil;
import org.androidpn.client.SerivceManager.ServiceManager;
import org.androidpn.client.helper.EasyPermissions;
import org.androidpn.client.helper.SwipeDismissListViewTouchListener;
import org.androidpn.client.helper.fixTheme;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


import java.util.List;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {
    //  implements AdapterView.OnItemClickListener {

    public static MainActivity instance = null;
    private static final int REQUEST_PREFS = 1;
    private ServiceManager serviceManager;
    SimpleCursorAdapter dataAdapter;
    PNNotificationDataSource datasource;
    private int RC_PHONE_STATE = 1;

    private static final String LOGTAG = LogUtil
            .makeLogTag(MainActivity.class);
    String[] perms = {Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean reset = fixTheme.fixTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
            set();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_phone_state),
                    RC_PHONE_STATE, perms);
        }
    }

    public void set() {
        resetList();

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefAuto = mySharedPreferences.getBoolean("prefAuto", true);

        // Start the service
        if (serviceManager == null) {
            if (!prefAuto) {
                serviceManager = new ServiceManager(this);
                serviceManager.setNotificationIcon(R.drawable.notification);
                serviceManager.startService();
            }
        }
    }

    @TargetApi(11)
    public void resetList() {
        datasource = new PNNotificationDataSource(this);
        datasource.open();
        ListView notifyList = (ListView) findViewById(R.id.listView);

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefDtTm = mySharedPreferences.getBoolean("prefDtTm", true);
        String prefDtTmFrmt = null;

        if (datasource.getAllNotifications().isEmpty()) {
            Log.d(LOGTAG, "No Notifications");
        } else {
            if (prefDtTm) {
                prefDtTmFrmt = mySharedPreferences.getString("prefDttmFormat", "%d/%m/%Y %H:%M");
            }
            // The desired columns to be bound
            Cursor cursor = datasource.fetchAllNotifications(prefDtTmFrmt);

            String[] columns = new String[]{
                    MySQLiteHelper.COLUMN_TITLE,
                    MySQLiteHelper.COLUMN_MESSAGE
            };

            // the XML defined views which the data will be bound to
            int[] to = new int[]{
                    R.id.tvTitle,
                    R.id.tvMessage
            };
            if (prefDtTm) {
                columns = new String[]{
                        MySQLiteHelper.COLUMN_TITLE,
                        MySQLiteHelper.COLUMN_MESSAGE,
                        MySQLiteHelper.COLUMN_DTTM
                };

                // the XML defined views which the data will be bound to
                to = new int[]{
                        R.id.tvTitle,
                        R.id.tvMessage,
                        R.id.tvDate
                };
            }
            // create the adapter using the cursor pointing to the desired data
            //as well as the layout information
            if (Build.VERSION.SDK_INT < 11) {
                dataAdapter = new SimpleCursorAdapter(
                        this, R.layout.row,
                        cursor,
                        columns,
                        to);
            } else {
                dataAdapter = new SimpleCursorAdapter(
                        this, R.layout.row,
                        cursor,
                        columns,
                        to,
                        0);
            }

            // Assign adapter to ListView
            dataAdapter.notifyDataSetChanged();
            notifyList.setAdapter(dataAdapter);

        }
        datasource.close();

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        notifyList,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                datasource.open();
                                for (int position : reverseSortedPositions) {
                                    datasource.deleteNotification(datasource.cursorTonotification((Cursor) dataAdapter.getItem(position)));
                                }
                                dataAdapter.notifyDataSetChanged();
                                datasource.close();
                                resetList();
                            }
                        });
        notifyList.setOnTouchListener(touchListener);
        onClickListener ocl = new onClickListener(this, dataAdapter, datasource);
        notifyList.setOnItemClickListener(ocl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String status = getString(R.string.disconnected);
        MenuItem item = menu.findItem(R.id.connection_status);
        if (serviceManager != null) {
            if(serviceManager.isLoggedIn()){
                status = getString(R.string.connected);
                //item.setEnabled(false);
            }
        }

        item.setTitle(status);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent prefs = new Intent(getBaseContext(), SetPreferenceActivity.class);
            startActivityForResult(prefs, REQUEST_PREFS);
            return true;
        }
        if (id == R.id.action_clear) {
            datasource.open();
            datasource.deleteAllNotifications();
//            dataAdapter.notifyDataSetChanged();
            datasource.close();
            resetList();
        }
        if (id == R.id.connection_status) {
            Log.d(LOGTAG, "Restarting sm");
            if (serviceManager != null) {

                serviceManager.stopService();
                serviceManager.setSettings();
                serviceManager.startService();

            } else {
                serviceManager = new ServiceManager(this);
                serviceManager.setNotificationIcon(R.drawable.notification);
                serviceManager.startService();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //super.onActivityResult(requestCode, resultCode, data);

      /*
       * To make it simple, always re-load Preference setting.
       */
        boolean reset = fixTheme.fixTheme(this);
        if (reset) {
            Log.d(LOGTAG, "Theme change");
            finish();
            startActivity(getIntent());
        }
        // loadPref();
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (serviceManager != null) {

                if (serviceManager.isNewSettings(this)) {
                    Log.d(LOGTAG, "Restarting sm");
                    serviceManager.stopService();
                    serviceManager.setSettings();
                    serviceManager.startService();
                }
            } else {
                serviceManager = new ServiceManager(this);
                serviceManager.setNotificationIcon(R.drawable.notification);
                serviceManager.startService();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean reset = fixTheme.fixTheme(this);
        instance = this;
        if (EasyPermissions.hasPermissions(this, perms)) {
            resetList();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_phone_state),
                    RC_PHONE_STATE, perms);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        instance = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        set();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied

    }

}
