package com.su.Tap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by su on 2016/5/9.
 */
public class SQLService extends Service {

    private final String TAG = "SQL";
    private SQLiteHelper sqLiteHelper;
    private LatLng MyLatLng;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("action");
        filter.addAction("anotherAction");
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, filter);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        sqLiteHelper = new SQLiteHelper(this);
        sqLiteHelper.setDB(sqLiteHelper.getWritableDatabase());
        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();
        try {
//            sqLiteHelper.sql("insert",new LatLng(Lat,Lng));
            Log.e(TAG,MyLatLng.toString());
        }catch (NullPointerException e){
            Log.e(TAG,"Service's Location is NullPointerException");
        }
    }
    public void onDestroy(){
        super.onDestroy();
//        unregisterReceiver(broadcastReceiverd);
        Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
    }
}


