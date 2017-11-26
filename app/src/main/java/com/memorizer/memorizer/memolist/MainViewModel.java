package com.memorizer.memorizer.memolist;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.cloudrail.si.CloudRail;
import com.memorizer.memorizer.BuildConfig;
import com.memorizer.memorizer.CloudLinkerDialog;
import com.memorizer.memorizer.DeveloperInfo;
import com.memorizer.memorizer.NoticeDialog;
import com.memorizer.memorizer.R;
import com.memorizer.memorizer.alarm.MemoAlarmDragActivity;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.PrefModel;
import com.memorizer.memorizer.models.ScheduleModel;
import com.memorizer.memorizer.scheduler.Scheduler;

import java.util.ArrayList;

/**
 * Created by soo13 on 2017-11-25.
 */

public class MainViewModel implements ViewModel {
    private Activity context;
    private PrefModel prefModel;

    private static final String BROWSABLE = "android.intent.category.BROWSABLE";

    public MainViewModel(Activity context) {
        this.context = context;
        this.prefModel = new PrefModel(context);
    }

    @Override
    public void onCreate() {
        setDatas();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    public void onNewIntent(Intent intent) {
        if(intent.getCategories().contains(BROWSABLE)) {
            // Here we pass the response to the SDK which will automatically
            // complete the authentication process
            CloudRail.setAuthenticationResponse(intent);
/*
            SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("googledrivePersistent", storage.get().saveAsString()).apply();*/
        }
    }

    public NavigationView.OnNavigationItemSelectedListener navigationSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.made_of) {
                Intent intent = new Intent(context, DeveloperInfo.class);
                context.startActivity(intent);
            } else if (id == R.id.backup_setting){
                cloudLinkerDialog = new CloudLinkerDialog(context);
                cloudLinkerDialog.show();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    public View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, MemoCreate.class);
            context.startActivity(intent);
        }
    };

    public AdapterView.OnItemSelectedListener filterListSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            prefModel.setFilter(position);
            setMemoDatas();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public SwipeRefreshLayout.OnRefreshListener swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // TODO 리프레쉬
            swipeRefreshLayout.setRefreshing(false);
            setMemoDatas();
        }
    };


    public void checkMemoCreated() {
        // MemoCreate에서 메모를 만든경우
        Intent mIntent = context.getIntent();
        MemoData memoData = (MemoData)mIntent.getSerializableExtra("mCreate");
        if (memoData != null) {
            Intent popupIntent = new Intent(context, MemoAlarmDragActivity.class);
            ArrayList<Integer> createdData = new ArrayList<>();
            createdData.add(memoData.get_id());
            popupIntent.putIntegerArrayListExtra("memoId", createdData);
            popupIntent.putExtra("isCreated", true);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(popupIntent);
        }
    }

    public void checkAppVersion() {
        final String version;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;

            // 업데이트 되었을 경우
            if (!version.equals(prefModel.getVersion()) || BuildConfig.BUILD_TYPE.equals("debug")) {
                noticeDialog = new NoticeDialog(this,
                        context.getString(R.string.notice_title),
                        context.getString(R.string.notice),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                prefModel.setVersion(version);
                                noticeDialog.dismiss();
                            }
                        });
                noticeDialog.show();
            }

        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    // memoDatas 재배치
    public void setMemoDatas() {
        // DB에서 메모목록 가져옴
        MemoModel memoModel = new MemoModel(this);
        memoDatas = memoModel.getAllDataShort(prefModel.getFilter()); // Content 글자수 제한

        if (memoListAdapter != null) {
            memoListAdapter.swap(memoDatas);
        }
    }

    public Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.REFRESH) {
                setMemoDatas();
            }
        }
    };



    private void setDatas() {

        // 스케쥴 주기 시작
        Scheduler.getScheduler().startSchedule(this);

        ScheduleModel scheduleModel = new ScheduleModel(this);
        Log.d("TEST", "스케쥴: "+scheduleModel.getAllData());
        scheduleModel.close();

        // 스피너 아이템 추가
        /*MemoModel memoModel = new MemoModel(this);
        labelDatas = memoModel.getLabelList(); // Content 글자수 제한
        memoModel.close();*/

        mainViewModel.checkMemoCreated();

        // memoListAdapter 생성하면서 연결
        memoListAdapter = new MemoListAdapter(this, memoDatas);
        recyclerView.setAdapter(memoListAdapter);

        // 데이터 채우기
        mainViewModel.setMemoDatas();

        // 앱 버전 확인
        mainViewModel.checkAppVersion();
    }
}
