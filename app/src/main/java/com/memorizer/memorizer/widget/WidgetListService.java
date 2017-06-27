package com.memorizer.memorizer.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by soo13 on 2017-06-25.
 */

public class WidgetListService extends RemoteViewsService {
/*
* So pretty simple just defining the Adapter of the listview
* here Adapter is ListProvider
* */

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new WidgetListProvider(this.getApplicationContext(), intent));
    }

}
