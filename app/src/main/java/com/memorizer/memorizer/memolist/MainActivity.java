package com.memorizer.memorizer.memolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.memorizer.memorizer.BuildConfig;
import com.memorizer.memorizer.CloudLinkerDialog;
import com.memorizer.memorizer.DeveloperInfo;
import com.memorizer.memorizer.NoticeDialog;
import com.memorizer.memorizer.R;
import com.memorizer.memorizer.backup.CloudService;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.LabelData;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.search.SearchActivity;
import com.splunk.mint.Mint;

import java.util.ArrayList;

import static com.memorizer.memorizer.models.Constants.ITEM_DELETE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ArrayList<MemoData> memoDatas = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private NoticeDialog noticeDialog;
    private CloudLinkerDialog cloudLinkerDialog;
    private MemoListAdapter memoListAdapter;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private Spinner filterList;
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private ArrayList<LabelData> labelDatas = new ArrayList<>();

    private MainViewModel mainViewModel = new MainViewModel(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Splunk Mint 용
        if (BuildConfig.BUILD_TYPE.equals("release")) {
            Mint.initAndStartSession(this.getApplication(), "e4b63be0");
        }

        setLayout();

        // Cloud 연결
        CloudService.getInstance().prepare(this);
        CloudService.getInstance().syncWithCloud(mainViewModel.refreshHandler);

        mainViewModel.onCreate();
    }

    private void setLayout() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                toolbar.setTitle(R.string.navigation_drawer_open);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                toolbar.setTitle(R.string.navigation_drawer_close);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(navigationSelectedListener);


        // Float Button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(fabClickListener);


        // 필터 리스트 세팅
        filterList = (Spinner) findViewById(R.id.filter_list);
        filterList.setOnItemSelectedListener(filterListSelectedListener);

        // RecyclerView Swipe 기능
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);

        // RecyclerView 생성
        recyclerView = (RecyclerView) swipeRefreshLayout.findViewById(R.id.memo_list);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager1);

        // RecyclerView를 Context 메뉴로 등록
        registerForContextMenu(recyclerView);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ITEM_DELETE:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "리프레쉬");
                    mainViewModel.setMemoDatas();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mainViewModel.onNewIntent(intent);
        super.onNewIntent(intent);
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


    public NavigationView.OnNavigationItemSelectedListener navigationSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.made_of) {
                Intent intent = new Intent(MainActivity.this, DeveloperInfo.class);
                startActivity(intent);
            } else if (id == R.id.backup_setting){
                cloudLinkerDialog = new CloudLinkerDialog(MainActivity.this);
                cloudLinkerDialog.show();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    public View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), MemoCreate.class);
            startActivity(intent);
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

}
