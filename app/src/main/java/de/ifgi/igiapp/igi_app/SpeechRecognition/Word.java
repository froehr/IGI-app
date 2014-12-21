package de.ifgi.igiapp.igi_app.SpeechRecognition;

public class Word {
    public String word;
    public String[] alternativeWords;

    public Word(String word, String[] alternativeWords) {
        this.word = word;
        this.alternativeWords = alternativeWords;
    }
}