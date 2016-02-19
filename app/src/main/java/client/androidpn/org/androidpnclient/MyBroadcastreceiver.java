package client.androidpn.org.androidpnclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import client.androidpn.org.client.ServiceManager;

/**
 * Created by daktak on 2/20/16.
 */

public class MyBroadcastreceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean prefAuto = mySharedPreferences.getBoolean("prefAuto", true);
        if (prefAuto) {
            Intent startServiceIntent = new Intent(context, ServiceManager.class);
            context.startService(startServiceIntent);
        }
    }
}