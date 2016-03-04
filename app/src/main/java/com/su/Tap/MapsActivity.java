package com.su.Tap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
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
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, LocationSource {

    private GoogleMap mMap;
    private Polyline polylineFinal = null;
    private AutoCompleteTextView startautoCompView = null, endautiComView = null;
    private TextView Time;

    private LatLng start = null, end = null;
    private CharSequence place_startname = null, place_endname = null;
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

        startautoCompView = (AutoCompleteTextView) findViewById(R.id.startautocomtxt);
        startautoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        startautoCompView.addTextChangedListener(startACVtxtchange);

        endautiComView = (AutoCompleteTextView) findViewById(R.id.endautocomtxt);
        endautiComView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMap != null && !mMap.isMyLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
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

    private static final int REQUEST_PLACE_PICKER = 1;
    public void onSearch(View view){
        if (view.getId() == R.id.placeB1)
            ButtonNumber = 1;
        else if (view.getId() == R.id.placeB2)
            ButtonNumber = 2;
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
            final LatLng latlng = place.getLatLng();
            final CharSequence name = place.getName();
            String attributions = PlacePicker.getAttributions(data);
            if (attributions == null) {
                attributions = "";
            }
            if(ButtonNumber == 1){
                startautoCompView.setText(name);
                start = latlng;
                this.place_startname = name.toString();
                Toast.makeText(MapsActivity.this,start.toString(),LENGTH_SHORT).show();
            }else if (ButtonNumber == 2){
                endautiComView.setText(name);
                end = latlng;
                this.place_endname = name.toString();
            }
        }
    }
    //判斷是否為空值、直接輸入文字或是選取地點
    public void onNavigation(View view) {
        String starttmp,endtmp;
        starttmp = startautoCompView.getText().toString();
        endtmp = endautiComView.getText().toString();
        if (starttmp == null || starttmp.equals("") || starttmp.equals("Your Location")) {
            startautoCompView.setText("Your Location");
            start = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
            if (endtmp == null || endtmp.equals(""))
                Toast.makeText(MapsActivity.this, R.string.endlocationnull, LENGTH_SHORT).show();
            else {
                if (place_endname != endtmp)
                    end = getLatLongFromAddress(endtmp);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute();
            }
        }else {
            if (place_startname != starttmp)
                start = getLatLongFromAddress(starttmp);
            if (place_endname != endtmp)
                end = getLatLongFromAddress(endtmp);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute();
        }
    }
    private LatLng getLatLongFromAddress(String address){
        LatLng p=null;
        Geocoder geoCoder = new Geocoder(this, Locale.TAIWAN);
        try{
            List<Address> addresses = geoCoder.getFromLocationName(address , 1);
            if (addresses.size() > 0)            {
                p = new LatLng((addresses.get(0).getLatitude()),(addresses.get(0).getLongitude()));
                return p;
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "El servicio de Google Maps no se encuentra disponible.\n"
                    + "Intente ms tarde.", Toast.LENGTH_LONG).show();
        }return p;
    }

    private TextWatcher startACVtxtchange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

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
        protected String doInBackground(Void... Void) {
            // For storing data from web service
            String data = "";  try {
                // Fetching the data from web service
//                if (startlocation == null)
                    data = DctAPI.downloadUrl(start,end);
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
                // Starts parsing data
                routes = DctAPI.parse(jObject);
                Toast.makeText(MapsActivity.this,routes.toString(),Toast.LENGTH_SHORT).show();
                if (routes == null)
                    Toast.makeText(MapsActivity.this,"NOT_FOUND",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("error",e.getMessage());
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
                        if (i == 0 && j == 0)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(6);  //導航路徑寬度
                    lineOptions.color(Color.BLUE); //導航路徑顏色
                }
                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null)
                    polylineFinal = mMap.addPolyline(lineOptions);
                else
                    Log.e(MapsActivity.class.getName(), "Error List size == 0");
            }catch (NullPointerException e){
                Log.e(MapsActivity.class.getName(),e.getMessage());
            }
        }
    }
}