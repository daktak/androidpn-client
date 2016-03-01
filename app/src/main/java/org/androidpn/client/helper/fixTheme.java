package org.androidpn.client.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;

import org.androidpn.client.R;
import org.androidpn.client.SerivceManager.LogUtil;
import org.androidpn.client.SerivceManager.Notifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by daktak on 3/1/16.
 */
public class fixTheme {
    private static final String LOGTAG = LogUtil.makeLogTag(Notifier.class);

    public static int getThemePref(Context context){
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
    public static boolean fixTheme(Activity act) {
        int themeResId = 0;
        try {
            Class<?> clazz  = ContextThemeWrapper.class;
            Method method = clazz.getMethod("getThemeResId");
            method.setAccessible(true);
            themeResId = (Integer) method.invoke(act);
        } catch (NoSuchMethodException e) {
            Log.e(LOGTAG, "Failed to get theme resource ID", e);
        } catch (IllegalAccessException e) {
            Log.e(LOGTAG, "Failed to get theme resource ID", e);
        } catch (IllegalArgumentException e) {
            Log.e(LOGTAG, "Failed to get theme resource ID", e);
        } catch (InvocationTargetException e) {
            Log.e(LOGTAG, "Failed to get theme resource ID", e);
        }
        int newTheme = getThemePref(act);
        boolean reset = false;
        if (newTheme != themeResId) {
            act.setTheme(newTheme);
            reset = true;
        }
        return reset;
    }
}
