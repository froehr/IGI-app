package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Story;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;

public class StoryElementActivity extends Activity {

    DatabaseHandler databaseHandler;
    Button nextStoryElementButton;
    Button previousStoryElementButton;
    Button backToMapButton;

    String nextStoryElementId;
    String previousStoryElementId;

    int positionInStory = -1;
    // index of story element from which this acivity was started
    int originPosition;

    // result code for returning to map
    int BACK_TO_MAP = 55;
    // request code for switching story
    int SWITCH_ELEMENT = 66;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_element);
        Intent intent = getIntent();
        String storyElementId = intent.getStringExtra("story-element-id");
        originPosition = intent.getIntExtra("origin-position", -1);

        // get global database-handler
        GlobalApplication application = (GlobalApplication) getApplicationContext();
        this.databaseHandler = application.getGlobalDatabaseHandler();

        // request story element from db by id (from intent)
        StoryElement storyElement = databaseHandler.getStoryElementByStoryElementId(storyElementId);

        // request story from db by story element id and look for index in story
        Story story = databaseHandler.getStoryByStoryElementId(storyElementId);
        String[] allIds = story.getStoryElementId();
        for (int i = 0; i < allIds.length; i++) {
            if (allIds[i].equals(storyElementId)) {
                positionInStory = i;
                break;
            }
        }

        render(storyElement);

        nextStoryElementButton = (Button) findViewById(R.id.nextStoryButton);
        previousStoryElementButton = (Button) findViewById(R.id.previousStoryButton);
        backToMapButton = (Button) findViewById(R.id.backToMapButton);

        // check if "previous" button is necessary and implement functionality
        if (positionInStory < 1) {
            previousStoryElementButton.setVisibility(View.GONE);
        } else {
            previousStoryElementId = allIds[positionInStory-1];
            previousStoryElementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPreviousStory();
                }
            });
        }

        // check if "next" button is necessary and implement functionality
        if (positionInStory == allIds.length - 1) {
            nextStoryElementButton.setVisibility(View.GONE);
        } else {
            nextStoryElementId = allIds[positionInStory + 1];
            nextStoryElementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToNextStory();
                }
            });
        }

        backToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToMapStory();
            }
        });

    }

    /**
     * Interface can be used by gesture recognition
     */
    public void goToPreviousStory() {
        // check again for gesture recognition
        if (previousStoryElementId != null) {
            // check if the activity which started this activity corresponds to the element we want to open now
            if (originPosition == positionInStory -1) {
                finish();
            } else {
                Intent intent = new Intent(StoryElementActivity.this, StoryElementActivity.class);
                intent.putExtra("story-element-id", previousStoryElementId);
                intent.putExtra("origin-position", positionInStory);
                startActivityForResult(intent, SWITCH_ELEMENT);
            }
        }
    }

    /**
     * Interface can be used by gesture recognition
     */
    public void goToNextStory() {
        if (nextStoryElementId != null) {
            // check if the activity which started this activity corresponds to the element we want to open now
            if (originPosition == positionInStory + 1) {
                finish();
            } else {
                Intent intent = new Intent(StoryElementActivity.this, StoryElementActivity.class);
                intent.putExtra("story-element-id", nextStoryElementId);
                intent.putExtra("origin-position", positionInStory);
                startActivityForResult(intent, SWITCH_ELEMENT);
            }
        }
    }

    /**
     * Interface can be used by gesture recognition
     */
    public void goBackToMapStory() {
        setResult(BACK_TO_MAP);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == BACK_TO_MAP){
            // handle down
            setResult(BACK_TO_MAP);
            finish();
        }
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
    public void render(StoryElement storyElement) {
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
