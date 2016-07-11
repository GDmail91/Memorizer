package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YS on 2016-07-11.
 */
public class DBmanager extends SQLiteOpenHelper {

    public DBmanager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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

        db.execSQL("CREATE TABLE MemoSchedule ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "memoId INTEGER, " +
                "alarmDate INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Memo");
        db.execSQL("DROP TABLE IF EXISTS MemoSchedule");
        onCreate(db);
        db.close();
    }
}
