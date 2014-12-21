package de.ifgi.igiapp.igi_app.SpeechRecognition;

import de.ifgi.igiapp.igi_app.SpeechRecognition.Word;

public class Dictionary {
    public Word pan = new Word("pan", new String[] {"turn", "that", "then", "time", "pen", "ten", "phan", "been", "gran", "10", "pin", "in", "penn", "hang", "depend", "can", "I'm", "send", "cond"});
    public Word zoom = new Word("zoom", new String[] {"lum", "hum", "sum", "newm", "who", "you", "bloom", "whom", "bowm", "blue", "rem", "xoom", "doom", "June"});

    public Word[] commandZoomIn = new Word[] {
            zoom,
            new Word("in", new String[] {"en", "an"})
    };

    public Word[] commandZoomOut = new Word[] {
            zoom,
            new Word("out", new String[] {"mode", "ote", "berg", "old", "ode"})
    };

    public Word[] commandPanLeft = new Word[] {
            pan,
            new Word("left", new String[] {})
    };

    public Word[] commandPanRight = new Word[] {
            pan,
            new Word("right", new String[] {"what", "droid", "ride", "fried", "drunk", "rod", "run", "front", "wide"})
    };

    public Word[] commandPanUp = new Word[] {
            pan,
            new Word("up", new String[] {"top", "tip"})
    };

    public Word[] commandPanDown = new Word[] {
            pan,
            new Word("down", new String[] {"on", "bot", "bottom", "one"})
    };

    public Word[] commandMoveTo = new Word[] {
            new Word("move", new String[] {}),
            new Word("to", new String[] {"2", "tube"})
    };

    public Word[] commandCenterAt = new Word[] {
            new Word("center", new String[] {"centre", "santa"}),
            new Word("at", new String[] {"of"})
    };

    public Word[] commandOpenMenu = new Word[] {
            new Word("open", new String[] {}),
            new Word("menu", new String[] {})
    };

    public Word[] commandShowLocation = new Word[] {
            new Word("show", new String[] {}),
            new Word("my", new String[] {}),
            new Word("location", new String[] {})
    };

    public Word[] commandSearchStoriesByTag = new Word[] {
            new Word("stories", new String[] {}),
            new Word("with", new String[] {}),
            new Word("tag", new String[] {})
    };
}