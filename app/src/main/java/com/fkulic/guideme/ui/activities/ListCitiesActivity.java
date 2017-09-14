package com.fkulic.guideme.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.FirebaseDbHelper;
import com.fkulic.guideme.model.City;
import com.fkulic.guideme.ui.LoadingDataDialog;
import com.fkulic.guideme.ui.adapters.CityAdapter;

import java.util.ArrayList;

import static com.fkulic.guideme.Constants.KEY_CITY_LATLNG;
import static com.fkulic.guideme.Constants.KEY_CITY_NAME;

public class ListCitiesActivity extends BaseActivity implements FirebaseDbHelper.FirebaseDataService, CityAdapter.OnCityClick {
    private static final String TAG = "ListCitiesActivity";

    private ArrayList<City> mCities;
    private RecyclerView rvCities;
    private RecyclerView.LayoutManager mLayoutManager;
    private CityAdapter mCityAdapter;
    private LoadingDataDialog mLoadingDataDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cities);
        setUpToolbar(R.string.list_of_cities, true);
        this.rvCities = (RecyclerView) this.findViewById(R.id.rvCities);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDbHelper fb = new FirebaseDbHelper(this);
        fb.getCities();
        mLoadingDataDialog = new LoadingDataDialog(ListCitiesActivity.this);
        mLoadingDataDialog.show();
    }

    private void setUpRecyclerView() {
        mLoadingDataDialog.stop();
        mLayoutManager = new LinearLayoutManager(ListCitiesActivity.this);
        mCityAdapter = new CityAdapter(mCities, this);
        rvCities.setHasFixedSize(true);
        rvCities.setLayoutManager(this.mLayoutManager);
        rvCities.setAdapter(this.mCityAdapter);
    }

    @Override
    public void onCityClick(City city) {
//        Log.d(TAG, "onItemClick: " + city.toString());
        Intent listLandmarkDetails = new Intent(ListCitiesActivity.this, ListLandmarksActivity.class);
        listLandmarkDetails.putExtra(KEY_CITY_NAME, city.name);
        listLandmarkDetails.putExtra(KEY_CITY_LATLNG, city.coordinates.getLatLngStringForDB());
        startActivity(listLandmarkDetails);
    }

    @Override
    public void citiesListDataReady(ArrayList<City> cities) {
        Log.d(TAG, "Cities data ready");
        this.mCities = cities;
        setUpRecyclerView();
    }

    @Override
    public void cityDataReady(City city) {
    }
}
