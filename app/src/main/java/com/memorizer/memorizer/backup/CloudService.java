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

    /**
     * Cloud 연결하는 Linker 객체 가져옴
     * @param linker 연결할 Linker (Cloud service 제공자)
     * @return CloudLinker
     */
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

    /**
     * 해당 Linker 가 연결상태인지 확인
     * @param linker 확인할 Linker (Cloud service 제공자)
     * @return boolean
     */
    public boolean isConnected(Linker linker) {
        return getService(linker).isConnected();
    }

    /**
     * 해당 Linker 의 연결해제
     * @param linker 연결해제할 Linker
     * @param mHandler 완료 후 반환될 Handler
     */
    public void disconnect(Linker linker, Handler mHandler) {
        getService(linker).disconnect();

        Message message = Message.obtain(mHandler, Constants.DONE);
        mHandler.sendMessage(message);
    }

    /**
     * 해당 Linker 연결
     * @param linker 연결할 Linker
     * @param mHandler 완료 후 반환될 Handler
     */
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

    /**
     * Cloud 에서 해당 메모파일 삭제
     * @param memoData 삭제할 MemoData
     */
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

    /**
     * Cloud 에 메모 업데이트
     * @param memoData 업데이트할 MemoData
     */
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

    /**
     * 서버와 데이터 Sync
     * Pull 과 Push 를 별도로 함 (모든 Cloud 로 부터 Pull 을 받고 난 다음에
     * Push 를 해야 Cloud 마다 데이터 무결성을 유지할 수 있음
     * @param mHandler 완료 후 반환할 Handler
     */
    public void syncWithCloud(final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 연결된 링커마다 Pull 작업
                for (Linker linkerName : Linker.values()) {
                    getService(linkerName).syncPull();
                }

                // 연결된 링커마다 Push 작업
                for (Linker linkerName : Linker.values()) {
                    getService(linkerName).syncPush();
                }

                if (mHandler != null) {
                    Message message = new Message();
                    message.what = Constants.REFRESH;
                    mHandler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 해당 Cloud 의 남은 용량 확인
     * @param linkerName 확인할 Linker
     * @param mHandler 완료 후 반환할 Handler
     */
    public void getAllocated(final Linker linkerName, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 연결된 링커의 용량 가져옴
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
