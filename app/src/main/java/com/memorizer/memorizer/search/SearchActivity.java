package com.memorizer.memorizer.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.memolist.MemoListAdapter;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;

import java.util.ArrayList;

/**
 * Created by YS on 2016-12-31.
 */

public class SearchActivity extends AppCompatActivity {

    private EditText searchTextView;

    private ArrayList<MemoData> memoDatas;
    private RecyclerView recyclerView;
    private MemoListAdapter memoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 검색바 생성
        searchTextView = (EditText) findViewById(R.id.search_text);
        searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        onSearch(v.getText().toString());
                        break;
                    default:
                        //Toast.makeText(getApplicationContext(), "기본", Toast.LENGTH_LONG).show();
                        return false;
                }
                return true;
            }
        });


        // RecyclerView 생성
        recyclerView = (RecyclerView) findViewById(R.id.memo_list);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager1);

        // memoListAdapter 생성하면서 연결
        memoListAdapter = new MemoListAdapter(this, memoDatas);
        recyclerView.setAdapter(memoListAdapter);

        // RecyclerView를 Context 메뉴로 등록
        registerForContextMenu(recyclerView);
    }

    private void onSearch(String searchText) {
        // DB에서 메모목록 가져옴
        MemoModel memoModel = new MemoModel(this, "Memo.db", null);
        memoDatas = memoModel.getSearchData(searchText); // Content 글자수 제한
        memoModel.close();

        if (memoListAdapter != null) {
            memoListAdapter.swap(memoDatas);
        }
    }
}
