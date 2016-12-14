package com.memorizer.memorizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.scheduler.Scheduler;

import java.io.Serializable;

/**
 * Created by YS on 2016-06-27.
 */
public class MemorizerReciver extends BroadcastReceiver {
    private String TAG = "RECIEVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        String name = intent.getAction();
        Log.i(TAG, "알람 리시브: "+name);

        if(name.equals("com.memorizer.memorizer.alarmTrigger")){
            Scheduler.getScheduler().setNextAlarm(context);
        }

        if(name.equals("com.memorizer.memorizer.nextAlarm")){
            Bundle bundle = intent.getExtras();
            bundle.clear();

            Serializable tempData = intent.getSerializableExtra("memoId");
            MemoData memoData = (MemoData) tempData;


            // 다음 알림 설정
            Scheduler.getScheduler().setSchedule(context, memoData, false);
            Log.d(TAG, "다음 알림 설정: "+memoData.getContent());

            Intent popupIntent = new Intent(context.getApplicationContext(), MemoAlarmActivity.class);
            popupIntent.putExtra("memoId", memoData);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(popupIntent);
        }
    }
}
