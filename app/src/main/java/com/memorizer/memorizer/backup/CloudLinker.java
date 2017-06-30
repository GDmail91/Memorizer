package com.memorizer.memorizer.backup;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cloudrail.si.interfaces.CloudStorage;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by soo13 on 2017-06-29.
 */

public abstract class CloudLinker {

    public final AtomicReference<CloudStorage> storage = new AtomicReference<>();
    protected Context mContext;
    protected Handler mHandler;

    public void setContext(Context context) {
        this.mContext = context;
    }
    public abstract boolean isConnected();
    public void setLink(Handler mHandler) {
        Log.d("CloudLinker", "setLink");
        this.mHandler = mHandler;
        if (isConnected()) {
            // disconnect
            unlinking();
        } else {
            // connect
            linking();
        }
    }
    protected abstract void load();
    protected abstract void linking();
    protected abstract void unlinking();

    protected interface LinkCallback {
        void callback();
    }
}
