package com.su.Tap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nicole on 2016/4/28.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "roadDB";
//    private static final String DATABASE2_NAME = "roadDB2";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    private final String TAG = "SQL";

    public SQLiteHelper(Context context, String roadDB) {
        //透過建構子MyDBHelper直接呼叫父類別建構子來建立參數的資料庫
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.DATABASE_NAME = roadDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE roadDB (" +
                " _ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " lat DOUBLE NOT NULL," +
                " lng DOUBLE NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void setData(SQLiteDatabase db){
        this.db = db;
    }

//    public void sql(String temp, LatLng latLng,int DB_ID) {
    public ArrayList<LatLng> sql(String temp, LatLng latLng) {
        ContentValues cv;
//        int count, id;
        switch (temp) {
            case "insert":
                long idtemp;
                cv = new ContentValues();
                cv.put("lat", latLng.latitude);
                cv.put("lng", latLng.longitude);
                idtemp = db.insert(DATABASE_NAME, null, cv);
                Log.e(TAG,"新增記錄成功"+ DATABASE_NAME + "  " + idtemp + ":" + latLng.toString());
                break;
            case "search":
                SqlQuery("SELECT * FROM " + DATABASE_NAME);
                return list;
            case "insertTest":
                long temptxt;
                cv = new ContentValues();
                cv.put("lat", latLng.latitude);
                cv.put("lng", latLng.longitude);
                temptxt = db.insert(DATABASE_NAME, null, cv);
                Log.e(TAG,"新增記錄成功" + temptxt + ":" + latLng.toString());
                SqlQuery("SELECT * FROM " + DATABASE_NAME);
                break;
        }
        return null;
    }

    HashMap<Integer,LatLng> hashMap = new HashMap<>();
    ArrayList<LatLng> list = new ArrayList<>();

    public ArrayList<LatLng> SqlQuery(String sql) {
        int i = 0;
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        do{
            double Lat = c.getDouble(c.getColumnIndex("lat"));
            double Lng = c.getDouble(c.getColumnIndex("lng"));
            LatLng latLng = new LatLng(Lat, Lng);
            try {
                i = c.getInt(c.getColumnIndex("_ID"));
            } catch (IllegalStateException e) {
                Log.e(TAG, "e");
            }
            Log.d(TAG, "c_index = " + c.getPosition() + " id = " + i + " Lat = " + Lat + " Lng = " + Lng);
            hashMap.put(i,latLng);
        }while (c.moveToNext());
        c.close();
        setlist();
        return list;
    }
    public ArrayList<LatLng> setlist() {
        int i = 1;
        LatLng latLng;
        do {
            latLng = hashMap.get(i);
            list.add(latLng);
            i++;
        } while (i != hashMap.size()+1);
        Log.d(TAG,"set list = " + list.toString());
        return list;
    }
}
