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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Story;
import de.ifgi.igiapp.igi_app.SpeechRecognition.StoryLineSpeechInputHandler;
import de.ifgi.igiapp.igi_app.SpeechRecognition.StoryListSpeechInputHandler;


public class StoryListActivity extends ActionBarActivity {

    private DatabaseHandler databaseHandler;
    final int STORY_MODE = 1;

    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private StoryListSpeechInputHandler speechInputHandler;

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
            Toast.makeText(getApplicationContext(), "Please wait until the points are loaded from our database and try again after.", Toast.LENGTH_SHORT).show();
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

        speechInputHandler = new StoryListSpeechInputHandler(this);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    public void startStory(String storyName) {
        Story[] stories = databaseHandler.getAllStories();

        for ( int i = 0; i < stories.length; i++ ) {
            if ( stories[i].getName().toLowerCase().equals(storyName.toLowerCase()) ) {
                String storyId = stories[i].getId();
                Intent intent = new Intent(StoryListActivity.this, StoryLineMap.class);
                intent.putExtra("story-id", storyId);
                startActivity(intent);
                return;
            }
        }
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
        if (requestCode == STORY_MODE){
            // do nothing
        }
        else if ( requestCode == REQ_CODE_SPEECH_INPUT ) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                speechInputHandler.handleSpeech(result);
            }
        }
    }
}
