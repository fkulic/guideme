package com.fkulic.guideme.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fkulic.guideme.BuildConfig;
import com.fkulic.guideme.R;
import com.fkulic.guideme.model.Coordinates;
import com.fkulic.guideme.model.Landmark;
import com.fkulic.guideme.ui.adapters.NewLandmarkAudioAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fkulic.guideme.Constants.KEY_AUDIO_NAME;
import static com.fkulic.guideme.Constants.KEY_AUDIO_PATH;
import static com.fkulic.guideme.Constants.KEY_LANDMARK;
import static com.fkulic.guideme.Constants.KEY_NEW_LANDMARK;
import static com.fkulic.guideme.Constants.PERMISSION_REQ_READ_EXT;
import static com.fkulic.guideme.Constants.PERMISSION_REQ_WRITE_EXT;
import static com.fkulic.guideme.Constants.REQ_AUDIO_FROM_FILE;
import static com.fkulic.guideme.Constants.REQ_AUDIO_RECORD;
import static com.fkulic.guideme.Constants.REQ_IMAGE_CAMERA;
import static com.fkulic.guideme.Constants.REQ_IMAGE_GALLERY;

public class NewLandmarkActivity extends BaseActivity implements NewLandmarkAudioAdapter.OnRemoveAudioFile {
    private static final String TAG = "NewLandmarkActivity";

    private Map<String, String> mAudioFiles = new HashMap<>();
    private String mImgUrl;
    private NewLandmarkAudioAdapter mAudioAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @BindView(R.id.etLandmarkName) EditText etLandmarkName;
    @BindView(R.id.ibFromGallery) ImageButton ibFromGallery;
    @BindView(R.id.ibCamera) ImageButton ibCamera;
    @BindView(R.id.ivNewLandmarkPhoto) ImageView ivNewLandmarkPhoto;
    @BindView(R.id.etLandmarkDescription) EditText etLandmarkDescription;
    @BindView(R.id.tvAudioFilesLabel) TextView tvAudioFilesLabel;
    @BindView(R.id.ibNewAudioFile) ImageButton ibNewAudioFile;
    @BindView(R.id.rvNewLandmarkAudio) RecyclerView rvNewLandmarkAudio;
    @BindView(R.id.fabPreviewNewLandmark) FloatingActionButton fabPreviewNewLandmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_landmark);
        setUpToolbar(R.string.new_landmark, true);
        ButterKnife.bind(this);
        this.mLayoutManager = new LinearLayoutManager(NewLandmarkActivity.this, LinearLayoutManager.VERTICAL, false);
        this.mAudioAdapter = new NewLandmarkAudioAdapter(new ArrayList<>(mAudioFiles.keySet()), this);
        this.rvNewLandmarkAudio.setLayoutManager(this.mLayoutManager);
        this.rvNewLandmarkAudio.setAdapter(mAudioAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    @OnClick(R.id.ibFromGallery)
    void imgFromGallery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQ_READ_EXT);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQ_IMAGE_GALLERY);
        }
    }

    @OnClick(R.id.ibCamera)
    void imgFromCamera() {
        if (checkRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_WRITE_EXT)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File imgFile = null;
                try {
                    imgFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Couldn't create image file." + e.getMessage());
                    Toast.makeText(this, R.string.error_creating_img_file, Toast.LENGTH_SHORT).show();
                }

                if (imgFile != null) {
                    Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", imgFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQ_IMAGE_CAMERA);
                }
            }
        }
    }

    @OnClick(R.id.ibNewAudioFile)
    void onClickNewAudioFile() {
        super.showListDialog(NewLandmarkActivity.this, R.string.pick_source, R.array.audio_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent fromFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        fromFileIntent.setType("audio/*");
                        startActivityForResult(fromFileIntent, REQ_AUDIO_FROM_FILE);
                        break;

                    case 1:
                        Intent recordIntent = new Intent(NewLandmarkActivity.this, NewAudioActivity.class);
                        startActivityForResult(recordIntent, REQ_AUDIO_RECORD);
                }

            }
        });
    }


    @OnClick(R.id.fabPreviewNewLandmark)
    void previewLandmark() {

        Landmark landmark = new Landmark(etLandmarkName.getText().toString().trim(),
                new Coordinates(44.561584, 18.676801),
                etLandmarkDescription.getText().toString(),
                mImgUrl,
                mAudioFiles);
        if (landmark.isValid()) {
            Intent intent = new Intent(NewLandmarkActivity.this, LandmarkDetailsActivity.class);
            intent.putExtra(KEY_LANDMARK, landmark);
            intent.putExtra(KEY_NEW_LANDMARK, true);
            startActivity(intent);
        } else {
            showSnackbar(R.string.landmark_not_valid);
        }
    }

    // Image functions
    private File createImageFile() throws IOException {
        StringBuilder imgName = new StringBuilder()
                .append("JPEG_GuideMe_")
                .append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "GuideMe");
        if (!(storageDir.exists() && storageDir.isDirectory())) {
            storageDir.mkdirs();
        }

        File img = File.createTempFile(
                imgName.toString(),
                ".jpg",
                storageDir);

        mImgUrl = img.getAbsolutePath();
        return img;
    }

    private void addToGallery() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mImgUrl);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        this.sendBroadcast(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_IMAGE_GALLERY:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    this.mImgUrl = getUrlFromUri(uri);
                    addToGallery();
                    setThumbnail();
                }
                break;
            case REQ_IMAGE_CAMERA:
                if (resultCode == RESULT_OK) {
                    setThumbnail();
                    addToGallery();
                } else {
                    mImgUrl = null;
                }
                break;
            case REQ_AUDIO_FROM_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    String filePath = getUrlFromUri(data.getData());
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                    fileName = fileName.split("\\.")[0];
                    mAudioFiles.put(fileName, filePath);
                    mAudioAdapter.addAudio(fileName);
                }
                break;
            case REQ_AUDIO_RECORD:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra(KEY_AUDIO_NAME);
                    String path = data.getStringExtra(KEY_AUDIO_PATH);
                    mAudioFiles.put(name, path);
                    mAudioAdapter.addAudio(name);
                }
                break;
        }
    }

    // return true if dialog aws shown
    private boolean showAreYouSureDialog() {
        if (startedNewLandmark()) {
            showYesNoDialog(NewLandmarkActivity.this, R.string.warning_leave_unsaved,
                    R.string.warning_leave_unsaved_landmark, mDialogLeavingClickListener);
            return true;
        }
        return false;
    }

    DialogInterface.OnClickListener mDialogLeavingClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    NewLandmarkActivity.this.finish();
                    break;
            }
        }
    };

    // return true if any of EditText views has text or if there is an image or audio
    private boolean startedNewLandmark() {
        if (etLandmarkName.getText().toString().trim().length() > 0 ||
                etLandmarkDescription.getText().toString().trim().length() > 0 ||
                mImgUrl != null ||
                mAudioAdapter.getItemCount() > 0) {
            return true;
        }
        return false;
    }

    private String getUrlFromUri(Uri uri) {
        String url;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            url = uri.getPath();
        } else {
            cursor.moveToFirst();
            url = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            cursor.close();
        }
        return url;
    }

    private void setThumbnail() {
        Log.d(TAG, "Thumbnail from: " + mImgUrl);
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mImgUrl), 128, 128);
        ivNewLandmarkPhoto.setImageBitmap(thumbnail);
    }

    // Permission functions
    private boolean checkRequestPermission(String permission, int reqCode) {
        if (ActivityCompat.checkSelfPermission(NewLandmarkActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewLandmarkActivity.this, new String[]{permission}, reqCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_WRITE_EXT:
                if (grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        showSnackbar(R.string.ext_storage_not_granted);
                    }
                } else {
                    imgFromCamera();
                }
                break;

            case PERMISSION_REQ_READ_EXT:
                if (grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        showSnackbar(R.string.ext_storage_not_granted_img);
                    } else {
                        imgFromGallery();
                    }
                }
                break;
        }
    }

    private void showSnackbar(int stringResource) {
        Snackbar.make(findViewById(R.id.clNewLandmark), stringResource, BaseTransientBottomBar.LENGTH_LONG).show();
    }

    @Override
    public void onRemoveAudioFile(String audioName) {
        mAudioAdapter.removeAudio(audioName);
        mAudioFiles.remove(audioName);
        mAudioAdapter.notifyDataSetChanged();
    }
}
