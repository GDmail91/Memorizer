package com.memorizer.memorizer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Created by YS on 2016-06-29.
 */
public class DeveloperInfo extends AppCompatActivity {
    String TAG = "MemoCreate";

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

    }
}
