package com.fkulic.guideme.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Created by Filip on 15.9.2017..
 */

public class UploadService extends Service {
    // TODO: 15.9.2017. finish upload service

    private final Binder mBinder = new UploadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class UploadBinder extends Binder {
        public UploadService getService() {
            return UploadService.this;
        }
    }
}
