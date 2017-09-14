package com.fkulic.guideme.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.SharedPrefsHelper;

import static com.fkulic.guideme.Constants.PERMISSION_REQ_FINE_LOC;

/**
 * Created by Filip on 9.9.2017..
 */

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected void setUpToolbar(String name, boolean enableHome) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

            if (toolbar != null) {
                toolbar.setTitle(name);
                setSupportActionBar(toolbar);
                actionBar = getSupportActionBar();
            }
        }

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enableHome);
        }
//        Log.d(TAG, "setUpToolbar: name: " + name + ", backButton: " + enableHome);
    }

    protected void setUpToolbar(@StringRes int resource, boolean enableHome) {
        String name = getString(resource);
        setUpToolbar(name, enableHome);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

     protected void setDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SharedPrefsHelper prefsHelper = SharedPrefsHelper.getInstance(this);
        prefsHelper.setWidth(metrics.widthPixels);
        prefsHelper.setHeight(metrics.heightPixels);
    }

    protected void showYesNoDialog(@NonNull Context context, String title, String message,
                                            DialogInterface.OnClickListener clickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", clickListener)
                .setNegativeButton("No", clickListener);
        builder.create().show();
    }

    protected void showYesNoDialog(@NonNull Context context, @StringRes int titleResource,
                                            @StringRes int messageResource, DialogInterface.OnClickListener clickListener) {
        String title = getString(titleResource);
        String message = getString(messageResource);
        showYesNoDialog(context, title, message, clickListener);
    }

    protected void showListDialog(@NonNull Context context, String title,
                                  String items[], DialogInterface.OnClickListener itemClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(items, itemClickListener);
        builder.create().show();
    }

    protected void showListDialog(@NonNull Context context, @StringRes int titleResource,
                                  @ArrayRes int itemsResource, DialogInterface.OnClickListener itemClickListener) {
        String title = getString(titleResource);
        String items[] = getResources().getStringArray(itemsResource);
        showListDialog(context, title, items, itemClickListener);
    }

    protected boolean checkGpsPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "checkGpsPermission: no permission");
            return false;
        }
//        Log.d(TAG, "checkGpsPermission: has permission");
        return true;
    }

    protected void askGpsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_FINE_LOC);
    }
}
