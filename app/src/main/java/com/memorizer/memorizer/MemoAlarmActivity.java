package com.memorizer.memorizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;

import java.util.ArrayList;

/**
 * Created by YS on 2016-06-27.
 */
public class MemoAlarmActivity extends Activity implements View.OnClickListener {
    private final String TAG = "MemoAlarmActivity";

    private Button mConfirm;
    private Button nextButton;
    private Button prevButton;
    private TextView messageView;
    private TextView label;
    private TextView messageCounter;
    private RelativeLayout labelHeader;
    private static ArrayList<MemoData> memoDatas = new ArrayList<>();
    private static int counter=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alarm);

        mConfirm = (Button) findViewById(R.id.alram_confirm);
        nextButton = (Button) findViewById(R.id.next_alarm);
        prevButton = (Button) findViewById(R.id.prev_alarm);
        mConfirm.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        messageView = (TextView) findViewById(R.id.message_view);
        labelHeader = (RelativeLayout) findViewById(R.id.label_header);
        label = (TextView) findViewById(R.id.label);
        messageCounter = (TextView) findViewById(R.id.message_counter);

        Intent intent = getIntent();
        Bundle intentBundle = intent.getExtras();
        ArrayList<Integer> tempData = intent.getIntegerArrayListExtra("memoId");
        MemoModel memoModel = new MemoModel(this);
        memoDatas = memoModel.getSelectedData(tempData);
        Log.d(TAG, "메모데이터 길이: "+memoDatas.size());

        intentBundle.clear();

        setMessageView();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alram_confirm:
                counter = 1;
                memoDatas.clear();
                this.finish();
                break;
            case R.id.next_alarm:
                if (memoDatas.size() > counter) {
                    counter++;
                }

                setMessageView();
                break;
            case R.id.prev_alarm:
                if (counter > 1) {
                    counter--;
                }

                setMessageView();
                break;
            default:
                break;
        }
    }

    public void setMessageView() {
        if (memoDatas.size() > 0) {
            if (memoDatas.size() > 1) {
                messageCounter.setText("("+counter + "/" + memoDatas.size()+")");

                if (counter == 1) {
                    nextButton.setVisibility(View.VISIBLE);
                    prevButton.setVisibility(View.GONE);
                } else if (counter >= memoDatas.size()) {
                    nextButton.setVisibility(View.GONE);
                    prevButton.setVisibility(View.VISIBLE);
                } else if (counter > 1) {
                    nextButton.setVisibility(View.VISIBLE);
                    prevButton.setVisibility(View.VISIBLE);
                }
            }

            MemoData memoData = memoDatas.get(counter-1);
            Log.d(TAG, memoData.printItem());
            messageView.setText(memoData.getContent());
            if (memoData.getLabel().equals("") && memoData.getLabelPos() == 0) {
                labelHeader.setVisibility(View.GONE);
            } else {
                labelHeader.setVisibility(View.VISIBLE);
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
    }
}
