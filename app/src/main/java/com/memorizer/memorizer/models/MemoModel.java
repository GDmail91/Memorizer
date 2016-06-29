package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.memorizer.memorizer.create.MemoData;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by YS on 2016-06-21.
 */
public class MemoModel extends SQLiteOpenHelper {

    public MemoModel(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);
        db.execSQL("CREATE TABLE Memo ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "memoContent TEXT, " +
                "memoDuring INTEGER, " +
                "memoTerm INTEGER, " +
                "memoTimeHour INTEGER, " +
                "memoTimeMinute INTEGER, " +
                "posted DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS dic");
        onCreate(db);
        db.close();
    }

    /** 삽입 SQL
     *
     * @param memoData
     * @return topNumber
     */
    public int insert(MemoData memoData) {
        SQLiteDatabase dbR = getReadableDatabase();
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
            sql = "INSERT INTO Memo (_id, memoContent, memoDuring, memoTerm, memoTimeHour, memoTimeMinute) " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'" + memoData.getContent() + "', " +
                    "'" + memoData.getWhileDate().getTimeInMillis() + "', " +
                    "'" + memoData.getTerm() + "', " +
                    "'" + memoData.getTimeOfHour() + "', " +
                    "'" + memoData.getTimeOfMinute() + "');";
        } else {
            sql = "INSERT INTO Memo (_id, memoContent, memoTerm, memoTimeHour, memoTimeMinute) " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'" + memoData.getContent() + "', " +
                    "'" + memoData.getTerm() + "', " +
                    "'" + memoData.getTimeOfHour() + "', " +
                    "'" + memoData.getTimeOfMinute() + "');";
        }

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
     * @param memoData
     * @return id
     */
    public int update(MemoData memoData) {
        String sql = "UPDATE Memo SET " +
                "memoContent='" + memoData.getContent() + "', " +
                "memoDuring='" + memoData.getWhileDate().getTimeInMillis() + "', " +
                "memoTerm='" + memoData.getTerm() + "', " +
                "memoTimeHour='" + memoData.getTimeOfHour() + "', " +
                "memoTimeMinute='" + memoData.getTimeOfMinute() + "' " +
                "WHERE _id='"+memoData.get_id()+"' ;";

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

        return memoData.get_id();
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
            db.execSQL("DELETE FROM Memo WHERE _id='" + ids + "'");
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
            db.execSQL("DELETE FROM Memo");
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

        Cursor cursor = db.rawQuery("SELECT * FROM Memo ORDER BY _id DESC", null);
        while(cursor.moveToNext()) {
            count += cursor.getInt(0);
        }
        db.close();
        return count;
    }

    public ArrayList<MemoData> getAllData() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MemoData> allData = new ArrayList<>();
        int i =0;
        Cursor cursor = db.rawQuery("SELECT * FROM Memo ORDER BY _id DESC", null);
        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getInt(2));

            MemoData tempData = new MemoData(
                    cursor.getInt(0),
                    cursor.getString(1),
                    calendar,
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6));

            allData.add(i++, tempData);
        }

        db.close();
        return allData;
    }

    public MemoData getData(int id) {
        SQLiteDatabase db = getReadableDatabase();
        MemoData data = new MemoData();

        Cursor cursor = db.rawQuery("SELECT * FROM Memo WHERE _id='"+id+"' ORDER BY _id DESC", null);
        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getInt(2));

            data.set_id(cursor.getInt(0));
            data.setContent(cursor.getString(1));
            data.setWhileDate(calendar);
            data.setTerm(cursor.getInt(3));
            data.setTimeOfHour(cursor.getInt(4));
            data.setTimeOfMinute(cursor.getInt(5));
            data.setPosted(cursor.getString(6));
        }

        db.close();
        return data;
    }
}