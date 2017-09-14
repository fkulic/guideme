package com.fkulic.guideme.audio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.fkulic.guideme.model.Landmark;

/**
 * Created by Filip on 3.9.2017..
 */

public class AudioPlayer {
    private static AudioPlayer audioPlayer;
    private static AudioPlayerService mAudioPlayerService;
    private NotificationAudioController mNotificationAudioController;
    private boolean mBound = false;
    private Context mAppContext;
    private Landmark mLandmark;

    public AudioPlayer(Context context) {
        mAppContext = context.getApplicationContext();
        mNotificationAudioController = new NotificationAudioController(context);
        audioPlayer = AudioPlayer.this;
        startAudioService();
    }

    public static AudioPlayer getInstance() {
        return audioPlayer;
    }

    public void play(Landmark landmark, String audioName) {
        this.mLandmark = landmark;
        mAudioPlayerService.play(landmark.audioUrls.get(audioName));
        mNotificationAudioController.createNotificationController(this.mLandmark, audioName);
    }

    public void pause() {
        mAudioPlayerService.pause();
        mNotificationAudioController.updateNotification();
    }

    public void resume() {
        mAudioPlayerService.resume();
        mNotificationAudioController.updateNotification();
    }

    public void stop() {
        mNotificationAudioController.destroyNotification();
        if (mAudioPlayerService != null) {
            mAudioPlayerService.destroy();
        }
        if (mBound) {
            mAppContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.AudioPlayerBinder binder = (AudioPlayerService.AudioPlayerBinder) service;
            mAudioPlayerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void startAudioService() {
        if (!mBound) {
            Intent intent = new Intent(mAppContext, AudioPlayerService.class);
            mAppContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
