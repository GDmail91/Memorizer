package com.memorizer.memorizer.models;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;
import static com.memorizer.memorizer.models.Constants.FILTER_ALARMED;
import static com.memorizer.memorizer.models.Constants.FILTER_MODIFY;
import static com.memorizer.memorizer.models.Constants.FILTER_NONE;

/**
 * Created by soo13 on 2017-11-27.
 */

public class PrefModel {
    private Context context;

    public PrefModel(Context context) {
        this.context = context;
    }

    public String getVersion() {
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("version", "");
    }

    public void setVersion(String version) {
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("version", version);
        editor.apply();
    }

    public int getFilter() {
        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getInt("filter",0);
    }

    public void setFilter(int filter) {
        switch (filter) {
            case FILTER_NONE:
            case FILTER_MODIFY:
            case FILTER_ALARMED:
                break;
            default:
                filter = FILTER_NONE;
                break;
        }

        SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("filter", filter);
        editor.apply();
    }
}
