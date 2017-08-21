package com.memorizer.memorizer.memolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.cloudrail.si.CloudRail;
import com.memorizer.memorizer.BuildConfig;
import com.memorizer.memorizer.CloudLinkerDialog;
import com.memorizer.memorizer.DeveloperInfo;
import com.memorizer.memorizer.NoticeDialog;
import com.memorizer.memorizer.R;
import com.memorizer.memorizer.alarm.MemoAlarmDragActivity;
import com.memorizer.memorizer.backup.CloudService;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.LabelData;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleModel;
import com.memorizer.memorizer.scheduler.Scheduler;
import com.memorizer.memorizer.search.SearchActivity;
import com.splunk.mint.Mint;

import java.util.ArrayList;

import static com.memorizer.memorizer.models.Constants.FILTER_ALARMED;
import static com.memorizer.memorizer.models.Constants.FILTER_MODIFY;
import static com.memorizer.memorizer.models.Constants.FILTER_NONE;
import static com.memorizer.memorizer.models.Constants.ITEM_DELETE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    ArrayList<MemoData> memoDatas = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private NoticeDialog noticeDialog;
    private CloudLinkerDialog cloudLinkerDialog;
    private RecyclerView recyclerView;
    private MemoListAdapter memoListAdapter;
    private Spinner filterList;
    private ArrayList<LabelData> labelDatas = new ArrayList<>();

    private static final String BROWSABLE = "android.intent.category.BROWSABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<String> test;
        
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MemoCreate.class);
                    startActivity(intent);
                }
            });
        }

        // Splunk Mint 용
        String buildtype = BuildConfig.BUILD_TYPE;
        if (buildtype.equals("release")) {
            //Do some admin stuff here.
            Mint.initAndStartSession(this.getApplication(), "e4b63be0");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 스케쥴 주기 시작
        Scheduler.getScheduler().startSchedule(this);

        // 필터 리스트 세팅
        filterList = (Spinner) findViewById(R.id.filter_list);
        filterList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                switch (position) {
                    case FILTER_NONE:
                        editor.putInt("filter", FILTER_NONE);
                        break;
                    case FILTER_MODIFY:
                        editor.putInt("filter", FILTER_MODIFY);
                        break;
                    case FILTER_ALARMED:
                        editor.putInt("filter", FILTER_ALARMED);
                        break;
                    default:
                        editor.putInt("filter", FILTER_NONE);
                        break;

                }
                editor.apply();
                setMemoDatas(pref.getInt("filter",FILTER_NONE));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // 데이터 채우기
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        setMemoDatas(pref.getInt("filter",0));

        // 앱 버전 확인
        final String version;
        try {
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = i.versionName;

            // 업데이트 되었을 경우
            if (!version.equals(pref.getString("version", "")) || buildtype.equals("debug")) {
                noticeDialog = new NoticeDialog(this,
                        getString(R.string.notice_title),
                        getString(R.string.notice),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("version", version);
                                editor.apply();
                                noticeDialog.dismiss();
                            }
                        });
                noticeDialog.show();
            }

        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ScheduleModel scheduleModel = new ScheduleModel(this);
        Log.d("TEST", "스케쥴: "+scheduleModel.getAllData());
        scheduleModel.close();

        // RecyclerView Swipe 기능
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO 리프레쉬
                swipeRefreshLayout.setRefreshing(false);
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                setMemoDatas(pref.getInt("filter",0));
            }
        });
        // RecyclerView 생성
        recyclerView = (RecyclerView) swipeRefreshLayout.findViewById(R.id.memo_list);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager1);

        // memoListAdapter 생성하면서 연결
        memoListAdapter = new MemoListAdapter(this, memoDatas);
        recyclerView.setAdapter(memoListAdapter);

        // RecyclerView를 Context 메뉴로 등록
        registerForContextMenu(recyclerView);

        // 스피너 아이템 추가
        /*MemoModel memoModel = new MemoModel(this);
        labelDatas = memoModel.getLabelList(); // Content 글자수 제한
        memoModel.close();*/

        // MemoCreate에서 메모를 만든경우
        Intent mIntent = getIntent();
        MemoData memoData = (MemoData)mIntent.getSerializableExtra("mCreate");
        if (memoData != null) {
            Intent popupIntent = new Intent(MainActivity.this, MemoAlarmDragActivity.class);
            ArrayList<Integer> createdData = new ArrayList<>();
            createdData.add(memoData.get_id());
            popupIntent.putIntegerArrayListExtra("memoId", createdData);
            popupIntent.putExtra("isCreated", true);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(popupIntent);
        }

        // Cloud 연결
        CloudService.getInstance().prepare(this);

        // File Sync
        CloudService.getInstance().syncWithCloud(refreshHandler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ITEM_DELETE:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "리프레쉬");
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    setMemoDatas(pref.getInt("filter",0));
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.made_of) {
            Intent intent = new Intent(MainActivity.this, DeveloperInfo.class);
            startActivity(intent);
        } else if (id == R.id.backup_setting){
            cloudLinkerDialog = new CloudLinkerDialog(this);
            cloudLinkerDialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getCategories().contains(BROWSABLE)) {
            // Here we pass the response to the SDK which will automatically
            // complete the authentication process
            CloudRail.setAuthenticationResponse(intent);
/*
            SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("googledrivePersistent", storage.get().saveAsString()).apply();*/
        }
        super.onNewIntent(intent);
    }

    // memoDatas 재배치
    private void setMemoDatas(int order) {
        // DB에서 메모목록 가져옴
        MemoModel memoModel = new MemoModel(this);
        memoDatas = memoModel.getAllDataShort(order); // Content 글자수 제한
        memoModel.close();

        if (memoListAdapter != null) {
            memoListAdapter.swap(memoDatas);
        }
    }

    public Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.REFRESH) {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                setMemoDatas(pref.getInt("filter",0));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
