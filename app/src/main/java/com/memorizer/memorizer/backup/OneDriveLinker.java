package com.memorizer.memorizer.backup;

import android.content.Context;
import android.content.SharedPreferences;

import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.services.OneDrive;
import com.memorizer.memorizer.R;

import static com.memorizer.memorizer.models.Constants.ONE_DRIVE_PERSISTENT;
import static com.memorizer.memorizer.models.Constants.ONE_DRIVE_USER;

/**
 * Created by soo13 on 2017-06-29.
 */

public class OneDriveLinker extends CloudLinker {
    private static final String TAG = "OneDriveLinker";

    @Override
    public void initLinker() {
        linker.set(new OneDrive(context,
                context.getResources().getString(R.string.one_id),
                context.getResources().getString(R.string.one_sec)));

        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);

        try {
            String persistent = sharedPreferences.getString(ONE_DRIVE_PERSISTENT, null);
            if (persistent != null) linker.get().loadAsString(persistent);
        } catch (ParseException e) {}
    }

    @Override
    public boolean isConnected() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        boolean isConnected = false;
        if (sharedPreferences.getString(ONE_DRIVE_PERSISTENT, null) != null)
            isConnected = true;

        return isConnected;
    }

    @Override
    public void disconnect() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ONE_DRIVE_PERSISTENT, null);
        editor.apply();
    }

    @Override
    public void connect() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ONE_DRIVE_USER, linker.get().getUserLogin());
        editor.apply();
    }

    @Override
    public void storePersistent() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(ONE_DRIVE_PERSISTENT, linker.get().saveAsString());
        editor.apply();
    }
}
