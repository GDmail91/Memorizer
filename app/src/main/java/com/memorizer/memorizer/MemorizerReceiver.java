package com.memorizer.memorizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.memorizer.memorizer.alarm.MemoAlarmDragActivity;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.scheduler.Scheduler;

import java.util.ArrayList;

/**
 * Created by YS on 2016-06-27.
 */
public class MemorizerReceiver extends BroadcastReceiver {
    private String TAG = "RECIEVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        String name = intent.getAction();
        Log.i(TAG, "알람 리시브: "+name);

        if(name.equals("com.memorizer.memorizer.alarmTrigger")){
            // 실제 알람 등록
            Scheduler.getScheduler().setNextAlarm(context);
            Scheduler.getScheduler().startSchedule(context);
        }

        if(name.equals("com.memorizer.memorizer.nextAlarm")){
            Bundle bundle = intent.getExtras();
            bundle.clear();

            ArrayList<Integer> tempDataList = intent.getIntegerArrayListExtra("memoId");

            MemoModel memoModel = new MemoModel(context);
            for (Integer tempDataId : tempDataList) {
                MemoData memoData = memoModel.getData(tempDataId);
                if (memoData != null) {

                    // TODO 알림 등록시 제거 후 새로등록 동시에 할 수 있도록 변경
                    // 이전 알림 삭제
                    Scheduler.getScheduler().deleteSelectedAlarm(context, tempDataId);
                    // 다음 알림 등록
                    Scheduler.getScheduler().setSchedule(context, memoData, false);
                } else {
                    tempDataList.remove(tempDataId);
                }
            }
            memoModel.close();
            Log.d(TAG, "리시버에서 받은 id 개수 : "+tempDataList.size());
            Log.d(TAG, "리시버에서 받은 id 리스트 : "+tempDataList.toString());

            if (tempDataList.size() > 0) {
                Intent popupIntent = new Intent(context.getApplicationContext(), MemoAlarmDragActivity.class);
                popupIntent.putIntegerArrayListExtra("memoId", tempDataList);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(popupIntent);
            }
        }
    }
}
