package com.memorizer.memorizer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.models.ScheduleData;
import com.memorizer.memorizer.models.ScheduleModel;

import java.util.ArrayList;

import static com.memorizer.memorizer.models.Constants.ITEM_DELETE;

/**
 * Created by soo13 on 2017-06-25.
 */

public class WidgetListProvider implements RemoteViewsService.RemoteViewsFactory {
    private static String TAG = "WidgetListProvider";
    private ArrayList<MemoData> memoDatas = new ArrayList();
    private Context context = null;
    private int appWidgetId;

    public WidgetListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }


    private void populateListItem() {
        // DB에서 메모목록 가져옴
        MemoModel memoModel = new MemoModel(context);
        memoDatas = memoModel.getAllDataShort(0); // Content 글자수 제한
        memoModel.close();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return memoDatas.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Similar to getView of Adapter where instead of View
     * we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.memo_item_adapter);

        //Log.d(TAG, "memo data 가져옴 : " + memoDatas.get(position).getContent());

        // Adapter 바인딩
        remoteView.setTextViewText(R.id.memo_content, memoDatas.get(position).getContent());
        remoteView.setTextViewText(R.id.memo_time, memoDatas.get(position).getTime());
        remoteView.setTextViewText(R.id.memo_posted, memoDatas.get(position).getPosted().split(" ")[0]); // 시간 제거

        // 라벨 설정

        if (memoDatas.get(position).getLabel().equals("") && memoDatas.get(position).getLabelPos() == 0) {
            remoteView.setViewVisibility(R.id.label, View.GONE);
        } else {
            remoteView.setViewVisibility(R.id.label, View.VISIBLE);
            remoteView.setInt(R.id.label, "setBackgroundResource", memoDatas.get(position).getLabelPosDraw());
            remoteView.setTextViewText(R.id.label, memoDatas.get(position).getLabel());
        }

        // 다음 알림 시간
        ScheduleModel scheduleModel = new ScheduleModel(context);
        ScheduleData nextSchedule = scheduleModel.getMemoSchedule(memoDatas.get(position).get_id());
        if (nextSchedule != null) {
            remoteView.setViewVisibility(R.id.next_schedule, View.VISIBLE);
            remoteView.setTextViewText(R.id.memo_term, ""+nextSchedule.getDaysNext());
        } else {
            remoteView.setViewVisibility(R.id.next_schedule, View.GONE);
        }
        scheduleModel.close();

        /*
        Intent openMemoIntent = new Intent(Constants.OPEN_MEMO);
        openMemoIntent.putExtra("memo_id", memoDatas.get(position).get_id());
        PendingIntent pIntentOpenMemo = PendingIntent.getBroadcast(context, 0, openMemoIntent, 0);
        remoteView.setOnClickPendingIntent(R.id.memo_item, pIntentOpenMemo);*/

        Bundle extras = new Bundle();
        extras.putInt("memo_id", memoDatas.get(position).get_id());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteView.setOnClickFillInIntent(R.id.memo_item, fillInIntent);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        //Log.d(TAG, "getLoadingView 실행");
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
