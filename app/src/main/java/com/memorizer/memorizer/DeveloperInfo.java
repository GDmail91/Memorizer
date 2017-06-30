package com.memorizer.memorizer;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.cloudrail.si.types.CloudMetaData;
import com.memorizer.memorizer.backup.CloudLinker;
import com.memorizer.memorizer.backup.CloudManager;

/**
 * Created by YS on 2016-06-29.
 */
public class DeveloperInfo extends AppCompatActivity {
    String TAG = "DeveloperInfo";

    TextView emailBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        emailBtn = (TextView) findViewById(R.id.email_txt);
        Linkify.addLinks(emailBtn, Linkify.EMAIL_ADDRESSES);

        getUsername();
    }

    private void getUsername() {
        Log.d(TAG, "getUsername");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "get Linker");
                String username = "";
                CloudManager cm = CloudManager.getInstance(DeveloperInfo.this);

                for (CloudLinker link : cm.getConnectedLinker()) {
                    CloudMetaData info = link.storage.get().getMetadata("/memorizer_app");
                    username += info.getFolder();
                    //username += link.storage.get().exists("/memorizer_app") + " ";
                }
                Log.d(TAG, "check all done");

                Log.d(TAG, "Connect Cloud Storage by : " + username);
            }
        }).start();

        /*new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                Log.d(TAG, "get Linker");
                String username = "";
                CloudManager cm = CloudManager.getInstance(DeveloperInfo.this);

                for (CloudLinker link : cm.getConnectedLinker()) {
                    username += link.storage.get().exists("/memorizer_app") + " ";
                }
                Log.d(TAG, "check all done");

                return username;
            }

            @Override
            protected void onPostExecute(String username) {
                Log.d(TAG, "Connect Cloud Storage by : " + username);
            }
        }.execute("");*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
