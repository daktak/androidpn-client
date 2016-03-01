package org.androidpn.client.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidpn.client.R;

/**
 * Created by daktak on 3/1/16.
 */
public class fixTheme {

    public static int getTheme(Context context){
        int Rid;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = mPrefs.getString("theme","dark");
        if (value.equals("light")) {
            Rid = R.style.AppThemeLight_NoActionBar;
        }else {
            Rid = R.style.AppThemeDark_NoActionBar;
        }
        return Rid;
    }
    public static void fixTheme(Activity act) {
        act.setTheme(getTheme(act));
    }
}
