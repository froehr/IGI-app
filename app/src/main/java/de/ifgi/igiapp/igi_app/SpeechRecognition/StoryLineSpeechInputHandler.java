package de.ifgi.igiapp.igi_app.SpeechRecognition;

import android.app.Activity;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.R;
import de.ifgi.igiapp.igi_app.StoryElementActivity;
import de.ifgi.igiapp.igi_app.StoryLineMap;

public class StoryLineSpeechInputHandler extends SpeechInputHandler implements SpeechHandler {
    public StoryLineSpeechInputHandler(Activity activity) {
        super(activity);
    }

    public void handleSpeech(ArrayList<String> results) {
        String command = results.get(0);
        Dictionary dict = new Dictionary();
        StoryLineMap slm = (StoryLineMap) act;

        String toastString = ""; //command + " --> ";

        if ( parseCommand(command, dict.commandStartStory) ) {
            toastString += act.getString(R.string.speech_command_start_story_short);
            slm.startStory();
        }
        else if ( parseCommand(command, dict.commandZoomIn) ) {
            toastString += act.getString(R.string.speech_command_zoom_in);
            slm.zoomIn();
        }
        else if ( parseCommand(command, dict.commandZoomOut) ) {
            toastString += act.getString(R.string.speech_command_zoom_out);
            slm.zoomOut();
        }
        else if ( parseCommand(command, dict.commandPanLeft) ) {
            toastString += act.getString(R.string.speech_command_pan_left);
            slm.panLeft();
        }
        else if ( parseCommand(command, dict.commandPanRight) ) {
            toastString += act.getString(R.string.speech_command_pan_right);
            slm.panRight();
        }
        else if ( parseCommand(command, dict.commandPanUp) ) {
            toastString += act.getString(R.string.speech_command_pan_up);
            slm.panUp();
        }
        else if ( parseCommand(command, dict.commandPanDown) ) {
            toastString += act.getString(R.string.speech_command_pan_down);
            slm.panDown();
        }
        else if ( parseCommand(command, dict.commandBasicMap) ) {
            toastString += act.getString(R.string.speech_command_basic_map);
            slm.changeMapLayerToNormal();
        }
        else if ( parseCommand(command, dict.commandSatelliteMap) ) {
            toastString += act.getString(R.string.speech_command_satellite_map);
            slm.changeMapLayerToSatellite();
        }
        else if ( parseCommand(command, dict.commandHybridMap) ) {
            toastString += act.getString(R.string.speech_command_hybrid_map);
            slm.changeMapLayerToHybrid();
        }
        else if ( parseCommand(command, dict.commandTerrainMap) ) {
            toastString += act.getString(R.string.speech_command_terrain_map);
            slm.changeMapLayerToTerrain();
        }
        else {
            toastString += act.getString(R.string.speech_command_error);
        }

        showToast(toastString);
    }
}
