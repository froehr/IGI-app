package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.ifgi.igiapp.igi_app.MongoDB.DatabaseHandler;
import de.ifgi.igiapp.igi_app.MongoDB.Poi;
import de.ifgi.igiapp.igi_app.MongoDB.StoryElement;

/***
 * This class provides the content for markers (pois AND story-elements)
 */
public class PoiActivity extends Activity {
    // request code for story elements
    int OPEN_STORY_ELEMENT = 77;
    // result code for returning to map
    int BACK_TO_MAP = 55;

    String basePictureUrl = "http://giv-interaction.uni-muenster.de/dbml/getImage.php?oid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String poiId = intent.getStringExtra("poi-id");

        // fill story-element list
        final ListView listview = (ListView) findViewById(R.id.story_element_list);

        // get global db-handler
        GlobalApplication globalApplication = (GlobalApplication) getApplicationContext();
        DatabaseHandler databaseHandler = globalApplication.getGlobalDatabaseHandler();

        // get story object from id
        Poi poi = databaseHandler.getPoiByPoiId(poiId);

        // check if image is available and render content in activity
        ImageView imageView = ((ImageView) findViewById(R.id.display_narrative_image));
        if (poi.getPictureIds() != null){
            // always take first picture (until now!)
            String url = basePictureUrl + poi.getPictureIds()[0];
            new DownloadImageTask(imageView).execute(url);
        } else {
            imageView.setImageResource(R.drawable.prinzipalmarkt1);
        }
        render(title, description);

        final List<StoryElement> storyElements = databaseHandler.getStoryElementByPoiId(poi.getId());

        // add story-element title to listview
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < storyElements.size(); ++i) {
            list.add(storyElements.get(i).getName());
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        // on story-element click
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                // open storyElementActivity
                Intent intent = new Intent(PoiActivity.this, StoryElementActivity.class);
                intent.putExtra("story-element-id", storyElements.get(position).getId());
                startActivityForResult(intent, OPEN_STORY_ELEMENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == BACK_TO_MAP){
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_narrative, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void render(String title, String description){
        // Create the text view
        TextView textTitle = (TextView) findViewById(R.id.display_narrative_title);
        textTitle.setText(title);

        // Create text view for description
        WebView textDescription = (WebView) findViewById(R.id.display_narrative_description);
        String descriptionHTML = "<html><body style=\"background-color: #EEEEEE; margin: 0px;\"><p align=\"justify\">" + description + "</p></body></html>";
        textDescription.loadData(descriptionHTML, "text/html; charset=utf-8", null);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}