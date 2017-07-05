package com.memorizer.memorizer.backup;

import android.content.Context;
import android.content.SharedPreferences;

import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.services.Dropbox;
import com.memorizer.memorizer.R;

import static com.memorizer.memorizer.models.Constants.DROPBOX_PERSISTENT;
import static com.memorizer.memorizer.models.Constants.DROPBOX_USER;

/**
 * Created by soo13 on 2017-06-29.
 */

public class DropboxLinker extends CloudLinker {

    private static final String TAG = "DropboxLinker";

    @Override
    public void initLinker() {
        linker.set(new Dropbox(
                context,
                context.getResources().getString(R.string.dropbox_id),
                context.getResources().getString(R.string.dropbox_sec)));

        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);

        try {
            String persistent = sharedPreferences.getString(DROPBOX_PERSISTENT, null);
            if (persistent != null) linker.get().loadAsString(persistent);
        } catch (ParseException e) {}
    }


    @Override
    public boolean isConnected() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        boolean isConnected = false;
        if (sharedPreferences.getString(DROPBOX_PERSISTENT, null) != null)
            isConnected = true;

        return isConnected;
    }

    @Override
    public void disconnect() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DROPBOX_PERSISTENT, null);
        editor.apply();
    }

    @Override
    public void connect() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DROPBOX_USER, linker.get().getUserLogin());
        editor.apply();
    }

    @Override
    public void storePersistent() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(DROPBOX_PERSISTENT, linker.get().saveAsString());
        editor.apply();
    }
}
