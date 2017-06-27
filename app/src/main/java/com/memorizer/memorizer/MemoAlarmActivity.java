/*
package com.memorizer.memorizer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.memorizer.memorizer.memolist.MainActivity;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;

import java.util.ArrayList;

import us.feras.mdv.MarkdownView;

import static com.memorizer.memorizer.models.Constants.NOTIFY_ID;

*/
/**
 * Created by YS on 2016-06-27.
 *//*

public class MemoAlarmActivity extends Activity implements View.OnClickListener {
    private final String TAG = "MemoAlarmActivity";

    private Button mConfirm;
    //private Button nextButton;
    //private Button prevButton;
    private TextView messageView;
    private MarkdownView messageMarkdown;
    private ScrollView alarmScrollView;
    private TextView label;
    //private TextView messageCounter;
    private RelativeLayout labelHeader;
    private static ArrayList<MemoData> memoDatas = new ArrayList<>();
    private static int counter=1;
    private boolean isCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alarm);

        mConfirm = (Button) findViewById(R.id.alram_confirm);
        //nextButton = (Button) findViewById(R.id.next_alarm);
        //prevButton = (Button) findViewById(R.id.prev_alarm);
        mConfirm.setOnClickListener(this);
        //nextButton.setOnClickListener(this);
        //prevButton.setOnClickListener(this);

        alarmScrollView = (ScrollView) findViewById(R.id.alarm_scroll_view);
        messageView = (TextView) findViewById(R.id.message_view);
        messageMarkdown = (MarkdownView) findViewById(R.id.markdown_view);
        labelHeader = (RelativeLayout) findViewById(R.id.label_header);
        label = (TextView) findViewById(R.id.label);
        //messageCounter = (TextView) findViewById(R.id.message_counter);

        Intent intent = getIntent();
        Bundle intentBundle = intent.getExtras();
        ArrayList<Integer> tempData = intent.getIntegerArrayListExtra("memoId");
        isCreated = intent.getBooleanExtra("isCreated", false);
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
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFY_ID);

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
                //messageCounter.setText("("+counter + "/" + memoDatas.size()+")");

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
            // 본문 출력
            if (memoData.isMarkdown()) {
                alarmScrollView.setBackgroundResource(R.color.colorPrimary);
                messageView.setVisibility(View.GONE);
                messageMarkdown.setVisibility(View.VISIBLE);
                messageMarkdown.loadMarkdown(memoData.getContent());
            } else {
                messageView.setText(memoData.getContent());
            }

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

            setStatusBarIcon(memoData.getContent());
        }
    }

    public void setStatusBarIcon(String content) {
        if (!isCreated) {
            //알림(Notification)을 관리하는 NotificationManager 얻어오기
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //알림(Notification)을 만들어내는 Builder 객체 생성
            //API 11 버전 이하도 지원하기 위해 NotificationCampat 클래스 사용
            //만약 minimum SDK가 API 11 이상이면 Notification 클래스 사용 가능
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            //Notification.Builder에게 Notification 제목, 내용, 이미지 등을 설정//////////////////////////////////////

            builder.setSmallIcon(R.drawable.write_memo_trans);//상태표시줄에 보이는 아이콘 모양
            builder.setTicker("Notification"); //알림이 발생될 때 잠시 보이는 글씨

            //상태바를 드래그하여 아래로 내리면 보이는 알림창(확장 상태바)의 아이콘 모양 지정
            //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_input_add));
            PendingIntent intent = PendingIntent.getActivity(
                    MemoAlarmActivity.this, 0,
                    new Intent(MemoAlarmActivity.this, MainActivity.class), 0);

            builder.setContentTitle(getString(R.string.remember));    //알림창에서의 제목
            //알림창에서의 글씨
            if (content.length() < 25)
                builder.setContentText(content.substring(0, content.length()));
            else
                builder.setContentText(content.substring(0, 25));
            builder.setContentIntent(intent);

            Notification notification = builder.build();   //Notification 객체 생성
            notification.flags |=  notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(NOTIFY_ID, notification);             //NotificationManager가 알림(Notification)을 표시
        }
    }
}
*/
