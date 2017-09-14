package com.fkulic.guideme.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fkulic.guideme.R;
import com.fkulic.guideme.model.Landmark;
import com.fkulic.guideme.ui.activities.LandmarkDetailsActivity;

import static com.fkulic.guideme.Constants.ACTION;
import static com.fkulic.guideme.Constants.ACTION_PAUSE;
import static com.fkulic.guideme.Constants.ACTION_PLAY;
import static com.fkulic.guideme.Constants.ACTION_STOP;
import static com.fkulic.guideme.Constants.KEY_LANDMARK;

/**
 * Created by Filip on 1.9.2017..
 */

public class NotificationAudioController  {
    private static final String TAG = "NotificationController";

    private static final int NOTIFICATION_ID = 10;
    private static final int PAUSE_ID = 0;
    private static final int STOP_ID = 1;
    private static final int PLAY_ID = 0;

    private Context mContext;
    private static NotificationManager notificationManager;
    private Notification mNotification;
    private Landmark mLandmark;
    private String mPlaying;

    NotificationAudioController(Context context) {
        mContext = context;
    }

    void createNotificationController(Landmark landmark, String audioName) {
        this.mLandmark = landmark;
        this.mPlaying = audioName;
        int playPauseResource;
        String playPause;
        PendingIntent playPauseIntent;

        if (AudioPlayerService.getPlayerStatus() == AudioPlayerService.PlayerStatus.PAUSED) {
            Log.d(TAG, "createNotificationController: play button");
            playPauseResource = R.drawable.ic_play;
            playPause = "Play";
            playPauseIntent = createPendingIntent(ACTION_PLAY, PLAY_ID);
        } else {
            Log.d(TAG, "createNotificationController: pause button");
            playPauseResource = R.drawable.ic_pause;
            playPause = "Pause";
            playPauseIntent = createPendingIntent(ACTION_PAUSE, PAUSE_ID);
        }

        Intent intentDetails = new Intent(mContext, LandmarkDetailsActivity.class);
        intentDetails.putExtra(KEY_LANDMARK, landmark);

        Bitmap bigIcon = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(landmark.imgUrl), 128, 128);

        mNotification = new NotificationCompat.Builder(mContext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(bigIcon)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(PendingIntent.getActivity(mContext, NOTIFICATION_ID, intentDetails, PendingIntent.FLAG_ONE_SHOT))
                .addAction(R.drawable.ic_stop, "Stop", createPendingIntent(ACTION_STOP, STOP_ID))
                .addAction(playPauseResource, playPause, playPauseIntent)
                .setDeleteIntent(createPendingIntent(ACTION_STOP, STOP_ID))
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(STOP_ID, PAUSE_ID))
//                        .setMediaSession() // use with MediaController
                .setContentTitle(landmark.name)
                .setContentText(audioName)
//                .setChannelId() // for api 26
                .build();

        if (notificationManager == null) {
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    private PendingIntent createPendingIntent(String action, int id) {
        Intent intent = new Intent(mContext.getApplicationContext(), AudioPlayerNotificationsReceiver.class);
        intent.putExtra(ACTION, action);
        return PendingIntent.getBroadcast(mContext.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    void updateNotification() {
        createNotificationController(mLandmark, mPlaying);
    }

    static void destroyNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
