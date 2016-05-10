package com.su.Tap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by su on 2016/5/9.
 */
public class SQLService extends Service {

    private final String TAG = "SQL";
    private MapsActivity mapsActivity;
    private LatLng myLocation;
    private SQLiteHelper sqLiteHelper;

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
        sqLiteHelper = new SQLiteHelper(this);
        sqLiteHelper.setDB(sqLiteHelper.getWritableDatabase());
        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();
        try {
            this.myLocation = mapsActivity.mLatLng();
            Log.e(TAG, "My Location:" + myLocation.toString());
        }catch (NullPointerException e){
            Log.e(TAG,"Service's Location is NullPointerException");
        }
        try{
//            sqLiteHelper.sql("insert", new LatLng(121, 125));
            sqLiteHelper.sql("insert", new LatLng(121, 125));
        }catch (NullPointerException e){
            Log.e(TAG,"sqLiteHelper is null point");
        }
    }
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
    }
}
