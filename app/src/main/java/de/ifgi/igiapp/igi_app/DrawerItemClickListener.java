package de.ifgi.igiapp.igi_app;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Tobias on 19.11.2014.
 */
public class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        selectItem(position, view);
    }

    private void selectItem(int position, View view) {
        if(position == 1){
            Intent intent = new Intent(view.getContext(), StoryListActivity.class);
            view.getContext().startActivity(intent);
        }
    }
}