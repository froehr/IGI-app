package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tobias on 28.11.2014.
 */
public class MyInfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {
    Activity activity;
    Map<String, String> markerPoiHandler = new HashMap<String, String>();

    public MyInfoWindowClickListener(Activity activity){
        this.activity = activity;
    }

    public void onInfoWindowClick(Marker marker){
        // Create intent for displaying poi with
        Intent intent = new Intent(this.activity, PoiActivity.class);
        String title = marker.getTitle();
        String description = marker.getSnippet();

        // get db-id of poi
        String poiId = markerPoiHandler.get(marker.getId());

        // add data for poi-activity
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("poi-id", poiId);
        this.activity.startActivity(intent);
    }
}