package de.ifgi.igiapp.igi_app.MongoDB;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.ifgi.igiapp.igi_app.MapsActivity;

/**
 * Created by René on 28.11.2014.
 *
 * The DatabaseHandler is used to get access and features from the database.
 * Every time the a request to the latest features is called, the database will store them locally as variables
 * and send them additionally to the "MapsActivity" (setStories, setStoryElements, setTags, setPois)
 * This is performed asynchronous and may fail, if no internet connection is available.
 * (requestAllStories, requestAllStoryElements, requestAllTags, requestAllPois)
 *
 * Additionally it is possible to get all features from the latest request without accessing the database again.
 * Therefore no internet connection is needed (getAllStories, getAllStoryElements, getAllPois, getAllTags)
 *
 * It is recommended to call the request method once at the startup at the application. Afterwards during this
 * session the local variables can be used except changes in the DB need to be updated.
 */
public class DatabaseHandler {

    MapsActivity map;

    static String baseUrl = "https://api.mongolab.com/api/1/databases/igi-tool-db";
    static String apiKey = "jh_B49WyJhI9ZmMiZI1Yy84-wvoU5Rmu";

    String pois = "/collections/pois";
    String stories = "/collections/stories";
    String storyElements = "/collections/story-elements";
    String tags = "/collections/tags";

    Story[] allStories;
    StoryElement[] allStoryElements;
    Tag[] allTags;
    Poi[] allPois;

    public DatabaseHandler(MapsActivity _map){
        this.map = _map;
    }

    public Story[] getAllStories(){
        return allStories;
    }

    public StoryElement[] getAllStoryElements(){
        return allStoryElements;
    }

    public Tag[] getAllTags(){
        return allTags;
    }

    public Poi[] getAllPois(){
        return allPois;
    }

    /*
    Get Story by Id
    Return null if no story could be found
     */
    public Story getStoryByStoryId(String id){
        if(allStories != null) {
            for (Story story: allStories) {
                if (story.getId().equals(id)) {
                    return story;
                }
            }
        }
        return null;
    }

    /*
    Get StoryElement by Id
    Return null if no story-element could be found
     */
    public StoryElement getStoryElementByStoryElementId(String id){
        if(allStoryElements != null) {
            for (StoryElement storyElement: allStoryElements) {
                if (storyElement.getId().equals(id)) {
                    return storyElement;
                }
            }
        }
        return null;
    }

    /*
    Get Tag by Id
    Return null if no tag could be found
     */
    public Tag getTagById(String id){
        if(allTags != null) {
            for (Tag tag: allTags) {
                if (tag.getId().equals(id)) {
                    return tag;
                }
            }
        }
        return null;
    }

    /*
    Get POIs by Id
    Return null if no poi could be found
     */
    public Poi getPoiByPoiId(String id){
        if(allPois != null) {
            for (Poi poi: allPois) {
                if (poi.getId().equals(id)) {
                    return poi;
                }
            }
        }
        return null;
    }

    /*
    Get StoryElements by POI-Id
    Return null if no story-element can be found
     */
    public List<StoryElement> getStoryElementByPoiId(String id){
        List<StoryElement> matchingStoryElements = new ArrayList<StoryElement>();
        if(allStoryElements != null){
            for(StoryElement storyElement: allStoryElements){
                if(storyElement.getPoiId().equals(id)){
                    matchingStoryElements.add(storyElement);
                }
            }
            return matchingStoryElements;
        }
        return null;
    }

    /*
    Get StoryElements by POI-Id
    Return null if no story-element can be found
     */
    public List<StoryElement> getStoryElementByTagId(String id){
        List<StoryElement> matchingStoryElements = new ArrayList<StoryElement>();
        if(allStoryElements != null){
            for(StoryElement storyElement: allStoryElements){
                // we assume that each story-element has at least one tag
                for(String tagId: storyElement.getTagId()){
                    if(tagId.equals(id)){
                        matchingStoryElements.add(storyElement);
                        break;
                    }
                }
            }
            return matchingStoryElements;
        }
        return null;
    }

    /*
    Get PolylineOptions by Story Id
    Return empty PolylineOption-Object if id could not be resolved or story has no pois
     */
    public PolylineOptions getPolylineOptionsByStoryId(String id){
        PolylineOptions storyLine = new PolylineOptions();

        // get Sotry from Id
        Story story = getStoryByStoryId(id);
        // get all elements of this story
        for(String storyElementId: story.getStoryElementId()){
            StoryElement storyElement = getStoryElementByStoryElementId(storyElementId);
            String poiId = storyElement.getPoiId();
            // get poi of this story-element
            Poi poi = getPoiByPoiId(poiId);
            // add location to polyline
            storyLine.add(poi.getLocation());
        }

        return storyLine;
    }

    public Tag getTagByTagName(String name){
        for(Tag tag: allTags){
            if(name.equals(tag.getName())){
                return tag;
            }
        }
        return null;
    }

    public Story getStoryByStoryElementId(String id){
        for(Story story: getAllStories()){
            for(String storyElementId: story.getStoryElementId()){
                if (storyElementId.equals(id)){
                    return story;
                }
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void requestAllStories(){
        new HttpAsyncTask().execute(stories);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void requestAllStoryElements(){
        new HttpAsyncTask().execute(storyElements);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void requestAllTags(){
        new HttpAsyncTask().execute(tags);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void requestAllPois(){
        new HttpAsyncTask().execute(pois);
    }

    public String dbRequest(String url){

        HttpClient httpclient = new DefaultHttpClient();

        // Prepare a request object
        String fullUrl = baseUrl + url + "?apiKey=" + apiKey;
        HttpGet httpget = new HttpGet(fullUrl);

        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            // Examine the response status
            Log.i("HttpRequest", response.getStatusLine().toString());

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);
                // now you have the string representation of the HTML request
                instream.close();

                String requestType = getRequestType(url);
                result = "{\"" + requestType + "\":" + result + "}";

                return result;
            }


        } catch (Exception e) { e.printStackTrace();}

        return null;
    }

    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public String getRequestType(String request){
        Log.i("RequestType:", request);
        if(request.equals(stories)){
            return "stories";
        }else if(request.equals(pois)){
            return "pois";
        }else if(request.equals(tags)) {
            return "tags";
        } else if(request.equals(storyElements)){
            return "story-elements";
        }else{
            return "unknown";
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return dbRequest(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                Log.i("Response", result);
                String type = result.substring(result.indexOf("{") + 2, result.indexOf(":") - 1);

                Log.i("Type of result: ", type);

                if(type.equals("stories")){
                    allStories = createStoriesFromJSON(result);
                    map.setStories(allStories);

                } else if(type.equals("story-elements")){
                    allStoryElements = createStoryElementsFromJSON(result);
                    map.setStoryElements(allStoryElements);

                } else if(type.equals("pois")){
                    allPois = createPoisFromJSON(result);
                    map.setPois(allPois);

                } else if(type.equals("tags")){
                    allTags = createTagsFromJSON(result);
                    map.setTags(allTags);
                }
            }
        }
    }

    /*
    Return array of story-objects from json-array in string representation
     */
    public Story[] createStoriesFromJSON(String jsonString){
        ArrayList<Story> stories = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray storyArray = jsonObject.getJSONArray("stories");
            stories = new ArrayList<Story>();
            for(int i = 0; i < storyArray.length(); i++){
                JSONObject story = storyArray.getJSONObject(i);
                String id = story.getString("_id");
                String name = story.getString("name");
                String description = story.getString("description");
                JSONArray jsonStoryElements = story.getJSONArray("story_element_id");
                if(jsonStoryElements != null){
                    String storyElements[] = new String[jsonStoryElements.length()];
                    for(int j = 0; j < jsonStoryElements.length(); j++){
                        storyElements[j] = jsonStoryElements.getString(j);
                    }
                    stories.add(new Story(id, name, description, storyElements));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Story[] storiesArray = new Story[stories.size()];
        for(int i = 0; i < stories.size(); i++){
            storiesArray[i] = stories.get(i);
        }
        return storiesArray;
    }

    public StoryElement[] createStoryElementsFromJSON(String jsonString){
        StoryElement[] storyElements = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray storyElementsArray = jsonObject.getJSONArray("story-elements");
            storyElements = new StoryElement[storyElementsArray.length()];
            for(int i = 0; i < storyElementsArray.length(); i++){
                JSONObject storyElement = storyElementsArray.getJSONObject(i);

                String id = storyElement.getString("_id");
                String name = storyElement.getString("name");
                String text = storyElement.getString("text");
                String poiId = storyElement.getString("poi_id");

                String tags[];
                try {
                    JSONArray jsonTagIds = storyElement.getJSONArray("tag_id");
                    tags = new String[jsonTagIds.length()];
                    for (int j = 0; j < jsonTagIds.length(); j++) {
                        tags[j] = jsonTagIds.getString(j);
                    }
                } catch (JSONException e){
                    tags = new String[1];
                    tags[0] = storyElement.getString("tag_id");
                }
                storyElements[i] = new StoryElement(id, poiId, tags, name, text);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return storyElements;
    }

    public Poi[] createPoisFromJSON(String jsonString){
        Poi[] pois = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray poiArray = jsonObject.getJSONArray("pois");
            pois = new Poi[poiArray.length()];
            for(int i = 0; i < poiArray.length(); i++){
                JSONObject poi = poiArray.getJSONObject(i);

                String id = poi.getString("_id");
                String name = poi.getString("name");
                String description = poi.getString("description");

                JSONObject jsonLocation = poi.getJSONObject("location");
                JSONArray jsonCoordinates = jsonLocation.getJSONArray("coordinates");
                LatLng location = new LatLng(jsonCoordinates.getDouble(0), jsonCoordinates.getDouble(1));

                pois[i] = new Poi(id, name, description, location);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pois;
    }

    public Tag[] createTagsFromJSON(String jsonString){
        Tag[] tags = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray tagArray = jsonObject.getJSONArray("tags");
            tags = new Tag[tagArray.length()];
            for(int i = 0; i < tagArray.length(); i++){
                JSONObject tag = tagArray.getJSONObject(i);

                String id = tag.getString("_id");
                String name = tag.getString("name");

                tags[i] = new Tag(id, name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tags;
    }
}
