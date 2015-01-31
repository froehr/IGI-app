package de.ifgi.igiapp.igi_app;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import de.ifgi.igiapp.igi_app.SharedPreferences.ActivityFirstLaunch;

/**
 * Created by Tobias on 19.11.2014.
 */
public class DrawerItemClickListener implements ListView.OnItemClickListener {
    private DrawerLayout mDrawerLayout;

    public DrawerItemClickListener(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        selectItem(position, view);
    }

    private void selectItem(int position, View view) {
        if(position == 0){
            Intent intent = new Intent(view.getContext(), MapsActivity.class);
            view.getContext().startActivity(intent);
        }
        if(position == 1){
            Intent intent = new Intent(view.getContext(), StoryListActivity.class);
            view.getContext().startActivity(intent);
        }
        if(position == 2){
            Intent intent = new Intent(view.getContext(), ActivityFirstLaunch.class);
            view.getContext().startActivity(intent);
        }
        if(position == 3){
            Intent intent = new Intent(view.getContext(), SpeechCommands.class);
            view.getContext().startActivity(intent);
        }
    }
}