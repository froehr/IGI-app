package de.ifgi.igiapp.igi_app.MongoDB;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by René on 29.11.2014.
 */
public class Poi {
    private String id;
    private String name;
    private String description;
    private LatLng location;

    public Poi(String _id, String _name, String _description, LatLng _location){
        this.id = _id;
        this.name = _name;
        this.description = _description;
        this.location = _location;
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

    public LatLng getLocation(){
        return this.location;
    }
}
