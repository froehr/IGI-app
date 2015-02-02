package de.ifgi.igiapp.igi_app.SpeechRecognition;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.util.ArrayList;

import de.ifgi.igiapp.igi_app.MapInterface;
import de.ifgi.igiapp.igi_app.R;

public class SpeechInputHandler {
    Activity act;

    public SpeechInputHandler(Activity activity) {
        this.act = activity;
    }

    public void showToast(String toastString) {
        Toast toast = Toast.makeText(act, toastString, Toast.LENGTH_LONG);
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
                    firstContained = word.alternativeWords[i].toLowerCase();
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