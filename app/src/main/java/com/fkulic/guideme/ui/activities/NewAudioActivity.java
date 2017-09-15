package com.fkulic.guideme.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fkulic.guideme.R;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.fkulic.guideme.Constants.KEY_AUDIO_NAME;
import static com.fkulic.guideme.Constants.KEY_AUDIO_PATH;
import static com.fkulic.guideme.Constants.PERMISSION_REQ_AUDIO_REC;

public class NewAudioActivity extends BaseActivity implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "NewAudioActivity";

    private final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GuideMe";
    private String mAudioName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean mRecording = false;
    CountDownTimer mTimer;
    private Integer mNameError;
    int mCount;

    @BindView(R.id.etAudioName) EditText etAudioName;
    @BindView(R.id.fabRecAudio) FloatingActionButton fabRecAudio;
    @BindView(R.id.ibNewAudioPlay) ImageButton ibNewAudioPlay;
    @BindView(R.id.ibNewAudioStop) ImageButton ibNewAudioStop;
    @BindView(R.id.tvAudioCounter) TextView tvAudioCounter;
    @BindView(R.id.bSaveAudio) Button bSaveAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_audio);
        setUpToolbar(R.string.record_new_audio, true);
        ButterKnife.bind(this);
        ibNewAudioPlay.setEnabled(false);
        ibNewAudioStop.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_AUDIO_REC);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (!showAreYouSureDialog()) {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showAreYouSureDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    // return true if dialog aws shown
    private boolean showAreYouSureDialog() {
        if (mAudioName != null || etAudioName.getText().toString().trim().length() > 0) {
            showYesNoDialog(NewAudioActivity.this, R.string.warning_leave_unsaved, R.string.warning_leave_unsaved_audio, mDialogOnClickListener);
            return true;
        }
        return false;
    }

    DialogInterface.OnClickListener mDialogOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (mAudioName != null) {
                        File recording = new File(mAudioName);
                        recording.delete();
                    }
                    NewAudioActivity.this.finish();
                    break;
            }
        }
    };

    private void finishActivityAndSendResult() {
        Intent resultIntent = new Intent();
        String fileName = etAudioName.getText().toString();
        File recording = new File(mAudioName);
        File audioFile = new File(dirPath, fileName + ".3gp");
        if (recording.renameTo(audioFile)) {
//            Log.d(TAG, "succes: " + audioFile.getAbsolutePath());
            resultIntent.putExtra(KEY_AUDIO_NAME, fileName);
            resultIntent.putExtra(KEY_AUDIO_PATH, audioFile.getAbsolutePath());
            this.setResult(RESULT_OK, resultIntent);
        } else {
//            Log.d(TAG, "failed to rename file");
            recording.delete();
        }
        this.finish();

    }

    @OnClick(R.id.fabRecAudio)
    void startStopRecording() {
        if (mAudioName == null) {
            mAudioName = dirPath + "/rec.3gp";
            Log.d(TAG, "File path: " + mAudioName);
            File dir = new File(dirPath);
            if (!(dir.exists() && dir.isDirectory())) {
                dir.mkdirs();
            }
        }
        if (checkRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_AUDIO_REC)) {
            if (mRecording) {
                changeIcons();
                mTimer.cancel();
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            } else {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(mAudioName);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "startRecording failed: " + e.getMessage());
                }
                changeIcons();
                startTimer();
                mRecorder.start();
            }
            mRecording = !mRecording;
        }
    }

    private void startTimer() {
        mCount = 0;
        mTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int min = mCount / 60;
                int sec = mCount % 60;
                mCount++;
                tvAudioCounter.setText(String.format("%d:%02d", min, sec));
            }

            @Override
            public void onFinish() {

            }
        };
        mTimer.start();
    }

    @OnClick(R.id.ibNewAudioPlay)
    void playPause() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mAudioName);
                mPlayer.setOnCompletionListener(this);
                mPlayer.prepare();
                mPlayer.start();
                ibNewAudioPlay.setImageResource(R.drawable.ic_pause);
            } catch (IOException e) {
                Log.e(TAG, "playPause failed: " + e.getMessage());
            }
        } else {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                ibNewAudioPlay.setImageResource(R.drawable.ic_play);
            } else {
                mPlayer.start();
                ibNewAudioPlay.setImageResource(R.drawable.ic_pause);
            }
        }
    }

    @OnClick(R.id.ibNewAudioStop)
    void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            ibNewAudioPlay.setImageResource(R.drawable.ic_play);
        }
    }

    @OnClick(R.id.bSaveAudio)
    void onClickSaveAudio() {
        validateName();
        if (mAudioName == null) {
            Toast.makeText(this, R.string.error_no_audio, Toast.LENGTH_SHORT).show();
        } else if (mNameError != null) {
            etAudioName.setError(getString(mNameError));
        } else {
            finishActivityAndSendResult();
        }
    }

    @OnTextChanged(R.id.etAudioName)
    public void validateName() {
        mNameError = null;
        String name = etAudioName.getText().toString().trim();
//        Log.d(TAG, "name = " + name);
        if (name.length() < 3) {
            mNameError = R.string.audio_name_too_short;
        } else if (name.contains(".")) {
            mNameError = R.string.error_has_dot;
        } else if (name.contains("$")) {
            mNameError = R.string.error_has_dollar;
        } else if (name.contains("#")) {
            mNameError = R.string.error_has_hashtag;
        } else if (name.contains("[") || name.contains("]")) {
            mNameError = R.string.error_has_sqrbrc;
        } else if (name.contains("/")) {
            mNameError = R.string.error_has_forw_slash;
        }
        if (mNameError != null) {
            etAudioName.setError(getString(mNameError));
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
    }

    private void changeIcons() {
        if (mRecording) {
            fabRecAudio.setImageResource(R.drawable.ic_mic);
            ibNewAudioPlay.setClickable(true);
            ibNewAudioStop.setClickable(true);
            ibNewAudioPlay.setEnabled(true);
            ibNewAudioStop.setEnabled(true);
        } else {
            fabRecAudio.setImageResource(R.drawable.ic_stop);
            ibNewAudioPlay.setClickable(false);
            ibNewAudioStop.setClickable(false);
            ibNewAudioPlay.setEnabled(false);
            ibNewAudioStop.setEnabled(false);
        }
    }

    private boolean checkRequestPermission(String permission, int reqCode) {
        if (ActivityCompat.checkSelfPermission(NewAudioActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewAudioActivity.this, new String[]{permission}, reqCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_AUDIO_REC:
                if (grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        showSnackbar(R.string.permission_rec_audio_not_granted);
                    }
                }
        }
    }

    private void showSnackbar(int stringResource) {
        Snackbar.make(findViewById(R.id.clNewLandmark), stringResource, BaseTransientBottomBar.LENGTH_LONG).show();
    }
}
