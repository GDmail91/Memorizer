package com.memorizer.memorizer.search;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.models.LabelData;

import java.util.ArrayList;

/**
 * Created by YS on 2017-01-10.
 */

public class LabelListAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<LabelData> labelDatas = new ArrayList<LabelData>() ;

    public LabelListAdapter(ArrayList<LabelData> labelDatas) {
        this.labelDatas = labelDatas;
    }

    public LabelListAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴.
    @Override
    public int getCount() {
        return labelDatas.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.label_item_adapter, parent, false);
        }

        ImageView labelColorView = (ImageView) convertView.findViewById(R.id.label_color);
        TextView labelNameView = (TextView) convertView.findViewById(R.id.label_name);

        LabelData listViewItem = labelDatas.get(position);


        labelColorView.setImageDrawable(listViewItem.getLabelDrawable());
        if (listViewItem.getLabelName().equals("") && listViewItem.getLabelPosition() == 0) {
            labelNameView.setText(context.getResources().getText(R.string.no_label));
        } else {
            labelNameView.setText(listViewItem.getLabelName());
        }

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return labelDatas.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String labelName, int labelPosition, Drawable labelColor) {
        LabelData item = new LabelData(labelName, labelPosition, labelColor);

        labelDatas.add(item);
    }
}
