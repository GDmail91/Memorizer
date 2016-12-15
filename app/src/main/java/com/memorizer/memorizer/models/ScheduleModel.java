package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by YS on 2016-07-11.
 */
public class ScheduleModel extends DBmanager {
    public static final int SCHEDULE_VERSION = 3;

    SQLiteDatabase dbR = getReadableDatabase();
    SQLiteDatabase dbW = getWritableDatabase();

    public ScheduleModel(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    /** 삽입 SQL
     *
     * @param scheduleData
     * @return topNumber
     */
    public int insert(ScheduleData scheduleData) {
        int topNumber = 0;

        Cursor cursor = dbR.rawQuery("SELECT _id FROM MemoSchedule ORDER BY _id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }

        topNumber = topNumber+1;

        String sql;
        sql = "INSERT INTO MemoSchedule (_id, memoId, alarmDate) " +
                "VALUES(" +
                "'" + topNumber + "', " +
                "'" + scheduleData.getMemoId() + "', " +
                "'" + (int)(scheduleData.getAlarmDate().getTimeInMillis() / 1000) + "');";

        // DB 작업 실행
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }

        return topNumber;
    }

    /** 수정 SQL
     *
     * @param scheduleData
     * @return id
     */
    public int update(ScheduleData scheduleData) {
        String sql = "UPDATE MemoSchedule SET " +
                "memoId='" + scheduleData.getMemoId() + "', " +
                "alarmDate='" + scheduleData.getAlarmDate().getTimeInMillis() + " " +
                "WHERE _id='"+ scheduleData.get_id() +"' ;";

        // DB 작업 실행
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }

        return scheduleData.get_id();
    }

    public void update(String _query) {
        dbW.execSQL(_query);
    }

    public void delete(int ids) {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM MemoSchedule WHERE _id='" + ids + "'");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public void deletePrevious() {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM MemoSchedule WHERE alarmDate<" + (int)(System.currentTimeMillis()/1000) + " OR memoId = 0");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
        dbW.close();
    }

    public void deleteAll() {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM MemoSchedule");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public int printCountOfData() {
        int count=0;

        Cursor cursor = dbR.rawQuery("SELECT * FROM MemoSchedule ORDER BY _id DESC", null);

        count = cursor.getCount();
        cursor.close();
        return count;
    }


    public ScheduleData getData(int id) {

        Cursor cursor = dbR.rawQuery("SELECT * FROM MemoSchedule WHERE _id='"+id+"' ORDER BY _id DESC", null);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cursor.getInt(2));

        ScheduleData data = new ScheduleData(
                cursor.getInt(0),
                cursor.getInt(1),
                calendar);

        return data;
    }

    public ScheduleData getMemoSchedule(int memoId) {

        Cursor cursor = dbR.rawQuery("SELECT * FROM MemoSchedule WHERE memoId='"+memoId+"' ORDER BY _id DESC LIMIT 1", null);

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

    public ScheduleData getNextData(long timeMiles) {

        // 오름 차순 정렬후 첫번째꺼 (timeMiles와 가장 가까운 알람)
        Cursor cursor = dbR.rawQuery("SELECT DISTINCT memoId, alarmDate FROM MemoSchedule WHERE alarmDate>"+(int)(timeMiles/1000)+" ORDER BY alarmDate ASC LIMIT 1", null);

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
        Cursor cursor = dbR.rawQuery("SELECT * FROM MemoSchedule", null);

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
        dbW.close();
        dbR.close();
    }
}
