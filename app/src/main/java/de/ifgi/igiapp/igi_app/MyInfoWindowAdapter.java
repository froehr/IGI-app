package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Tobias on 20.11.2014.
 */
class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;

    MyInfoWindowAdapter(Activity activity){
        myContentsView = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoContents(Marker marker) {

        TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
        tvTitle.setText(marker.getTitle());
        //TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
        //tvSnippet.setText(marker.getSnippet());
        ImageView image = ((ImageView)myContentsView.findViewById(R.id.marker_image));
        image.setMaxWidth(600);
        image.setImageResource(R.drawable.prinzipalmarkt);

        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Auto-generated method stub
        return null;
    }

}
