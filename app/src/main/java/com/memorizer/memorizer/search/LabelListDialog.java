package com.memorizer.memorizer.search;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.LabelData;
import com.memorizer.memorizer.models.MemoModel;

import java.util.ArrayList;

/**
 * Created by YS on 2017-01-10.
 */

public class LabelListDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.label_list_dialog);

        ListView listview ;
        LabelListAdapter adapter = new LabelListAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.label_list);
        listview.setAdapter(adapter);

        MemoModel memoModel = new MemoModel(this);
        ArrayList<LabelData> labelDatas = memoModel.getLabelList(); // Content 글자수 제한
        memoModel.close();

        for (LabelData labelData : labelDatas) {
            Drawable drawable = getResources().getDrawable(R.drawable.color_selector_blue, null);;
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
    }
}
