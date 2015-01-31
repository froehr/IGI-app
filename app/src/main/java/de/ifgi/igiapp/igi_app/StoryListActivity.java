package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Story;


public class StoryListActivity extends ActionBarActivity {

    private DatabaseHandler databaseHandler;
    final int STORY_MODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        GlobalApplication application = (GlobalApplication) getApplicationContext();
        databaseHandler = application.getGlobalDatabaseHandler();

        final Story stories[] = databaseHandler.getAllStories();

        final ListView listview = (ListView) findViewById(R.id.story_list);

        final ArrayList<String> list = new ArrayList<String>();
        try {
            for (int i = 0; i < stories.length; ++i) {
                list.add(stories[i].getName());
            }
        } catch (NullPointerException ex) {
            Toast.makeText(getApplicationContext(), "Wait until point are loaded from Database", Toast.LENGTH_SHORT).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String storyId = stories[position].getId();
                Intent intent = new Intent(StoryListActivity.this, StoryLineMap.class);
                intent.putExtra("story-id", storyId);
                startActivityForResult(intent, STORY_MODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == STORY_MODE){
            // do nothing
        }
    }
}
