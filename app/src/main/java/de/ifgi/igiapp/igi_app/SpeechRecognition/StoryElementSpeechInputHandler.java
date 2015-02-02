package de.ifgi.igiapp.igi_app.SpeechRecognition;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MapInterface;
import de.ifgi.igiapp.igi_app.R;
import de.ifgi.igiapp.igi_app.StoryElementActivity;

public class StoryElementSpeechInputHandler extends SpeechInputHandler implements SpeechHandler {
    public StoryElementSpeechInputHandler(Activity activity) {
        super(activity);
    }

    public void handleSpeech(ArrayList<String> results) {
        String command = results.get(0);
        Dictionary dict = new Dictionary();
        StoryElementActivity sea = (StoryElementActivity) act;

        String toastString = ""; //command + " --> ";

        if ( parseCommand(command, dict.commandPrevious) ) {
            toastString += act.getString(R.string.speech_command_previous);
            sea.goToPreviousStory();
        }
        else if ( parseCommand(command, dict.commandNext) ) {
            toastString += act.getString(R.string.speech_command_next);
            sea.goToNextStory();
        }
        else if ( parseCommand(command, dict.commandBackToMap) ) {
            toastString += act.getString(R.string.speech_command_back_to_map);
            sea.goBackToMapStory();
        }
        else {
            toastString += act.getString(R.string.speech_command_error);
        }

        showToast(toastString);
    }
}
