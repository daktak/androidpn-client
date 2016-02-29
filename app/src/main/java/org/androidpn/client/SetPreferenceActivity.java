package org.androidpn.client;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by daktak on 2/19/16.
 */
public class SetPreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

}
