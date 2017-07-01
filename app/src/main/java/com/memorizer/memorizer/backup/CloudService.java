package com.memorizer.memorizer.backup;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.AuthenticationException;
import com.cloudrail.si.exceptions.HttpException;
import com.cloudrail.si.exceptions.NotFoundException;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.cloudrail.si.types.CloudMetaData;
import com.memorizer.memorizer.models.Constants;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.memorizer.memorizer.models.Constants.DROPBOX_PERSISTENT;
import static com.memorizer.memorizer.models.Constants.DROPBOX_USER;
import static com.memorizer.memorizer.models.Constants.GOOGLE_DIRVE_PERSISTENT;
import static com.memorizer.memorizer.models.Constants.GOOGLE_DRIVE_USER;
import static com.memorizer.memorizer.models.Constants.ONE_DRIVE_PERSISTENT;
import static com.memorizer.memorizer.models.Constants.ONE_DRIVE_USER;

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
        dropbox.set(new Dropbox(
                context,
                "yjifdzmw3rbxksr",
                "art4q5vkrfatbh5"));
    }

    private void initGoogleDrive() {
        googledrive.set(new GoogleDrive(
                context,
                "920389922391-r6tnenk9scn4vh2qgjlc1rosh6fgvm33.apps.googleusercontent.com",
                "",
                "com.memorizer.memorizer:/oauth2redirect",
                ""));
        ((GoogleDrive) googledrive.get()).useAdvancedAuthentication();
    }

    private void initOneDrive() {
        onedrive.set(new OneDrive(context,
                "6f0055e9-005d-4289-8c99-c72a00f0a504",
                "Xx1sbgQLMN2vckQs7jP0qmS"));
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
            String persistent = sharedPreferences.getString(DROPBOX_PERSISTENT, null);
            if (persistent != null) dropbox.get().loadAsString(persistent);
            persistent = sharedPreferences.getString(GOOGLE_DIRVE_PERSISTENT, null);
            if (persistent != null) googledrive.get().loadAsString(persistent);
            persistent = sharedPreferences.getString(ONE_DRIVE_PERSISTENT, null);
            if (persistent != null) onedrive.get().loadAsString(persistent);
        } catch (ParseException e) {}
    }

    public CloudStorage getService(Linker linker) {
        AtomicReference<CloudStorage> ret = new AtomicReference<>();

        switch (linker) {
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

    public boolean isConnected(Linker linker) {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        boolean isConnected = false;
        switch (linker) {
            case DropboxLinker:
                if (sharedPreferences.getString(DROPBOX_PERSISTENT, null) != null)
                    isConnected = true;
                break;
            case GoogleDriveLinker:
                if (sharedPreferences.getString(GOOGLE_DIRVE_PERSISTENT, null) != null)
                    isConnected = true;
                break;
            case OneDriveLinker:
                if (sharedPreferences.getString(ONE_DRIVE_PERSISTENT, null) != null)
                    isConnected = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown service!");
        }

        return isConnected;
    }

    public void disconnect(final Linker linker, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (linker) {
                    case DropboxLinker:
                        editor.putString(DROPBOX_PERSISTENT, null);
                        break;
                    case GoogleDriveLinker:
                        editor.putString(GOOGLE_DIRVE_PERSISTENT, null);
                        break;
                    case OneDriveLinker:
                        editor.putString(ONE_DRIVE_PERSISTENT, null);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown service!");
                }
                editor.apply();

                Message message = Message.obtain(mHandler, Constants.DONE);
                mHandler.sendMessage(message);
            }
        }).start();
    }

    public void connect(final Linker linker, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (linker) {
                    case DropboxLinker:
                        editor.putString(DROPBOX_USER, dropbox.get().getUserLogin());
                        break;
                    case GoogleDriveLinker:
                        editor.putString(GOOGLE_DRIVE_USER, googledrive.get().getUserLogin());
                        break;
                    case OneDriveLinker:
                        editor.putString(ONE_DRIVE_USER, onedrive.get().getUserLogin());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown service!");
                }
                editor.apply();
                CloudService.getInstance().storePersistent(linker);
                Message message = Message.obtain(mHandler, Constants.DONE);
                mHandler.sendMessage(message);
            }
        }).start();
    }

    public void getFileList(final Linker linker) {
        final String path = "/";
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CloudMetaData> infoList;
                try {
                    switch (linker) {
                        case DropboxLinker:
                            infoList = dropbox.get().getChildren(path);
                            break;
                        case GoogleDriveLinker:
                            infoList = googledrive.get().getChildren(path);
                            break;
                        case OneDriveLinker:
                            infoList = onedrive.get().getChildren(path);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown service!");
                    }
                    Iterator<CloudMetaData> iterInfoList = infoList.iterator();
                    while (iterInfoList.hasNext()) {
                        CloudMetaData cmd = iterInfoList.next();
                        if (cmd.getFolder()) {
                            Log.d("CloudService", cmd.toString());
                        }
                    }

                } catch(NotFoundException e) {
                    // 경로에 파일이 없을경우
                    e.printStackTrace();
                } catch(HttpException e) {
                    // 네트워크 실패시 연결 해제
                    e.printStackTrace();
                } catch(AuthenticationException e) {
                    // 인증실패시 연결 해제
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void storePersistent(Linker linker) {
        SharedPreferences sharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (linker) {
            case DropboxLinker:
                editor.putString(DROPBOX_PERSISTENT, dropbox.get().saveAsString());
                break;
            case GoogleDriveLinker:
                editor.putString(GOOGLE_DIRVE_PERSISTENT, googledrive.get().saveAsString());
                break;
            case OneDriveLinker:
                editor.putString(ONE_DRIVE_PERSISTENT, onedrive.get().saveAsString());
                break;
            default:
                throw new IllegalArgumentException("Unknown service!");
        }
        editor.apply();
    }
}
