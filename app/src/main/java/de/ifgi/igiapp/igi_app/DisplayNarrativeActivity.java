package de.ifgi.igiapp.igi_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


public class DisplayNarrativeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_narrative);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        render(title);

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

    protected void render(String title){


        // Create the text view
        //TextView textView = new TextView(this);
        TextView textView = (TextView) findViewById(R.id.display_narrative_title);
        textView.setText(title);

        //Create the image view
        ImageView image = ((ImageView) findViewById(R.id.display_narrative_image));
        image.setImageResource(R.drawable.prinzipalmarkt1);
        // Set the text view as the activity layout
        //setContentView(textView);
    }
}
