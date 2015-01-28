package de.ifgi.igiapp.igi_app.SpeechRecognition;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MapInterface;
import de.ifgi.igiapp.igi_app.R;

public class SpeechInputHandler {
    MapInterface map;

    public SpeechInputHandler(MapInterface mapActivity) {
        this.map = mapActivity;
    }

    public void handleSpeech(ArrayList<String> results) {
        String command = results.get(0);
        Dictionary dict = new Dictionary();
        ActionBarActivity act = (ActionBarActivity) map;

        String toastString = ""; //command + " --> ";

        if ( parseCommand(command, dict.commandZoomIn) ) {
            toastString += act.getString(R.string.speech_command_zoom_in);
            map.zoomIn();
        }
        else if ( parseCommand(command, dict.commandZoomOut) ) {
            toastString += act.getString(R.string.speech_command_zoom_out);
            map.zoomOut();
        }
        else if ( parseCommand(command, dict.commandPanLeft) ) {
            toastString += act.getString(R.string.speech_command_pan_left);
            map.panLeft();
        }
        else if ( parseCommand(command, dict.commandPanRight) ) {
            toastString += act.getString(R.string.speech_command_pan_right);
            map.panRight();
        }
        else if ( parseCommand(command, dict.commandPanUp) ) {
            toastString += act.getString(R.string.speech_command_pan_up);
            map.panUp();
        }
        else if ( parseCommand(command, dict.commandPanDown) ) {
            toastString += act.getString(R.string.speech_command_pan_down);
            map.panDown();
        }

        else if ( parseCommand(command, dict.commandBasicMap) ) {
            toastString += "change to basic map";
            map.changeMapLayerToNormal();
        }

        else if ( parseCommand(command, dict.commandSatelliteMap) ) {
            toastString += "change to satellite map";
            map.changeMapLayerToSatellite();
        }

        else if ( parseCommand(command, dict.commandHybridMap) ) {
            toastString += "change to hybrid map";
            map.changeMapLayerToHybrid();
        }

        else if ( parseCommand(command, dict.commandTerrainMap) ) {
            toastString += "change to terrain map";
            map.changeMapLayerToTerrain();
        }

        else if ( parseCommand(command, dict.commandMoveTo) ||
                parseCommand(command, dict.commandCenterAt) ||
                parseCommand(command, dict.commandFind)
                ) {
            String firstContained = " ";
            if ( parseCommand(command, dict.commandMoveTo) ) {
                toastString += act.getString(R.string.speech_command_move_to);
                firstContained = firstContained(command, dict.commandMoveTo[1]);
            }
            else if ( parseCommand(command, dict.commandCenterAt) ) {
                toastString += act.getString(R.string.speech_command_center_at);
                firstContained = firstContained(command, dict.commandCenterAt[1]);
            }
            else if ( parseCommand(command, dict.commandFind) ) {
                toastString += act.getString(R.string.speech_command_find);
                firstContained = firstContained(command, dict.commandFind[0]);
            }

            String location = command.substring(command.lastIndexOf(firstContained) + firstContained.length());
            toastString += " " + location + ".";
            map.searchLocation(location);
        }
        else if ( parseCommand(command, dict.commandOpenMenu) ) {
            toastString += act.getString(R.string.speech_command_open_menu);
            map.openDrawer();
        }
        else if ( parseCommand(command, dict.commandShowLocation) ) {
            if ( map.centerAtCurrentLocation() ) {
                toastString += act.getString(R.string.speech_command_show_location);
            }
            else {
                toastString += act.getString(R.string.enable_localization);
            }
        }
        else if ( parseCommand(command, dict.commandSearchStoryElementsByTag) ) {
            String firstContained = firstContained(command, dict.commandSearchStoryElementsByTag[3]);
            String tag = command.substring(command.lastIndexOf(firstContained) + firstContained.length()).toLowerCase();
            toastString += act.getString(R.string.speech_command_search_story_elements_by_tag) + " \"" + tag + "\".";
            map.searchStoryElementsByTag(tag);
        }
        else if ( parseCommand(command, dict.commandShowStories) ) {
            toastString += act.getString(R.string.speech_command_show_stories);
            map.showStories();
        }
        else if ( parseCommand(command, dict.commandStartStory) ) {
            String firstContained = firstContained(command, dict.commandStartStory[1]);
            String storyName = command.substring(command.lastIndexOf(firstContained) + firstContained.length());
            toastString += act.getString(R.string.speech_command_start_story) + " \"" + storyName + "\".";
            map.startStory(storyName);
        }
        else {
            toastString += act.getString(R.string.speech_command_error);
        }

        Toast toast = Toast.makeText((FragmentActivity) map, toastString, Toast.LENGTH_LONG);
        toast.show();
    }

    public String firstContained(String command, Word word) {
        String firstContained = "";

        if ( command.toLowerCase().contains(word.word.toLowerCase()) ) {
            firstContained = word.word.toLowerCase();
        }
        else {
            for (int i = 0; i < word.alternativeWords.length; i++) {
                if (command.toLowerCase().contains(word.alternativeWords[i].toLowerCase())) {
                    firstContained = word.alternativeWords[i];
                    break;
                }
            }
        }

        return firstContained + " ";
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
                    break;
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
}