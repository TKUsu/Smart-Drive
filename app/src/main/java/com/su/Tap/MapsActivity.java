package com.su.Tap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Polyline polylineFinal = null;
    private AutoCompleteTextView startautoCompView = null, endautiComView = null;
    private Marker startMarker=null,endMarker=null;
    private TextView Time;

    private LatLng start = null, end = null;
    private CharSequence place_startname = null, place_endname = null;
    private int ButtonNumber = 0;

    private Location mLocation;
    /** Tracks the status of the location updates request. Value changes when the user presses the
      * Start Updates and Stop Updates buttons.  */
    protected Boolean mRequestingLocationUpdates;
    /**  Time when the location was updated represented as a String.  */
    protected String mLastUpdateTime;
    private LocationManager mLocationmgr;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    protected static final String TAG = "location-updates";
    /**  The desired interval for location updates. Inexact. Updates may be more or less frequent.*/
    protected static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";


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

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        // Update values using data stored in the Bundle.
        updataValueFromBundle(savedInstanceState);
        buildGoogleApiClient();

        startautoCompView = (AutoCompleteTextView) findViewById(R.id.startautocomtxt);
        startautoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        startautoCompView.addTextChangedListener(startACVtxtchange);
        startautoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) parent.getItemAtPosition(position);
                start = getLatLongFromAddress(s);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(start));
                startMarker = mMap.addMarker(new MarkerOptions().title(s).position(getLatLongFromAddress(s)));
            }
        });

        endautiComView = (AutoCompleteTextView) findViewById(R.id.endautocomtxt);
        endautiComView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        startautoCompView.addTextChangedListener(endACVtxtchange);
        endautiComView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String)parent.getItemAtPosition(position);
                end = getLatLongFromAddress(s);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(end));
                endMarker = mMap.addMarker(new MarkerOptions().title(s).position(getLatLongFromAddress(s)));
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates)
            startLocationUpdates();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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
        mGoogleApiClient.disconnect();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uis = mMap.getUiSettings();
        uis.setMyLocationButtonEnabled(true);
        uis.setZoomControlsEnabled(true);
        uis.setCompassEnabled(true);
        uis.setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setOnMarkerClickListener(this);

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
            onLocationChanged(location);
        } else
            Toast.makeText(MapsActivity.this, R.string.failure_position, LENGTH_SHORT).show();
        if(ButtonNumber == 0)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 18));
        else
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.isInfoWindowShown())
            marker.hideInfoWindow();
        else marker.showInfoWindow();
        return false;
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
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            final LatLng latlng = place.getLatLng();
            final CharSequence number = place.getPhoneNumber();
            final String Type = place.getPlaceTypes().toString();

            String attributions = PlacePicker.getAttributions(data);
            if (attributions == null) {
                attributions = "";
            }
            if(ButtonNumber == 1){
                startautoCompView.setText(name);
                start = latlng;
                this.place_startname = name.toString();
                startMarker = mMap.addMarker(new MarkerOptions()
                                .title(name.toString()).position(start)
                                .snippet("Address : " + address.toString()
                                    + "\n" + "Phone number : " + number.toString()
                                    + "\n" + "Place type : " + Type)
                        );
            }else if (ButtonNumber == 2){
                endautiComView.setText(name);
                end = latlng;
                this.place_endname = name.toString();
                endMarker = mMap.addMarker(new MarkerOptions()
                                .title(name.toString()).position(start)
                                .snippet("Address : " + address.toString()
                                        + "\n" + "Phone number : " + number.toString()
                                        + "\n" + "Place type : " + Type)
                );
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

    private TextWatcher endACVtxtchange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (endMarker != null)  endMarker.remove();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher startACVtxtchange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (startMarker != null)    startMarker.remove();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //--------------------------LocationUpdate-----------------------------------------
    /**  Updates fields based on data stored in the bundle.
     *   @param savedInstanceState The activity state saved in the Bundle.     */
    private void updataValueFromBundle(Bundle savedInstanceState){
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY))
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation is not null.
                mLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY))
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
        }
    }
    /**  Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.  */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /** Requests location updates from the FusedLocationApi.  */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    /** Removes location updates from the FusedLocationApi.  */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mLocation == null) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
    @Override
    public void onLocationChanged(Location location){
        mLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Toast.makeText(this,"Location Update now",Toast.LENGTH_SHORT).show();
    }
    /** Stores activity data in the Bundle.  */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
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