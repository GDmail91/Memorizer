package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import static com.memorizer.memorizer.models.DBmanager.COLUMN_SCHEDULE_ALARM_DATE;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_SCHEDULE_MEMO_ID;
import static com.memorizer.memorizer.models.DBmanager.TABLE_NAME_SCHEDULE;

/**
 * Created by YS on 2016-07-11.
 */
public class ScheduleModel{
    public static final String TAG = "ScheduleModel";

    private DBmanager dBmanager;

    public ScheduleModel(Context context) {
        this.dBmanager= new DBmanager(context);
    }

    /** 삽입 SQL
     *
     * @param scheduleData
     * @return topNumber
     */
    public int insert(ScheduleData scheduleData) {
        ArrayList<String> sqlList = new ArrayList<>();
        int topNumber = 0;

        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT _id FROM "+TABLE_NAME_SCHEDULE+" ORDER BY _id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }

        topNumber = topNumber+1;

        sqlList.add("INSERT INTO "+TABLE_NAME_SCHEDULE+" (_id, "+COLUMN_SCHEDULE_MEMO_ID+", "+COLUMN_SCHEDULE_ALARM_DATE+") " +
                "VALUES(" +
                "'" + topNumber + "', " +
                "'" + scheduleData.getMemoId() + "', " +
                "'" + (int)(scheduleData.getAlarmDate().getTimeInMillis() / 1000) + "');");

        dBmanager.startTransaction(sqlList);

        return topNumber;
    }

    /** 수정 SQL
     *
     * @param scheduleData
     * @return id
     */
    public int update(ScheduleData scheduleData) {
        ArrayList<String> sqlList = new ArrayList<>();
        sqlList.add("UPDATE "+TABLE_NAME_SCHEDULE+" SET " +
                COLUMN_SCHEDULE_MEMO_ID+"='" + scheduleData.getMemoId() + "', " +
                COLUMN_SCHEDULE_ALARM_DATE+"='" + (int)(scheduleData.getAlarmDate().getTimeInMillis() / 1000) + "' " +
                "WHERE _id='"+ scheduleData.get_id() +"' ;");

        dBmanager.startTransaction(sqlList);

        return scheduleData.get_id();
    }

    public void update(String _query) {
        dBmanager.getDbW().execSQL(_query);
    }

    public void delete(int ids) {
        dBmanager.getDbW().beginTransaction();
        try {
            dBmanager.getDbW().execSQL("DELETE FROM MemoSchedule WHERE _id='" + ids + "'");
            dBmanager.getDbW().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dBmanager.getDbW().endTransaction();
        }
    }

    public void deleteByMemoId(int memoId) {
        Log.d(TAG, "삭제가??");
        dBmanager.getDbW().beginTransaction();
        try {
            dBmanager.getDbW().execSQL("DELETE FROM MemoSchedule WHERE memoId=" + memoId);
            dBmanager.getDbW().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dBmanager.getDbW().endTransaction();
        }
        dBmanager.getDbW().close();
    }

    public void deletePrevious() {
        Log.d(TAG, "왜!?");
        dBmanager.getDbW().beginTransaction();
        try {
            dBmanager.getDbW().execSQL("DELETE FROM MemoSchedule WHERE alarmDate<" + (int)(System.currentTimeMillis()/1000) + " OR memoId = 0");
            dBmanager.getDbW().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dBmanager.getDbW().endTransaction();
        }
        dBmanager.getDbW().close();
    }

    public void deleteAll() {
        dBmanager.getDbW().beginTransaction();
        try {
            dBmanager.getDbW().execSQL("DELETE FROM MemoSchedule");
            dBmanager.getDbW().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dBmanager.getDbW().endTransaction();
        }
    }

    public int printCountOfData() {
        int count=0;

        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT * FROM MemoSchedule ORDER BY _id DESC", null);

        count = cursor.getCount();
        cursor.close();
        return count;
    }


    public ScheduleData getData(int id) {

        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT * FROM MemoSchedule WHERE _id='"+id+"' ORDER BY _id DESC", null);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cursor.getInt(2));

        ScheduleData data = new ScheduleData(
                cursor.getInt(0),
                cursor.getInt(1),
                calendar);
        cursor.close();

        return data;
    }

    public ScheduleData getMemoSchedule(int memoId) {

        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT * FROM MemoSchedule WHERE memoId='"+memoId+"' ORDER BY _id DESC LIMIT 1", null);

        ScheduleData data = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long)cursor.getInt(2)*1000);

            data = new ScheduleData(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    calendar);
        }
        cursor.close();

        return data;
    }

    public ArrayList<ScheduleData> getNextData() { // 같은시간에 울리는것 리스트에 담아서 전달

        // 오름 차순 정렬후 첫번째꺼 (timeMiles와 가장 가까운 알람)
        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT DISTINCT _id, "+COLUMN_SCHEDULE_MEMO_ID+", "+COLUMN_SCHEDULE_ALARM_DATE+" "+
                "FROM "+TABLE_NAME_SCHEDULE+" " +
                "WHERE "+COLUMN_SCHEDULE_ALARM_DATE+"=" +
                "   (SELECT "+COLUMN_SCHEDULE_ALARM_DATE+" " +
                "   FROM "+TABLE_NAME_SCHEDULE+" " +
                "   ORDER BY "+COLUMN_SCHEDULE_ALARM_DATE+" ASC LIMIT 1) ", null);

        ArrayList<ScheduleData> data = new ArrayList<>();
        while(cursor.moveToNext()) {
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) cursor.getInt(2) * 1000);

            data.add(new ScheduleData(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    calendar));
        }
        cursor.close();

        return data;
    }

    public ScheduleData getNextDataByTime(long timeMiles) {

        // 오름 차순 정렬후 첫번째꺼 (timeMiles와 가장 가까운 알람)
        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT DISTINCT memoId, alarmDate FROM MemoSchedule WHERE alarmDate>"+(int)(timeMiles/1000)+" ORDER BY alarmDate ASC LIMIT 1", null);

        ScheduleData data = null;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) cursor.getInt(1) * 1000);

            data = new ScheduleData(
                    cursor.getInt(0),
                    calendar);
        }
        cursor.close();

        return data;
    }

    public ArrayList<ScheduleData> getAllData() {

        // 오름 차순 정렬후 첫번째꺼 (timeMiles와 가장 가까운 알람)
        Cursor cursor = dBmanager.getDbR().rawQuery("SELECT * FROM MemoSchedule", null);

        ArrayList<ScheduleData> arrData = new ArrayList<>();
        ScheduleData data;
        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long)cursor.getInt(2)*1000);

            data = new ScheduleData(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    calendar);
            arrData.add(data);
        }
        cursor.close();

        return arrData;
    }

    public void close() {
        dBmanager.getDbR().close();
    }
}
