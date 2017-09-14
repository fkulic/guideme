package com.fkulic.guideme.ui;

import android.app.ProgressDialog;
import android.content.Context;

import com.fkulic.guideme.R;

/**
 * Created by Filip on 8.9.2017..
 */

public class LoadingDataDialog {

    private ProgressDialog mProgressDialog;
    private Context mContext;

    public LoadingDataDialog(Context context) {
        mContext = context;
    }

    public void show() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getString(R.string.loading));
        mProgressDialog.setMessage(mContext.getString(R.string.wait_loading));
        mProgressDialog.show();
    }

    public void stop() {
        mProgressDialog.dismiss();
    }
}
