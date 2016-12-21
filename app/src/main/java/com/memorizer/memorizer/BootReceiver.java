package com.memorizer.memorizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.memorizer.memorizer.scheduler.Scheduler;

/**
 * Created by YS on 2016-12-19.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_PACKAGE_ADDED:
                // 앱이 설치되었을 때
                Scheduler.getScheduler().startSchedule(context);
                break;
            case Intent.ACTION_PACKAGE_REMOVED:
                // 앱이 삭제되었을 때
                break;
            case Intent.ACTION_PACKAGE_REPLACED:
                // 앱이 업데이트 되었을 때
                Scheduler.getScheduler().startSchedule(context);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                Scheduler.getScheduler().startSchedule(context);
                break;
        }
    }
}