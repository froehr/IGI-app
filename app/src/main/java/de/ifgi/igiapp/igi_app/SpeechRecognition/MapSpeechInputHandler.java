package de.ifgi.igiapp.igi_app.SpeechRecognition;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MapInterface;
import de.ifgi.igiapp.igi_app.R;

public class MapSpeechInputHandler extends SpeechInputHandler implements SpeechHandler {
    public MapSpeechInputHandler(Activity activity) {
        super(activity);
    }

    public void handleSpeech(ArrayList<String> results) {
        String command = results.get(0);
        Dictionary dict = new Dictionary();
        MapInterface map = (MapInterface) act;

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
            toastString += act.getString(R.string.speech_command_basic_map);
            map.changeMapLayerToNormal();
        }

        else if ( parseCommand(command, dict.commandSatelliteMap) ) {
            toastString += act.getString(R.string.speech_command_satellite_map);
            map.changeMapLayerToSatellite();
        }

        else if ( parseCommand(command, dict.commandHybridMap) ) {
            toastString += act.getString(R.string.speech_command_hybrid_map);
            map.changeMapLayerToHybrid();
        }

        else if ( parseCommand(command, dict.commandTerrainMap) ) {
            toastString += act.getString(R.string.speech_command_terrain_map);
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
            String tag = command.substring(command.toLowerCase().lastIndexOf(firstContained) + firstContained.length()).toLowerCase();
            toastString += act.getString(R.string.speech_command_search_story_elements_by_tag) + " \"" + tag + "\".";
            map.searchStoryElementsByTag(tag);
        }
        else if ( parseCommand(command, dict.commandShowStories) ) {
            toastString += act.getString(R.string.speech_command_show_stories);
            map.showStories();
        }
        else if ( parseCommand(command, dict.commandStartStory) ) {
            String firstContained = firstContained(command, dict.commandStartStory[1]);
            String storyName = command.substring(command.toLowerCase().lastIndexOf(firstContained) + firstContained.length());
            toastString += act.getString(R.string.speech_command_start_story) + " \"" + storyName + "\".";
            map.startStory(storyName);
        }
        else {
            toastString += act.getString(R.string.speech_command_error);
        }

        showToast(toastString);
    }
}
