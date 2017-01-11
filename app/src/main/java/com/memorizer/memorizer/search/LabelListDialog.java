package com.memorizer.memorizer.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.LabelData;
import com.memorizer.memorizer.models.MemoModel;

import java.util.ArrayList;

/**
 * Created by YS on 2017-01-10.
 */

public class LabelListDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.label_list_dialog);

        ListView listview ;
        LabelListAdapter adapter = new LabelListAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.label_list);
        listview.setAdapter(adapter);

        MemoModel memoModel = new MemoModel(this);
        final ArrayList<LabelData> labelDatas = memoModel.getLabelList(); // Content 글자수 제한
        // 전체 선택자
        labelDatas.add(0, new LabelData(getResources().getString(R.string.all), 0));
        memoModel.close();

        for (LabelData labelData : labelDatas) {
            Drawable drawable = getResources().getDrawable(R.drawable.color_selector, null);
            if (labelData.getLabelPosition() != 0) {
                switch (labelData.getLabelPosition()) {
                    case Constants.COLOR_BLUE:
                        drawable = getResources().getDrawable(R.drawable.color_selector_blue, null);
                        break;
                    case Constants.COLOR_RED:
                        drawable = getResources().getDrawable(R.drawable.color_selector_red, null);
                        break;
                    case Constants.COLOR_ORANGE:
                        drawable = getResources().getDrawable(R.drawable.color_selector_orange, null);
                        break;
                    case Constants.COLOR_GREEN:
                        drawable = getResources().getDrawable(R.drawable.color_selector_green, null);
                        break;
                }
            }

            adapter.addItem(labelData.getLabelName(), labelData.getLabelPosition(), drawable) ;
        }

        //ListView의 아이템 하나가 클릭되는 것을 감지하는 Listener객체 설정 (Button의 OnClickListener와 같은 역할)
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent labelIntent = new Intent();
                        if (position == 0) {
                            labelIntent.putExtra("selectAll", true);
                        } else {
                            labelIntent.putExtra("selectAll", false);
                            labelIntent.putExtra("labelFilter", labelDatas.get(position));
                        }

                        setResult(RESULT_OK, labelIntent);
                        finish();
                    }
                });
    }

    protected void onCancel(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
