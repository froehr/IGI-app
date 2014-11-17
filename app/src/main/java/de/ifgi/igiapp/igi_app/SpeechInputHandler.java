package de.ifgi.igiapp.igi_app;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by René on 15.11.2014.
 */
public class SpeechInputHandler {

    MapInterface map;
    protected static final String TAG = "SpeechInputHandler";

    public SpeechInputHandler(MapInterface mapActivity){
        this.map = mapActivity;
    }

    public void handleSpeech(ArrayList<String> results){
        String command = results.get(0);
        Log.i(TAG, results.get(0));

        //TODO: More sophisticated handling (language parser?)
        if(command.contains("zoom") && command.contains("in")){
            map.zoomIn();
        } else if(command.contains("zoom") && command.contains("out")){
            map.zoomOut();
        } else if(command.contains("pan") && command.contains("left")){
            map.panLeft();
        } else if(command.contains("pan") && command.contains("right")){
            map.panRight();
        } else if(command.contains("pan") && command.contains("up")){
            map.panUp();
        } else if(command.contains("pan") && command.contains("down")){
            map.panDown();
        }
    }


}
