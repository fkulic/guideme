package com.fkulic.guideme.services;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.FirebaseDbHelper;
import com.fkulic.guideme.helper.SharedPrefsHelper;
import com.fkulic.guideme.model.Landmark;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.fkulic.guideme.Constants.KEY_LANDMARK;


/**
 * Created by Filip on 15.9.2017..
 */

public class UploadService extends IntentService implements OnSuccessListener<UploadTask.TaskSnapshot>,
        OnProgressListener<UploadTask.TaskSnapshot>, OnFailureListener {

    private static final String TAG = "UploadService";
    private static final int UPLOAD_NOTIFICATION_ID = 600;

    private FirebaseStorage mFirebaseStorage;
    private int mUploadFilesCount = 0;
    private List<String> mAudioKeys;
    private Landmark mLandmark;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private long mCurrentFileSize;
    private String mCityLatLng;

    public UploadService() {
        super(TAG);
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(KEY_LANDMARK)) {
            SharedPrefsHelper prefsHelper = SharedPrefsHelper.getInstance(this);
            mCityLatLng = prefsHelper.getNewLandmarkCityCoordinates(); // get city coordinates
            prefsHelper.setNewLandmarkCityCoordinates(null); // reset coordinates

            mLandmark = intent.getParcelableExtra(KEY_LANDMARK);
            mAudioKeys = new ArrayList<>(mLandmark.audioUrls.keySet());

            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.new_landmark))
                    .setContentText(getString(R.string.uploading_files))
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_upload);

            startForeground(UPLOAD_NOTIFICATION_ID, mBuilder.build());
            uploadLandmark();
        }
    }

    private void stopUploadService() {
        stopForeground(false);
        stopSelf();
    }

    public void uploadLandmark() {
        if (mLandmark != null) {
            StorageReference storage = mFirebaseStorage.getReference();
            String latLng = mLandmark.coordinates.getLatLngStringForDB();
            Uri uri;
            if (mUploadFilesCount == 0) {
                uri = Uri.parse(mLandmark.imgUrl);
                storage = storage.child("images/" + latLng + "/" + uri.getLastPathSegment());
            } else {
                uri = Uri.parse(mLandmark.audioUrls.get(mAudioKeys.get(mUploadFilesCount - 1)));
                storage = storage.child("audio/" + latLng + "/" + uri.getLastPathSegment());
            }

            Log.d(TAG, "uploadFile: uri = " + uri.toString());

            try {
                File file = new File(uri.toString());
                mCurrentFileSize = file.length();
                InputStream stream = new FileInputStream(file);
                UploadTask uploadTask = storage.putStream(stream);
                uploadTask.addOnSuccessListener(this);
                uploadTask.addOnProgressListener(this);
                uploadTask.addOnFailureListener(this);
            } catch (IOException e) {
                Log.e(TAG, "Failed to upload. File not found.", e);
            }
        }
    }

    private void updateDatabase() {
        // TODO: 16.9.2017. add city if not exists (possible future)
        FirebaseDbHelper.addLandmark(mCityLatLng, mLandmark);
        mBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.ic_success)
                .setContentTitle(getString(R.string.upload_success))
                .setContentText(getString(R.string.new_landmark_created))
                .setProgress(0, 0, false);
        mNotificationManager.notify(UPLOAD_NOTIFICATION_ID, mBuilder.build());
        stopUploadService();
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        String url = taskSnapshot.getDownloadUrl().toString();
        if (mUploadFilesCount == 0) {
            mLandmark.imgUrl = url;
        } else {
            mLandmark.audioUrls.put(mAudioKeys.get(mUploadFilesCount - 1), url);
        }
        mUploadFilesCount++;
        if (mUploadFilesCount < mLandmark.getFileCount()) {
            uploadLandmark();
        } else {
            updateDatabase();
        }
    }

    @Override
    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
        Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / mCurrentFileSize;
        Log.d(TAG, "onProgress: " + progress.intValue() + "/100%");
        mBuilder.setContentText("Uploading files: " + (mUploadFilesCount + 1) + "/" + mLandmark.getFileCount());
        mBuilder.setProgress(100, progress.intValue(), false);
        mNotificationManager.notify(UPLOAD_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        // TODO: 15.9.2017. delete if something was uploaded
        mBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.ic_failure)
                .setContentTitle(getString(R.string.upload_failure))
                .setContentText(getString(R.string.new_landmark_not_created))
                .setProgress(0, 0, false);
        mNotificationManager.notify(UPLOAD_NOTIFICATION_ID, mBuilder.build());
        stopUploadService();
        e.printStackTrace();
        Toast.makeText(this, R.string.error_uploading_files, Toast.LENGTH_SHORT).show();
    }

}
