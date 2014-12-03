package de.ifgi.igiapp.igi_app;

/**
 * Created by René on 15.11.2014.
 */
public interface MapInterface {

    public void zoomIn();

    public void zoomOut();

    public void panUp();

    public void panDown();

    public void panRight();

    public void panLeft();

    public void openDrawer();

    public void closeDrawer();

    public void searchLocation(String location);
}
