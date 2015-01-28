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
    boolean isStoryElement;

    public MyInfoWindowClickListener(Activity activity) {
        this.activity = activity;
    }

    public void onInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        String description = marker.getSnippet();

        // decide which activity should be started
        if (isStoryElement) {
            // Create intent for displaying story element
            Intent intent = new Intent(this.activity, StoryElementActivity.class);
            intent.putExtra("title", title);
            // snippet is id
            intent.putExtra("story-element-id", description);
            this.activity.startActivity(intent);
        } else {
            // Create intent for displaying poi
            Intent intent = new Intent(this.activity, PoiActivity.class);
            // get db-id of poi
            String poiId = markerPoiHandler.get(marker.getId());

            // add data for poi-activity
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("poi-id", poiId);
            this.activity.startActivity(intent);
        }
    }
}