package com.memorizer.memorizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleModel;
import com.memorizer.memorizer.scheduler.Scheduler;
import com.newrelic.agent.android.NewRelic;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<MemoData> memoDatas;
    MemoListAdapter memoListAdapter;
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

        // DB에서 메모목록 가져옴
        MemoModel memoModel = new MemoModel(this, "Memo.db", null);
        memoDatas = memoModel.getAllData();
        memoModel.close();

        ScheduleModel scheduleModel = new ScheduleModel(this, "Memo.db", null);
        Log.d("TEST", "스케쥴: "+scheduleModel.getAllData());
        scheduleModel.close();

        // ListView 생성하면서 작성할 값 초기화
        memoListAdapter = new MemoListAdapter(memoDatas);

        // ListView 어댑터 연결
        ListView memoListView = (ListView) findViewById(R.id.memo_list);
        if (memoListView != null) {
            memoListView.setAdapter(memoListAdapter);
        }

        //ListView를 Context 메뉴로 등록
        registerForContextMenu(memoListView);

        Intent mIntent = getIntent();
        MemoData memoData = (MemoData)mIntent.getSerializableExtra("mCreate");
        if (memoData != null) {
            Intent popupIntent = new Intent(MainActivity.this, MemoAlarmActivity.class);
            popupIntent.putExtra("memoId", memoData);
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(popupIntent);
        }


    }

    //Context 메뉴로 등록한 View(여기서는 ListView)가 처음 클릭되어 만들어질 때 호출되는 메소드
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memo_list_menu, menu);
    }

    //Context 메뉴로 등록한 View(여기서는 ListView)가 클릭되었을 때 자동으로 호출되는 메소드
    public boolean onContextItemSelected(MenuItem item) {

        //AdapterContextMenuInfo
        //AdapterView가 onCreateContextMenu할때의 추가적인 menu 정보를 관리하는 클래스
        //ContextMenu로 등록된 AdapterView(여기서는 Listview)의 선택된 항목에 대한 정보를 관리하는 클래스
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        int index= info.position; //AdapterView안에서 ContextMenu를 보여즈는 항목의 위치

        //선택된 ContextMenu의  아이템아이디를 구별하여 원하는 작업 수행
        //예제에서는 선택된 ListView의 항목(String 문자열) data와 해당 메뉴이름을 출력함
        switch( item.getItemId() ){
            case R.id.memo_delete:
                MemoModel memoModel = new MemoModel(this, "Memo.db", null);
                memoModel.delete(memoDatas.get(index).get_id());
                memoModel.close();
                ScheduleModel scheduleModel = new ScheduleModel(this, "Memo.db", null);
                scheduleModel.deleteByMemoId(memoDatas.get(index).get_id());
                scheduleModel.close();
                Toast.makeText(this, memoDatas.get(index).get_id() + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                memoDatas.remove(index);
                memoListAdapter.notifyDataSetChanged();
                break;

            case R.id.memo_edit:
                Intent intent = new Intent(MainActivity.this, MemoCreate.class);
                intent.putExtra("is_edit", true);
                intent.putExtra("memo_id", memoDatas.get(index).get_id());

                startActivity(intent);
                break;

        }

        return true;
    };

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
}
