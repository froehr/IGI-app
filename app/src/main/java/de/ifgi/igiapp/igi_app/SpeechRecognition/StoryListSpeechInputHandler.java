package de.ifgi.igiapp.igi_app.SpeechRecognition;

import android.app.Activity;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.R;
import de.ifgi.igiapp.igi_app.StoryElementActivity;
import de.ifgi.igiapp.igi_app.StoryListActivity;

public class StoryListSpeechInputHandler extends SpeechInputHandler implements SpeechHandler {
    public StoryListSpeechInputHandler(Activity activity) {
        super(activity);
    }

    public void handleSpeech(ArrayList<String> results) {
        String command = results.get(0);
        Dictionary dict = new Dictionary();
        StoryListActivity sla = (StoryListActivity) act;

        String toastString = ""; //command + " --> ";

        sla.startStory(command);
    }
}
