package de.ifgi.igiapp.igi_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Poi;
import de.ifgi.igiapp.igi_app.MongoDB.Story;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;

public class StoryLineMap extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private String storyId;
    private DatabaseHandler databaseHandler;
    private LocationClient mLocationClient;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private boolean connected = false;
    private boolean startIfConnected = false;

    Button startStoryButton;
    Button NextFakeLocation;

    String[] storyElementIds;

    // request code for visiting story element
    private int VISIT_STORYELEMENT = 1;

    // all markers of this story
    MarkerOptions[] markerCollection;

    // array index of approaching marker
    private int approachingMarker;

    // minimum distance before story element is opened
    private final double ACTIVATION_DISTANCE = 0.05;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_line_map);
        Intent intent = getIntent();
        storyId = intent.getStringExtra("story-id");

        GlobalApplication application = (GlobalApplication) getApplicationContext();
        databaseHandler = application.getGlobalDatabaseHandler();

        setUpMapIfNeeded();

        // location manager for updating position and calculating distances
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // for initial position
        mLocationClient = new LocationClient(this, this,this);
        mLocationClient.connect();

        startStoryButton = (Button) findViewById(R.id.startStoryButton);
        startStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start as soon as connection is established
                startIfConnected = true;

                // make button invisible
                startStoryButton.setVisibility(View.GONE);

                // if not yet connect inform user
                if (!connected) {
                    Toast.makeText(getApplicationContext(), "Waiting for GPS", Toast.LENGTH_LONG).show();
                } else {
                    startStory();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.story_map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // wait until map is loaded before adding polygon and center view on it
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Story story = databaseHandler.getStoryByStoryId(storyId);
                storyElementIds = story.getStoryElementId();
                StoryElement[] storyElements = new StoryElement[storyElementIds.length];
                for(int i = 0; i < storyElementIds.length; i++){
                    storyElements[i] = databaseHandler.getStoryElementByStoryElementId(storyElementIds[i]);
                }

                addStoryElementsToMap(storyElements);

                PolylineOptions storyLine = databaseHandler.getPolylineOptionsByStoryId(storyId);
                storyLine.color(Color.BLUE);
                addPolylineToMap(storyLine);
            }
        });
    }

    public void addStoryElementsToMap(StoryElement[] storyElements){

        markerCollection = new MarkerOptions[storyElements.length];

        for(int i = 0; i < storyElements.length; i++){
            String storyElementPoiId = storyElements[i].getPoiId();
            Poi storyElementPoi = databaseHandler.getPoiByPoiId(storyElementPoiId);
            MarkerOptions markerOptions = new MarkerOptions().position(storyElementPoi.getLocation());
            markerCollection[i] = markerOptions;
            mMap.addMarker(markerOptions);
        }
    }

    public void addPolylineToMap(PolylineOptions polylineOptions){
        Polyline polyline = mMap.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < polyline.getPoints().size();i++){
            builder.include(polyline.getPoints().get(i));
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public void startStory(){
        approachingMarker = 0;

        Location mCurrentLocation = mLocationClient.getLastLocation();
        centerMapOnLocation(mCurrentLocation);
        startLocationListener();
    }

    private void startLocationListener(){
        mLocationListener = new LocationListener(){
            public void onLocationChanged (Location location) {
                Toast.makeText(getApplicationContext(), "Received new position", Toast.LENGTH_SHORT).show();
                centerMapOnLocation(location);
                checkDistanceToMarker(location, markerCollection[approachingMarker].getPosition());
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        // register listener
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    public void centerMapOnLocation(Location location){
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
    }

    public void checkDistanceToMarker(Location currentLocation, LatLng markerPosition){

        // calculate distance
        final int earthRadius = 6371;
        float dLat = (float) Math.toRadians(markerPosition.latitude - currentLocation.getLatitude());
        float dLon = (float) Math.toRadians(markerPosition.longitude - currentLocation.getLongitude());
        float a =
            (float) (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(currentLocation.getLatitude()))
                * Math.cos(Math.toRadians(markerPosition.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float distance = earthRadius * c;

        if ( distance < ACTIVATION_DISTANCE){
            // stop watching location as long as user is in story element
            mLocationManager.removeUpdates(mLocationListener);
            // open new activity
            openApproachingStoryElement();
        }
    }

    public void openApproachingStoryElement(){
        Intent intent = new Intent (StoryLineMap.this, StoryElementActivity.class);
        intent.putExtra("story-element-id", storyElementIds[approachingMarker]);
        startActivityForResult(intent, VISIT_STORYELEMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VISIT_STORYELEMENT) {
            if (resultCode == RESULT_OK) {
                // marker has been visited go to next one if available
                if(markerCollection.length == approachingMarker){
                    // TODO finish story
                    Toast.makeText(getApplicationContext(), "You visited all story elements", Toast.LENGTH_SHORT).show();
                } else {
                    approachingMarker++;
                    // start watching out for next element
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,mLocationListener);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
        Toast.makeText(getApplicationContext(), "GPS established", Toast.LENGTH_LONG).show();

        if(startIfConnected){
            startStory();
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
    }
}
