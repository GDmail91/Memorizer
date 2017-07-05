package com.memorizer.memorizer.backup;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.types.SpaceAllocation;
import com.memorizer.memorizer.backup.CloudLinker.Linker;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;

/**
 * Created by soo13 on 2017-06-30.
 */

public class CloudService {
    private final static String CLOUDRAIL_LICENSE_KEY = "59532b678f61ae2abef337ac";
    private final static CloudService ourInstance = new CloudService();

    private final CloudLinker dropbox = new DropboxLinker();
    private final CloudLinker googledrive = new GoogleDriveLinker();
    private final CloudLinker onedrive = new OneDriveLinker();

    public static CloudService getInstance() {
        return ourInstance;
    }

    private CloudService() {
    }

    // --------- Public Methods -----------
    public void prepare(Activity context) {
        CloudRail.setAppKey(CLOUDRAIL_LICENSE_KEY);

        for (Linker linkerName : Linker.values()) {
            CloudService.getInstance().getService(linkerName).init(context);
        }
    }

    public CloudLinker getService(Linker linker) {
        CloudLinker cloudLinker = null;

        switch (linker) {
            case DropboxLinker:
                cloudLinker = this.dropbox;
                break;
            case GoogleDriveLinker:
                cloudLinker = this.googledrive;
                break;
            case OneDriveLinker:
                cloudLinker = this.onedrive;
                break;
            default:
                throw new IllegalArgumentException("Unknown service!");
        }

        return cloudLinker;
    }

    public boolean isConnected(Linker linker) {
        return getService(linker).isConnected();
    }

    public void disconnect(Linker linker, Handler mHandler) {
        getService(linker).disconnect();

        Message message = Message.obtain(mHandler, Constants.DONE);
        mHandler.sendMessage(message);
    }

    public void connect(final Linker linker, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getService(linker).connect();
                getService(linker).storePersistent();
                Message message = Message.obtain(mHandler, Constants.DONE);
                mHandler.sendMessage(message);
            }
        }).start();
    }

    public void onDelete(final MemoData memoData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Linker linkerName : Linker.values()) {
                    getService(linkerName).onDelete(memoData);
                }
            }
        }).start();
    }

    public void onUpdate(final MemoData memoData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Linker linkerName : Linker.values()) {
                    getService(linkerName).onUpdate(memoData);
                }
            }
        }).start();
    }

    public void syncWithCloud(final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Linker linkerName : Linker.values()) {
                    getService(linkerName).sync();
                }

                if (mHandler != null) {
                    Message message = new Message();
                    message.what = Constants.REFRESH;
                    mHandler.sendMessage(message);
                }
            }
        }).start();
    }

    public void getAllocated(final Linker linkerName, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SpaceAllocation alloc = getService(linkerName).getAllocation();

                if (alloc != null) {
                    Message message = Message.obtain(mHandler);
                    message.what = Constants.DONE;
                    switch (linkerName) {
                        case DropboxLinker:
                            message.arg1 = Constants.DROPBOX_DONE;
                            break;
                        case GoogleDriveLinker:
                            message.arg1 = Constants.GOOGLE_DONE;
                            break;
                        case OneDriveLinker:
                            message.arg1 = Constants.ONE_DONE;
                            break;
                    }
                    message.obj = "(" + toByteString(alloc.getUsed()) + "/"
                            + toByteString(alloc.getTotal()) + ")";
                    mHandler.sendMessage(message);
                }
            }
        }).start();
    }


    // --------- Private Methods ---------
    private static String toByteString(Long usage) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int i = 0;
        long size = (long) usage;
        while(size > 1024 && i < 5) {
            size = size/1024;
            i++;
        }
        return size + " " + units[i];
    }
}
