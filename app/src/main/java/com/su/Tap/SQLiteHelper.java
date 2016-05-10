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
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
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

    public void sql(String temp, LatLng latLng) {
        ContentValues cv;
//        int count, id;
        switch (temp) {
            case "insert":
                long idtemp;
                cv = new ContentValues();
                cv.put("lat", latLng.latitude);
                cv.put("lng", latLng.longitude);
                idtemp = db.insert("roadDBs", null, cv);
                Log.e(TAG,"新增記錄成功" + idtemp);
                break;
            case "search":
                SqlQuery("SELECT * FROM " + "roadDBs");
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
        }
    }

    public void SqlQuery(String sql) {
        String[] colNames;
        String str = "";
        Cursor c = db.rawQuery(sql, null);
        colNames = c.getColumnNames();
        for (int i = 0; i < colNames.length; i++) {
            str += colNames[i] + "\t\t";
        }
        str += "\n";
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            str += c.getString(0) + "\t";
            str += c.getString(1) + "\t";
            str += c.getString(2) + "\n";
            c.moveToNext();
        }
        Log.e(TAG + "Search Road BD : ",str.toString());
    }

    public void call(){
        Log.e(TAG,"call back");
    }
}
