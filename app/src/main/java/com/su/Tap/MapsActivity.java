package com.su.Tap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, LocationSource {

    private GoogleMap mMap;
    private Polyline polylineFinal = null;
    private AutoCompleteTextView startautoCompView = null, endautiComView = null;
    private TextView Time;
    private Button startdelete,enddelete;

    private String start = null, end = null;
    private CharSequence place_startname = null,place_startaddress = null,place_endname = null,place_endaddress = null;
    private int ButtonNumber = 0;

    private LocationManager mLocationmgr;
    private Location mLocation;
    private OnLocationChangedListener mLocationchangeListener;

    private DirectionAPI DctAPI = new DirectionAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationmgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        startdelete = (Button)findViewById(R.id.delete);

        startautoCompView = (AutoCompleteTextView) findViewById(R.id.startautocomtxt);
        startautoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
//        startautoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//            }
//        });

        endautiComView = (AutoCompleteTextView) findViewById(R.id.endautocomtxt);
        endautiComView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
//        endautiComView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//            }
//        });

//        if (startautoCompView.getText().toString() == "" || startautoCompView.getText().toString() == null) {
//            startdelete.setEnabled(false);
//            RemoteViews remoteViews =
//                    new RemoteViews(context.getPackageName(), R.layout.);
//            remoteViews.setOnClickPendingIntent
//                    (
//                            R.id.Button01, configPendingIntent
//                    );
//        }
//        else    startdelete.setEnabled(true);

//        startdelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                start = null;
////                polylineFinal.remove();
//                startautoCompView.setText(null);
//                place_startname = place_startaddress = null;
//            }
//        });
//
//        enddelete = (Button)findViewById(R.id.delete2);
//        if (endautiComView.getText().toString() == "" || endautiComView.getText().toString() == null)
//            enddelete.setEnabled(false);
//        else{
//            enddelete.setEnabled(true);
//        }
//        enddelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                end = null;
////                polylineFinal.remove();
//                endautiComView.setText(null);
//                place_endname = place_endaddress = null;
//            }
//        });

        Button SBT1 = (Button)findViewById(R.id.SearchB1);
        SBT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch();
                ButtonNumber = 1;
            }
        });
        Button SBT2 = (Button)findViewById(R.id.SearchB2);
        SBT2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch();
                ButtonNumber = 2;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(mMap !=null && !mMap.isMyLocationEnabled())
            mMap.setMyLocationEnabled(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    private static final int REQUEST_PLACE_PICKER = 1;
    public void onSearch(){
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        Intent intent;
        try {
            intent = intentBuilder.build(MapsActivity.this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER && resultCode == RESULT_OK) {
            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);

            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = PlacePicker.getAttributions(data);
            if (attributions == null) {
                attributions = "";
            }
            if(ButtonNumber == 1){
                startautoCompView.setText(name);
                start = address.toString();
                if (address.equals("台灣"))   start = name.toString();
                this.place_startname = name.toString();
                this.place_startaddress = address.toString();
            }else if (ButtonNumber == 2){
                endautiComView.setText(name);
                end = address.toString();
                if (address.equals("台灣"))   end = name.toString();
                this.place_endname = name.toString();
                this.place_endaddress = address.toString();
            }
        }
    }
    public void onNavigation(View view) {
        if (start !=  place_startname && start != place_startaddress){
            start = startautoCompView.getText().toString();
        }else if (end !=  place_endname && end != place_endaddress){
            end = endautiComView.getText().toString();
        }
        if (start == null || start.equals("")) {
            Toast.makeText(MapsActivity.this, R.string.startlocationnull, LENGTH_SHORT).show();
        } else if (end == null || end.equals("")) {
            Toast.makeText(MapsActivity.this, R.string.endlocationnull, LENGTH_SHORT).show();
        } else {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uis = mMap.getUiSettings();
        uis.setMyLocationButtonEnabled(true);
        uis.setZoomControlsEnabled(true);
        uis.setCompassEnabled(true);
        uis.setAllGesturesEnabled(true);
        mMap.setLocationSource(this);
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);

        if (Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Location location = mLocationmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = mLocationmgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            Toast.makeText(MapsActivity.this, "Get success the position the former", LENGTH_SHORT).show();
            onLocationChanged(location);
        } else
            Toast.makeText(MapsActivity.this, "Get failure the position the former", LENGTH_SHORT).show();
    }
//------------------Location Source
    @Override
    public void activate(OnLocationChangedListener listener) {
        mLocationchangeListener = listener;
        enableLocationUpdate();
        Toast.makeText(MapsActivity.this, "Map's my-location-layer is start", LENGTH_SHORT).show();
    }

    @Override
    public void deactivate() {
        mLocationchangeListener = null;
        disableLocationUpdate();
        Toast.makeText(MapsActivity.this, "Map's my-location-layer is stop", LENGTH_SHORT).show();
    }
//-------------------Location Listener
    @Override
    public void onLocationChanged(Location location) {
        //手機定位更新時執行，Location物件包含最新的定位資訊
        if (mLocationchangeListener != null)
            mLocationchangeListener.onLocationChanged(location);
        this.mLocation = location;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //手機定位功能狀態更新時執行的method
        //status: OUT_OF_SERVER(定位功能無拉使用)
        //TEMPORARILY_UNAVAILABLE(暫時無法定位)
        //AVAILABLE(定位功能運作正常，每一次執行onLocationChanged()時，也會傳入此值)
        String str = provider;
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                str += "Locate function cann't use";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                str += "Temporarily unable to use";
                break;
        }
        Toast.makeText(MapsActivity.this, str, LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        //手機定位功能開啟時執行
        Toast.makeText(MapsActivity.this, provider + "Locate function is open", LENGTH_SHORT).show();
        enableLocationUpdate();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //手機定位功能關閉時執行
        Toast.makeText(MapsActivity.this, provider + "Locate function is close", LENGTH_SHORT).show();
    }
//------------------------------------------
    private void enableLocationUpdate() {
        //優先使用ＧＰＳ
        if (mLocationmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            mLocationmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Toast.makeText(MapsActivity.this,"Use GPS locate", LENGTH_SHORT).show();
        }else if (mLocationmgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationmgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            Toast.makeText(MapsActivity.this, "Use network locate", LENGTH_SHORT).show();
        }
    }

    private void disableLocationUpdate(){
        if (Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mLocationmgr.removeUpdates(this);
        Toast.makeText(MapsActivity.this, "Locate is stop", LENGTH_SHORT).show();
    }

    //---------------------------------------------------------------------------------
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<Void, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(Void... params) {
            // For storing data from web service
            String data = "";  try {
                // Fetching the data from web service
                data = DctAPI.downloadUrl(start,end);
                if (data == null)
                    Log.d("Error data", data);
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
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    } /** 解析JSON格式 **/

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
//                Log.e(MapsActivity.class.getName(), String.valueOf(jsonData[0]));
                jObject = new JSONObject(jsonData[0]);
                String status = jObject.getString("status");
                if (!status.equals("OK"))
                    Toast.makeText(MapsActivity.this,"NOT_FOUND",Toast.LENGTH_SHORT).show();
                // Starts parsing data
                routes = DctAPI.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            int i,j;

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
                    if (i==0 && j==0)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,15));
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(6);  //導航路徑寬度
                lineOptions.color(Color.BLUE); //導航路徑顏色
            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null)
                polylineFinal = mMap.addPolyline(lineOptions);
            else
                Log.e(MapsActivity.class.getName(), "Error List size == 0");
        }
    }
}