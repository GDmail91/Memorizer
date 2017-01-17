package com.memorizer.memorizer.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by YS on 2016-07-11.
 */
public class DBmanager extends SQLiteOpenHelper{
    private static final String TAG = "DBManager";
    protected static final int DB_VERSION = 5;
    private static final String DB_NAME = "Memo.db";

    private SQLiteDatabase dbR;
    private SQLiteDatabase dbW;

    public DBmanager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        dbR = getReadableDatabase();
        dbW = getWritableDatabase();
    }

    // Memo 테이블
    protected static final String TABLE_NAME_MEMO = "Memo";
    protected static final String COLUMN_MEMO_CONTENT = "memoContent";
    protected static final String COLUMN_MEMO_DURING= "memoDuring";
    protected static final String COLUMN_MEMO_TERM = "memoTerm";
    protected static final String COLUMN_MEMO_LABEL = "memoLabel";
    protected static final String COLUMN_MEMO_IS_RANDOM = "isRandom";
    protected static final String COLUMN_MEMO_HOUR = "memoTimeHour";
    protected static final String COLUMN_MEMO_MINUTE = "memoTimeMinute";
    protected static final String COLUMN_MEMO_POSTED = "posted";
    protected static final String COLUMN_MEMO_EDITED = "edited";

    // Label 테이블
    protected static final String TABLE_NAME_LABEL = "Label";
    protected static final String COLUMN_LABEL_NAME = "labelName";
    protected static final String COLUMN_LABEL_COLOR = "labelColor";

    // MemoSchedule 테이블
    protected static final String TABLE_NAME_SCHEDULE = "MemoSchedule";
    protected static final String COLUMN_SCHEDULE_MEMO_ID = "memoId";
    protected static final String COLUMN_SCHEDULE_ALARM_DATE = "alarmDate";

    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블을 생성한다.
        // create table 테이블명 (컬럼명 타입 옵션);

        Log.d(TAG, "생성");
        db.execSQL("CREATE TABLE "+TABLE_NAME_MEMO+" ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MEMO_CONTENT+" TEXT, " +
                COLUMN_MEMO_DURING+" INTEGER DEFAULT 0, " +
                COLUMN_MEMO_TERM+" INTEGER, " +
                COLUMN_MEMO_IS_RANDOM+" INTEGER DEFAULT 0, " +
                COLUMN_MEMO_HOUR+" INTEGER, " +
                COLUMN_MEMO_MINUTE+" INTEGER, " +
                COLUMN_MEMO_POSTED+" DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_MEMO_LABEL+" INTEGER, " +
                COLUMN_MEMO_EDITED+" DATETIME DEFAULT CURRENT_TIMESTAMP " +
                ");");

        db.execSQL("CREATE TABLE "+TABLE_NAME_SCHEDULE+" ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCHEDULE_MEMO_ID+" INTEGER, " +
                COLUMN_SCHEDULE_ALARM_DATE+" INTEGER " +
                ");");

        db.execSQL("CREATE TABLE "+TABLE_NAME_LABEL+" (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LABEL_NAME+" TEXT, " +
                COLUMN_LABEL_COLOR+" INTEGER " +
                ");");

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SQLite DB 버전 관리
        Log.d(TAG, oldVersion + " => " +newVersion);

        switch (oldVersion)
        {
            case 1:
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
            case 2:
            case 3:
            case 4:
                //upgrade from version 4 to 5
                db.execSQL("ALTER TABLE " + TABLE_NAME_MEMO + " ADD COLUMN " + COLUMN_MEMO_LABEL + " INTEGER;");
                db.execSQL("CREATE TABLE "+TABLE_NAME_LABEL+" (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_LABEL_NAME+" TEXT, " +
                        COLUMN_LABEL_COLOR+" INTEGER " +
                        ");");
                db.execSQL("ALTER TABLE " + TABLE_NAME_MEMO + " ADD COLUMN " + COLUMN_MEMO_EDITED + " DATETIME;");
            case 5:

        }
    }

    /**
     * DB 재 생성 필요시
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onRecreate(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, oldVersion + " => " +newVersion);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_MEMO);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_LABEL);
        onCreate(db);
    }

    protected void startTransaction(ArrayList<String> sqlList) {
        // DB 작업 실행
        dbW.beginTransaction();
        try {
            for (String sql : sqlList) {
                dbW.execSQL(sql);
                Log.d(TAG, sql);
            }
            dbW.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbW.endTransaction(); //트랜잭션을 끝내는 메소드.
        }
    }

    public SQLiteDatabase getDbR() {return dbR;}

    public SQLiteDatabase getDbW() {return dbW;}
}
