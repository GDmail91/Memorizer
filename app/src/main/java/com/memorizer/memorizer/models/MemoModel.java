package com.memorizer.memorizer.models;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Calendar;

import static com.memorizer.memorizer.models.DBmanager.COLUMN_LABEL_COLOR;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_LABEL_NAME;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_CONTENT;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_DURING;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_HOUR;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_IS_RANDOM;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_LABEL;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_MINUTE;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_POSTED;
import static com.memorizer.memorizer.models.DBmanager.COLUMN_MEMO_TERM;
import static com.memorizer.memorizer.models.DBmanager.TABLE_NAME_LABEL;
import static com.memorizer.memorizer.models.DBmanager.TABLE_NAME_MEMO;
import static com.memorizer.memorizer.models.DBmanager.dbR;
import static com.memorizer.memorizer.models.DBmanager.dbW;

/**
 * Created by YS on 2016-06-21.
 */
public class MemoModel {
    private static final String TAG = "MemoModel";

    private DBmanager dBmanager;

    public MemoModel(DBmanager dBmanager) {
        this.dBmanager = dBmanager;
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
        ArrayList<String> sqlList = new ArrayList<>();

        // 라벨 모델 삽입
        int labelTopNumber = 0;
        String checkSql = "SELECT _id FROM "+TABLE_NAME_LABEL+" "+
                "WHERE "+COLUMN_LABEL_NAME+"='" + memoData.getLabel() + "' " +
                "AND "+COLUMN_LABEL_COLOR+"='" + memoData.getLabelPos() + "'";
        Cursor checkCursor = dbR.rawQuery(checkSql, null);

        if (checkCursor != null && checkCursor.moveToFirst()) {
            // 라벨이 이미 등록되어 있는 경우
            labelTopNumber =  checkCursor.getInt(0);
            checkCursor.close();
        } else {
            // 라벨이 없는 경우
            Cursor labelCursor = dbR.rawQuery("SELECT _id FROM " + TABLE_NAME_LABEL + " ORDER BY _id DESC LIMIT 1", null);
            if (labelCursor != null && labelCursor.moveToFirst()) {
                do {
                    labelTopNumber = labelCursor.getInt(0);
                } while (labelCursor.moveToNext());
                labelCursor.close();
            }


            labelTopNumber = labelTopNumber + 1;

            sqlList.add("INSERT INTO " + TABLE_NAME_LABEL + " (_id, " + COLUMN_LABEL_NAME + ", " + COLUMN_LABEL_COLOR + ") " +
                    "VALUES(" +
                    "'" + labelTopNumber + "', " +
                    "'" + memoData.getLabel() + "', " +
                    "'" + memoData.getLabelPos() + "');");
        }


        // 메모 모델 삽입
        Cursor cursor = dbR.rawQuery("SELECT _id FROM "+TABLE_NAME_MEMO+" ORDER BY _id DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                topNumber = cursor.getInt(0);
            } while (cursor.moveToNext());
            cursor.close();
        }

        topNumber = topNumber+1;

        if (memoData.getWhileDate() != null) {
            sqlList.add("INSERT INTO "+TABLE_NAME_MEMO+" (_id, "+COLUMN_MEMO_CONTENT+", "+COLUMN_MEMO_DURING+", " +
                    COLUMN_MEMO_TERM+", "+COLUMN_MEMO_LABEL+", "+COLUMN_MEMO_IS_RANDOM+", "+COLUMN_MEMO_HOUR+", "+COLUMN_MEMO_MINUTE+") " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'" + memoData.getContent() + "', " +
                    "'" + memoData.getWhileDate().getTimeInMillis() + "', " +
                    "'" + memoData.getTerm() + "', " +
                    "'" + labelTopNumber + "', " +
                    "'" + memoData.isRandom() + "', " +
                    "'" + memoData.getTimeOfHour() + "', " +
                    "'" + memoData.getTimeOfMinute() + "');");
        } else {
            sqlList.add("INSERT INTO "+TABLE_NAME_MEMO+" (_id, "+COLUMN_MEMO_CONTENT+", "+COLUMN_MEMO_TERM+", " +
                    COLUMN_MEMO_LABEL+", "+COLUMN_MEMO_IS_RANDOM+", "+COLUMN_MEMO_HOUR+", "+COLUMN_MEMO_MINUTE+") " +
                    "VALUES(" +
                    "'" + topNumber + "', " +
                    "'" + memoData.getContent() + "', " +
                    "'" + memoData.getTerm() + "', " +
                    "'" + labelTopNumber + "', " +
                    "'" + memoData.isRandom() + "', " +
                    "'" + memoData.getTimeOfHour() + "', " +
                    "'" + memoData.getTimeOfMinute() + "');");
        }

        // 트랜잭션 실행
        dBmanager.startTransaction(sqlList);

        return topNumber;
    }

    /** 수정 SQL
     *
     * @param memoData
     * @return id
     */
    public int update(MemoData memoData) {
        ArrayList<String> sqlList = new ArrayList<>();

        // 변경된 라벨 삽입
        int labelTopNumber = 0;
        String checkSql = "SELECT _id FROM "+TABLE_NAME_LABEL+" "+
                "WHERE "+COLUMN_LABEL_NAME+"='" + memoData.getLabel() + "' " +
                "AND "+COLUMN_LABEL_COLOR+"='" + memoData.getLabelPos() + "'";
        Cursor checkCursor = dbR.rawQuery(checkSql, null);

        if (checkCursor != null && checkCursor.moveToFirst()) {
            // 라벨이 이미 등록되어 있는 경우
            labelTopNumber =  checkCursor.getInt(0);
            checkCursor.close();
        } else {
            // 라벨이 없는 경우
            Cursor labelCursor = dbR.rawQuery("SELECT _id FROM " + TABLE_NAME_LABEL + " ORDER BY _id DESC LIMIT 1", null);
            if (labelCursor != null && labelCursor.moveToFirst()) {
                do {
                    labelTopNumber = labelCursor.getInt(0);
                } while (labelCursor.moveToNext());
                labelCursor.close();
            }


            labelTopNumber = labelTopNumber + 1;

            sqlList.add("INSERT INTO " + TABLE_NAME_LABEL + " (_id, " + COLUMN_LABEL_NAME + ", " + COLUMN_LABEL_COLOR + ") " +
                    "VALUES(" +
                    "'" + labelTopNumber + "', " +
                    "'" + memoData.getLabel() + "', " +
                    "'" + memoData.getLabelPos() + "');");
        }

        // 메모 업데이트
        sqlList.add("UPDATE "+TABLE_NAME_MEMO+" SET " +
                COLUMN_MEMO_CONTENT+"='" + memoData.getContent() + "', " +
                COLUMN_MEMO_DURING+"='" + memoData.getWhileDate().getTimeInMillis() + "', " +
                COLUMN_MEMO_TERM+"='" + memoData.getTerm() + "', " +
                COLUMN_MEMO_LABEL+"='" + labelTopNumber + "', " +
                COLUMN_MEMO_IS_RANDOM+"='" + memoData.isRandom() + "', " +
                COLUMN_MEMO_HOUR+"='" + memoData.getTimeOfHour() + "', " +
                COLUMN_MEMO_MINUTE+"='" + memoData.getTimeOfMinute() + "' " +
                "WHERE _id='"+memoData.get_id()+"' ;");

        // 트랜잭션 실행
        dBmanager.startTransaction(sqlList);

        return memoData.get_id();
    }

    public void update(String _query) {
        dbW.execSQL(_query);
    }

    public void delete(MemoData memoData) {
        dbW.beginTransaction();
        try {
            int labelId = 0;
            Cursor cursor = dbR.rawQuery("SELECT "+COLUMN_MEMO_LABEL+" FROM "+TABLE_NAME_MEMO+" WHERE _id='"+memoData.get_id()+"'", null);
            if (cursor != null && cursor.moveToFirst()) {
                labelId = cursor.getInt(0);
                cursor.close();
            }

            dbW.execSQL("DELETE FROM "+TABLE_NAME_MEMO+" WHERE _id='" +memoData.get_id()+ "'");

            cursor = dbR.rawQuery("SELECT _id FROM "+TABLE_NAME_MEMO+" WHERE "+COLUMN_MEMO_LABEL+"="+labelId, null);
            if (cursor == null) {
                dbW.execSQL("DELETE FROM "+TABLE_NAME_LABEL+" WHERE _id='" + labelId + "'");
            } else {
                cursor.close();
            }

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
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getString(7));

            allData.add(i++, tempData);
        }

        return allData;
    }

    public ArrayList<MemoData> getAllDataShort() {
        ArrayList<MemoData> allData = new ArrayList<>();
        int i =0;
        Cursor cursor = dbR.rawQuery("SELECT "+TABLE_NAME_MEMO+"._id, " +
                "substr("+COLUMN_MEMO_CONTENT+",0,25) AS "+COLUMN_MEMO_CONTENT+", "+
                COLUMN_MEMO_DURING+", "+
                COLUMN_MEMO_TERM+", "+
                TABLE_NAME_LABEL+"."+COLUMN_LABEL_NAME+", "+
                TABLE_NAME_LABEL+"."+COLUMN_LABEL_COLOR+", " +
                COLUMN_MEMO_IS_RANDOM+", "+
                COLUMN_MEMO_HOUR+", "+
                COLUMN_MEMO_MINUTE+", "+
                COLUMN_MEMO_POSTED+" " +
                "FROM "+TABLE_NAME_MEMO+" INNER JOIN "+TABLE_NAME_LABEL+" " +
                "ON "+TABLE_NAME_MEMO+"."+COLUMN_MEMO_LABEL+"="+TABLE_NAME_LABEL+"._id ORDER BY "+TABLE_NAME_MEMO+"._id DESC", null);

        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(2));

            MemoData tempData = new MemoData(
                    cursor.getInt(0),
                    cursor.getString(1),
                    calendar,
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getString(9));

            allData.add(i++, tempData);
        }
        cursor.close();

        return allData;
    }

    public MemoData getData(int id) {
        MemoData data = null;

        Cursor cursor = dbR.rawQuery("SELECT "+TABLE_NAME_MEMO+"._id, " +
                "substr("+COLUMN_MEMO_CONTENT+",0,25) AS "+COLUMN_MEMO_CONTENT+", "+
                COLUMN_MEMO_DURING+", "+
                COLUMN_MEMO_TERM+", "+
                TABLE_NAME_LABEL+"."+COLUMN_LABEL_NAME+", "+
                TABLE_NAME_LABEL+"."+COLUMN_LABEL_COLOR+", " +
                COLUMN_MEMO_IS_RANDOM+", "+
                COLUMN_MEMO_HOUR+", "+
                COLUMN_MEMO_MINUTE+", "+
                COLUMN_MEMO_POSTED+" " +
                "FROM "+TABLE_NAME_MEMO+" INNER JOIN "+TABLE_NAME_LABEL+" " +
                "ON "+TABLE_NAME_MEMO+"."+COLUMN_MEMO_LABEL+"="+TABLE_NAME_LABEL+"._id " +
                "WHERE "+TABLE_NAME_MEMO+"._id='"+id+"' ORDER BY "+TABLE_NAME_MEMO+"._id DESC", null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(2));

            data = new MemoData();
            data.set_id(cursor.getInt(0));
            data.setContent(cursor.getString(1));
            data.setWhileDate(calendar);
            data.setTerm(cursor.getInt(3));
            data.setLabel(cursor.getString(8));
            data.setLabelPos(cursor.getInt(9));
            data.setRandom(cursor.getInt(4));
            data.setTimeOfHour(cursor.getInt(5));
            data.setTimeOfMinute(cursor.getInt(6));
            data.setPosted(cursor.getString(7));
        }
        cursor.close();

        return data;
    }

    public ArrayList<MemoData> getSearchData(String searchText) {
        ArrayList<MemoData> allData = new ArrayList<>();
        int i =0;
        // TODO 쿼리 완성
        Cursor cursor = dbR.rawQuery("SELECT "+TABLE_NAME_MEMO+"._id, " +
                "substr("+COLUMN_MEMO_CONTENT+",0,25) AS "+COLUMN_MEMO_CONTENT+", "+
                COLUMN_MEMO_DURING+", "+
                COLUMN_MEMO_TERM+", "+
                TABLE_NAME_LABEL+"."+COLUMN_LABEL_NAME+", "+
                TABLE_NAME_LABEL+"."+COLUMN_LABEL_COLOR+", " +
                COLUMN_MEMO_IS_RANDOM+", "+
                COLUMN_MEMO_HOUR+", "+
                COLUMN_MEMO_MINUTE+", "+
                COLUMN_MEMO_POSTED+" " +
                "FROM "+TABLE_NAME_MEMO+" INNER JOIN "+TABLE_NAME_LABEL+" " +
                "ON "+TABLE_NAME_MEMO+"."+COLUMN_MEMO_LABEL+"="+TABLE_NAME_LABEL+"._id " +
                "WHERE memoContent LIKE '%"+searchText+"%' " +
                "ORDER BY "+TABLE_NAME_MEMO+"._id DESC", null);

        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(2));

            MemoData tempData = new MemoData(
                    cursor.getInt(0),
                    cursor.getString(1),
                    calendar,
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getString(9));

            allData.add(i++, tempData);
        }
        cursor.close();

        return allData;
    }

    public void close() {
        dbR.close();
    }
}