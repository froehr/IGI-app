package de.ifgi.igiapp.igi_app.MongoDB;

/**
 * Created by René on 29.11.2014.
 */
public class StoryElement {
    private String id;
    private String poiId;
    private String[] tagId;
    private String name;
    private String text;

    public StoryElement(String _id, String _poiId, String[] _tagId, String _name, String _text){
        this.id = _id;
        this.poiId = _poiId;
        this.tagId = _tagId;
        this.name = _name;
        this.text = _text;
    }

    public String getId(){
        return this.id;
    }

    public String getPoiId(){
        return this.poiId;
    }

    public String[] getTagId(){
        return this.tagId;
    }

    public String getName(){
        return this.name;
    }

    public String getText(){
        return this.text;
    }
}
