package de.ifgi.igiapp.igi_app;

import android.app.Application;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;

/**
 * Created by René on 14.12.2014.
 */
public class GlobalApplication extends Application {

    private DatabaseHandler globalDatabaseHandler;

    public void setGlobalDatabaseHandler(DatabaseHandler _databaseHandler){
        this.globalDatabaseHandler = _databaseHandler;
    }

    public DatabaseHandler getGlobalDatabaseHandler(){
        return this.globalDatabaseHandler;
    }
}
