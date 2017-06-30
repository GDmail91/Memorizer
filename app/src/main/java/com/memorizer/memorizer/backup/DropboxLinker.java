package com.memorizer.memorizer.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.AuthenticationException;
import com.cloudrail.si.exceptions.HttpException;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.services.Dropbox;
import com.memorizer.memorizer.models.Constants;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by soo13 on 2017-06-29.
 */

public class DropboxLinker extends CloudLinker {

    private static final String TAG = "DropboxLinker";

    @Override
    public boolean isConnected() {
        Log.d(TAG, "isConnected");
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        try {
            final String persistent = pref.getString("dropboxPersistent", null);
            if (persistent != null && storage.get() != null) {
                Log.d(TAG, "connected!");
                storage.get().loadAsString(persistent);
                return true;
            } else if (persistent != null) {
                load();
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
            storage.set(new Dropbox(
                    mContext,
                    "yjifdzmw3rbxksr",
                    "art4q5vkrfatbh5"));
        } catch(HttpException e) {
            // 네트워크 실패시 연결 해제
            e.printStackTrace();
        } catch(AuthenticationException e) {
            // 인증실패시 연결 해제
            e.printStackTrace();
        }
    }

    @Override
    protected void linking() {
        Log.d(TAG, "linking");
        new Thread() {
            @Override
            public void run() {
                storage.get().login();

                SharedPreferences pref = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("dropboxPersistent", storage.get().saveAsString()).apply();
                Message message = Message.obtain(mHandler, Constants.DROPBOX_DONE);
                mHandler.sendMessage(message);
            }
        }.start();
    }

    @Override
    protected void unlinking() {
        Log.d(TAG, "unlinking");
        // 연결해지는 pref 값 변경으로만 해결
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("dropboxPersistent", null).apply();

        Message message = Message.obtain(mHandler, Constants.DROPBOX_DONE);
        mHandler.sendMessage(message);
    }
}
