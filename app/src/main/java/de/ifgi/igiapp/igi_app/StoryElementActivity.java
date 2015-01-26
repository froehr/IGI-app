package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;

public class StoryElementActivity extends ActionBarActivity {

    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_element);
        Intent intent = getIntent();
        String storyElementId = intent.getStringExtra("story-element-id");

        // get global database-handler
        GlobalApplication application = (GlobalApplication) getApplicationContext();
        this.databaseHandler = application.getGlobalDatabaseHandler();

        // request story element from db by id (from intent)
        StoryElement storyElement = databaseHandler.getStoryElementByStoryElementId(storyElementId);

        render(storyElement);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story_element, menu);
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

    // Fill activity with content
    public void render(StoryElement storyElement){
        TextView textTitle = (TextView) findViewById(R.id.story_element_title);
        textTitle.setText(storyElement.getName());

        //Create text view for description
        TextView textDescription = (TextView) findViewById(R.id.story_element_description);
        textDescription.setText(storyElement.getText());

        ImageView image = ((ImageView) findViewById(R.id.story_element_image));
        // set sample image
        image.setImageResource(R.drawable.prinzipalmarkt1);
    }
}
