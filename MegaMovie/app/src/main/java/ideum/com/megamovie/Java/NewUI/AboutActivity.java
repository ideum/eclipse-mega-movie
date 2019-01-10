package ideum.com.megamovie.Java.NewUI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.net.Uri;

import org.w3c.dom.Text;

import ideum.com.megamovie.R;

/**
 * Gives description of app and megamovie project, lists partners, and provides link to privacy policy.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("About");

        TextView aboutText = (TextView) findViewById(R.id.about_text);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());

        View ideum_card = (View) findViewById(R.id.ideum_credit_card);
        ideum_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(getString(R.string.ideum_url));
            }
        });

        View ssl_card = (View) findViewById(R.id.ssl_credit_card);
        ssl_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(getString(R.string.ssl_url));
            }
        });
        View eaa_card = (View) findViewById(R.id.eaa_credit_card);
        eaa_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage(getString(R.string.eaa_url));
            }
        });

        TextView privacy_link = (TextView) findViewById(R.id.privacy_link);
        privacy_link.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.dismiss) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
