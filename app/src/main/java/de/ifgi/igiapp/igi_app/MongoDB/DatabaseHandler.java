package de.ifgi.igiapp.igi_app.MongoDB;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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

import de.ifgi.igiapp.igi_app.MapsActivity;

/**
 * Created by René on 28.11.2014.
 */
public class DatabaseHandler {

    MapsActivity map;

    static String baseUrl = "https://api.mongolab.com/api/1/databases/igi-tool-db";
    static String apiKey = "2Q1SmomE3Hihh_MqC4nshAwWRowZSeiT";

    String pois = "/collections/pois";
    String stories = "/collections/stories";
    String storyElements = "/collections/story-elements";
    String tags = "/collections/tags";

    public DatabaseHandler(MapsActivity _map){
        this.map = _map;
    }

    public void getAllStories(){
        new HttpAsyncTask().execute(stories);
    }

    public void getAllStoryElements(){
        new HttpAsyncTask().execute(storyElements);
    }

    public void getAllTags(){
        new HttpAsyncTask().execute(tags);
    }

    public void getAllPois(){
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
                Log.i("HttpRequest", result);

                if(requestType.equals("stories")){
                    Story[] stories = createStoriesFromJSON(result);
                    map.setStories(stories);

                } else if(requestType.equals("story-elements")){
                    StoryElement[] storyElements = createStoryElementsFromJSON(result);
                    map.setStoryElements(storyElements);

                } else if(requestType.equals("pois")){
                    Poi[] pois = createPoisFromJSON(result);
                    map.setPois(pois);

                } else if(requestType.equals("tags")){
                    Tag[] tags = createTagsFromJSON(result);
                    map.setTags(tags);
                }

                return result;
            }


        } catch (Exception e) {}

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

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
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
            }
        }
    }

    /*
    Return array of story-objects from json-array in string representation
     */
    public Story[] createStoriesFromJSON(String jsonString){
        Story[] stories = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray storyArray = jsonObject.getJSONArray("stories");
            stories = new Story[storyArray.length()];
            for(int i = 0; i < storyArray.length(); i++){
                JSONObject story = storyArray.getJSONObject(i);
                String id = story.getString("_id");
                String name = story.getString("name");
                String description = story.getString("description");
                JSONArray jsonStoryElements = story.getJSONArray("story_element_id");
                String storyElements[] = new String[jsonStoryElements.length()];
                for(int j = 0; j < jsonStoryElements.length(); j++){
                    storyElements[j] = jsonStoryElements.getString(j);
                }
                stories[i] = new Story(id, name, description, storyElements);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stories;
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
                    tags[1] = storyElement.getString("tag_id");
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
