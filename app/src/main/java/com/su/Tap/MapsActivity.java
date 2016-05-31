package com.su.Tap;

import android.annotation.TargetApi;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;

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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener, AsyncResponse {

    private Direction direction;

    private Thread thread;
    private Intent intentService;

    private AutoCompleteTextView startautoCompView = null, endautiComView = null;
    private LinearLayout mSearchBar;
    private Button mButtonDown;
    private FloatingActionButton fabLocation, fabService, fabTest,fabTest2;
    private TextView txt;

    private LatLng start = null, end = null;
    private CharSequence place_startname = null, place_endname = null;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<LatLng> myLocaitonList = new ArrayList<>();
    public SQLiteHelper sqLiteHelper;
    private Polyline polyline = null;
    private PolylineOptions lineOptions;
    int dcolor = R.color.smartDriverColor2;
    int lineWidth = 10;

    private Boolean loactionchangejudge = false;
    private Boolean myLocationDrawJudgement = false;
    private Boolean startstop = false;
    private Boolean isRunning = false;
    public int ButtonNumber = 0;
    //    private float DownX = 0;
    public GoogleMap mMap;
    private UiSettings mUis;
    private LocationManager mLocationmgr;

    private Location mLocation = null;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * 2016/05/16
     * SDK is 23 up,So location access is error,has been access for user
     */
    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private final String TAG_Activity = "Activity is :";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /**     2016/05/16
         * SDK is 23 up,So location access is error,has been access for user
         */
        if (!canAccessLocation() || !canAccessContacts())
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);

        Theme();

        fabControl(myLocaitonList);

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
                markers.add(setMapMarker(0, s, null, start, null, null));
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
                markers.add(setMapMarker(1, s, null, end, null, null));
            }
        });

        //使用該LOG可以知道當下是使用哪個執行緒,1為主執行緒
        Log.e("thread", "Map id=" + String.valueOf(Thread.currentThread().getId()));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        txt = (TextView)findViewById(R.id.txt);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates)
            startLocationUpdates();
        Log.i(TAG_Activity, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
//        if (mGoogleApiClient.isConnected())
//            stopLocationUpdates();
        Log.i(TAG_Activity, "onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        mGoogleApiClient.connect();
        if (mMap != null && !mMap.isMyLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) return;
            mMap.setMyLocationEnabled(true);
        }
        Log.i(TAG_Activity, "onStart");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.su.Tap/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.su.Tap/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
//        if (!isRunning)
//            mGoogleApiClient.disconnect();
        Log.i(TAG_Activity, "onStop");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mGoogleApiClient.disconnect();
            stopService(intentService);
        } catch (RuntimeException e) {
        }
        isRunning = false;
        startstop = false;
        Log.i(TAG_Activity, "onDestroy");
    }
    Boolean tempjudge = false;
    int temp = 0;
    ArrayList<LatLng> arrayListtemp = new ArrayList<>();
    private void fabControl(final ArrayList<LatLng> output) {
        fabService = (FloatingActionButton) findViewById(R.id.fab);
        fabService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocation != null) {
                    if (!startstop) {
                        toast("Service Start");
                        fabService.setImageResource(R.drawable.ic_pause_dark);
                        isRunning = true;
                        startstop = true;
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                String s = "("+mLocation.getLatitude()+"),("+mLocation.getLatitude()+")";
//                                txt.setText(s);
                                while (isRunning) {
                                    intentService = new Intent(MapsActivity.this, SQLService.class);
                                    intentService.putExtra("Lat", mLocation.getLatitude());
                                    intentService.putExtra("Lng", mLocation.getLatitude());
                                    startService(intentService);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        thread.start();
                    } else {
                        toast("Service Stop");
                        fabService.setImageResource(R.drawable.ic_play_dark);
                        isRunning = false;
                        startstop = false;
                        intentService = new Intent(MapsActivity.this, SQLService.class);
                        stopService(intentService);
                    }
                } else
                    toast("Please check your GPS is opened");
            }
        });
        fabTest = (FloatingActionButton) findViewById(R.id.fabTest);
        fabTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d("TestRoad",output.toString());
//                for (int i = 0;i < output.size();i++) {
//                    sqLiteHelper.sql("insert", output.get(i));
//                    Log.d("TestRoad_",output.get(i).toString());
//                }
                if (!myLocationDrawJudgement) {
                    lineOptions = new PolylineOptions();
                    Log.d("SQL", "Drawing now~~~");
                    //Adding all the points in the route to LineOptions
                    lineOptions.addAll(output);
                    lineOptions.width(lineWidth);  //導航路徑寬度
                    lineOptions.color(dcolor); //導航路徑顏色
                    polyline = mMap.addPolyline(lineOptions);
                    myLocationDrawJudgement = true;
                }else if(myLocationDrawJudgement){
                    polyline.remove();
                    myLocationDrawJudgement = false;
                }
//                temp++;
//                toast("change");
//                Log.d("SQL", String.valueOf(temp));
            }
        });
//        fabTest2 = (FloatingActionButton)findViewById(R.id.fabTest2);
//        fabTest2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (int i = 0;i < output.size();i++) {
//                    arrayListtemp.add(output.get(i));
//                }
//            }
//        });
        fabLocation = (FloatingActionButton) findViewById(R.id.fabLocation);
        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fabTest2.hide();
//                fabTest.hide();

                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng(), 18));
                } catch (NullPointerException e) {
                    toast("Please check your GPS is opened");
                    Log.e("My Location", e.toString());
                }
//                if (temp == 0) {

//                    sqLiteHelper = new SQLiteHelper(MapsActivity.this, "roadDB");
//                    sqLiteHelper.setData(sqLiteHelper.getWritableDatabase());
////                    ArrayList<LatLng> arrayList = new ArrayList<LatLng>();
//                    for (int i = 0;i < output.size();i++) {
//                        sqLiteHelper.sql("insert",output.get(i));
//                        Log.d("TestRoad_",output.get(i).toString());
////                        arrayListtemp.add(output.get(i));
//                    }

//                    lineOptions = new PolylineOptions();
//                    Log.d("SQL", "Drawing now~~~");
//                     Adding all the points in the route to LineOptions
//                    lineOptions.addAll(arrayList);
//                    lineOptions.width(lineWidth);  //導航路徑寬度
//                    lineOptions.color(dcolor); //導航路徑顏色
//                    polyline = mMap.addPolyline(lineOptions);
//                    myLocationDrawJudgement = true;
//                }else if(temp == 1){
//                    sqLiteHelper = new SQLiteHelper(MapsActivity.this, "roadDB2");
//                    sqLiteHelper.setData(sqLiteHelper.getWritableDatabase());
////                    ArrayList<LatLng> arrayList = new ArrayList<LatLng>();
//                    for (int i = 0;i < output.size();i++) {
//                        sqLiteHelper.sql("insert", output.get(i));
//                        Log.d("TestRoad_",output.get(i).toString());
//                    }
//                    lineOptions = new PolylineOptions();
//                    Log.d("SQL", "Drawing now~~~");
                    // Adding all the points in the route to LineOptions
//                    lineOptions.addAll(arrayList);
//                    lineOptions.width(lineWidth);  //導航路徑寬度
//                    lineOptions.color(dcolor); //導航路徑顏色
//                    polyline = mMap.addPolyline(lineOptions);
//                    myLocationDrawJudgement = true;
//                }
            }
        });
    }

    /**
     * Map
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        Log.e(TAG, "On Map Ready");
        mUis = mMap.getUiSettings();
        mUis.setMyLocationButtonEnabled(false);
        mUis.setZoomControlsEnabled(true);
        mUis.setCompassEnabled(true);
        mUis.setAllGesturesEnabled(true);
        mUis.setIndoorLevelPickerEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setPadding(0, 0, 0, 2 * getNavigationHeight(this));

        //Check Location is ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION is ok
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (mLocation != null) {
            Log.e(TAG, "Fist Location");
            if (ButtonNumber == 0)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng(), 18));
            else
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mLatLng()));
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

    private Marker setMapMarker(int i, String name, String address, LatLng latLng, String number, String Type) {
        BitmapDescriptor markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        if (i == 0)
            markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        else if (i == 1)
            markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
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
                .icon(markerColor));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        return marker;
    }

    private int PRange, SRange, DRange;

    public void OnDownButtonClick(View view) {
        /**
         *      1 :Map padding ride up 0 to 200
         *      2 :Search bar ride up -200 to 0
         *      3 :Button down ride down -10 to -100
         */
        if (view.getId() == R.id.down) {
            PRange = view.getHeight();
            SRange = view.getHeight();
            DRange = view.getHeight();
        }
        Log.e("Test:", "PRange = " + String.valueOf(PRange) +
                "SRange = " + String.valueOf(SRange) + "DRange = " + String.valueOf(DRange));
        myAnimation(0, 200, -200, 200, -10, -90);
        mButtonDown.setTranslationX(-100);
//        direction.parserTask.deletePolyLine();
        mMap.clear();
    }

    private void SearchBarRideUp() {
        /**
         *      1 :Map padding ride up 200 to 0
         *      2 :Search bar ride up 0 to -200
         *      3 :Button down ride down -100 to -10
         *      myAnimation((1,2,3).translation,(1,2,3).start point)
         */
        myAnimation(200, -200, 0, -200, -100, 90);
        mButtonDown.setTranslationX(mSearchBar.getWidth() / 2 - 20);
    }

    private void myAnimation(int Pstart, int PRange, int Sstart, int SRange, int Dstart, int DRange) {
        PRange = PRange / 10;
        SRange = SRange / 10;
        DRange = DRange / 10;
        int i = 0;
        Handler handler1 = new Handler();
        while (i < 10) {
            Pstart += PRange;
            Sstart += SRange;
            Dstart += DRange;
            final int finalPtemp = Pstart;
            final int finalStemp = Sstart;
            final int finalDtemp = Dstart;
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    mMap.setPadding(0, finalPtemp, 0, 0);
                    mSearchBar.setTranslationY(finalStemp);
                    mButtonDown.setTranslationY(finalDtemp);
                }
            }, 25 * i);
            i++;
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
                markers.add(setMapMarker(0, name, address, start, number, Type));
            } else if (ButtonNumber == 2) {
                endautiComView.setText(name);
                end = latlng;
                this.place_endname = name;
                markers.add(setMapMarker(1, name, address, end, number, Type));
            }
        }
    }

    /**
     * Navigation    判斷是否為空值、直接輸入文字或是選取地點
     */
    int judgenavigation = 0;
    public void onNavigation(View view) {
        String starttmp, endtmp;
        String temp = null;
        starttmp = startautoCompView.getText().toString();
        endtmp = endautiComView.getText().toString();
        if (!starttmp.equals("")) {
            startautoCompView.setHint("Start Location");
            startautoCompView.setHintTextColor(0x454545);
        }
        if (starttmp.equals("")) {
            startautoCompView.setHint("Your Location...");
            startautoCompView.setHintTextColor(Color.BLACK);
            try {
                start = mLatLng();
            } catch (NullPointerException e) {
                toast("Your locate isn't found,please check GPS is open");
            }
            if (endtmp.equals(""))
                toast(String.valueOf(R.string.endlocationnull));
            else {
                if (!place_endname.equals(endtmp))
                    end = getLatLongFromAddress(endtmp);
                SearchBarRideUp();
                direction = new Direction(start, end, mMap);
                direction.delegate = this;
                try {
                    direction.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!place_startname.equals(starttmp))
                start = getLatLongFromAddress(starttmp);
            if (!place_endname.equals(endtmp))
                end = getLatLongFromAddress(endtmp);
            SearchBarRideUp();
            direction = new Direction(start, end, mMap);
            direction.delegate = this;
            try {
                direction.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 18));
        if (judgenavigation == 0) {
            draw(1);
//            draw(2);
            judgenavigation++;
        }
    }

    private void ShowPercent() {

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
            toast("El servicio de Google Maps no se encuentra disponible.\n"
                    + "Intente ms tarde.");
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
    protected static final long UPDATE_INTERVAL_IN_MILLISECONDS = 100;
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
//        try{toast("Updating values from bundle"+mLocation.toString());}catch (NullPointerException e){}
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
//        toast("Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        Log.i(TAG, "Create Location Request");
//        toast("Create Location Request");
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
        //  Check Location is ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION is ok
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.i(TAG, "Connected to GoogleApiClient");
//        toast("Connected to GoogleApiClient");
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        Log.i(TAG, "Stop Location Update");

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
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
//        toast("Connected to GoogleApiClient"+mLocation.toString());
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
        Log.i(TAG, "On Location Changed");
//        toast("On Location Changed" + location.toString()+mLastUpdateTime);
        mLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.i(TAG, "Location : " + location.toString() + "Update Time: " + mLastUpdateTime);
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

    /**
     * ====================================================================
     */

    public LatLng mLatLng() {
        return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void Theme() {
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

    private int getNavigationHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private int getStatusHeight(Context context, int type) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            if (type == 0)
                return resources.getDimensionPixelSize(resourceId)
                        + mSearchBar.getHeight();
            else if (type == 1)
                return resources.getDimensionPixelSize(resourceId)
                        + mSearchBar.getHeight();
        }
        return 0;
    }

    public void toast(String tmp) {
        try {
            Toast.makeText(MapsActivity.this, tmp, Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            Log.e("MapActivity toast", e.toString());
        }
    }

    /**
     * 2016/05/16
     * SDK is 23 up,So location access is error,has been access for user
     */
    private boolean canAccessContacts() {
        return (hasPermission(Manifest.permission.READ_CONTACTS));
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    @Override
    public void processFinish(ArrayList<LatLng> output) {
        Log.d("TestRoad", "Direction list2:" + output.toString());
        this.myLocaitonList = output;
        fabControl(output);
//                try {
//                    Log.d("SQL", "MapActivity list:" + myLocaitonList.toString());
//                } catch (NullPointerException e) {
//                    Log.d("SQL", "MapActivity list is NULL");
//                }
    }

    private void draw(int i){
        switch (i) {
            case 1:
                sqLiteHelper = new SQLiteHelper(MapsActivity.this, "roadDB");
                break;
//            case 2:
////                sqLiteHelper = new SQLiteHelper(MapsActivity.this, "roadDB2");
//                PolylineOptions lineOptions = new PolylineOptions();
//                Log.d("SQL", "Drawing now~~~");
//                // Adding all the points in the route to LineOptions
//                lineOptions.addAll(arrayListtemp);
//                lineOptions.width(lineWidth);  //導航路徑寬度
//                lineOptions.color(dcolor); //導航路徑顏色
//                polyline = mMap.addPolyline(lineOptions);
//                break;
//            case 3:
//                sqLiteHelper = new SQLiteHelper(MapsActivity.this, "roadDB3");
//                break;
//            case 4:
//                sqLiteHelper = new SQLiteHelper(MapsActivity.this, "roadDB4");
//                break;
        }
        sqLiteHelper.setData(sqLiteHelper.getWritableDatabase());
        ArrayList<LatLng> arrayList = new ArrayList<LatLng>();
        arrayList = sqLiteHelper.sql("search", null);
        lineOptions = new PolylineOptions();
        Log.d("SQL", "Drawing now~~~");
        // Adding all the points in the route to LineOptions
        lineOptions.addAll(arrayList);
        lineOptions.width(lineWidth);  //導航路徑寬度
        lineOptions.color(dcolor); //導航路徑顏色
        polyline = mMap.addPolyline(lineOptions);
        myLocationDrawJudgement = true;
    }
}