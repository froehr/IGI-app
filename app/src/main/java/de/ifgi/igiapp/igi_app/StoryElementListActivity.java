package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;
import de.ifgi.igiapp.igi_app.MongoDB.Tag;


public class StoryElementListActivity extends Activity {

    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_element_list);

        Intent intent = getIntent();
        String tagname = intent.getStringExtra("tag");

        setTitle("Storyelements with tag \"" + tagname + "\"");

        GlobalApplication application = (GlobalApplication) getApplicationContext();
        this.databaseHandler = application.getGlobalDatabaseHandler();

        Tag tag = databaseHandler.getTagByTagName(tagname);
        if(tag == null){
            Toast.makeText(this, "We couldn't find any story-elements with tag \"" + tagname + "\"", Toast.LENGTH_LONG).show();
            return;
        }
        final List<StoryElement> storyElements = databaseHandler.getStoryElementByTagId(tag.getId());

        final ListView listview = (ListView) findViewById(R.id.story_element_activity_list);

        final ArrayList<String> list = new ArrayList<String>();
        for (StoryElement storyElement: storyElements) {
            list.add(storyElement.getName());
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String storyElementId = storyElements.get(position).getId();
                Intent intent = new Intent(StoryElementListActivity.this, StoryElementActivity.class);
                intent.putExtra("story-element-id", storyElementId);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_story_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
