package com.memorizer.memorizer.backup;

import android.content.SharedPreferences;
import android.util.Log;

import com.cloudrail.si.CloudRail;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by soo13 on 2017-06-29.
 */

public class OneDriveLinker extends CloudLinker {
    private static final String TAG = "OneDriveLinker";

    @Override
    public boolean isConnected() {
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getBoolean("oneDriveLink", false);
    }

    @Override
    protected void load() {
        CloudRail.setAppKey("59532b678f61ae2abef337ac");

    }

    @Override
    protected void linking() {
        Log.d(TAG, "linking");
    }

    @Override
    protected void unlinking() {
        Log.d(TAG, "unlinking");
    }
}
