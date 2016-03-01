package org.androidpn.client.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import org.androidpn.client.R;
import org.androidpn.client.SerivceManager.LogUtil;
import org.androidpn.client.SerivceManager.Notifier;


/**
 * Created by daktak on 3/1/16.
 */
public class fixTheme {
    private static final String LOGTAG = LogUtil.makeLogTag(Notifier.class);

    public static int getThemePref(String value){
        int Rid;
        if (value.equals("light")) {
            Rid = R.style.AppThemeLight_NoActionBar;
        }else {
            Rid = R.style.AppThemeDark_NoActionBar;
        }
        return Rid;
    }
    public static boolean fixTheme(Activity act) {
        boolean reset = false;
        TypedValue outValue = new TypedValue();
        act.getTheme().resolveAttribute(R.attr.themeName, outValue, true);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(act);
        String value = mPrefs.getString("theme","dark");
        if (!value.equals(outValue.string)) {
            act.setTheme(getThemePref(value));
            reset = true;
        }
        return reset;
    }
}
