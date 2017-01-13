package com.memorizer.memorizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;

import java.io.Serializable;

/**
 * Created by YS on 2016-06-27.
 */
public class MemoAlarmActivity extends Activity implements View.OnClickListener {
    private final String TAG = "MemoAlarmActivity";

    private Button mConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alarm);

        mConfirm = (Button) findViewById(R.id.alram_confirm);
        mConfirm.setOnClickListener(this);

        Intent intent = getIntent();
        Bundle intentBundle = intent.getExtras();
        Serializable tempData = intent.getSerializableExtra("memoId");
        MemoData memoData = (MemoData) tempData;
        intentBundle.clear();

        TextView messageView = (TextView) findViewById(R.id.message_view);
        RelativeLayout labelHeader = (RelativeLayout) findViewById(R.id.label_header);
        TextView label = (TextView) findViewById(R.id.label);

        messageView.setText(memoData.getContent());
        if (memoData.getLabel().equals("") && memoData.getLabelPos() == 0) {
            labelHeader.setVisibility(View.GONE);
        } else {
            int color = 0;
            switch (memoData.getLabelPos()) {
                case Constants.COLOR_BLUE:
                    color = R.drawable.color_selector_blue;
                    break;
                case Constants.COLOR_RED:
                    color = R.drawable.color_selector_red;
                    break;
                case Constants.COLOR_ORANGE:
                    color = R.drawable.color_selector_orange;
                    break;
                case Constants.COLOR_GREEN:
                    color = R.drawable.color_selector_green;
                    break;
                default:
                    color = R.drawable.color_selector;
            }
            label.setBackground(ContextCompat.getDrawable(this, color));
            label.setText(memoData.getLabel());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alram_confirm:
                this.finish();
                break;
            default:
                break;
        }
    }
}
