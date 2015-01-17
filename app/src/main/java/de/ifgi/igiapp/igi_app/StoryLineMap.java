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
        GooglePlayServicesClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private String storyId;
    private DatabaseHandler databaseHandler;
    private LocationClient mLocationClient;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private boolean connected = false;
    private boolean startIfConnected = false;

    Button startStoryButton;
    Button nextFakeLocButton;

    String[] storyElementIds;

    // request code for visiting story element
    private int VISIT_STORYELEMENT = 1;

    // all markers of this story
    MarkerOptions[] markerCollection;

    // array index of approaching marker
    private int approachingMarker;

    // minimum distance before story element is opened
    private final double ACTIVATION_DISTANCE = 0.15;

    //fake variables
    LatLng[] fakeLocations = {
            new LatLng(51.95071924140229, 7.597646713256836),
            new LatLng(51.95071924140229, 7.600564956665039),
            new LatLng(51.95056053871301, 7.603483200073242),
            new LatLng(51.950296032982756, 7.605113983154297),
            new LatLng(51.95071924140229, 7.607431411743164),
            new LatLng(51.95346999877737, 7.611465454101562),
            new LatLng(51.954739522191694, 7.614984512329102),
            new LatLng(51.95659084607304, 7.619190216064452),
            new LatLng(51.959499914870015, 7.618932723999023),
            new LatLng(51.966586674132465, 7.617559432983399),
            new LatLng(51.97150443797158, 7.622795104980469),
            new LatLng(51.97256195108668, 7.628889083862305),
            new LatLng(51.97309069828516, 7.633008956909179),
            new LatLng(51.97361943924433, 7.636699676513671),
            new LatLng(51.97282632546583, 7.6387596130371085),
            new LatLng(51.9714515616607, 7.640390396118164),
            new LatLng(51.97028826703529, 7.639617919921874),
            new LatLng(51.969495094294324, 7.638587951660156)
    };
    int currentFakeLocation = 0;

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
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

        startStoryButton = (Button) findViewById(R.id.startStoryButton);
        nextFakeLocButton = (Button) findViewById(R.id.fakeLocationButton);

        startStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStory();
            }
        });

        nextFakeLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFakeLocation++;
                if(currentFakeLocation > fakeLocations.length){
                    return;
                }
                centerMapOnLocation(fakeLocations[currentFakeLocation]);
                checkDistanceToMarker(fakeLocations[currentFakeLocation], markerCollection[approachingMarker].getPosition());
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
                for (int i = 0; i < storyElementIds.length; i++) {
                    storyElements[i] = databaseHandler.getStoryElementByStoryElementId(storyElementIds[i]);
                }

                addStoryElementsToMap(storyElements);

                PolylineOptions storyLine = databaseHandler.getPolylineOptionsByStoryId(storyId);
                storyLine.color(Color.BLUE);
                addPolylineToMap(storyLine);
            }
        });
    }

    public void addStoryElementsToMap(StoryElement[] storyElements) {

        markerCollection = new MarkerOptions[storyElements.length];

        for (int i = 0; i < storyElements.length; i++) {
            String storyElementPoiId = storyElements[i].getPoiId();
            Poi storyElementPoi = databaseHandler.getPoiByPoiId(storyElementPoiId);
            MarkerOptions markerOptions = new MarkerOptions().position(storyElementPoi.getLocation());
            markerCollection[i] = markerOptions;
            mMap.addMarker(markerOptions);
        }
    }

    public void addPolylineToMap(PolylineOptions polylineOptions) {
        Polyline polyline = mMap.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < polyline.getPoints().size(); i++) {
            builder.include(polyline.getPoints().get(i));
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public void startStory() {
        // make "Start Story" button invisible and display "fake location" button
        startStoryButton.setVisibility(View.GONE);
        nextFakeLocButton.setVisibility(View.VISIBLE);
        approachingMarker = 0;

        // TODO: REMOVE AFTERWARDS (ONLY FOR FAKE LOCATION)
        centerMapOnLocation(fakeLocations[currentFakeLocation]);
        checkDistanceToMarker(fakeLocations[currentFakeLocation], markerCollection[approachingMarker].getPosition());

        // TODO: REMOVE COMMENTS (FOR REAL LOCATION:)
        /*
        // start as soon as connection is established
        startIfConnected = true;

        // if not yet connect inform user
        if (!connected) {
            Toast.makeText(getApplicationContext(), "Waiting for GPS", Toast.LENGTH_LONG).show();
        } else {
            Location mCurrentLocation = mLocationClient.getLastLocation();
            centerMapOnLocation(mCurrentLocation);
            startLocationListener();
        }
        */
    }

    private void startLocationListener() {
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), "Received new position", Toast.LENGTH_SHORT).show();
                centerMapOnLocation(location);
                checkDistanceToMarker(location, markerCollection[approachingMarker].getPosition());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // register listener
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
    }

    public void centerMapOnLocation(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
    }

    // TODO: REMOVE AFTERWARDS (ONLY FOR FAKE LOCATIONS)
    public void centerMapOnLocation(LatLng location) {
        LatLng latlng = new LatLng(location.latitude, location.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
    }

    public void checkDistanceToMarker(Location currentLocation, LatLng markerPosition) {
        // calculate distance
        final int earthRadius = 6371;
        float dLat = (float) Math.toRadians(markerPosition.latitude - currentLocation.getLatitude());
        float dLon = (float) Math.toRadians(markerPosition.longitude - currentLocation.getLongitude());
        float a =
                (float) (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(currentLocation.getLatitude()))
                        * Math.cos(Math.toRadians(markerPosition.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float distance = earthRadius * c;

        if (distance < ACTIVATION_DISTANCE) {
            // stop watching location as long as user is in story element
            mLocationManager.removeUpdates(mLocationListener);
            // open new activity
            openApproachingStoryElement();
        }
    }

    // TODO: REMOVE AFTERWARDS (ONLY FOR FAKE LOCATIONS)
    public void checkDistanceToMarker(LatLng currentLocation, LatLng markerPosition) {
        // calculate distance
        final int earthRadius = 6371;
        float dLat = (float) Math.toRadians(markerPosition.latitude - currentLocation.latitude);
        float dLon = (float) Math.toRadians(markerPosition.longitude - currentLocation.longitude);
        float a =
                (float) (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(currentLocation.latitude))
                        * Math.cos(Math.toRadians(markerPosition.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float distance = earthRadius * c;

        if (distance < ACTIVATION_DISTANCE) {

            //TODO REMOVE COMMENTS (FOR REAL LOCATION)
            /*
            // stop watching location as long as user is in story element
            mLocationManager.removeUpdates(mLocationListener);
            */

            // open new activity
            openApproachingStoryElement();
        }
    }

    public void openApproachingStoryElement() {
        Intent intent = new Intent(StoryLineMap.this, StoryElementActivity.class);
        intent.putExtra("story-element-id", storyElementIds[approachingMarker]);
        startActivityForResult(intent, VISIT_STORYELEMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VISIT_STORYELEMENT) {
            // marker has been visited go to next one if available
            if (markerCollection.length == approachingMarker) {
                // TODO finish story
                Toast.makeText(getApplicationContext(), "You visited all story elements", Toast.LENGTH_SHORT).show();
            } else {
                approachingMarker++;
                Toast.makeText(getApplicationContext(), "You returned to the map " + approachingMarker, Toast.LENGTH_SHORT).show();

                // TODO REMOVE COMMENTS (FOR REAL LOCATION)
                    /*
                    // start watching out for next element
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                    */
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
        Toast.makeText(getApplicationContext(), "GPS established", Toast.LENGTH_LONG).show();

        if (startIfConnected) {
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
