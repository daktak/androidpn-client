package org.androidpn.client.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import org.androidpn.client.R;


/**
 * Created by daktak on 3/1/16.
 */
public class fixTheme {

    public static int getThemePref(String value){
        int Rid;
        if (value.equals("dark")) {
            Rid = R.style.AppThemeDark_NoActionBar;
        }else {
            Rid = R.style.AppThemeLight_NoActionBar;
        }
        return Rid;
    }
    public static boolean fixTheme(Activity act) {
        boolean reset = false;
        TypedValue outValue = new TypedValue();
        act.getTheme().resolveAttribute(R.attr.themeName, outValue, true);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
        String value = mPrefs.getString("theme","dark");
        act.setTheme(getThemePref(value));
        if (!value.contentEquals(outValue.string)) {
            reset = true;
        }
        return reset;
    }
}
