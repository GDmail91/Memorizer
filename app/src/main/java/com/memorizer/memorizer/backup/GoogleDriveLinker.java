package com.memorizer.memorizer.backup;

import android.content.Context;
import android.content.SharedPreferences;

import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.services.GoogleDrive;
import com.memorizer.memorizer.R;

import static com.memorizer.memorizer.models.Constants.GOOGLE_DIRVE_PERSISTENT;
import static com.memorizer.memorizer.models.Constants.GOOGLE_DRIVE_USER;

/**
 * Created by soo13 on 2017-06-29.
 */

public class GoogleDriveLinker extends CloudLinker {
    private static final String TAG = "GoogleDriveLinker";

    @Override
    public void initLinker() {
        linker.set(new GoogleDrive(
                context,
                context.getResources().getString(R.string.google_id),
                "",
                "com.memorizer.memorizer:/oauth2redirect",
                ""));
        ((GoogleDrive) linker.get()).useAdvancedAuthentication();

        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);

        try {
            String  persistent = sharedPreferences.getString(GOOGLE_DIRVE_PERSISTENT, null);
            if (persistent != null) linker.get().loadAsString(persistent);
        } catch (ParseException e) {}
    }


    @Override
    public boolean isConnected() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        boolean isConnected = false;
        if (sharedPreferences.getString(GOOGLE_DIRVE_PERSISTENT, null) != null)
            isConnected = true;

        return isConnected;
    }

    @Override
    public void disconnect() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GOOGLE_DIRVE_PERSISTENT, null);
        editor.apply();
    }

    @Override
    public void connect() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GOOGLE_DRIVE_USER, linker.get().getUserLogin());
        editor.apply();
    }

    @Override
    public void storePersistent() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(GOOGLE_DIRVE_PERSISTENT, linker.get().saveAsString());
        editor.apply();
    }
}
