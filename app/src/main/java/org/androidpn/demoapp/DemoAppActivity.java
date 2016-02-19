/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.demoapp;

import org.androidpn.client.ServiceManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This is an androidpn client demo application.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */

public class DemoAppActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DemoAppActivity", "onCreate()...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        //final Intent i = new Intent(this, MyPreferencesActivity.class);
        // Settings
        loadPref();
        Button okButton = (Button) findViewById(R.id.btn_settings);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //ServiceManager.viewNotificationSettings(DemoAppActivity.this);

                Intent intent = new Intent();
                intent.setClass(DemoAppActivity.this, SetPreferenceActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        // Start the service
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.setNotificationIcon(R.drawable.notification);
        serviceManager.startService();
    }



     @Override
     public boolean onOptionsItemSelected(MenuItem item) {

      /*
       * Because it's onlt ONE option in the menu.
       * In order to make it simple, We always start SetPreferenceActivity
       * without checking.
       */

        Intent intent = new Intent();
        intent.setClass(DemoAppActivity.this, SetPreferenceActivity.class);
        startActivityForResult(intent, 0);

        return true;
     }


     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      // TODO Auto-generated method stub
      //super.onActivityResult(requestCode, resultCode, data);

      /*
       * To make it simple, always re-load Preference setting.
       */

      loadPref();
     }

     private void loadPref() {
         SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    /*
      boolean my_checkbox_preference = mySharedPreferences.getBoolean("checkbox_preference", false);
      prefCheckBox.setChecked(my_checkbox_preference);

      String my_edittext_preference = mySharedPreferences.getString("edittext_preference", "");
         prefEditText.setText(my_edittext_preference);
     */

     }
}
