package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Tobias on 27.11.2014.
 */
public class HttpAsyncTask extends AsyncTask<String, Void, String> {
    public String data;
    @Override
    protected String doInBackground(String... urls) {

        return GET(urls[0]);
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //Context context = getApplicationContext();
        //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
        //getResponse.setText(result);
        this.data = result;
        //System.out.println("Content received! this.data: " + this.data);
        //System.out.println("Content received! getData(): " + getData());
        System.out.println("Content received! result: " + result);
        try {
            JSONArray json = new JSONArray(result);
            MapsActivity.drawMarkers(json);
        } catch (JSONException e){
            System.out.print("+++ Exception thrown: " + e);
        }

    }

    public String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public void setData(String data){
        this.data = data;
        System.out.println("+++ AsyncTask.setData():String data ---> " + data);
        System.out.println("+++ AsyncTask.setData():String this.data ---> " + this.data);
    }

    public String getData(){
        System.out.println("+++ getData():String this.data ---> " + this.data);
        return this.data;
    }

}
