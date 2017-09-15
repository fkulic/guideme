package com.fkulic.guideme.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.FirebaseDbHelper;
import com.fkulic.guideme.helper.SharedPrefsHelper;
import com.fkulic.guideme.model.City;
import com.fkulic.guideme.model.Landmark;
import com.fkulic.guideme.ui.LoadingDataDialog;
import com.fkulic.guideme.ui.adapters.LandmarkAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fkulic.guideme.Constants.KEY_CITY_LATLNG;
import static com.fkulic.guideme.Constants.KEY_LANDMARK;

public class ListLandmarksActivity extends BaseActivity implements FirebaseDbHelper.FirebaseDataService, LandmarkAdapter.LandmarkOnClickListener {
    private static final String TAG = "ListLandmarksActivity";

    private City mCity;
    private RecyclerView.LayoutManager mManager;
    private LandmarkAdapter mLandmarkAdapter;
    private LoadingDataDialog mLoadingDataDialog;

    @BindView(R.id.rvListLandmarks) RecyclerView rvListLandmarks;
    @BindView(R.id.ibNewLandmark) ImageButton ibNewLandmark;
    @BindView(R.id.ibSeeMap) ImageButton ibSeeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_landmarks);
        ButterKnife.bind(this);

        this.mLandmarkAdapter = new LandmarkAdapter(this);
        this.mManager = new LinearLayoutManager(ListLandmarksActivity.this, LinearLayoutManager.VERTICAL, false);
        this.rvListLandmarks.setAdapter(mLandmarkAdapter);
        this.rvListLandmarks.setLayoutManager(this.mManager);
        mLoadingDataDialog = new LoadingDataDialog(ListLandmarksActivity.this);
        mLoadingDataDialog.show();

        Intent intent = this.getIntent();
        String latlng;
        if (intent.hasExtra(KEY_CITY_LATLNG)) {
            latlng = intent.getStringExtra(KEY_CITY_LATLNG);
        } else {
            latlng = SharedPrefsHelper.getInstance(this).getCurrentCity();
        }
        if (latlng == null) {
            mLoadingDataDialog.stop();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            finish();
        }
        FirebaseDbHelper fb = new FirebaseDbHelper(this);
        fb.getLandmarks(latlng);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(ListLandmarksActivity.this, MainActivity.class));
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(ListLandmarksActivity.this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.ibNewLandmark)
    void onClickNewLandmark() {
        Intent intent = new Intent(ListLandmarksActivity.this, NewLandmarkActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ibSeeMap)
    void onClickSeeMap() {
        Intent intent = new Intent(ListLandmarksActivity.this, MapActivity.class);
        if (mCity != null) {
            intent.putExtra(KEY_CITY_LATLNG, mCity.coordinates.getLatLngStringForDB());
        }
        startActivity(intent);
    }

    @Override
    public void citiesListDataReady(ArrayList<City> cities) {
    }

    @Override
    public void cityDataReady(City city) {
        mCity = city;
        setUpToolbar(city.name, true);
        mLoadingDataDialog.stop();
        this.mLandmarkAdapter.loadLandmarks(new ArrayList<>(city.landmarks.values()));
    }

    @Override
    public void landmarkOnClickListener(Landmark landmark) {
        Intent intent = new Intent(ListLandmarksActivity.this, LandmarkDetailsActivity.class);
        intent.putExtra(KEY_LANDMARK, landmark);
//        Log.d(TAG, "landmarkOnClickListener: " + landmarkLatLng );
        startActivity(intent);
    }
}
