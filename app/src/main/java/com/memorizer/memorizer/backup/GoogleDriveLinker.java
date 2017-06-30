package com.memorizer.memorizer.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.AuthenticationException;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.services.GoogleDrive;
import com.memorizer.memorizer.models.Constants;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by soo13 on 2017-06-29.
 */

public class GoogleDriveLinker extends CloudLinker {
    private static final String TAG = "GoogleDriveLinker";

    @Override
    public boolean isConnected() {
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        try {
            final String persistent = pref.getString("googledrivePersistent", null);
            if (persistent != null && storage.get() != null) {
                storage.get().loadAsString(persistent);
                return true;
            } else if (persistent != null) {
                try {
                    storage.get().loadAsString(persistent);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void load() {
        CloudRail.setAppKey("59532b678f61ae2abef337ac");

        try {
            storage.set(new GoogleDrive(
                    mContext,
                    "920389922391-r6tnenk9scn4vh2qgjlc1rosh6fgvm33.apps.googleusercontent.com",
                    "", "com.memorizer.memorizer:/oauth2redirect", ""));
            ((GoogleDrive) storage.get()).useAdvancedAuthentication();

        } catch(AuthenticationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void linking() {
        new Thread() {
            @Override
            public void run() {
                SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("googledrivePersistent", storage.get().saveAsString()).apply();

                storage.get().login();

                Log.d(TAG, "is Calling back?");
                Message message = Message.obtain(mHandler, Constants.GOOGLE_DONE);
                mHandler.sendMessage(message);
            }
        }.start();
    }

    @Override
    protected void unlinking() {
        // 연결해지는 pref 값 변경으로만 해결
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("googledrivePersistent", null).apply();

        Message message = Message.obtain(mHandler, Constants.GOOGLE_DONE);
        mHandler.sendMessage(message);
    }
}
