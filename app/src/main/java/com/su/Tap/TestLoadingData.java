package com.su.Tap;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sujustin on 2016/10/14.
 */
public class TestLoadingData  extends AsyncTask<Void, Void, ArrayList<LatLng>>{
    String SDpath;
    Boolean getData = false;
    GoogleMap tMap;
    String origin,destination;
    LatLng origin_latlng,destination_latlng;
    int flag;

    public TestLoadingData(GoogleMap mMap,int flag) {
        this.tMap = mMap;
        this.flag = flag;
    }

    @Override
    protected ArrayList<LatLng> doInBackground(Void... input) {
        ArrayList<LatLng> point = new ArrayList<LatLng>();
        JSONObject jsonObject = null;
        String jsonStr = "",filename = "";
        try {
            switch (flag){
                case 1:
                    filename = "json.txt";
                    break;
                case 2:
                    filename = "json2.txt";
                    break;
                case 3:
                    filename = "json3.txt";
                    break;
                default:
                    filename = "json.txt";
                    break;
            }
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + filename);
            FileInputStream stream = new FileInputStream(file);

            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            jsonStr = Charset.defaultCharset().decode(bb).toString();

            stream.close();

            jsonObject = new JSONObject(jsonStr);

            point = decodeJson(jsonObject);
        } catch (FileNotFoundException e) {
            Log.d("FileNotFoundException",e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return point;
    }

    @Override
    protected void onPostExecute(ArrayList<LatLng> result){
        super.onPostExecute(result);

        PolylineOptions polyline = new PolylineOptions();
        polyline.addAll(result);
        switch (flag){
            case 2:
                polyline.color(Color.RED);
                break;
            default:
                polyline.color(Color.GRAY);
                break;
        }
        polyline.width(8);

        tMap.addPolyline(polyline);
    }

    public ArrayList<LatLng> decodeJson(JSONObject jsonObject) throws JSONException {

        Log.d("test",jsonObject.get("status").toString());
        if (jsonObject.get("status").equals("OK"))
            getData = true;
        else
            getData = false;

        ArrayList<LatLng> path = new ArrayList<>();
        try {
            JSONArray jRoutes = jsonObject.getJSONArray("routes");
            for (int i = 0; i < jRoutes.length(); i++) {
                JSONArray jLocation = ((JSONObject) jRoutes.get(i)).getJSONArray("location");
                for (int j = 0; j < jLocation.length(); j++) {
                    JSONObject start_loaction = jLocation.getJSONObject(0).getJSONObject("start_location");
                    JSONObject end_loaction = jLocation.getJSONObject(0).getJSONObject("end_location");

                    origin = ((JSONObject) jLocation.get(j)).get("end_address").toString();
                    destination = ((JSONObject) jLocation.get(j)).get("start_address").toString();
                    origin_latlng = new LatLng(start_loaction.getDouble("lat"), start_loaction.getDouble("lng"));
                    destination_latlng = new LatLng(end_loaction.getDouble("lat"), end_loaction.getDouble("lng"));
                }

                JSONArray jStep = ((JSONObject) jRoutes.get(i)).getJSONArray("steps");
                for (int k = 0; k < jStep.length(); k++) {
                    String point = ((JSONObject) jStep.get(k)).get("polyline_point").toString();
                    List<LatLng> list = decodePoly(point);
                    for (int m = 0; m < list.size(); m++){
                        Double lat = list.get(m).latitude;
                        Double lng = list.get(m).longitude;
                        path.add(new LatLng(lat,lng));
                    }
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return path;
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;   shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    //drtermine get data is true !?
    public Boolean getDataFlag(){
        return getData;
    }

    public String getOrigin(String str){
        switch (str) {
            case "address":
                return this.origin;
            case "latlng":
                return this.origin_latlng.toString();
        }return null;
    }

    public String getDestination(String str){
        switch (str) {
            case "address":
                return this.destination;
            case "latlng":
                return this.destination_latlng.toString();
        }return null;
    }

    public String getSD(){
        return this.SDpath;
    }

}
