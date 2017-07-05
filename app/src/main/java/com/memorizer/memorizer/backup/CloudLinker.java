package com.memorizer.memorizer.backup;

import android.app.Activity;
import android.text.Html;

import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.types.CloudMetaData;
import com.cloudrail.si.types.SpaceAllocation;
import com.memorizer.memorizer.models.CheckListData;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static android.R.attr.id;
import static com.memorizer.memorizer.models.Constants.APP_DIR;

/**
 * Created by soo13 on 2017-06-29.
 */

public abstract class CloudLinker {

    protected Activity context;
    protected AtomicReference<CloudStorage> linker = new AtomicReference<>();

    public enum Linker {
        DropboxLinker, GoogleDriveLinker, OneDriveLinker
    }

    public void init(Activity context) {
        this.context = context;
        initLinker();
    }
    protected abstract void initLinker();

    public CloudStorage get() {
        return linker.get();
    }
    public abstract boolean isConnected();
    public abstract void disconnect();
    public abstract void connect();
    public abstract void storePersistent();

    public void onDelete(MemoData memoData) {
        if (isConnected()) {
            // 디렉토리가 있을 경우
            if (linker.get().exists(APP_DIR)) {
                linker.get().delete(APP_DIR + "/" + memoData.getFileName());
            }
        }
    }

    public void onUpdate(MemoData memoData) {
        if (isConnected()) {
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

            // 디렉토리가 있을 경우
            if (linker.get().exists(APP_DIR)) {
                // 파일 있는지 확인
                if (linker.get().exists(APP_DIR + "/" + fileName)) {
                    // 파일 있다면 메타정보 확인
                    CloudMetaData metaData = linker.get().getMetadata(APP_DIR + "/" + fileName);

                    Long modifyTime = metaData.getModifiedAt();
                    if (modifyTime <= modifiedDate) { // 수정시간이 최종 수정시간보다 작으면
                        // 파일이름, 인풋스트림, 파일크기, 덮어씀
                        linker.get().upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                    }
                } else {
                    // 바로 덮어씀
                    linker.get().upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
                }
            } else {
                // 디렉토리 생성후 파일 생성
                linker.get().createFolder(APP_DIR);
                linker.get().upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
            }
        }
    }

    public void sync() {
        if (isConnected()) {
            // 디렉토리 생성
            if (!linker.get().exists(APP_DIR)) {
                linker.get().createFolder(APP_DIR);
            }

            // 서버에서 Pull
            // 서버 파일 목록 가져옴
            List<CloudMetaData> childrenList = linker.get().getChildren(APP_DIR);
            for (CloudMetaData children : childrenList) {
                String remoteFileName = children.getName();

                String[] temp = remoteFileName.split(" ");
                String postedDate = temp[0]+" "+temp[1];    // 생성일
                postedDate = postedDate.replace("_", ":");  // 생성시간 이름의 ":" 가 "_" 로 변경되어 있음
                int id = Integer.valueOf(temp[2].replace(".json", "")); // 메모 ID
                Long remoteFileModifyDate = children.getModifiedAt();   // 수정일

                MemoModel memoModel = new MemoModel(context);

                // 해당 메모가 업데이트가 필요한지 확인
                if (memoModel.needUpdate(id, postedDate, remoteFileModifyDate)) {
                    // 가져오기
                    pull(remoteFileName);
                }
                memoModel.close();
            }

            // 로컬에서 Push
            MemoModel memoModel = new MemoModel(context);
            // 등록된 메모 가져오기
            ArrayList<MemoData> allData = memoModel.getAllData();
            memoModel.close();
            for (MemoData memoData : allData) {
                // 보내기
                push(memoData);
            }
        }
    }

    protected void pull(String remoteFileName) {
        try {
            InputStream inputStream = linker.get().download(APP_DIR+"/"+remoteFileName);
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
            MemoModel memoModel = new MemoModel(context);
            memoModel.update(remoteMemo);
            memoModel.close();

            // 알림 설정
            Scheduler.getScheduler().deleteSelectedAlarm(context, remoteMemo.get_id());
            Scheduler.getScheduler().setSchedule(context, remoteMemo, true);
        } catch (IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

    }

    protected void push(MemoData memoData) {
        // 임시 파일 생성후 스트림 가져옴
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

        // 파일 있는지 확인
        if (linker.get().exists(APP_DIR + "/" + fileName)) {
            // 파일 있다면 메타정보 확인
            CloudMetaData metaData = linker.get().getMetadata(APP_DIR + "/" + fileName);

            Long modifyTime = metaData.getModifiedAt();
            if (modifyTime <= modifiedDate) { // 수정시간이 최종 수정시간보다 작으면
                // 파일이름, 인풋스트림, 파일크기, 덮어씀
                linker.get().upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
            }
        } else {
            // 바로 덮어씀
            linker.get().upload(APP_DIR + "/" + fileName, inputStream, fileSize, true);
        }
    }

    public SpaceAllocation getAllocation() {
        if (isConnected()) {
            SpaceAllocation alloc = linker.get().getAllocation();
            return alloc;
        }
        return null;
    }
}
