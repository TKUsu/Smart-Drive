package com.su.Tap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener{

    private Direction direction;

    private AutoCompleteTextView startautoCompView = null, endautiComView = null;
    private LinearLayout mSearchBar;
    private Button mButtonDown;

    private LatLng start = null, end = null;
    private CharSequence place_startname = null, place_endname = null;
    private ArrayList<Marker> markers = new ArrayList<>();

    private int ButtonNumber = 0;
    private float DownX = 0;
    private GoogleMap mMap;
    private UiSettings mUis;
    private LocationManager mLocationmgr;
    private Location mLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Theme();

        mButtonDown = (Button) findViewById(R.id.down);
        mSearchBar = (LinearLayout) findViewById(R.id.searchBar);
        mLocationmgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        // Update values using data stored in the Bundle.
        updataValueFromBundle(savedInstanceState);
        buildGoogleApiClient();

        startautoCompView = (AutoCompleteTextView) findViewById(R.id.startautocomtxt);
        startautoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        startautoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) parent.getItemAtPosition(position);
                start = getLatLongFromAddress(s);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(start));
                markers.add(setMapMarker(s, null, start, null, null));
            }
        });

        endautiComView = (AutoCompleteTextView) findViewById(R.id.endautocomtxt);
        endautiComView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
        endautiComView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) parent.getItemAtPosition(position);
                end = getLatLongFromAddress(s);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(end));
                markers.add(setMapMarker(s, null, end, null, null));
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) return;
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     *              Map
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        mUis = mMap.getUiSettings();
        mUis.setMyLocationButtonEnabled(true);
        mUis.setZoomControlsEnabled(true);
        mUis.setCompassEnabled(true);
        mUis.setAllGesturesEnabled(true);
        mUis.setIndoorLevelPickerEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setPadding(0, 200, 0, getNavigationHeight(this));

        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        if (mLocation != null) {
            if (ButtonNumber == 0)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng(), 18));
            else
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mLatLng()));
        } else {
            Location location = mLocationmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                location = mLocationmgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                if (ButtonNumber == 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
                else
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            } else
                Toast.makeText(MapsActivity.this, R.string.failure_position, LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.isInfoWindowShown())
            marker.hideInfoWindow();
        else marker.showInfoWindow();
        mUis.setMapToolbarEnabled(false);
        return false;
    }

    private Marker setMapMarker(String name, String address, LatLng latLng, String number, String Type) {
        if(ButtonNumber == 1) {
            String markertxt = "";
            if (address != null)
                markertxt += "Address : " + address + "\n";
            if (number != null)
                markertxt += "Phone number : " + number + "\n";
            if (Type != null)
                markertxt += "Place type : " + Type;
            Marker marker = mMap.addMarker(new MarkerOptions()
                            .title(name)
                            .position(latLng)
                            .snippet(markertxt)
                            .flat(true)
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            return marker;
        }return null;
    }

    private int PRange,SRange,DRange;
    public void OnDownButtonClick(View view) {
        /**
         *      1 :Map padding ride up 0 to 200
         *      2 :Search bar ride up -200 to 0
         *      3 :Button down ride down -10 to -100
         */
        if(view.getId() == R.id.down) {
            PRange = view.getHeight();
            SRange = view.getHeight();
            DRange = view.getHeight();
        }
        myAnimation(0,200,-200,200,-10,-90);
        mButtonDown.setTranslationX(-100);
//        direction.deletePolyLine();
        direction.parserTask.deletePolyLine();
    }
    private void SearchBarRideUp() {
        /**
         *      1 :Map padding ride up 200 to 0
         *      2 :Search bar ride up 0 to -200
         *      3 :Button down ride down -100 to -10
         *      myAnimation((1,2,3).translation,(1,2,3).start point)
         */
        myAnimation(200,-200,0,-200,-100,90);
        mButtonDown.setTranslationX(mSearchBar.getWidth() / 2 - 20);
    }
    private void myAnimation(int Pstart, int PRange, int Sstart, int SRange, int Dstart, int DRange) {
        PRange = PRange / 10;
        SRange = SRange / 10;
        DRange = DRange / 10;
        int i = 0;
        Handler handler1 = new Handler();
        while (i<10){
            Pstart += PRange;
            Sstart += SRange;
            Dstart += DRange;
            final int finalPtemp = Pstart;
            final int finalStemp = Sstart;
            final int finalDtemp = Dstart;
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMap.setPadding(0, finalPtemp, 0, 0);
                    mSearchBar.setTranslationY(finalStemp);
                    mButtonDown.setTranslationY(finalDtemp);
                }
            },25 * i);     i++;
        }
    }

    /**
     * =============================================================================================
     * Places Picker
     */
    private static final int REQUEST_PLACE_PICKER = 1;

    public void onSearch(View view) {
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
            final String name = place.getName().toString();
            final String address = place.getAddress().toString();
            final LatLng latlng = place.getLatLng();
            final String number = place.getPhoneNumber().toString();
            final String Type = place.getPlaceTypes().toString();

            if (ButtonNumber == 1) {
                startautoCompView.setText(name);
                start = latlng;
                this.place_startname = name;
                markers.add(setMapMarker(name, address, start, number, Type));
            } else if (ButtonNumber == 2) {
                endautiComView.setText(name);
                end = latlng;
                this.place_endname = name;
                markers.add(setMapMarker(name, address, end, number, Type));
            }
        }
    }

    /**
     * Navigation    判斷是否為空值、直接輸入文字或是選取地點
     */
    public void onNavigation(View view) {
        String starttmp, endtmp;
        starttmp = startautoCompView.getText().toString();
        endtmp = endautiComView.getText().toString();
        if (!starttmp.equals("")) {
            startautoCompView.setHint("Start Location");
            startautoCompView.setHintTextColor(0x454545);
        }else if (starttmp.equals("")) {
            startLocationUpdates();
            startautoCompView.setHint("Your Location...");
            startautoCompView.setHintTextColor(Color.BLACK);
            try {
                start = mLatLng();
            }catch (NullPointerException e){
                Toast.makeText(MapsActivity.this, "Your locate isn't found,please check GPS is open", LENGTH_SHORT).show();
            }
            if (endtmp.equals(""))
                Toast.makeText(MapsActivity.this, R.string.endlocationnull, LENGTH_SHORT).show();
            else {
                if (!place_endname.equals(endtmp))
                    end = getLatLongFromAddress(endtmp);
                SearchBarRideUp();
                direction = new Direction(start, end, mMap);
                direction.execute();
            }
        } else {
            if (!place_startname.equals(starttmp))
                start = getLatLongFromAddress(starttmp);
            if (!place_endname.equals(endtmp))
                end = getLatLongFromAddress(endtmp);
            SearchBarRideUp();
            direction = new Direction(start, end, mMap);
            direction.execute();
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 18));
//        mPolyLine = direction.parserTask.getPolyLine();
        try {
//            mPolyline.setColor(Color.BLACK);
            direction.dcolor = Color.BLACK;
        }catch (NullPointerException e){
            Toast.makeText(MapsActivity.this,e.toString(),Toast.LENGTH_LONG);
        }
    }

    private void ShowPercent(){

    }
    /**
     * getLatLongFromAddress
     */
    private LatLng getLatLongFromAddress(String address) {
        LatLng p = null;
        Geocoder geoCoder = new Geocoder(this, Locale.TAIWAN);
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                p = new LatLng((addresses.get(0).getLatitude()), (addresses.get(0).getLongitude()));
                return p;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "El servicio de Google Maps no se encuentra disponible.\n"
                    + "Intente ms tarde.", Toast.LENGTH_LONG).show();
        }
        return p;
    }

    /**
     * =============================================================================================
     * LocationUpdate
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle
     */

    protected static final String TAG = "location-updates";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    protected static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private void updataValueFromBundle(Bundle savedInstanceState) {
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

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
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

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
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
    public void onLocationChanged(Location location) {
        mLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    private LatLng mLatLng(){
        return new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
    }

    private void Theme(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private int getNavigationHeight(Context context){
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private int getStatusHeight(Context context,int type){
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            if (type == 0)
                return resources.getDimensionPixelSize(resourceId)
                        + mSearchBar.getHeight();
            else if (type ==1)
                return resources.getDimensionPixelSize(resourceId)
                        + mSearchBar.getHeight();
        }
        return 0;
    }
}