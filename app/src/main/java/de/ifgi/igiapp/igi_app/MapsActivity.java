package de.ifgi.igiapp.igi_app;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.ifgi.igiapp.igi_app.Bus.AnswerAvailableEvent;
import de.ifgi.igiapp.igi_app.Bus.BusProvider;
import de.ifgi.igiapp.igi_app.Gestures.GestureService;
import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Poi;
import de.ifgi.igiapp.igi_app.MongoDB.Story;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;
import de.ifgi.igiapp.igi_app.MongoDB.Tag;
import de.ifgi.igiapp.igi_app.SpeechRecognition.SpeechInputHandler;

public class MapsActivity extends ActionBarActivity implements MapInterface,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MyInfoWindowClickListener infoWindowClickListener;
    private LocationClient mLocationClient;
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ListView mDrawerList;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int maxResults = 5;
    SpeechInputHandler speechInputHandler;
    Geocoder geocoder;

    GestureService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));
        mMap.setOnInfoWindowClickListener(new MyInfoWindowClickListener(this));
        mLocationClient = new LocationClient(this, this, this);
        mTitle = mDrawerTitle = getTitle();

        // request all data from db and make it global available
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        databaseHandler.requestAllPois();
        databaseHandler.requestAllStories();
        databaseHandler.requestAllTags();
        databaseHandler.requestAllStoryElements();
        GlobalApplication globalApplication = (GlobalApplication) getApplicationContext();
        globalApplication.setGlobalDatabaseHandler(databaseHandler);

        infoWindowClickListener = new MyInfoWindowClickListener(this);
        mMap.setOnInfoWindowClickListener(infoWindowClickListener);

        //Navigation Drawer
        mPlanetTitles = getResources().getStringArray(R.array.drawer_content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[3];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_map_grey, "Map");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_stories_grey, "Stories");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_explore_grey, "Tour");

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.drawer_list_item, drawerItem);
        mDrawerList.setAdapter(adapter);

        // Set the adapter for the list view
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //       R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        // Speech recognition
        mMap.getUiSettings().setZoomControlsEnabled(false);


        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        geocoder = new Geocoder(this, Locale.ENGLISH);
        speechInputHandler = new SpeechInputHandler(this);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }



    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechInputHandler.handleSpeech(result);
                }
                break;
            }

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        BusProvider.getInstance().register(this);
        setUpMapIfNeeded();
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        MarkerOptions options = new MarkerOptions().position(new LatLng(51.963572, 7.613196)).title("Castle");
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.962585, 7.628442)).title("Lamberti"));
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.962585, 7.628442), 12));

    }
/*
    private void locate() {
        Location location = mMap.getMyLocation();

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(myLocation, 5);
            mMap.animateCamera(yourLocation);
        }
    }
*/

    public void onGestureButtonClick(View view) {
        Intent intent = new Intent(this, GestureService.class);

        ToggleButton button = (ToggleButton) view;

        if (button.isChecked()) {
            startService(intent);
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        } else {
            stopService(intent);
            /*if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }*/

        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GestureService.LocalBinder binder = (GestureService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    public void zoomIn(){
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOut(){
        mMap.animateCamera(CameraUpdateFactory.zoomOut());
    }

    public void panUp(){
        mMap.animateCamera(CameraUpdateFactory.scrollBy(0, -400));
    }

    public void panDown(){
        mMap.animateCamera(CameraUpdateFactory.scrollBy(0, 400));
    }

    public void panRight(){
        mMap.animateCamera(CameraUpdateFactory.scrollBy(400, 0));
    }

    public void panLeft(){
        mMap.animateCamera(CameraUpdateFactory.scrollBy(-400, 0));
    }

    public void openDrawer(){ (mDrawerLayout).openDrawer(Gravity.LEFT);}

    public void centerAtCurrentLocation() {
        Location mCurrentLocation = mLocationClient.getLastLocation();
        LatLng latlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        Log.d("", mCurrentLocation.toString());
    }

    public void searchLocation(String location) {
        try {
            // request place
            List<Address> locationList = geocoder.getFromLocationName(location, maxResults);
            // access first result
            if(!locationList.isEmpty()){
                Address address = locationList.get(0);

                double lat = address.getLatitude();
                double lng = address.getLongitude();

                LatLng latLng = new LatLng(lat, lng);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            } else {
                Toast.makeText(getApplicationContext(),
                        "could not find any results: " + location,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "network unavailable or any other I/O problem occurs: " + location,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setStories(Story[] stories){
        // do something with incoming stories
    }

    public void setStoryElements(StoryElement[] storyElements){
        // do somesting with incoming storyElements
    }

    public void setPois(Poi[] pois){
        // do something with incoming pois
        drawMarkers(pois);
    }

    public void setTags(Tag[] tags){
        // do something with incoming tags
    }

    public void drawMarkers(Poi[] pois){
        for (int i = 0; i < pois.length; i++){
            MarkerOptions markerOptions = new MarkerOptions().position(pois[i].getLocation()).title(pois[i].getName()).snippet(pois[i].getDescription());
            Marker marker = mMap.addMarker(markerOptions);
            infoWindowClickListener.markerPoiHandler.put(marker.getId(), pois[i].getId());
        }    
    }
    @Subscribe
    public void answerAvailable(AnswerAvailableEvent event) {
        if (event.getEvent() == BusProvider.PAN_LEFT) {
            this.panLeft();
        } else if (event.getEvent() == BusProvider.PAN_RIGHT) {
            this.panRight();
        } else if (event.getEvent() == BusProvider.PAN_DOWN) {
            this.panDown();
        }else if (event.getEvent() == BusProvider.PAN_UP) {
            this.panUp();
        }else if (event.getEvent() == BusProvider.ZOOM_IN) {
            this.zoomIn();
        }else if (event.getEvent() == BusProvider.ZOOM_OUT) {
            this.zoomOut();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
}

