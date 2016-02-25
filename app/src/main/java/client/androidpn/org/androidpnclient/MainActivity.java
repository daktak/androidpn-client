package client.androidpn.org.androidpnclient;

import client.androidpn.org.client.LogUtil;
import client.androidpn.org.client.MySQLiteHelper;
import client.androidpn.org.client.PNNotificationDataSource;
import client.androidpn.org.client.ServiceManager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/* TODO
 * remove space / trim settings hostname
 * swipe to dismiss
 * URI / click to load
 * long click / copy text
 * create message
 * show / get userid via qr code
 * settings / header
 * selectable themes
 * click notification to load app
 */

public class MainActivity extends AppCompatActivity {

    public static MainActivity instance = null;
    private static final int REQUEST_PREFS = 1;
    private ServiceManager serviceManager;
    SimpleCursorAdapter dataAdapter;
    PNNotificationDataSource datasource;

    private static final String LOGTAG = LogUtil
            .makeLogTag(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        //loadPref();

        resetList();
        // Start the service
        serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.notification);
        serviceManager.startService();
    }

    public void resetList(){
        datasource = new PNNotificationDataSource(this);
        datasource.open();
        ListView notifyList = (ListView) findViewById(R.id.listView);

        if (datasource.getAllNotifications().isEmpty()) {
            Log.d(LOGTAG, "No Notifications");
        } else {

            // The desired columns to be bound
            Cursor cursor = datasource.fetchAllNotifications();

            String[] columns = new String[] {
                    MySQLiteHelper.COLUMN_TITLE,
                    MySQLiteHelper.COLUMN_MESSAGE
            };

            // the XML defined views which the data will be bound to
            int[] to = new int[] {
                    R.id.textView1,
                    R.id.textView2
            };

            // create the adapter using the cursor pointing to the desired data
            //as well as the layout information
            dataAdapter = new SimpleCursorAdapter(
                    this, R.layout.row,
                    cursor,
                    columns,
                    to,
                    0);

            // Assign adapter to ListView
            dataAdapter.notifyDataSetChanged();
            notifyList.setAdapter(dataAdapter);

        }
        datasource.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //super.onActivityResult(requestCode, resultCode, data);

      /*
       * To make it simple, always re-load Preference setting.
       */

       // loadPref();
        try {
            serviceManager.stopService();
            serviceManager.startService();
        }
        catch (Exception e) {
            serviceManager = new ServiceManager(this);
            serviceManager.setNotificationIcon(R.drawable.notification);
            serviceManager.startService();
        }
    }
/*
    private void loadPref() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences sharedPrefs = notificationService.getSharedPreferences();
//      boolean my_checkbox_preference = mySharedPreferences.getBoolean(Constants.SETTINGS_SOUND_ENABLED, true);
  //      CheckBoxPreference sound = findViewById(R.xml.settings.SETTINGS_SOUND_ENABLED);
        //SETTINGS_SOUND_ENABLED.setChecked(my_checkbox_preference);
/*
      String my_edittext_preference = mySharedPreferences.getString("edittext_preference", "");
         prefEditText.setText(my_edittext_preference);
     */

  //  }
  @Override
  protected void onResume() {
      super.onResume();
      instance = this;
      resetList();
  }

    @Override
    protected void onPause() {
        super.onPause();
        instance = null;
    }
}
