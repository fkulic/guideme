package com.fkulic.guideme.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fkulic.guideme.R;
import com.fkulic.guideme.audio.AudioListAdapter;
import com.fkulic.guideme.audio.AudioPlayer;
import com.fkulic.guideme.helper.FirebaseDbHelper;
import com.fkulic.guideme.helper.SharedPrefsHelper;
import com.fkulic.guideme.model.Landmark;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fkulic.guideme.Constants.KEY_LANDMARK;
import static com.fkulic.guideme.Constants.KEY_LANDMARK_DIRECTIONS;
import static com.fkulic.guideme.Constants.KEY_NEW_LANDMARK;

public class LandmarkDetailsActivity extends AppCompatActivity implements AudioListAdapter.OnPlayAudioFile,
        OnProgressListener<UploadTask.TaskSnapshot>, OnFailureListener, OnSuccessListener<UploadTask.TaskSnapshot> {
    private static final String TAG = "LandmarkDetailsActivity";

    private Landmark mLandmark;
    private AudioListAdapter mAudioListAdapter;
    private AudioPlayer mAudioPlayer;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mUploadFilesCount = 0;
    private List<String> mAudioKeys;

    @BindView(R.id.fabTakeMeThere) FloatingActionButton fabTakeMeThere;
    @BindView(R.id.ivLandmarkPhotoDetails) ImageView ivLandmarkPhotoDetails;
    @BindView(R.id.tvLandmarkDescription) TextView tvLandmarkDescription;
    @BindView(R.id.tvAudioLabel) TextView tvAudioLabel;
    @BindView(R.id.rvAudio) RecyclerView rvAudio;
    @BindView(R.id.ibSaveLandmark) ImageButton ibSaveLandmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_details);
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_LANDMARK)) {
            this.mLandmark = intent.getParcelableExtra(KEY_LANDMARK);
            Log.d(TAG, "onCreate: " + this.mLandmark.toString());
            ButterKnife.bind(this);
            setUpUI();
            mAudioPlayer = new AudioPlayer(this);
            if (intent.hasExtra(KEY_NEW_LANDMARK)) {
                if (intent.getBooleanExtra(KEY_NEW_LANDMARK, false)) {
                    fabTakeMeThere.setVisibility(View.GONE);
                    ibSaveLandmark.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setUpUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLandmarkDetalis);
        toolbar.setTitle(mLandmark.name);
        setSupportActionBar(toolbar);

        int width;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = SharedPrefsHelper.getInstance(this).getWidth();
        } else {
            width = SharedPrefsHelper.getInstance(this).getHeight();
        }
        if (width > 2048) {
            width = 2048;
        }
        int height = (int) getResources().getDimension(R.dimen.app_bar_height);
//        Log.d(TAG, "dimensions: " +width + "x" +height);
        Picasso.with(this)
                .load(mLandmark.imgUrl)
                .resize(width, height)
                .centerCrop()
                .into(this.ivLandmarkPhotoDetails);


        this.tvLandmarkDescription.setText(mLandmark.description);

        if (mLandmark.audioUrls.size() > 0) {
            mAudioKeys = new ArrayList<>(mLandmark.audioUrls.keySet());
            this.tvAudioLabel.setVisibility(View.VISIBLE);
            this.mLayoutManager = new LinearLayoutManager(LandmarkDetailsActivity.this, LinearLayoutManager.VERTICAL, false);
            mAudioListAdapter = new AudioListAdapter(mAudioKeys, this);
            this.rvAudio.setLayoutManager(this.mLayoutManager);
            this.rvAudio.setAdapter(mAudioListAdapter);
        }
    }

    @OnClick(R.id.fabTakeMeThere)
    public void takeMeToLandmark() {
        Intent intent = new Intent(LandmarkDetailsActivity.this, MapActivity.class);
        intent.putExtra(KEY_LANDMARK_DIRECTIONS, mLandmark);
        startActivity(intent);
    }

    @Override
    public void onPlayAudioFile(String audioName) {
        mAudioPlayer.play(mLandmark, audioName);
    }

    @OnClick(R.id.ibSaveLandmark)
    public void onClickSaveLandmark() {
        uploadFile();
    }

    private void uploadFile() {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        String latLng = mLandmark.coordinates.getLatLngStringForDB();
        Uri uri;
        if (mUploadFilesCount == 0) {
            uri = Uri.parse(mLandmark.imgUrl);
            storage = storage.child("images/" + latLng + "/" + uri.getLastPathSegment());
        } else {
            uri = Uri.parse(mLandmark.audioUrls.get(mAudioKeys.get(mUploadFilesCount-1)));
            storage = storage.child("audio/" + latLng + "/" + uri.getLastPathSegment());
        }

        Log.d(TAG, "uploadFile: uri = " + uri.toString());

        try {
            InputStream stream = new FileInputStream(new File(uri.toString()));
            UploadTask uploadTask = storage.putStream(stream);
            uploadTask.addOnSuccessListener(this);
            uploadTask.addOnProgressListener(this);
            uploadTask.addOnFailureListener(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to upload. File not found.", e);
        }
    }

    private void updateDatabase() {
        FirebaseDbHelper.addLandmark(SharedPrefsHelper.getInstance(this).getCurrentCity(), mLandmark);
        Toast.makeText(getApplicationContext(), R.string.new_landmark_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
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
            uploadFile();
        } else {
            updateDatabase();
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        e.printStackTrace();
        Toast.makeText(this, R.string.error_uploading_files, Toast.LENGTH_SHORT).show();
    }

}
