package com.fkulic.guideme.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fkulic.guideme.R;
import com.fkulic.guideme.audio.AudioPlayer;
import com.fkulic.guideme.helper.SharedPrefsHelper;
import com.fkulic.guideme.model.Landmark;
import com.fkulic.guideme.services.UploadService;
import com.fkulic.guideme.ui.adapters.AudioListAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fkulic.guideme.Constants.KEY_CITY_LATLNG;
import static com.fkulic.guideme.Constants.KEY_LANDMARK;
import static com.fkulic.guideme.Constants.KEY_LANDMARK_DIRECTIONS;
import static com.fkulic.guideme.Constants.KEY_NEW_LANDMARK;

public class LandmarkDetailsActivity extends AppCompatActivity implements AudioListAdapter.OnPlayAudioFile {
    private static final String TAG = "LandmarkDetailsActivity";

    private Landmark mLandmark;
    private AudioListAdapter mAudioListAdapter;
    private AudioPlayer mAudioPlayer;
    private RecyclerView.LayoutManager mLayoutManager;
    private int mUploadFilesCount = 0;
    private List<String> mAudioKeys;
    private boolean mNewLandmark;

    @BindView(R.id.fabTakeMeThere) FloatingActionButton fabTakeMeThere;
    @BindView(R.id.ivLandmarkPhotoDetails) ImageView ivLandmarkPhotoDetails;
    @BindView(R.id.tvLandmarkDescription) TextView tvLandmarkDescription;
    @BindView(R.id.tvAudioLabel) TextView tvAudioLabel;
    @BindView(R.id.rvAudio) RecyclerView rvAudio;
    @BindView(R.id.bSaveLandmark) Button bSaveLandmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_details);
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_LANDMARK)) {
            this.mLandmark = intent.getParcelableExtra(KEY_LANDMARK);
//            Log.d(TAG, "onCreate: " + this.mLandmark.toString());
            ButterKnife.bind(this);
            mNewLandmark = intent.getBooleanExtra(KEY_NEW_LANDMARK, false);
            setUpUI();
            mAudioPlayer = new AudioPlayer(this);
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            startActivity(new Intent(LandmarkDetailsActivity.this, ListLandmarksActivity.class));
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                startActivity(new Intent(LandmarkDetailsActivity.this, ListLandmarksActivity.class));
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

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

        String local = "";
        if (mNewLandmark) {
            fabTakeMeThere.setVisibility(View.GONE);
            bSaveLandmark.setVisibility(View.VISIBLE);
            local = "file://";
        }

//        Log.d(TAG, "setUpUI: img = " + local + mLandmark.imgUrl);
        Picasso.with(this)
                .load(local + mLandmark.imgUrl)
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
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(LandmarkDetailsActivity.this, MapActivity.class);
            intent.putExtra(KEY_LANDMARK_DIRECTIONS, mLandmark);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.warning_gps_off_directions, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPlayAudioFile(String audioName) {
        Toast.makeText(this, "Playing " + audioName + ".", Toast.LENGTH_SHORT).show();
        mAudioPlayer.play(mLandmark, audioName);
    }

    @OnClick(R.id.bSaveLandmark)
    public void onClickSaveLandmark() {
        // get latlng string before service resets it
        String city = SharedPrefsHelper.getInstance(this).getNewLandmarkCityCoordinates();

        // start uploading landmark
        Intent uploadIntent = new Intent(getApplicationContext(), UploadService.class);
        uploadIntent.putExtra(KEY_LANDMARK, mLandmark);
        startService(uploadIntent);

        // go to list landmarks
        Intent listLandmarksIntent = new Intent(LandmarkDetailsActivity.this, ListLandmarksActivity.class);
        listLandmarksIntent.putExtra(KEY_CITY_LATLNG, city);
        startActivity(listLandmarksIntent);
    }
}
