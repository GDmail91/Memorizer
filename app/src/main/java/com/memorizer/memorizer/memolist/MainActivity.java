package com.memorizer.memorizer.memolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.memorizer.memorizer.DeveloperInfo;
import com.memorizer.memorizer.MemoAlarmActivity;
import com.memorizer.memorizer.R;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.LabelData;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleModel;
import com.memorizer.memorizer.scheduler.Scheduler;
import com.memorizer.memorizer.search.SearchActivity;
import com.newrelic.agent.android.NewRelic;

import java.util.ArrayList;

import static com.memorizer.memorizer.models.Constants.ITEM_DELETE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    ArrayList<MemoData> memoDatas = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MemoListAdapter memoListAdapter;
    private Spinner filterList;
    private ArrayList<LabelData> labelDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // NewRelic 용
        NewRelic.withApplicationToken(
                "AAc483aa8dc9458949bf00b5e1bd56257947fdf68b"
        ).start(this.getApplication());

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
                    case 0:
                        editor.putInt("filter", 0);
                        break;
                    case 1:
                        editor.putInt("filter", 1);
                        break;
                    case 2:
                        editor.putInt("filter", 2);
                        break;
                    default:
                        editor.putInt("filter", 0);
                        break;

                }
                editor.apply();
                setMemoDatas(pref.getInt("filter",0));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // 데이터 채우기
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        setMemoDatas(pref.getInt("filter",0));

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
            Intent popupIntent = new Intent(MainActivity.this, MemoAlarmActivity.class);
            popupIntent.putExtra("memoId", memoData);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(popupIntent);
        }


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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
