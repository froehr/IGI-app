package de.ifgi.igiapp.igi_app;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechInputHandler {
    MapInterface map;

    protected static final String TAG = "SpeechInputHandler";

    public SpeechInputHandler(MapInterface mapActivity) {
        this.map = mapActivity;
    }

    public void handleSpeech(ArrayList<String> results) {
        String command = results.get(0);
        Log.i(TAG, results.get(0));

        Toast toast = Toast.makeText((FragmentActivity) map, results.get(0), Toast.LENGTH_LONG);
        toast.show();

        Word pan = new Word("pan", new String[] {"turn", "move", "that", "then", "time", "pen", "ten", "an", "phan", "been", "gran", "10", "pin", "in", "penn", "hang", "depend", "can", "I'm", "send"});

        Word[] commandZoomIn = new Word[] {
                new Word("zoom", new String[] {"lum", "hum", "sum", "newm", "who", "you", "bloom", "whom"}),
                new Word("in", new String[] {"en", "an"})
        };

        Word[] commandZoomOut = new Word[] {
                new Word("zoom", new String[] {"blue", "rem", "who", "you", "bloom", "whom"}),
                new Word("out", new String[] {"mode", "ote", "berg", "old"})
        };

        Word[] commandPanLeft = new Word[] {
                pan,
                new Word("left", new String[] {})
        };

        Word[] commandPanRight = new Word[] {
                pan,
                new Word("right", new String[] {"what", "droid", "ride", "fried", "drunk", "rod", "run", "front", "wide"})
        };

        Word[] commandPanUp = new Word[] {
                pan,
                new Word("up", new String[] {"top", })
        };

        Word[] commandPanDown = new Word[] {
                pan,
                new Word("down", new String[] {"on", "bot", "bottom"})
        };

        if ( parseCommand(command, commandZoomIn) ) {
            map.zoomIn();
        }
        else if ( parseCommand(command, commandZoomOut) ) {
            map.zoomOut();
        }
        else if ( parseCommand(command, commandPanLeft) ) {
            map.panLeft();
        }
        else if ( parseCommand(command, commandPanRight) ) {
            map.panRight();
        }
        else if ( parseCommand(command, commandPanUp) ) {
            map.panUp();
        }
        else if ( parseCommand(command, commandPanDown) ) {
            map.panDown();
        }
    }

    public boolean parseCommand(String command, Word[] words) {
        int containedWords = 0;

        for ( int i = 0; i < words.length; i++ ) {
            if ( command.toLowerCase().contains(words[i].word.toLowerCase()) ) {
                containedWords++;
                continue;
            }

            for ( int j = 0; j < words[i].alternativeWords.length; j++ ) {
                if ( command.toLowerCase().contains(words[i].alternativeWords[j].toLowerCase()) ) {
                    containedWords++;
                    continue;
                }
            }
        }

        if ( containedWords == words.length ) {
            return true;
        }
        else {
            return false;
        }
    }

    private class Word {
        public String word;
        public String[] alternativeWords;

        public Word(String word, String[] alternativeWords) {
            this.word = word;
            this.alternativeWords = alternativeWords;
        }
    }
}