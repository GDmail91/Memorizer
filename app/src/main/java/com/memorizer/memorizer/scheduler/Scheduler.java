package com.memorizer.memorizer.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleData;
import com.memorizer.memorizer.models.ScheduleModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by YS on 2016-06-27.
 */
public class Scheduler {
    static Scheduler scheduler = new Scheduler();
    final String TAG = "Scheduler";
    final int nextAlarmFlag = 200;
    final int addAlarmFlag = 300;

    private static final long NEXT = 24 * 3600 * 1000;

    public static Scheduler getScheduler() {return scheduler;}

    public void startSchedule(Context context) {
        Intent intent = new Intent("com.memorizer.memorizer.alarmTrigger");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, addAlarmFlag, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // 반복되는 시간 설정
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, pIntent);
    }

    public void setSchedule(Context context, MemoData memoData, boolean isCreate) {
        // 종료날짜가 아닐경우 실행
        if (!checkEndDay(memoData.getWhileDate(), memoData.getTerm())) {
            setAlarm(context, memoData, isCreate);
        }
    }

    protected void deleteAlarm(Context context) {
        ScheduleModel scheduleModel = new ScheduleModel(context, "Memo.db", null);
        scheduleModel.deletePrevious();
        scheduleModel.close();
    }

    /**
     * 실제 알람 등록
     * Schedule 중 가장 가까운 알람 울리도록 등록
     * @param context
     */
    public void setNextAlarm(Context context) {
        // 가장 가까운 알람 가져옴
        ScheduleModel scheduleModel = new ScheduleModel(context, "Memo.db", null);
        scheduleModel.printCountOfData();
        ScheduleData nextSchedule = scheduleModel.getNextData(System.currentTimeMillis());
        scheduleModel.close();

        // nextSchedule이 null이 아닐경우 등록
        if (nextSchedule != null) {
            // 알림용 intent 등록
            MemoModel memoModel = new MemoModel(context, "Memo.db", null);
            MemoData memoData = memoModel.getData(nextSchedule.getMemoId());
            memoModel.close();

            // memoData가 지워진 경우 등록하지 않음
            if (memoData != null) {
                Intent intent = new Intent("com.memorizer.memorizer.nextAlarm");
                intent.putExtra("memoId", memoData);

                PendingIntent pIntent = PendingIntent.getBroadcast(context, nextAlarmFlag, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // 알람 실행
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextSchedule.getAlarmDate().getTimeInMillis(), pIntent);
            } else {
                // 해당 스케쥴 삭제
                scheduleModel.delete(nextSchedule.get_id());
                // NextAlarm 재실행
                setNextAlarm(context);
            }
        }
        deleteAlarm(context);
    }

    /**
     * 알람 일정 등록 (next가 true 일경우 당일부터 시작)
     * 알람은 현재 ID의 알람만 관리
     * @param context
     * @param memoData
     * @param next
     */
    protected void setAlarm(Context context, MemoData memoData, boolean next) {
        Calendar setDay = Calendar.getInstance();

        if (memoData.isRandom()) {
            setDay.setTimeInMillis(System.currentTimeMillis());

            Random random = new Random(System.currentTimeMillis());
            int hour = 24 - setDay.get(Calendar.HOUR_OF_DAY);
            int minute = 60 - setDay.get(Calendar.MINUTE);

            setDay.set(
                    setDay.get(Calendar.YEAR),
                    setDay.get(Calendar.MONTH),
                    setDay.get(Calendar.DAY_OF_MONTH),
                    setDay.get(Calendar.HOUR_OF_DAY) + random.nextInt(hour),
                    setDay.get(Calendar.MINUTE) + random.nextInt(minute),
                    0);
        } else {
            // 현재 시간부터 다음 간격시간 후에 알림
            if (next) {
                setDay.setTimeInMillis(System.currentTimeMillis());
                Log.d(TAG, "오늘부터 시작!");
                setDay.set(
                        setDay.get(Calendar.YEAR),
                        setDay.get(Calendar.MONTH),
                        setDay.get(Calendar.DAY_OF_MONTH),
                        memoData.getTimeOfHour(),
                        memoData.getTimeOfMinute(),
                        0);
            } else {
                long term = memoData.getTerm();
                setDay.setTimeInMillis(System.currentTimeMillis() + (term * NEXT)); // 현재 날짜에서 Term 기간만큼 증가후 저장
                Log.d(TAG, "다음꺼!");
                //setDay.setTimeInMillis(System.currentTimeMillis() + ((long) memoData.getTerm() * 60 * 1000));
                //setDay.setTimeInMillis(System.currentTimeMillis());
                setDay.set(
                        setDay.get(Calendar.YEAR),
                        setDay.get(Calendar.MONTH),
                        setDay.get(Calendar.DAY_OF_MONTH),
                        setDay.get(Calendar.HOUR_OF_DAY),
                        setDay.get(Calendar.MINUTE),
                        0);
            }
        }

        ScheduleData scheduleData = new ScheduleData(memoData.get_id(), setDay);
        // DB에 저장
        ScheduleModel scheduleModel = new ScheduleModel(context, "Memo.db", null);

        ArrayList<ScheduleData> sd = scheduleModel.getAllData();
        Log.d(TAG, scheduleModel.printCountOfData()+"");
        for(int i=0; i<sd.size(); i++) {

            Log.d(TAG, sd.get(i).toString());
        }
        scheduleModel.insert(scheduleData);
        scheduleModel.close();

        // 변경된 스케쥴 반영
        setNextAlarm(context);

    }

    protected boolean checkEndDay(Calendar endDay, int term) {
        if (endDay == null
        || endDay.getTimeInMillis() == 0) {
            return false;
        }

        Calendar setDay = Calendar.getInstance();
        setDay.setTimeInMillis(System.currentTimeMillis() + term * 1000);

        return setDay.get(Calendar.DAY_OF_YEAR) < endDay.get(Calendar.DAY_OF_YEAR);
    }
}
