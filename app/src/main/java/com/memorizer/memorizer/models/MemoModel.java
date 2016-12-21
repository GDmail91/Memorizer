package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by YS on 2016-06-21.
 */
public class MemoModel extends DBmanager {
    private static final String TAG = "MemoModel";
    SQLiteDatabase dbR = getReadableDatabase();
    SQLiteDatabase dbW = getWritableDatabase();

    public MemoModel(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public void fuckyou() {
        dbW.execSQL("DROP TABLE IF EXISTS Memo");
        dbW.execSQL("DROP TABLE IF EXISTS MemoSchedule");
    }

    /** 삽입 SQL
     *
     * @param memoData
     * @return topNumber
     */
    public int insert(MemoData memoData) {
        int topNumber = 0;

        Cursor cursor = dbR.rawQuery("SELECT _id FROM Memo ORDER BY _id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }

        topNumber = topNumber+1;

        String sql;
        if (memoData.getWhileDate() != null) {
            sql = "INSERT INTO Memo (_id, memoContent, memoDuring, memoTerm, isRandom, memoTimeHour, memoTimeMinute) " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'" + memoData.getContent() + "', " +
                    "'" + memoData.getWhileDate().getTimeInMillis() + "', " +
                    "'" + memoData.getTerm() + "', " +
                    "'" + memoData.isRandom() + "', " +
                    "'" + memoData.getTimeOfHour() + "', " +
                    "'" + memoData.getTimeOfMinute() + "');";
        } else {
            sql = "INSERT INTO Memo (_id, memoContent, memoTerm, isRandom, memoTimeHour, memoTimeMinute) " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'" + memoData.getContent() + "', " +
                    "'" + memoData.getTerm() + "', " +
                    "'" + memoData.isRandom() + "', " +
                    "'" + memoData.getTimeOfHour() + "', " +
                    "'" + memoData.getTimeOfMinute() + "');";
        }

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
     * @param memoData
     * @return id
     */
    public int update(MemoData memoData) {
        String sql = "UPDATE Memo SET " +
                "memoContent='" + memoData.getContent() + "', " +
                "memoDuring='" + memoData.getWhileDate().getTimeInMillis() + "', " +
                "memoTerm='" + memoData.getTerm() + "', " +
                "isRandom='" + memoData.isRandom() + "', " +
                "memoTimeHour='" + memoData.getTimeOfHour() + "', " +
                "memoTimeMinute='" + memoData.getTimeOfMinute() + "' " +
                "WHERE _id='"+memoData.get_id()+"' ;";

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

        return memoData.get_id();
    }

    public void update(String _query) {
        dbW.execSQL(_query);
    }

    public void delete(int ids) {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM Memo WHERE _id='" + ids + "'");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public void deleteAll() {
        dbW.beginTransaction();
        try {
            dbW.execSQL("DELETE FROM Memo");
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction();
        }
    }

    public int printCountOfData() {
        int count=0;

        Cursor cursor = dbR.rawQuery("SELECT * FROM Memo ORDER BY _id DESC", null);
        while(cursor.moveToNext()) {
            count += cursor.getInt(0);
        }
        return count;
    }

    public ArrayList<MemoData> getAllData() {
        ArrayList<MemoData> allData = new ArrayList<>();
        int i =0;
        Cursor cursor = dbR.rawQuery("SELECT * FROM Memo ORDER BY _id DESC", null);
        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(2));

            MemoData tempData = new MemoData(
                    cursor.getInt(0),
                    cursor.getString(1),
                    calendar,
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getString(7));

            allData.add(i++, tempData);
        }

        return allData;
    }

    public MemoData getData(int id) {
        MemoData data = null;

        Cursor cursor = dbR.rawQuery("SELECT * FROM Memo WHERE _id='"+id+"' ORDER BY _id DESC", null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(2));

            data = new MemoData();
            data.set_id(cursor.getInt(0));
            data.setContent(cursor.getString(1));
            data.setWhileDate(calendar);
            data.setTerm(cursor.getInt(3));
            data.setRandom(cursor.getInt(4));
            data.setTimeOfHour(cursor.getInt(5));
            data.setTimeOfMinute(cursor.getInt(6));
            data.setPosted(cursor.getString(7));
        }

        return data;
    }

    public void close() {
        dbR.close();
    }
}