package com.memorizer.memorizer.backup;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by soo13 on 2017-06-30.
 */

public class CloudService {
    private final static String CLOUDRAIL_LICENSE_KEY = "59532b678f61ae2abef337ac";
    private final static CloudService ourInstance = new CloudService();

    private final AtomicReference<CloudStorage> dropbox = new AtomicReference<>();
    private final AtomicReference<CloudStorage> googledrive = new AtomicReference<>();
    private final AtomicReference<CloudStorage> onedrive = new AtomicReference<>();

    public enum Linker {
        DropboxLinker, GoogleDriveLinker, OneDriveLinker
    }

    private Activity context = null;

    public static CloudService getInstance() {
        return ourInstance;
    }

    private CloudService() {
    }

    private void initDropbox() {
        dropbox.set(new Dropbox(context, "yjifdzmw3rbxksr", "art4q5vkrfatbh5"));
    }

    private void initGoogleDrive() {
        googledrive.set(new GoogleDrive(context, "920389922391-r6tnenk9scn4vh2qgjlc1rosh6fgvm33.apps.googleusercontent.com", "", "com.memorizer.memorizer:/oauth2redirect", ""));
        ((GoogleDrive) googledrive.get()).useAdvancedAuthentication();
    }

    private void initOneDrive() {
        onedrive.set(new OneDrive(context, "ABC", "DEF"));
    }

    // --------- Public Methods -----------
    public void prepare(Activity context) {
        this.context = context;

        CloudRail.setAppKey(CLOUDRAIL_LICENSE_KEY);

        this.initDropbox();
        this.initGoogleDrive();
        this.initOneDrive();

        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);

        try {
            String persistent = sharedPreferences.getString("dropboxPersistent", null);
            if (persistent != null) dropbox.get().loadAsString(persistent);
            persistent = sharedPreferences.getString("googledrivePersistent", null);
            if (persistent != null) googledrive.get().loadAsString(persistent);
            persistent = sharedPreferences.getString("onedrivePersistent", null);
            if (persistent != null) onedrive.get().loadAsString(persistent);
        } catch (ParseException e) {}
    }

    CloudStorage getService(Linker service) {
        AtomicReference<CloudStorage> ret = new AtomicReference<>();

        switch (service) {
            case DropboxLinker:
                ret = this.dropbox;
                break;
            case GoogleDriveLinker:
                ret = this.googledrive;
                break;
            case OneDriveLinker:
                ret = this.onedrive;
                break;
            default:
                throw new IllegalArgumentException("Unknown service!");
        }

        return ret.get();
    }

    void storePersistent() {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("dropboxPersistent", dropbox.get().saveAsString());
        editor.putString("googledrivePersistent", googledrive.get().saveAsString());
        editor.putString("onedrivePersistent", onedrive.get().saveAsString());
        editor.apply();
    }
}
