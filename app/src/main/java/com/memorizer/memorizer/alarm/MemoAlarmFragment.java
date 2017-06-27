package com.memorizer.memorizer.alarm;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;

import us.feras.mdv.MarkdownView;

/**
 * Created by YS on 2017-05-18.
 */

public class MemoAlarmFragment extends android.support.v4.app.Fragment {

    private MemoData memoData;

    private TextView messageView;
    private MarkdownView messageMarkdown;
    private ScrollView alarmScrollView;
    private TextView label;
    private RelativeLayout labelHeader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        memoData = (MemoData) bundle.getSerializable("memoData");

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.activity_alarm,container,false);

        alarmScrollView = (ScrollView) relativeLayout.findViewById(R.id.alarm_scroll_view);
        messageView = (TextView) relativeLayout.findViewById(R.id.message_view);
        messageMarkdown = (MarkdownView) relativeLayout.findViewById(R.id.markdown_view);
        labelHeader = (RelativeLayout) relativeLayout.findViewById(R.id.label_header);
        label = (TextView) relativeLayout.findViewById(R.id.label);

        // 본문 출력
        if (memoData.isMarkdown()) {
            alarmScrollView.setBackgroundResource(R.color.colorPrimary);
            messageView.setVisibility(View.GONE);
            messageMarkdown.setVisibility(View.VISIBLE);
            messageMarkdown.loadMarkdown(memoData.getContent());
        } else {
            messageView.setText(memoData.getContent());
        }

        if (memoData.getLabel().equals("") && memoData.getLabelPos() == 0) {
            labelHeader.setVisibility(View.GONE);
        } else {
            labelHeader.setVisibility(View.VISIBLE);
            label.setBackground(ContextCompat.getDrawable(inflater.getContext(), memoData.getLabelPosDraw()));
            label.setText(memoData.getLabel());
        }

        return relativeLayout;
    }
}