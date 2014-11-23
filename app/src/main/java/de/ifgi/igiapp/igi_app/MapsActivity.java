package de.ifgi.igiapp.igi_app;


import android.location.Location;
import android.location.LocationProvider;
import android.provider.SyncStateContract;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements MapInterface {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int maxResults = 5;
    SpeechInputHandler speechInputHandler;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));

        //Navigation Drawer
        mPlanetTitles = getResources().getStringArray(R.array.drawer_content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Switch statement handles the clicks on the buttons of the action bar
        switch (id) {
            /*case R.id.action_locate:
                System.out.println("Location button pressed");
                //locate();
                return true;*/
            case R.id.action_settings:
                System.out.println("Settings button pressed");
                panUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        setUpMapIfNeeded();
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
                        "could not finde any results: " + location,
                        Toast.LENGTH_SHORT).show();;
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "network unavailable or any other I/O problem occurs: " + location,
                    Toast.LENGTH_SHORT).show();
        }
    }


}

