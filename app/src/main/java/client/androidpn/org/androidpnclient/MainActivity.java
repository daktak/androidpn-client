package client.androidpn.org.androidpnclient;

import client.androidpn.org.client.Constants;
import client.androidpn.org.client.ServiceManager;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    ServiceManager serviceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        //loadPref();
        // Start the service
        serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.notification);
        serviceManager.startService();
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

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SetPreferenceActivity.class);
            startActivityForResult(intent, 0);
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

}
