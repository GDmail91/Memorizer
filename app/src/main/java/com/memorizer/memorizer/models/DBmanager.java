package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by YS on 2016-07-11.
 */
public class DBmanager extends SQLiteOpenHelper {
    protected static final int DB_VERSION = 4;

    public DBmanager(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, DB_VERSION);
    }

    // Memo 테이블
    private static final String TABLE_NAME_MEMO = "Memo";
    private static final String COLUMN_MEMO_CONTENT = "memoContent";
    private static final String COLUMN_MEMO_DURING= "memoDuring";
    private static final String COLUMN_MEMO_TERM = "memoTerm";
    private static final String COLUMN_MEMO_LABEL = "memoLabel";
    private static final String COLUMN_MEMO_IS_RANDOM = "isRandom";
    private static final String COLUMN_MEMO_HOUR = "memoTimeHour";
    private static final String COLUMN_MEMO_MINUTE = "memoTimeMinute";
    private static final String COLUMN_MEMO_POSTED = "posted";

    // MemoSchedule 테이블
    private static final String TABLE_NAME_SCHEDULE = "MemoSchedule";
    private static final String COLUMN_SCHEDULE_MEMO_ID = "memoId";
    private static final String COLUMN_SCHEDULE_ALARM_DATE = "alarmDate";

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);

        Log.d("MEMOMODEL", "생성");
        db.execSQL("CREATE TABLE "+TABLE_NAME_MEMO+" ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MEMO_CONTENT+" TEXT, " +
                COLUMN_MEMO_DURING+" INTEGER DEFAULT 0, " +
                COLUMN_MEMO_TERM+" INTEGER, " +
                COLUMN_MEMO_LABEL+" TEXT, " +
                COLUMN_MEMO_IS_RANDOM+" INTEGER DEFAULT 0, " +
                COLUMN_MEMO_HOUR+" INTEGER, " +
                COLUMN_MEMO_MINUTE+" INTEGER, " +
                COLUMN_MEMO_POSTED+" DATETIME DEFAULT CURRENT_TIMESTAMP);");

        db.execSQL("CREATE TABLE "+TABLE_NAME_SCHEDULE+" ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCHEDULE_MEMO_ID+" INTEGER, " +
                COLUMN_SCHEDULE_ALARM_DATE+" INTEGER);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SQLite DB 버전 관리
        Log.d("MEMOMODEL", oldVersion + " => " +newVersion);

        switch (oldVersion)
        {
            case 1:
            case 2:
            case 3:
            case 4:
                // 기존 버전
                db.execSQL("CREATE TABLE "+TABLE_NAME_MEMO+" ( " +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_MEMO_CONTENT+" TEXT, " +
                        COLUMN_MEMO_DURING+" INTEGER DEFAULT 0, " +
                        COLUMN_MEMO_TERM+" INTEGER, " +
                        COLUMN_MEMO_IS_RANDOM+" INTEGER DEFAULT 0, " +
                        COLUMN_MEMO_HOUR+" INTEGER, " +
                        COLUMN_MEMO_MINUTE+" INTEGER, " +
                        COLUMN_MEMO_POSTED+" DATETIME DEFAULT CURRENT_TIMESTAMP);");

                db.execSQL("CREATE TABLE "+TABLE_NAME_SCHEDULE+" ( " +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_SCHEDULE_MEMO_ID+" INTEGER, " +
                        COLUMN_SCHEDULE_ALARM_DATE+" INTEGER);");
            case 5:
                //upgrade from version 4 to 5
                db.execSQL("ALTER TABLE " + TABLE_NAME_MEMO + " ADD COLUMN " + COLUMN_MEMO_LABEL + " TEXT;");
        }
    }

    /**
     * DB 재 생성 필요시
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onRecreate(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("MEMOMODEL", oldVersion + " => " +newVersion);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_MEMO);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_SCHEDULE);
        onCreate(db);
    }
}
