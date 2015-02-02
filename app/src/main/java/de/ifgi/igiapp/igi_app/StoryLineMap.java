package de.ifgi.igiapp.igi_app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Poi;
import de.ifgi.igiapp.igi_app.MongoDB.Story;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;
import de.ifgi.igiapp.igi_app.SpeechRecognition.StoryElementSpeechInputHandler;
import de.ifgi.igiapp.igi_app.SpeechRecognition.StoryLineSpeechInputHandler;

public class StoryLineMap extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private String storyId;
    private DatabaseHandler databaseHandler;
    private GoogleApiClient mLocationClient;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private boolean connected = false;
    private boolean startIfConnected = false;

    Button startStoryButton;
    Button nextFakeLocButton;

    // current position marker
    Marker markerCurrentPosition;

    String[] storyElementIds;

    // request code for visiting story element
    private int VISIT_STORYELEMENT = 1;

    // line to next marker/story-element
    Polyline lineToNextElement;

    // all markers of this story
    Marker[] markerCollection;

    // array index of approaching marker
    private int approachingMarker;

    // minimum distance before story element is opened
    private final double ACTIVATION_DISTANCE = 0.15;

    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private StoryLineSpeechInputHandler speechInputHandler;

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

        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));
        MyInfoWindowClickListener infoWindowClickListener = new MyInfoWindowClickListener(this);
        infoWindowClickListener.isStoryElement = true;
        mMap.setOnInfoWindowClickListener(infoWindowClickListener);

        // location manager for updating position and calculating distances
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // for initial position
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
                if(currentFakeLocation >= fakeLocations.length){
                    Toast.makeText(getApplicationContext(), "Visited all fake locations", Toast.LENGTH_SHORT).show();
                    return;
                }
                centerMapOnLocation(fakeLocations[currentFakeLocation]);
                checkDistanceToMarker(fakeLocations[currentFakeLocation], markerCollection[approachingMarker].getPosition());
            }
        });

        speechInputHandler = new StoryLineSpeechInputHandler(this);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
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
                ArrayList<StoryElement> storyElements = new ArrayList<StoryElement>();
                for (int i = 0; i < storyElementIds.length; i++) {
                    StoryElement currentElement = databaseHandler.getStoryElementByStoryElementId(storyElementIds[i]);
                    if(currentElement != null){
                        storyElements.add(databaseHandler.getStoryElementByStoryElementId(storyElementIds[i]));
                    }
                }

                addStoryElementsToMap(storyElements);

                PolylineOptions storyLine = databaseHandler.getPolylineOptionsByStoryId(storyId);
                storyLine.color(Color.BLUE);
                addPolylineToMap(storyLine);
            }
        });
    }

    public void addStoryElementsToMap(ArrayList<StoryElement> storyElements) {

        ArrayList<MarkerOptions> markerOptions = new ArrayList<MarkerOptions>();
        markerCollection = new Marker[storyElements.size()];

        boolean firstElement = true;
        for (StoryElement storyElement: storyElements) {
            String storyElementPoiId = storyElement.getPoiId();
            Poi storyElementPoi = databaseHandler.getPoiByPoiId(storyElementPoiId);
            MarkerOptions currentMarkerOption = new MarkerOptions().position(storyElementPoi.getLocation());
            currentMarkerOption.title(storyElement.getName());
            currentMarkerOption.snippet(storyElement.getId());
            if (firstElement){
                // first element is colored green
                currentMarkerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                firstElement = false;
            } else {
                // all other are colored orange
                currentMarkerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }
            markerOptions.add(currentMarkerOption);
        }
        for(int i = 0; i < markerOptions.size(); i++){
            markerCollection[i] =  mMap.addMarker(markerOptions.get(i));
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

        if(markerCurrentPosition != null){
            markerCurrentPosition.remove();
        }

        markerCurrentPosition = mMap.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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

        // remove polyline if existing
        if (lineToNextElement != null){
            lineToNextElement.remove();
        }
        // draw new polyline to next marker
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        polylineOptions.add(markerPosition);
        polylineOptions.color(Color.GREEN);
        lineToNextElement = mMap.addPolyline(polylineOptions);

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

        // remove polyline if existing
        if (lineToNextElement != null){
            lineToNextElement.remove();
        }
        // draw new polyline to next marker
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(currentLocation);
        polylineOptions.add(markerPosition);
        polylineOptions.color(Color.GREEN);
        lineToNextElement = mMap.addPolyline(polylineOptions);

        if (distance < ACTIVATION_DISTANCE) {
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
            // for changing the icon of a marker it is necessary to delete it
            // and create an old one (due to google maps android)
            markerCollection[approachingMarker].remove();
            MarkerOptions oldMarker = new MarkerOptions()
                    .position(markerCollection[approachingMarker].getPosition())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title(markerCollection[approachingMarker].getTitle())
                    .snippet(markerCollection[approachingMarker].getSnippet());
            markerCollection[approachingMarker] = mMap.addMarker(oldMarker);

            approachingMarker++;

            // marker has been visited go to next one if available
            if (markerCollection.length == approachingMarker) {
                // TODO finish story
                Toast.makeText(getApplicationContext(), "You visited all story elements", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // for changing the icon of a marker it is necessary to delete it
                // and create an old one (due to google maps android)
                markerCollection[approachingMarker].remove();
                MarkerOptions newMarker = new MarkerOptions()
                        .position(markerCollection[approachingMarker].getPosition())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(markerCollection[approachingMarker].getTitle())
                        .snippet(markerCollection[approachingMarker].getSnippet());
                markerCollection[approachingMarker] = mMap.addMarker(newMarker);

                // TODO REMOVE COMMENTS (FOR REAL LOCATION)
                    /*
                    // start watching out for next element
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                    */
            }
        }
        else if ( requestCode == REQ_CODE_SPEECH_INPUT ) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                speechInputHandler.handleSpeech(result);
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
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
    }


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

    public void changeMapLayerToNormal() { mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); }

    public void changeMapLayerToSatellite() { mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); }

    public void changeMapLayerToHybrid() { mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); }

    public void changeMapLayerToTerrain() { mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN); }
}
