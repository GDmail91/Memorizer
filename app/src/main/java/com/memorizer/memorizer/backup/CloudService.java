package com.memorizer.memorizer.backup;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
import com.cloudrail.si.types.SpaceAllocation;
import com.memorizer.memorizer.R;
import com.memorizer.memorizer.models.CheckListData;
import com.memorizer.memorizer.models.Constants;
import com.memorizer.memorizer.models.MemoData;
import com.memorizer.memorizer.models.MemoModel;
import com.memorizer.memorizer.scheduler.Scheduler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.memorizer.memorizer.models.Constants.APP_DIR;
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
                context.getResources().getString(R.string.dropbox_id),
                context.getResources().getString(R.string.dropbox_sec)));
    }

    private void initGoogleDrive() {
        googledrive.set(new GoogleDrive(
                context,
                context.getResources().getString(R.string.google_id),
                "",
                "com.memorizer.memorizer:/oauth2redirect",
                ""));
        ((GoogleDrive) googledrive.get()).useAdvancedAuthentication();
    }

    private void initOneDrive() {
        onedrive.set(new OneDrive(context,
                context.getResources().getString(R.string.one_id),
                context.getResources().getString(R.string.one_sec)));
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

    public void onDelete(final MemoData memoData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (CloudService.Linker linkerName : CloudService.Linker.values()) {
                    if (CloudService.getInstance().isConnected(linkerName)) {
                        CloudStorage linker = CloudService.getInstance().getService(linkerName);
                        // 디렉토리가 있을 경우
                        if (linker.exists(APP_DIR)) {
                            linker.delete(APP_DIR + "/" + memoData.getFileName());
                        }
                    }
                }
            }
        }).start();
    }

    public void onUpdate(final MemoData memoData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // DB 에서 이름으로 스트림, size, 수정날짜 가져옴
                File memoFile = memoData.saveFile(context);
                String fileName = memoData.getFileName();

                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(memoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Long fileSize = memoFile.length();
                Long modifiedDate = memoData.getEditedToLong();

                for (CloudService.Linker linkerName : CloudService.Linker.values()) {
                    if (CloudService.getInstance().isConnected(linkerName)) {
                        CloudStorage linker = CloudService.getInstance().getService(linkerName);
                        // 디렉토리가 있을 경우
                        if (linker.exists(APP_DIR)) {
                            // 파일 있는지 확인
                            if (linker.exists(APP_DIR + "/" + fileName)) {
                                // 파일 있다면 메타정보 확인
                                CloudMetaData metaData = linker.getMetadata(APP_DIR + "/" + fileName);

                                Long modifyTime = metaData.getModifiedAt();
                                if (modifyTime <= modifiedDate) { // 수정시간이 최종 수정시간보다 작으면
                                    // 파일이름, 인풋스트림, 파일크기, 덮어씀
                                    linker.upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                                }
                            } else {
                                // 바로 덮어씀
                                linker.upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                            }
                        } else {
                            // 디렉토리 생성후 파일 생성
                            linker.createFolder(APP_DIR);
                            linker.upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                        }
                    }
                }
            }
        }).start();
    }

    public void syncWithCloud(final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Linker linkerName : Linker.values()) {
                    if (CloudService.getInstance().isConnected(linkerName)) {
                        CloudStorage linker = CloudService.getInstance().getService(linkerName);
                        // 디렉토리 생성
                        if (!linker.exists(APP_DIR)) {
                            linker.createFolder(APP_DIR);
                        }

                        // 서버에서 Pull
                        List<CloudMetaData> childrenList = linker.getChildren(APP_DIR);
                        for (CloudMetaData children : childrenList) {
                            String remoteFileName = children.getName();

                            String[] temp = remoteFileName.split(" ");
                            String postedDate = temp[0]+" "+temp[1];
                            int id = Integer.valueOf(temp[2].replace(".json", ""));
                            Long remoteFileModifyDate = children.getModifiedAt();

                            MemoModel memoModel = new MemoModel(context);

                            // 해당 메모가 업데이트가 필요한경우
                            if (memoModel.needUpdate(id, postedDate.replace("_", ":"), remoteFileModifyDate)) {
                                try {
                                    InputStream inputStream = linker.download(APP_DIR+"/"+remoteFileName);
                                    JSONParser jsonParser = new JSONParser();
                                    JSONObject jsonObject = (JSONObject)jsonParser.parse(
                                            new InputStreamReader(inputStream, "UTF-8"));

                                    String[] isCheck = ((String) jsonObject.get("isCheck")).split(",");
                                    String[] checkMessage = ((String) jsonObject.get("checkMessage")).split(",");

                                    ArrayList<CheckListData> checklist = new ArrayList<>();

                                    int i = 0;
                                    while (i < isCheck.length) {
                                        checklist.add(
                                                new CheckListData(
                                                        Boolean.valueOf(isCheck[i]),
                                                        Html.fromHtml(checkMessage[i]).toString()));
                                        i++;
                                    }
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis((Long) jsonObject.get("whileDate"));

                                    MemoData remoteMemo = new MemoData(
                                            id,
                                            Html.fromHtml((String) jsonObject.get("content")).toString(),
                                            calendar,
                                            Integer.valueOf(((Long) jsonObject.get("term")).toString()),
                                            Html.fromHtml((String) jsonObject.get("label")).toString(),
                                            Integer.valueOf(((Long) jsonObject.get("labelPos")).toString()),
                                            (Boolean) jsonObject.get("isRandom"),
                                            Integer.valueOf(((Long) jsonObject.get("timeOfHour")).toString()),
                                            Integer.valueOf(((Long) jsonObject.get("timeOfMinute")).toString()),
                                            (String) jsonObject.get("posted"),
                                            (String) jsonObject.get("edited"),
                                            (Boolean) jsonObject.get("isMarkdown"),
                                            checklist);

                                    // 파일 업데이트
                                    memoModel.update(remoteMemo);

                                    // 알림 설정
                                    Scheduler.getScheduler().deleteSelectedAlarm(context, remoteMemo.get_id());
                                    Scheduler.getScheduler().setSchedule(context, remoteMemo, true);
                                } catch (IOException | org.json.simple.parser.ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            memoModel.close();
                        }

                        // 로컬에서 Push
                        MemoModel memoModel = new MemoModel(context);
                        // 등록된 메모 가져오기
                        ArrayList<MemoData> allData = memoModel.getAllData();
                        for (MemoData memoData : allData) {
                            // 임시 파일 생성후 스트림 가져옴
                            File memoFile = memoData.saveFile(context);
                            String fileName = memoData.getFileName();

                            memoModel.close();

                            FileInputStream inputStream = null;
                            try {
                                inputStream = new FileInputStream(memoFile);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            Long fileSize = memoFile.length();
                            Long modifiedDate = memoData.getEditedToLong();

                            // 파일 있는지 확인
                            if (linker.exists(APP_DIR + "/" + fileName)) {
                                // 파일 있다면 메타정보 확인
                                CloudMetaData metaData = linker.getMetadata(APP_DIR + "/" + fileName);

                                Long modifyTime = metaData.getModifiedAt();
                                if (modifyTime <= modifiedDate) { // 수정시간이 최종 수정시간보다 작으면
                                    // 파일이름, 인풋스트림, 파일크기, 덮어씀
                                    linker.upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                                }
                            } else {
                                // 바로 덮어씀
                                linker.upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                            }
                        }
                    }
                }

                if (mHandler != null) {
                    Message message = new Message();
                    message.what = Constants.REFRESH;
                    mHandler.sendMessage(message);
                }
            }
        }).start();
    }

    public void getAllocated(final CloudService.Linker linkerName, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (CloudService.getInstance().isConnected(linkerName)) {
                    SpaceAllocation alloc = CloudService.getInstance().getService(linkerName).getAllocation();
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

    private static String toByteString(Long usage) {
        if (usage > 1000000000) {
            // GB
            return usage/1000000000 + " GB";
        } else if (usage > 1000000) {
            // MB
            return usage/1000000 + " MB";
        } else if (usage > 1000) {
            // KB
            return usage/1000 + " KB";
        } else {
            return usage + " B";
        }
    }
}
