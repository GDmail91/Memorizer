package com.memorizer.memorizer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RemoteViews;

import com.memorizer.memorizer.R;
import com.memorizer.memorizer.create.MemoCreate;
import com.memorizer.memorizer.models.Constants;

import static android.content.Context.MODE_PRIVATE;
import static com.memorizer.memorizer.models.Constants.ITEM_DELETE;

/**
 * Created by soo13 on 2017-06-25.
 */

public class ListWidget extends AppWidgetProvider {
    private static final String TAG = "ListWidget";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (Constants.OPEN_MEMO.equals(action)) {
            int memoId = intent.getIntExtra("memo_id", 0);
            if (memoId > 0) {
                // 뷰 누를경우 상세 보기로 이동
                Intent openMemoIntent = new Intent(context, MemoCreate.class);
                openMemoIntent.putExtra("is_edit", true);
                openMemoIntent.putExtra("memo_id", memoId);

                openMemoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                callActivity(context, openMemoIntent);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = updateWidgetListView(context);
        for(int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews updateWidgetListView(Context context) {

        // Set ListView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_memo_list);
        Intent svcIntent = new Intent(context, WidgetListService.class);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.widget_memo_list, svcIntent);

        // Set onClink Each items
        Intent toastIntent = new Intent(context, ListWidget.class);
        toastIntent.setAction(Constants.OPEN_MEMO);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_memo_list, toastPendingIntent);

        // Set onClink new memo
        Intent newMemoIntent = new Intent(Constants.NEW_MEMO);
        PendingIntent pIntentNewMemo = PendingIntent.getBroadcast(context, 0, newMemoIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_new_memo, pIntentNewMemo);

        return remoteViews;
    }


    /**
     * Activity 호출 (Intent.FLAG_ACTIVITY_NEW_TASK)
     */
    private void callActivity(Context context, Intent intent){
        context.startActivity(intent);
    }

}
