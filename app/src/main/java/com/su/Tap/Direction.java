package com.su.Tap;


import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Direction extends AsyncTask<Void, Void, String> {
    // Downloading data in non-ui thread

    DirectionAPI DctAPI = new DirectionAPI();
    public ParserTask parserTask = new ParserTask();

    private LatLng start, end;
    private GoogleMap dMap;

    public static int dcolor = Color.BLUE;
    public static int lineWidth = 10;

    private PolylineOptions lineOptions;

    public Direction(LatLng start, LatLng end, GoogleMap mMap) {
        this.start = start;
        this.end = end;
        this.dMap = mMap;
    }

    @Override
    protected String doInBackground(Void... Void) {
        // For storing data from web service
        String data = "";
        try {
            // Fetching the data from web service
            data = DctAPI.downloadUrl(start, end);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // Invokes the thread for parsing the JSON data
        try {
            parserTask.execute(result).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /** 解析JSON格式 **/
    public class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        Polyline dPolyline = null;
        ArrayList<LatLng> points = null;

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                routes = DctAPI.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("error", e.getMessage());
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            super.onPostExecute(result);
            int i,j;
            try {
                // Traversing through all the routes
                for (i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);  // Fetching all the points in i-th route
                    for (j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(lineWidth);  //導航路徑寬度
                    lineOptions.color(dcolor); //導航路徑顏色
                }
                // Drawing polyline in the Google Map for the i-th route
                dPolyline = dMap.addPolyline(lineOptions);
            }catch (NullPointerException e){

            }
        }
        public void deletePolyLine(){
            try {
                dPolyline.remove();
            }catch (NullPointerException e) {
                Log.e("Direction", e.toString());
            }
        }
    }
}