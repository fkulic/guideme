package com.fkulic.guideme.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.fkulic.guideme.Constants.ACTION;
import static com.fkulic.guideme.Constants.ACTION_PAUSE;
import static com.fkulic.guideme.Constants.ACTION_PLAY;
import static com.fkulic.guideme.Constants.ACTION_STOP;


/**
 * Created by Filip on 4.9.2017..
 */

public class AudioPlayerNotificationsReceiver extends BroadcastReceiver {
    private static final String TAG = "AudioPlayerNotification";

    public AudioPlayerNotificationsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioPlayer audioPlayer = AudioPlayer.getInstance();

        if (intent.hasExtra(ACTION)) {
            switch (intent.getStringExtra(ACTION)) {
                case ACTION_PLAY:
                    Log.d(TAG, "onReceive: resuming playback...");
                    audioPlayer.resume();
                    break;
                case ACTION_PAUSE:
                    Log.d(TAG, "onReceive: pausing playback...");
                    audioPlayer.pause();
                    break;
                case ACTION_STOP:
                    Log.d(TAG, "onReceive: stopping playback...");
                    audioPlayer.stop();
                    break;
            }
        }
    }
}
