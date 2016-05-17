package com.su.Tap;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by su on 2016/5/9.
 */
public class SQLService extends Service {

    public static LatLng MyLatLng;
    private final String TAG = "SQL";
    private static SQLiteHelper sqLiteHelper;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        sqLiteHelper = new SQLiteHelper(this);
        sqLiteHelper.setData(sqLiteHelper.getWritableDatabase());
        Thread mServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                double Lat,Lng;
                try {
                    Bundle bundle = intent.getExtras();
                    Lat = bundle.getDouble("Lat");
                    Lng = bundle.getDouble("Lng");
                    MyLatLng = new LatLng(Lat, Lng);
                    Log.e(TAG, "Service: " + MyLatLng.toString());
                    sqLiteHelper.sql("insert", new LatLng(Lat, Lng));
                }catch (NullPointerException e){
                    Log.e(TAG, "Service's Location isn't point");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mServiceThread.start();
        return  START_STICKY ;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}


