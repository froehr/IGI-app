package de.ifgi.igiapp.igi_app;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
import de.ifgi.igiapp.igi_app.SharedPreferences.ActivityFirstLaunch;
import de.ifgi.igiapp.igi_app.SpeechRecognition.SpeechInputHandler;

public class MapsActivity extends ActionBarActivity implements MapInterface,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MyInfoWindowClickListener infoWindowClickListener;
    private GoogleApiClient mLocationClient;
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
    DatabaseHandler databaseHandler;

    GestureService mService;
    boolean mBound = false;
    private boolean mGestureServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // first time run?
        if (pref.getBoolean("firstTimeRun", true)) {

            // start the preferences activity
            startActivity(new Intent(getBaseContext(), ActivityFirstLaunch.class));

            //get the preferences editor
            SharedPreferences.Editor editor = pref.edit();

            // avoid for next run
            editor.putBoolean("firstTimeRun", false);
            editor.commit();
        }

        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));
        mMap.setOnInfoWindowClickListener(new MyInfoWindowClickListener(this));
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mTitle = mDrawerTitle = getTitle();

        // request all data from db and make it global available
        databaseHandler = new DatabaseHandler(this);
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

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[4];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_map_grey, "Map");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_stories_grey, "Stories");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_explore_grey, "Tour");
        drawerItem[3] = new ObjectDrawerItem(R.drawable.ic_tutorial_grey, "Tutorial");

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

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.962585, 7.628442), 12));

    }

    public void onGestureButtonClick(View view) {
        Intent intent = new Intent(this, GestureService.class);

        ToggleButton button = (ToggleButton) view;

        if (button.isChecked()) {
            startService(intent);
            mGestureServiceRunning = true;
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        } else {
            stopService(intent);
            mGestureServiceRunning = false;
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
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
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

    public void showStories() {
        Intent intent = new Intent(MapsActivity.this, StoryListActivity.class);
        startActivity(intent);
    }

    public void searchStoryElementsByTag(String tag) {
        Intent intent = new Intent(MapsActivity.this, StoryElementListActivity.class);
        intent.putExtra("tag", tag);
        startActivity(intent);
    }

    public void startStory(String storyName) {
        Story[] stories = databaseHandler.getAllStories();

        for ( int i = 0; i < stories.length; i++ ) {
            if ( stories[i].getName().toLowerCase().equals(storyName.toLowerCase()) ) {
                String storyId = stories[i].getId();
                Intent intent = new Intent(MapsActivity.this, StoryLineMap.class);
                intent.putExtra("story-id", storyId);
                startActivity(intent);
                return;
            }
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
        } else if (event.getEvent() == BusProvider.PAN_UP) {
            this.panUp();
        } else if (event.getEvent() == BusProvider.ZOOM_IN) {
            this.zoomIn();
        } else if (event.getEvent() == BusProvider.ZOOM_OUT) {
            this.zoomOut();
        } else if (event.getEvent() == BusProvider.CENTER_CURRENT_LOCATION) {
            this.centerAtCurrentLocation();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
        if (mGestureServiceRunning) {
            Intent intent = new Intent(this, GestureService.class);
            startService(intent);
        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        if (mGestureServiceRunning) {
            Intent intent = new Intent(this, GestureService.class);
            stopService(intent);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, GestureService.class);
        stopService(intent);
        mGestureServiceRunning = false;
        super.onDestroy();
    }
}

