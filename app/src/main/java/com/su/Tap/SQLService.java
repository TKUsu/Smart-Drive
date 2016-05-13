package com.su.Tap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
//    private SQLiteHelper sqLiteHelper;
    private BroadcastReceiver broadcastReceiverd;
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
    }

//    private Double MyLat,MyLng;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    public void onStart(Intent intent, int startId) {
//        sqLiteHelper = new SQLiteHelper(this);
//        sqLiteHelper.setDB(sqLiteHelper.getWritableDatabase());
        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();
        try {
//            this.myLocation = mapsActivity.mLatLng();
            myBroadcastReceiver = new MyBroadcastReceiver();
//        myBroadcastReceiver.SQLstart();
            MyLatLng = myBroadcastReceiver.getLatLng();
//            sqLiteHelper.sql("insert",MyLatLng);
            Log.e(TAG,MyLatLng.toString());
        }catch (NullPointerException e){
            Log.e(TAG,"Service's Location is NullPointerException");
        }
    }
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
    }

    public void A(){
        broadcastReceiverd = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }

    public static class MyBroadcastReceiver extends BroadcastReceiver{
        private Double Lat,Lng;
        private SQLiteHelper sqLiteHelper;
        private Context context;

        public MyBroadcastReceiver(){

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String strAction = intent.getAction();
            Log.e("SQL", "action:" + strAction);
            Bundle bundle = intent.getExtras();
            try{
                this.context = context;
                Lat = bundle.getDouble("Lat");
                Lng = bundle.getDouble("Lng");
                Log.e("SQL",Lat.toString()+Lng.toString());
            }catch (NullPointerException e){
                Log.e("SQL","MyBroadcastReceiver is NullPointerException");
            }
        }
        public LatLng getLatLng(){
            Log.e("SQL","getLatLng"+Lat.toString()+Lng.toString());
            return (new LatLng(Lat,Lng));
        }

        public void SQLstart(){
            try {
                sqLiteHelper = new SQLiteHelper(context);
                sqLiteHelper.setDB(sqLiteHelper.getWritableDatabase());
//            this.myLocation = mapsActivity.mLatLng();
//            MyLatLng = myBroadcastReceiver.getLatLng();
//            sqLiteHelper.sql("insert",MyLatLng);
//            Log.e(TAG,MyLatLng.toString());
            }catch (NullPointerException e){
                Log.e("SQL","Service's Location is NullPointerException");
            }
        }
    }
}


