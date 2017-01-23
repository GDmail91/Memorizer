package com.memorizer.memorizer.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleData;
import com.memorizer.memorizer.models.ScheduleModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

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
        Log.d(TAG, "스케쥴링 시작");
        Intent intent = new Intent("com.memorizer.memorizer.alarmTrigger");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, addAlarmFlag, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // 반복되는 시간 설정
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, currentTimeMillis() + 60 * 1000, pIntent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, currentTimeMillis() + 60 * 1000, pIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, currentTimeMillis() + 60 * 1000, pIntent);
            }
        }
    }

    public void setSchedule(Context context, MemoData memoData, boolean isCreate) {
        // 종료날짜가 아닐경우 실행
        if (!checkEndDay(memoData.getWhileDate(), memoData.getTerm())) {
            // 알람 일정 등록
            setAlarmSchedule(context, memoData, isCreate);
        }
    }

    /**
     * 이전 알람 제거
     * @param context
     */
    public void deletePreviousAlarm(Context context) {
        ScheduleModel scheduleModel = new ScheduleModel(context);
        scheduleModel.deletePrevious();
        scheduleModel.close();
    }

    /**
     * 선택된 알람 제거
     * @param context
     * @param deleteMemoId Memo ID
     */
    public void deleteSelectedAlarm(Context context, int deleteMemoId) {
        ScheduleModel scheduleModel = new ScheduleModel(context);
        scheduleModel.deleteByMemoId(deleteMemoId);
        scheduleModel.close();
    }

    /**
     * 실제 알람 등록
     * Schedule 중 가장 가까운 알람 울리도록 등록
     * @param context
     */
    public void setNextAlarm(Context context) {
        // 가장 가까운 알람 가져옴
        ScheduleModel scheduleModel = new ScheduleModel(context);
        ArrayList<ScheduleData> nextScheduleList = scheduleModel.getNextData();
        Log.d(TAG, "같은시간에 울리는 알람 개수 : " + nextScheduleList.size());
        ArrayList<Integer> memoIdList = new ArrayList<>();

        // nextSchedule이 null이 아닐경우 등록
        for (ScheduleData nextSchedule : nextScheduleList) {
            // 알림용 intent 등록
            MemoModel memoModel = new MemoModel(context);
            MemoData memoData = memoModel.getData(nextSchedule.getMemoId());
            memoModel.close();

            // memoData가 지워진 경우 등록하지 않음
            if (memoData != null) {
                // 안지워진 경우 다시 등록

                long nextTime;

                // 알람이 누락된 경우 확인
                long duringTime = System.currentTimeMillis() - nextSchedule.getAlarmDate().getTimeInMillis();
                if (duringTime >= 0) {
                    // 누락된 경우
                    Log.d(TAG, "누락됨");
                    nextTime = nextSchedule.getAlarmDate().getTimeInMillis() // 현재 등록된 시간
                            + (duringTime/(memoData.getTerm() * NEXT) // 남은시간/알림간격
                                    * memoData.getTerm()) * NEXT    // 총 지나간 간격
                            + memoData.getTerm() * NEXT;    // 다음 알림 간격

                    // 기존 스케쥴 제거
                    // deleteSelectedAlarm(context, memoData.get_id());
                    // 새 스케쥴 등록
                    Calendar setDay = Calendar.getInstance();
                    setDay.setTimeInMillis(nextTime);
                    Log.d(TAG, setDay.get(Calendar.YEAR) + "-"+setDay.get(Calendar.MONTH) + "-"+ setDay.get(Calendar.DAY_OF_MONTH) +" " +setDay.get(Calendar.HOUR_OF_DAY) + ":"+setDay.get(Calendar.MINUTE));
                    setDay.set(
                            setDay.get(Calendar.YEAR),
                            setDay.get(Calendar.MONTH),
                            setDay.get(Calendar.DAY_OF_MONTH),
                            memoData.getTimeOfHour(),
                            memoData.getTimeOfMinute(),
                            0);

                    Log.d(TAG, setDay.get(Calendar.YEAR) + "-"+setDay.get(Calendar.MONTH) + "-"+ setDay.get(Calendar.DAY_OF_MONTH) +" " +setDay.get(Calendar.HOUR_OF_DAY) + ":"+setDay.get(Calendar.MINUTE));
                    nextSchedule.setAlarmDate(setDay);
                    Log.d(TAG, nextSchedule.toString());
                    scheduleModel.update(nextSchedule);

                } else {
                    // 실제 알람 등록
                    nextTime = nextSchedule.getAlarmDate().getTimeInMillis();

                    if (memoData.getWhileDate().getTimeInMillis() == 0
                            || memoData.getWhileDate().getTimeInMillis() > nextTime) {
                        memoIdList.add(memoData.get_id());
                    }
                }
            } else {
                // TODO 등록된 알람 제거
                /*Intent intent = new Intent("com.memorizer.memorizer.nextAlarm");
                intent.putExtra("memoId", memoData);

                PendingIntent pIntent = PendingIntent.getBroadcast(context, memoData.get_id(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel();*/
                // memoData가 지워졌기 때문에 해당 스케쥴 삭제
                scheduleModel.delete(nextSchedule.get_id());
                // NextAlarm 재실행
                setNextAlarm(context);
            }
        }


        if (memoIdList.size() > 0) {
            Intent intent = new Intent("com.memorizer.memorizer.nextAlarm");
            intent.putIntegerArrayListExtra("memoId", memoIdList);

            Log.d(TAG, "다음알람 등록");
            PendingIntent pIntent = PendingIntent.getBroadcast(context, nextAlarmFlag, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            long nextAlertTime = nextScheduleList.get(0).getAlarmDate().getTimeInMillis();

            // 다음 알람 등록
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlertTime, pIntent);
            } else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlertTime, pIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlertTime, pIntent);
            }
        }

        scheduleModel.close();
    }

    /**
     * 알람 일정 등록 (next가 true 일경우 당일부터 시작)
     * 알람은 현재 ID의 알람만 관리
     * @param context
     * @param memoData
     * @param next
     */
    protected void setAlarmSchedule(Context context, MemoData memoData, boolean next) {
        Calendar setDay = Calendar.getInstance();

        // TODO switch 문으로 변경 (random인 경우, random 아닌경우, 다음걸 실행하는 경우)
        if (memoData.isRandom()) {
            setDay.setTimeInMillis(currentTimeMillis());

            Random random = new Random(currentTimeMillis());
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
                setDay.setTimeInMillis(currentTimeMillis());
                // 설정 시간이 이미 지난 시간일경우
                if (memoData.getTimeOfHour() <= setDay.get(Calendar.HOUR_OF_DAY)
                        && memoData.getTimeOfMinute() <= setDay.get(Calendar.MINUTE)) {
                    setDay.setTimeInMillis(currentTimeMillis() + (memoData.getTerm() * NEXT)); // 현재 날짜에서 Term 기간만큼 증가후 저장
                }

                Log.d(TAG, "오늘부터 시작!");
                setDay.set(
                        setDay.get(Calendar.YEAR),
                        setDay.get(Calendar.MONTH),
                        setDay.get(Calendar.DAY_OF_MONTH),
                        memoData.getTimeOfHour(),
                        memoData.getTimeOfMinute(),
                        0);
            } else {
                setDay.setTimeInMillis(currentTimeMillis() + (memoData.getTerm() * NEXT)); // 현재 날짜에서 Term 기간만큼 증가후 저장
                Log.d(TAG, "다음꺼!");
                //setDay.setTimeInMillis(System.currentTimeMillis() + ((long) memoData.getTerm() * 60 * 1000));
                //setDay.setTimeInMillis(System.currentTimeMillis());
                setDay.set(
                        setDay.get(Calendar.YEAR),
                        setDay.get(Calendar.MONTH),
                        setDay.get(Calendar.DAY_OF_MONTH),
                        memoData.getTimeOfHour(),
                        memoData.getTimeOfMinute(),
                        0);
            }
        }

        ScheduleData scheduleData = new ScheduleData(memoData.get_id(), setDay);
        // DB에 저장
        ScheduleModel scheduleModel = new ScheduleModel(context);

        scheduleModel.insert(scheduleData);
        scheduleModel.close();

        // 변경된 스케쥴 반영
        setNextAlarm(context);

    }

    protected boolean checkEndDay(Calendar endDay, int term) {
        // 무제한인 경우
        if (endDay == null
        || endDay.getTimeInMillis() == 0) {
            return false;
        }

        Calendar setDay = Calendar.getInstance();
        setDay.setTimeInMillis(currentTimeMillis() + term * NEXT);

        return setDay.get(Calendar.DAY_OF_YEAR) > endDay.get(Calendar.DAY_OF_YEAR);
    }
}
