package org.androidpn.client;

import org.androidpn.client.SerivceManager.LogUtil;
import org.androidpn.client.SerivceManager.ServiceManager;
import org.androidpn.client.helper.SwipeDismissListViewTouchListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/* TODO
 * long click / copy text
 * settings / header
 * selectable themes?
 */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener  {

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

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefDtTm = mySharedPreferences.getBoolean("prefDtTm", true);
        String prefDtTmFrmt = null;

        if (datasource.getAllNotifications().isEmpty()) {
            Log.d(LOGTAG, "No Notifications");
        } else {
            if (prefDtTm){
                prefDtTmFrmt = mySharedPreferences.getString("prefDttmFormat", "%d/%m/%Y %H:%M");
            }
            // The desired columns to be bound
            Cursor cursor = datasource.fetchAllNotifications(prefDtTmFrmt);

            String[] columns = new String[] {
                    MySQLiteHelper.COLUMN_TITLE,
                    MySQLiteHelper.COLUMN_MESSAGE
            };

            // the XML defined views which the data will be bound to
            int[] to = new int[] {
                    R.id.tvTitle,
                    R.id.tvMessage
            };
            if (prefDtTm){
                columns = new String[] {
                        MySQLiteHelper.COLUMN_TITLE,
                        MySQLiteHelper.COLUMN_MESSAGE,
                        MySQLiteHelper.COLUMN_DTTM
                };

                // the XML defined views which the data will be bound to
                 to = new int[] {
                        R.id.tvTitle,
                        R.id.tvMessage,
                        R.id.tvDate
                };
            }
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
        notifyList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String uri = datasource.cursorTonotification((Cursor) dataAdapter.getItem(position)).getUri();
        if (uri != null && uri.length() > 0) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            try {
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            } catch (Exception e) {
                Log.w(LOGTAG,e.toString());
            }

        }
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
        if (id == R.id.action_clear) {
            datasource.open();
            datasource.deleteAllNotifications();
            dataAdapter.notifyDataSetChanged();
            datasource.close();
            resetList();
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
