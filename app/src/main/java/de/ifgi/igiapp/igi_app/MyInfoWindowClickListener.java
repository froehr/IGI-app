package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Tobias on 28.11.2014.
 */
public class MyInfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {
    Activity activity;

    public MyInfoWindowClickListener(Activity activity){
        this.activity = activity;
    }

    public void onInfoWindowClick(Marker marker){
        Intent intent = new Intent(this.activity, DisplayNarrativeActivity.class);
        String title = marker.getTitle();
        String description = marker.getSnippet();
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        this.activity.startActivity(intent);
    }
}
