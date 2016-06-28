package com.memorizer.memorizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.memorizer.memorizer.create.MemoData;
import com.memorizer.memorizer.scheduler.Scheduler;

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
        Log.d(TAG, intentBundle.toString());
        MemoData memoData = (MemoData) intentBundle.getSerializable("memoId");
        intentBundle.clear();

        TextView messageView = (TextView) findViewById(R.id.message_view);

        messageView.setText(memoData.getContent());

        // 알림 설정
        Scheduler.getScheduler().setSchedule(this, memoData);
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
