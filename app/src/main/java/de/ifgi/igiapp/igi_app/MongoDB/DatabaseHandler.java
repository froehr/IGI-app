package de.ifgi.igiapp.igi_app.MongoDB;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by René on 28.11.2014.
 */
public class DatabaseHandler {

    static String baseUrl = "https://api.mongolab.com/api/1/databases/igi-tool-db";
    static String apiKey = "2Q1SmomE3Hihh_MqC4nshAwWRowZSeiT";

    String pois = "/collections/pois";
    String stories = "/collections/stories";
    String tags = "/collections/tags";

    public void getAllStories(){
        new HttpAsyncTask().execute(stories);
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

                result = "{\"" + getRequestType(url) + "\":" + result + "}";

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
        }else if(request.equals(tags)){
            return "tags";
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
            Log.i("Response", result);
        }
    }
}
