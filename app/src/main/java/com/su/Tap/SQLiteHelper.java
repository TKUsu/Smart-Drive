package com.su.Tap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nicole on 2016/4/28.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "roadDB";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;
    private String TAG = "SQL";

    public SQLiteHelper(Context context) {
        //透過建構子MyDBHelper直接呼叫父類別建構子來建立參數的資料庫
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE roadDBs (" +
                " _ID INTEGER  PRIMARY KEY," +
                "number INTEGER,"+
                " lat DOUBLE," +
                " lng DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void setDB(SQLiteDatabase db){
        this.db = db;
    }

//    public HashMap sql(String temp, LatLng latLng,int DB_ID) {
    public HashMap sql(String temp, LatLng latLng) {
        ContentValues cv;
        HashMap map = null;
//        int count, id;
        switch (temp) {
            case "insert":
                long idtemp;
                cv = new ContentValues();
//                cv.put("number", DB_ID);
                cv.put("lat", latLng.latitude);
                cv.put("lng", latLng.longitude);
                idtemp = db.insert("roadDBs", null, cv);
                Log.e(TAG,"新增記錄成功" + /*" DB_ID:" + DB_ID +*/ idtemp + ":" + latLng.toString());
                break;
            case "search":
                map = SqlQuery("SELECT * FROM " + "roadDBs");
                break;
//                case "update":
//                    id = Integer.parseInt(tid.getText().toString());
//                    cv = new ContentValues();
//                    cv.put("score", Double.parseDouble(score.getText().toString()));
//                    count = db.update("road", cv, "_id=" + id, null);
//                    output.setText("更新記錄成功" + count);
//                    break;

//                case "delete":
//                    id = Integer.parseInt(tid.getText().toString());
//                    count = db.delete("road" +
//                            "", "_id=" + id, null);
//                    output.setText("刪除記錄成功" + count);
//                    break;
        }return map;
    }

    public HashMap SqlQuery(String sql) {
        HashMap map = new HashMap();
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();
        while(c.moveToNext()){
            double Lat = c.getDouble(c.getColumnIndex("lat"));
            double Lng = c.getDouble(c.getColumnIndex("lng"));
//            int i = c.getInt(c.getColumnIndex("_id"));
            map.put(1,new LatLng(Lat,Lng));
        }return map;
    }
}
