package com.memorizer.memorizer.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TimePicker;
import android.widget.Toast;

import com.memorizer.memorizer.MainActivity;
import com.memorizer.memorizer.R;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.scheduler.Scheduler;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Created by YS on 2016-06-20.
 */
public class MemoCreate extends AppCompatActivity {
    String TAG = "MemoCreate";
    EditText alarmContent;
    Button alarmWhileBtn, alarmTermBtn, alarmTimeBtn;
    MemoData memoData = new MemoData();
    android.support.v7.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_memo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        alarmContent = (EditText) findViewById(R.id.alarm_content);
        alarmTermBtn = (Button) findViewById(R.id.alarm_term_btn);
        alarmWhileBtn = (Button) findViewById(R.id.alarm_while_btn);
        alarmTimeBtn = (Button) findViewById(R.id.alarm_time_btn);

        dialog = new AlertDialog.Builder(this).create();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null
        && bundle.getBoolean("is_edit")) {
            MemoModel memoModel = new MemoModel(this, "Memo.db", null);
            this.memoData = memoModel.getData(bundle.getInt("memo_id"));
            memoModel.close();

            alarmContent.setText(memoData.getContent());
            alarmTermBtn.setText(""+memoData.getTerm());
            String during;
            if (memoData.getWhileDate() == null
                    || memoData.getWhileDate().getTimeInMillis() == 0)
                during = "무제한";
            else {
                during = memoData.getWhileDate().get(Calendar.YEAR) + "." +
                        memoData.getWhileDate().get(Calendar.MONTH) + "." +
                        memoData.getWhileDate().get(Calendar.DAY_OF_MONTH);
            }
            alarmWhileBtn.setText(during);
            alarmTimeBtn.setText(makeTime(memoData.getTimeOfHour(), memoData.getTimeOfMinute()));
        }
    }

    public void onClickListen(View v) {
        switch (v.getId()) {
            case R.id.alarm_term_btn:
                // 버튼 클릭시 팝업 메뉴가 나오게 하기
                // PopupMenu 는 API 11 레벨부터 제공한다
                PopupMenu p = new PopupMenu(getApplicationContext(), v);
                this.getMenuInflater().inflate(R.menu.activity_create_memo_termpick, p.getMenu());
                // 이벤트 처리
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String[] days = item.getTitle().toString().split(" ");
                        if (days[1].equals("일")) {
                            memoData.setTerm(Integer.parseInt(days[0]));
                        } else {
                            // TODO 달로 저장
                            memoData.setTerm(Integer.parseInt(days[0]));
                        }

                        // 버튼 글자 바꿈
                        alarmTermBtn.setText(item.getTitle());
                        return false;
                    }
                });
                p.show();
                break;

            // 기간 설정
            case R.id.alarm_while_btn:
                // Dialog 생성
                LayoutInflater whileInflater = this.getLayoutInflater();
                View dialogWhileView = whileInflater.inflate(R.layout.dialog_during_select, null);
                android.support.v7.app.AlertDialog.Builder whileDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                //AlertDialog.Builder whileDialogBuilder= new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
                whileDialogBuilder.setTitle("기간 설정"); //Dialog 제목
                whileDialogBuilder.setIcon(android.R.drawable.ic_menu_today); //제목옆의 아이콘 이미지(원하는 이미지 설정)
                whileDialogBuilder.setView(dialogWhileView);

                //설정한 값으로 AlertDialog 객체 생성
                dialog=whileDialogBuilder.create();

                //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
                dialog.setCanceledOnTouchOutside(true); // 없어지도록 설정
                dialog.show();
                break;

            // 기간 다이얼로그 > 무제한
            case R.id.alarm_while_unlimit:
                memoData.setWhileDate(null);
                // 버튼 글자 바꿈
                alarmWhileBtn.setText("무제한");

                // Dialog창 사라짐
                dialog.cancel();
                break;

            // 기간 다이얼로그 > 기간 설정
            case R.id.alarm_while_set_limit:
                GregorianCalendar calendar = new GregorianCalendar();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day= calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(MemoCreate.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, monthOfYear, dayOfMonth);
                        memoData.setWhileDate(cal);

                        // Dialog창 사라짐
                        dialog.cancel();

                        // 버튼 글자 바꿈
                        String ment = memoData.getWhileDate().get(Calendar.YEAR) + "." + memoData.getWhileDate().get(Calendar.MONTH) + "." + memoData.getWhileDate().get(Calendar.DAY_OF_MONTH);

                        alarmWhileBtn.setText(ment);
                    }
                }, year, month, day).show();

                break;

            // 시간 설정 버튼
            case R.id.alarm_time_btn:
                // Dialog 생성
                LayoutInflater timeInflater = this.getLayoutInflater();
                View dialogTimeView = timeInflater.inflate(R.layout.dialog_time_select, null);
                android.support.v7.app.AlertDialog.Builder timeDialogBuider= new android.support.v7.app.AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
                timeDialogBuider.setTitle("시간 설정"); //Dialog 제목
                timeDialogBuider.setIcon(android.R.drawable.ic_menu_day); //제목옆의 아이콘 이미지(원하는 이미지 설정)
                timeDialogBuider.setView(dialogTimeView);

                //설정한 값으로 AlertDialog 객체 생성
                dialog=timeDialogBuider.create();

                //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
                dialog.setCanceledOnTouchOutside(true); // 없어지도록 설정
                dialog.show();
                break;

            // 시간 설정 다이얼로그 > 랜덤
            case R.id.alarm_time_random:
                memoData.setRandom();
                Random random = new Random(System.currentTimeMillis());
                memoData.setTimeOfHour(random.nextInt(24));
                memoData.setTimeOfMinute(random.nextInt(60));
                // 버튼 글자 바꿈
                alarmTimeBtn.setText("랜덤");

                // Dialog창 사라짐
                dialog.cancel();
                break;

            // 시간 설정 다이얼로그 > 직접 설정
            case R.id.alarm_time_set:
                new TimePickerDialog(MemoCreate.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        memoData.setTimeOfHour(hourOfDay);
                        memoData.setTimeOfMinute(minute);

                        // 버튼 글자 바꿈
                        alarmTimeBtn.setText(makeTime(hourOfDay, minute));

                        // Dialog창 사라짐
                        dialog.cancel();
                    }
                }, 0, 0, false).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_create_memo_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.memo_create) {
            Log.d(TAG, alarmContent.getText().toString());
            if (alarmContent.getText().toString().length() != 0) {
                Bundle bundle = getIntent().getExtras();
                memoData.setContent(alarmContent.getText().toString());

                // DB에 저장
                MemoModel memoModel = new MemoModel(this, "Memo.db", null);
                if (bundle != null
                && bundle.getBoolean("is_edit")) {
                    memoModel.update(memoData);

                    // 알림 설정
                    Scheduler.getScheduler().setSchedule(this, memoData, true);
                } else {
                    memoData.set_id(memoModel.insert(memoData));

                    Log.d("TEST", "설정하는 시간: "+memoData.getTimeOfHour()+":"+memoData.getTimeOfMinute());
                    // 알림 설정
                    Scheduler.getScheduler().setSchedule(this, memoData, true);
                }

                memoModel.close();

                Intent intent = new Intent(MemoCreate.this, MainActivity.class);
                intent.putExtra("mCreate", memoData);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "메모 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected String makeTime(int hourOfDay, int minute) {
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

        return hourStr +" : "+minStr+ " "+ampm;
    }
}
