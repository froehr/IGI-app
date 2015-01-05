package de.ifgi.igiapp.igi_app.MongoDB;

/**
 * Created by René on 29.11.2014.
 */
public class Story {
    private String id;
    private String name;
    private String description;
    private String[] storyElementId;

    public Story(String _id, String _name, String _description, String[] _storyElementId){
        this.id = _id;
        this.name = _name;
        this.description = _description;
        this.storyElementId = _storyElementId;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public String[] getStoryElementId(){
        return this.storyElementId;
    }
}
