package com.memorizer.memorizer.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleData;
import com.memorizer.memorizer.models.ScheduleModel;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by YS on 2017-05-07.
 */

public class MainWidget extends AppWidgetProvider {

    private static final String TAG = "WIDGET TEST";
    static ArrayList<MemoData> memoDatas = new ArrayList<>();
    private static int curIndex = -1;
    private static final int LEFT = 1;  // 최근 메모
    private static final int RIGHT = -1;    // 예전 메모
    private static final String STR_WIDGET_INDEX = "widget_index";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        Log.d(TAG, "onReceive() action = " + action);

        // Default Recevier
        if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)){
            SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
            curIndex = pref.getInt(STR_WIDGET_INDEX, -1);
        } else if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            setUI(context, manager, manager.getAppWidgetIds(new ComponentName(context, getClass())), 0);
        } else if(AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)){

        } else if(AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)){

        }

        // Custom Recevier
        else if(Constants.NEW_MEMO.equals(action)){
            Intent newMemoIntent = new Intent(context, MemoCreate.class);
            newMemoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callActivity(context, newMemoIntent);
        } else if (Constants.LEFT_MEMO.equals(action)) {
            // TODO get left memo if exist
            //memoChange(context, LEFT);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            setUI(context, manager, manager.getAppWidgetIds(new ComponentName(context, getClass())), LEFT);
        } else if (Constants.RIGHT_MEMO.equals(action)) {
            // TODO get right memo if exist
            //memoChange(context, RIGHT);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            setUI(context, manager, manager.getAppWidgetIds(new ComponentName(context, getClass())), RIGHT);
        } else if (Constants.OPEN_MEMO.equals(action)) {
            SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);

            int memoId = pref.getInt(STR_WIDGET_INDEX, 0);
            if (memoId > 0) {
                Intent openMemoIntent = new Intent(context, MemoCreate.class);
                openMemoIntent.putExtra("is_edit", true);
                openMemoIntent.putExtra("memo_id", pref.getInt(STR_WIDGET_INDEX, 0));

                openMemoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                callActivity(context, openMemoIntent);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("WIDGET LIFECYCLE", "update");
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WIDGET LIFECYCLE", "endable");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("WIDGET LIFECYCLE", "disable");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * UI 설정 이벤트 설정
     */
    public void setUI(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int direction) {
        Log.i(TAG, "======================= initUI() =======================");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_memo);

        Intent newMemoIntent = new Intent(Constants.NEW_MEMO);
        Intent leftMemoIntent = new Intent(Constants.LEFT_MEMO);
        Intent rightMemoIntent = new Intent(Constants.RIGHT_MEMO);
        Intent openMemoIntent = new Intent(Constants.OPEN_MEMO);

        PendingIntent pIntentNewMemo = PendingIntent.getBroadcast(context, 0, newMemoIntent, 0);
        PendingIntent pIntentLeftMemo = PendingIntent.getBroadcast(context, 0, leftMemoIntent, 0);
        PendingIntent pIntentRightMemo = PendingIntent.getBroadcast(context, 0, rightMemoIntent, 0);
        PendingIntent pIntentOpenMemo = PendingIntent.getBroadcast(context, 0, openMemoIntent, 0);


        views.setOnClickPendingIntent(R.id.widget_new_memo, pIntentNewMemo);
        views.setOnClickPendingIntent(R.id.widget_left_btn, pIntentLeftMemo);
        views.setOnClickPendingIntent(R.id.widget_right_btn, pIntentRightMemo);
        views.setOnClickPendingIntent(R.id.message_view, pIntentOpenMemo);

        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        MemoModel memoModel = new MemoModel(context);
        MemoData memoData = null;
        if (direction == RIGHT) {
            memoData = memoModel.getNearData(pref.getInt(STR_WIDGET_INDEX, 0), false); // Content 글자수 제한
        } else if (direction == LEFT){
            memoData = memoModel.getNearData(pref.getInt(STR_WIDGET_INDEX, 0), true); // Content 글자수 제한
        } else {
            memoData = memoModel.getData(pref.getInt(STR_WIDGET_INDEX, 0)); // Content 글자수 제한
        }
        memoModel.close();
        SharedPreferences.Editor editor = pref.edit();
        if (memoData != null) {
            editor.putInt(STR_WIDGET_INDEX, memoData.get_id());
            editor.apply();

            ScheduleModel scheduleModel = new ScheduleModel(context);
            ScheduleData nextSchedule = scheduleModel.getMemoSchedule(memoData.get_id());
            scheduleModel.close();

            String datetime = makeTime(nextSchedule, memoData.getTimeOfHour(), memoData.getTimeOfMinute());

            views.setTextViewText(R.id.message_view, memoData.getContent());
            views.setTextViewText(R.id.alarm_time, datetime);

            if (memoData.getLabel().equals("") && memoData.getLabelPos() == 0) {
                views.setViewVisibility(R.id.label, View.GONE);
            } else {
                views.setViewVisibility(R.id.label, View.VISIBLE);
                views.setInt(R.id.label, "setBackgroundResource", memoData.getLabelPosDraw());
                //views.setImageViewResource(R.id.label, color);
                views.setTextViewText(R.id.label, memoData.getLabel());
            }
        } else {
            Log.d(TAG, "memo data null");
            /*editor.putInt(STR_WIDGET_INDEX, -1);
            editor.apply();*/

            //views.setTextViewText(R.id.message_view, context.getString(R.string.memo_not_regist_widget));
            //views.setTextViewText(R.id.alarm_time, context.getString(R.string.memo_not_regist_widget));
            //views.setViewVisibility(R.id.label, View.GONE);
        }

        /*if (memoDatas.size() == 0) {
            getMemoDatas(context);
        }

        boolean isFind = false;
        for (MemoData memoData : memoDatas) {
            if (memoData.get_id() == curIndex) {
                ScheduleModel scheduleModel = new ScheduleModel(context);
                ScheduleData nextSchedule = scheduleModel.getMemoSchedule(memoData.get_id());
                scheduleModel.close();

                String datetime = makeTime(nextSchedule, memoData.getTimeOfHour(), memoData.getTimeOfMinute());

                views.setTextViewText(R.id.message_view, memoData.getContent());
                views.setTextViewText(R.id.alarm_time, datetime);

                isFind = true;
            }
        }

        if (!isFind) {
            MemoData memoData = memoDatas.get(0);
            ScheduleModel scheduleModel = new ScheduleModel(context);
            ScheduleData nextSchedule = scheduleModel.getMemoSchedule(memoData.get_id());
            scheduleModel.close();

            String datetime = makeTime(nextSchedule, memoData.getTimeOfHour(), memoData.getTimeOfMinute());

            views.setTextViewText(R.id.message_view, memoData.getContent());
            views.setTextViewText(R.id.alarm_time, datetime);

            curIndex = memoDatas.get(0).get_id();
            SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(STR_WIDGET_INDEX, curIndex);
            editor.apply();

        }*/

        for(int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /**
     * Activity 호출 (Intent.FLAG_ACTIVITY_NEW_TASK)
     */
    private void callActivity(Context context, Intent intent){
        Log.d(TAG, "callActivity()");

        context.startActivity(intent);
    }

    /**
     * Dialog Activity 호출 (PendingIntent)
     */
    private void createDialog(Context context){
        Log.d(TAG, "createDialog()");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent Intent = new Intent("arabiannight.tistory.com.widget.CALL_PROGRESSDIALOG");
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, Intent, 0);

        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pIntent);
    }


    private void memoChange(Context context, int direction) {
        Log.d("WIDGET TEST", ""+curIndex);
        Log.d("WIDGET TEST", ""+memoDatas.size());
        //Log.d("WIDGET TEST", ""+memoDatas.);
        for (int i=0; i<memoDatas.size(); i++) {
            if (memoDatas.get(i).get_id() == curIndex) {
                Log.d("WIDGET TEST", "찾음");
                switch (direction) {
                    case RIGHT:
                        if (i != memoDatas.size()) {
                            curIndex = memoDatas.get(i + direction).get_id();
                        }
                        break;
                    case LEFT:
                        if (i != 0) {
                            curIndex = memoDatas.get(i + direction).get_id();
                        }
                }
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(STR_WIDGET_INDEX, curIndex);
                editor.apply();

                break;
            }
        }
    }

    // memoDatas 재배치
    private void getMemoDatas(Context context) {
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);

        // DB에서 메모목록 가져옴
        MemoModel memoModel = new MemoModel(context);
        memoDatas = memoModel.getAllDataShort(pref.getInt("filter",0)); // Content 글자수 제한
        memoModel.close();

    }

    protected String makeTime(ScheduleData nextSchedule, int hourOfDay, int minute) {

        String date = "";
        if (nextSchedule != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() + (nextSchedule.getDaysNext() * 24 * 60 * 1000));
            date += calendar.get(Calendar.YEAR) + "-" +
                    (calendar.get(Calendar.MONTH)+1) + "-" +
                    calendar.get(Calendar.DAY_OF_MONTH);
        }

        String hourStr, minStr, ampm = "am";

        if (hourOfDay >= 22) {
            hourStr = String.valueOf(hourOfDay - 12);
            ampm = "pm";
        } else if (hourOfDay > 12) {
            hourStr = "0" + String.valueOf(hourOfDay - 12);
            ampm = "pm";
        } else if (hourOfDay == 0) {
            hourStr = "12";
        } else if (hourOfDay < 10) {
            hourStr = "0" + String.valueOf(hourOfDay);
        } else if (hourOfDay == 12) {
            hourStr = String.valueOf(hourOfDay);
            ampm = "pm";
        } else
            hourStr = String.valueOf(hourOfDay);

        if (minute < 10) {
            minStr = "0" + String.valueOf(minute);
        } else
            minStr = String.valueOf(minute);

        return date + " " + hourStr +" : "+minStr+ " "+ampm;
    }

}
