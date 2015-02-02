package de.ifgi.igiapp.igi_app.SpeechRecognition;

public class Dictionary {
    public Word pan = new Word("pan", new String[] {"turn", "that", "then", "time", "pen", "ten", "phan", "been", "gran", "10", "pin", "in", "penn", "hang", "depend", "can", "I'm", "send", "cond"});
    public Word zoom = new Word("zoom", new String[] {"lum", "hum", "sum", "newm", "who", "you", "bloom", "whom", "bowm", "blue", "rem", "xoom", "doom", "june", "zuma"});
    public Word change = new Word("change to", new String[] {"change 2"});
    public Word map = new Word("map", new String[] {"mep", "met", "my"});
    public Word start = new Word("start", new String[] {"stalled", "stop", "third", "started", "stock", "about", "sob", "dog", "top"});
    public Word story = new Word("story", new String[] {"storage", "stories"});

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
            new Word("right", new String[] {"what", "droid", "ride", "rite", "fried", "drunk", "rod", "run", "front", "wide", "write"})
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

    public Word[] commandFind = new Word[] {
            new Word("find", new String[] {})
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

    public Word[] commandSearchStoryElementsByTag = new Word[] {
            new Word("story", new String[] {"stories"}),
            new Word("elements", new String[] {"element"}),
            new Word("with", new String[] {"was", "pissed"}),
            new Word("tag", new String[] {"tax", "pak", "packed", "peg", "pic", "tech", "take", "text", "tech", "eric"})
    };

    public Word[] commandShowStories = new Word[] {
            new Word("show", new String[] {"short", "true", "view", "open", "new", "zoo", "do you", "you"}),
            new Word("stories", new String[] {"story"})
    };

    public Word[] commandStartStory = new Word[] {
            start,
            story
    };

    public Word[] commandBasicMap = new Word[] {
            change,
            new Word("basic", new String[] {}),
            map
    };

    public Word[] commandSatelliteMap = new Word[] {
            change,
            new Word("satellite", new String[] {"setalite", "settle art", "set alight"}),
            map
    };

    public Word[] commandHybridMap = new Word[] {
            change,
            new Word("hybrid", new String[] {"high bread"}),
            map
    };

    public Word[] commandTerrainMap = new Word[] {
            change,
            new Word("terrain", new String[] {"rain"}),
            map
    };

    public Word[] commandPrevious = new Word[] {
            new Word("previous", new String[] {})
    };

    public Word[] commandBackToMap = new Word[] {
            new Word("back", new String[] {}),
            new Word("to", new String[] {}),
            map
    };

    public Word[] commandNext = new Word[] {
            new Word("next", new String[] {})
    };
}