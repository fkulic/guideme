package com.fkulic.guideme.audio;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.fkulic.guideme.R;

import java.io.IOException;

/**
 * Created by Filip on 1.9.2017..
 */

public class AudioPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "AudioPlayerService";

    private final IBinder mBinder = new AudioPlayerBinder();
    private static MediaPlayer mediaPlayer;
    private static String currentlyPlaying;
    private static PlayerStatus playerStatus = PlayerStatus.STOPPED;

    enum PlayerStatus {
        PLAYING, PAUSED, STOPPED
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Log.d(TAG, "onBind: called");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playerStatus != PlayerStatus.STOPPED) {
            NotificationAudioController.destroyNotification();
        }
    }

    private void initializePlayer() {
//        Log.d(TAG, "initializing...");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // CPU on, Screen and Keyboard off
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public static PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void play(String url) {
        if (mediaPlayer == null) {
            initializePlayer();
            try {
                currentlyPlaying = url;
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(url));
                mediaPlayer.prepareAsync();
//                Log.d(TAG, "playing " + url);
            } catch (IOException e) {
                Toast.makeText(this, R.string.error_audio_player_file_not_found, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else {
            if (!currentlyPlaying.equals(url)) { // new audio file
                stop();
                play(url);
            } else { // resume playback
            }
        }
        playerStatus = PlayerStatus.PLAYING;
    }

    public void pause() {
        if (mediaPlayer != null) {
//            Log.d(TAG, "pausing playback");
            playerStatus = PlayerStatus.PAUSED;
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (playerStatus == PlayerStatus.PAUSED) {
//            Log.d(TAG, "resuming playback");
            playerStatus = PlayerStatus.PLAYING;
            mediaPlayer.start();
        }
    }

    public void stop() {
//        Log.d(TAG, "stopping playback");
        playerStatus = PlayerStatus.STOPPED;
        currentlyPlaying = null;
        NotificationAudioController.destroyNotification();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void destroy() {
        stop();
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        Log.d(TAG, "playback completed");
        stop();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public class AudioPlayerBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }
}
