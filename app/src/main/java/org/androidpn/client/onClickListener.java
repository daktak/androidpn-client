package org.androidpn.client;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import org.androidpn.client.SerivceManager.LogUtil;

/**
 * Created by daktak on 2/03/16.
 */
public class onClickListener
       implements AdapterView.OnItemClickListener {

    private static final String LOGTAG = LogUtil
            .makeLogTag(onClickListener.class);
    SimpleCursorAdapter dataAdapter;
    PNNotificationDataSource datasource;
    Context context;

    public onClickListener(Context context, SimpleCursorAdapter dataAdapter, PNNotificationDataSource datasource){
        this.dataAdapter = dataAdapter;
        this.datasource = datasource;
        this.context = context;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String uri = datasource.cursorTonotification((Cursor) dataAdapter.getItem(position)).getUri();
        if (uri != null && uri.length() > 0) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            try {
                intent.setData(Uri.parse(uri));
                context.startActivity(intent);
            } catch (Exception e) {
                Log.w(LOGTAG, e.toString());
            }

        }
    }
}
