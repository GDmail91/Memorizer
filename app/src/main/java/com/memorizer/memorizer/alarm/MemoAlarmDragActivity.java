package com.memorizer.memorizer.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.memolist.MainActivity;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

import static com.memorizer.memorizer.models.Constants.NOTIFY_ID;

/**
 * Created by YS on 2017-05-18.
 */

public class MemoAlarmDragActivity extends AppCompatActivity  implements View.OnClickListener {

    private static ArrayList<MemoData> memoDatas = new ArrayList<>();
    private boolean isCreated = false;
    Fragment cur_fragment = new Fragment();
    private Button mConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_draggable_alarm);

        Intent intent = getIntent();
        Bundle intentBundle = intent.getExtras();
        ArrayList<Integer> tempData = intent.getIntegerArrayListExtra("memoId");
        MemoModel memoModel = new MemoModel(this);
        memoDatas = memoModel.getSelectedData(tempData);

        intentBundle.clear();

        FragmentPagerAdapter adapter = new adapter(getSupportFragmentManager());
        // Memo 본문 생성
        ViewPager contentViewPager = (ViewPager)findViewById(R.id.viewpager);
        contentViewPager.setAdapter(adapter);
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(contentViewPager);

        isCreated = intent.getBooleanExtra("isCreated", false);
        setStatusBarIcon(memoDatas.get(0).getContent());

        // VIEW BINDING
        mConfirm = (Button) findViewById(R.id.alarm_confirm);
        mConfirm.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alarm_confirm:
                memoDatas.clear();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFY_ID);

                this.finish();
                break;
        }
    }

    private class adapter extends FragmentPagerAdapter {
        public adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position<0 || memoDatas.size()<=position)
                return null;

            cur_fragment = new MemoAlarmFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("memoData", memoDatas.get(position));
            cur_fragment.setArguments(bundle);
            return cur_fragment;
        }

        @Override
        public int getCount() {
            return memoDatas.size();
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
                    MemoAlarmDragActivity.this, 0,
                    new Intent(MemoAlarmDragActivity.this, MainActivity.class), 0);

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