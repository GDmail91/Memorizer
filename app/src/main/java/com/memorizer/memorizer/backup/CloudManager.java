package com.memorizer.memorizer.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by soo13 on 2017-06-29.
 */

public class CloudManager {

    protected static Context mContext;
    protected static CloudManager cm;
    protected static HashMap<String, CloudLinker> linkerList = new HashMap<>();

    public enum Linker {
        DropboxLinker, GoogleDriveLinker, OneDriveLinker
    }

    private CloudManager(Context context) {
        mContext = context;
    }

    public static CloudManager getInstance(Context context) {
        if (cm == null) {
            cm = new CloudManager(context.getApplicationContext());
        }

        return cm;
    }

    public ArrayList<CloudLinker> getConnectedLinker() {
        ArrayList<CloudLinker> linkers = new ArrayList<>();
        synchronized (linkerList) {
            for (String linkerName : linkerList.keySet()) {
                if (linkerList.get(linkerName).isConnected()) {
                    linkers.add(linkerList.get(linkerName));
                } else {
                    linkerList.remove(linkerName);
                }
            }
        }

        return linkers;
    }

    public CloudLinker getLinker(Linker linker) {
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        String persistent = "";
        CloudLinker cloudLinker = null;
        switch (linker) {
            case DropboxLinker:
                persistent = pref.getString("dropboxPersistent", "");
                cloudLinker = new DropboxLinker();
                break;
            case GoogleDriveLinker:
                persistent = pref.getString("googledrivePersistent", "");
                cloudLinker = new GoogleDriveLinker();
                break;
            case OneDriveLinker:
                persistent = pref.getString("onedrivePersistent", "");
                cloudLinker = new OneDriveLinker();
                break;
        }

        if (!persistent.equals("")) {
            cloudLinker.setContext(mContext);
            cloudLinker.load();
            linkerList.put(String.valueOf(linker), cloudLinker);
        } else {
            // 해당 링커를 찾는다
            cloudLinker = linkerList.get(String.valueOf(linker));
        }

        if (cloudLinker != null) {
            if (cloudLinker.isConnected()) {
                // 연결되어있는 링커 반환
                return cloudLinker;
            }
        }
        return null;
    }

    public void setLinker(Linker linker, Handler mHandler) {
        Log.d("CloudManager", "링커 새로 생성");
        CloudLinker cloudLinker = linkerList.get(String.valueOf(linker));
        if (cloudLinker == null) {
            // 링커가 없을경우 새로 생성
            try {
                cloudLinker = (CloudLinker) Class.forName(mContext.getPackageName() + ".backup." + linker.toString()).newInstance();
                cloudLinker.setContext(mContext);
                cloudLinker.load();
                cloudLinker.setLink(mHandler);
                linkerList.put(String.valueOf(linker), cloudLinker);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // 링커가 존재할경우 삭제후 생성
            linkerList.remove(String.valueOf(linker));
            try {
                cloudLinker = (CloudLinker) Class.forName(mContext.getPackageName() + ".backup." + linker.toString()).newInstance();
                cloudLinker.setContext(mContext);
                cloudLinker.load();
                cloudLinker.setLink(mHandler);
                linkerList.put(String.valueOf(linker), cloudLinker);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
