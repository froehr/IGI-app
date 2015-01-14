package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Poi;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;

public class PoiActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String poiId = intent.getStringExtra("poi-id");

        // fill poi-description
        render(title, description);

        // fill story-element list
        final ListView listview = (ListView) findViewById(R.id.story_element_list);

        // get global db-handler
        GlobalApplication globalApplication = (GlobalApplication) getApplicationContext();
        DatabaseHandler databaseHandler = globalApplication.getGlobalDatabaseHandler();

        // get story object from id
        Poi poi = databaseHandler.getPoiByPoiId(poiId);
        final List<StoryElement> storyElements = databaseHandler.getStoryElementByPoiId(poi.getId());

        // add story-element title to listview
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < storyElements.size(); ++i) {
            list.add(storyElements.get(i).getName());
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        // on story-element click
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                // open storyElementActivity
                Intent intent = new Intent(PoiActivity.this, StoryElementActivity.class);
                intent.putExtra("story-element-id", storyElements.get(position).getId());
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_narrative, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void render(String title, String description){
        // Create the text view
        TextView textTitle = (TextView) findViewById(R.id.display_narrative_title);
        textTitle.setText(title);

        // Create text view for description
        WebView textDescription = (WebView) findViewById(R.id.display_narrative_description);
        String descriptionHTML = "<html><body style=\"background-color: #EEEEEE; margin: 0px;\"><p align=\"justify\">" + description + "</p></body></html>";
        textDescription.loadData(descriptionHTML, "text/html; charset=utf-8", null);

        //Create the image view
        ImageView image = ((ImageView) findViewById(R.id.display_narrative_image));
        image.setImageResource(R.drawable.prinzipalmarkt1);
    }
}