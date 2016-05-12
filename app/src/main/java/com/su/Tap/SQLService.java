package com.su.Tap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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
    private SQLiteHelper sqLiteHelper;
    private BroadcastReceiver broadcastReceiverd;

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

    private Double MyLat,MyLng;

    @Override
    public void onStart(Intent intent, int startId) {
        sqLiteHelper = new SQLiteHelper(this);
        sqLiteHelper.setDB(sqLiteHelper.getWritableDatabase());
        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();
        try {
//            this.myLocation = mapsActivity.mLatLng();
            broadcastReceiverd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.equals("com.my.location")){
                        intent.getDoubleExtra("MyLat", MyLat);
                        intent.getDoubleExtra("MyLng",MyLng);
                    }
                }
            };
            Log.e(TAG, "My Location:" + MyLat+"."+MyLng);

            sqLiteHelper.sql("insert", new LatLng(MyLat, MyLng));
        }catch (NullPointerException e){
            Log.e(TAG,"Service's Location is NullPointerException");
        }
    }
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
    }
}
