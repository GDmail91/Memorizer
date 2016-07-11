package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by YS on 2016-07-11.
 */
public class ScheduleModel extends DBmanager {

    public ScheduleModel(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /** 삽입 SQL
     *
     * @param scheduleData
     * @return topNumber
     */
    public int insert(ScheduleData scheduleData) {
        SQLiteDatabase dbR = getReadableDatabase();
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
        SQLiteDatabase dbW = getWritableDatabase();
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }

        dbW.close();
        dbR.close();
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
        SQLiteDatabase dbW = getWritableDatabase();
        dbW.beginTransaction();
        try {
            dbW.execSQL(sql);
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }

        dbW.close();

        return scheduleData.get_id();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(int ids) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM MemoSchedule WHERE _id='" + ids + "'");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void deletePrevious() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM MemoSchedule WHERE alarmDate<" + (int)(System.currentTimeMillis()/1000) + " OR memoId = 0");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM MemoSchedule");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public int printCountOfData() {
        SQLiteDatabase db = getReadableDatabase();
        int count=0;

        Cursor cursor = db.rawQuery("SELECT * FROM MemoSchedule ORDER BY _id DESC", null);

        count = cursor.getCount();
        while(cursor.moveToNext()) {
            Log.d("TEST", ""+cursor.getInt(0));
            Log.d("TEST", ""+cursor.getInt(1));
            Log.d("TEST", ""+cursor.getInt(2));
        }
        cursor.close();
        db.close();
        return count;
    }

    public ScheduleData getData(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM MemoSchedule WHERE _id='"+id+"' ORDER BY _id DESC", null);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cursor.getInt(2));

        ScheduleData data = new ScheduleData(
                cursor.getInt(0),
                cursor.getInt(1),
                calendar);

        db.close();
        return data;
    }

    public ScheduleData getNextData(long timeMiles) {
        SQLiteDatabase db = getReadableDatabase();

        // 오름 차순 정렬후 첫번째꺼 (timeMiles와 가장 가까운 알람)
        Cursor cursor = db.rawQuery("SELECT DISTINCT memoId, alarmDate FROM MemoSchedule WHERE alarmDate>"+(int)(timeMiles/1000)+" ORDER BY alarmDate ASC LIMIT 1", null);

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
        db.close();

        return data;
    }

    public ArrayList<ScheduleData> getAllData() {
        SQLiteDatabase db = getReadableDatabase();

        // 오름 차순 정렬후 첫번째꺼 (timeMiles와 가장 가까운 알람)
        Cursor cursor = db.rawQuery("SELECT * FROM MemoSchedule", null);

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
        db.close();

        return arrData;
    }
}
