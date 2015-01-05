package de.ifgi.igiapp.igi_app.MongoDB;

/**
 * Created by René on 29.11.2014.
 */
public class Tag {
    private String id;
    private String name;

    public Tag(String _id, String _name){
        this.id = _id;
        this.name = _name;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
}
