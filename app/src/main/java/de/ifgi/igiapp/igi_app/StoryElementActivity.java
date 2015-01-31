package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Story;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;
import de.ifgi.igiapp.igi_app.SpeechRecognition.SpeechInputHandler;
import de.ifgi.igiapp.igi_app.SpeechRecognition.StoryElementSpeechInputHandler;

public class StoryElementActivity extends ActionBarActivity {

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
    private final int BACK_TO_MAP = 55;
    // request code for switching story
    private final int SWITCH_ELEMENT = 66;

    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private StoryElementSpeechInputHandler speechInputHandler;

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

        speechInputHandler = new StoryElementSpeechInputHandler(this);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechInputHandler.handleSpeech(result);
                }
                break;
            }
            case BACK_TO_MAP:
                setResult(BACK_TO_MAP);
                finish();
                break;
        }
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

    // Fill activity with content
    public void render(StoryElement storyElement) {
        TextView textTitle = (TextView) findViewById(R.id.story_element_title);
        textTitle.setText(storyElement.getName());

        // Create text view for description
        WebView textDescription = (WebView) findViewById(R.id.story_element_description);
        String descriptionHTML = "<html><body style=\"background-color: #EEEEEE; margin: 0px;\"><p align=\"justify\">" + storyElement.getText() + "</p></body></html>";
        textDescription.loadData(descriptionHTML, "text/html; charset=utf-8", null);

        ImageView image = ((ImageView) findViewById(R.id.story_element_image));
        // set sample image
        image.setImageResource(R.drawable.prinzipalmarkt1);
    }
}
