package com.fkulic.guideme.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.GpsStatus;
import com.fkulic.guideme.helper.SharedPrefsHelper;
import com.fkulic.guideme.model.City;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fkulic.guideme.Constants.KEY_CITY;
import static com.fkulic.guideme.Constants.KEY_CITY_LATLNG;
import static com.fkulic.guideme.Constants.KEY_CITY_NAME;
import static com.fkulic.guideme.Constants.PERMISSION_REQ_FINE_LOC;

public class MainActivity extends BaseActivity implements LocationListener {
    private static final String TAG = "MainActivity";

    private LocationManager mLocationManager;
    private City mCity;
    private GpsStatus mGpsStatus = GpsStatus.NO_PERMISSION;

    @BindView(R.id.tvMessage) TextView tvMessage;
    @BindView(R.id.tvLocation) TextView tvLocation;
    @BindView(R.id.tvDenyAction) TextView tvDenyAction;
    @BindView(R.id.bAction) Button bAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUpLocationListener();
        setUpUi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setDisplayMetrics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpUi();
    }

    @Override
    protected void onDestroy() {
        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_CITY, mCity);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCity = savedInstanceState.getParcelable(KEY_CITY);
    }

    private void setUpUi() {
        @StringRes int messageString;
        @StringRes int buttonText;
        @StringRes int denyActionString;
        int tvLocationVisibility;
        switch (mGpsStatus) {
            case ON:
                messageString = R.string.tv_confirm_location;
                buttonText = R.string.b_confirm_location;
                denyActionString = R.string.not_there;
                tvLocationVisibility = View.VISIBLE;
                if (mCity == null) {
                    tvLocation.setText(R.string.searching_gps);
                } else {
                    tvLocation.setText(mCity.toString());
                }
                break;

            case OFF:
                messageString = R.string.turn_gps_on;
                buttonText = R.string.button_turn_gps_on;
                denyActionString = R.string.no_gps_check_city_list;
                tvLocationVisibility = View.INVISIBLE;
                break;


            case NO_PERMISSION:
                messageString = R.string.no_permission_location;
                buttonText = R.string.give_permission;
                denyActionString = R.string.no_permission_check_list_cities;
                tvLocationVisibility = View.INVISIBLE;
                break;

            default:
            case NOT_AVAILABLE:
                messageString = R.string.gps_not_available;
                bAction.setVisibility(View.GONE);
                buttonText = R.string.sad_face;
                denyActionString = R.string.gps_not_available_check_cities;
                tvLocationVisibility = View.INVISIBLE;
                break;
        }
        tvMessage.setText(messageString);
        bAction.setText(buttonText);
        tvDenyAction.setText(denyActionString);
        tvLocation.setVisibility(tvLocationVisibility);
    }

    @OnClick(R.id.bAction)
    void onClickButtonConfirmLoc() {
        switch (mGpsStatus) {
            case ON:
                if (mCity != null) {
                    SharedPrefsHelper.getInstance(this).setCurrentCity(mCity.coordinates.getLatLngStringForDB());
                    Intent listLandmarksIntent = new Intent(MainActivity.this, ListLandmarksActivity.class);
                    listLandmarksIntent.putExtra(KEY_CITY_NAME, mCity.name);
                    listLandmarksIntent.putExtra(KEY_CITY_LATLNG, mCity.coordinates.getLatLngStringForDB());
                    startActivity(listLandmarksIntent);
                } else {
                    Toast.makeText(this, R.string.still_searching, Toast.LENGTH_SHORT).show();
                }
                break;

            case OFF:
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;

            case NO_PERMISSION:
                super.askGpsPermission(this);
                break;

            default:
            case NOT_AVAILABLE:
                // no can do...
                break;
        }
    }

    @OnClick(R.id.tvDenyAction)
    public void denyAction() {
        SharedPrefsHelper.getInstance(this).setCurrentCity(null);
        Intent intent = new Intent(MainActivity.this, ListCitiesActivity.class);
        startActivity(intent);
    }

    private void cityFromLocation(Location location) throws IOException {
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
            Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
            String cityString = address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName();
//            Log.d(TAG, "From location: " + cityString);
            address = geocoder.getFromLocationName(cityString, 1).get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mCity = new City(address.getLocality(), address.getAdminArea(), address.getCountryName(), latLng);
//            Log.d(TAG, "City from location name: " + mCity.toString());
        }
    }

    private void setUpLocationListener() {
        if (super.checkGpsPermission()) {
            // have permission
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 1000, this);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 1000, this);

            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // gps is turned on
                mGpsStatus = GpsStatus.ON;
            } else {
                // gps is turned off
                mGpsStatus = GpsStatus.OFF;
            }
        } else {
            // no permission, ask if you haven't asked before
            mGpsStatus = GpsStatus.NO_PERMISSION;
            if (!SharedPrefsHelper.getInstance(this).getPermissionAskedLocation()) {
                SharedPrefsHelper.getInstance(this).setPermissionAskedLocation(true);
                super.askGpsPermission(MainActivity.this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_FINE_LOC:
                if (grantResults.length > 0) {
                    Log.d(TAG, "onRequestPermissionsResult: called");
                    setUpLocationListener();
                    setUpUi();
                }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mGpsStatus = GpsStatus.ON;
        try {
            cityFromLocation(location);
        } catch (IOException e) {
            mGpsStatus = GpsStatus.NOT_AVAILABLE;
            e.printStackTrace();
        } finally {
            setUpUi();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                mGpsStatus = GpsStatus.ON;
                break;

            default:
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mGpsStatus = GpsStatus.NOT_AVAILABLE;
                break;
        }
        setUpUi();
    }

    @Override
    public void onProviderEnabled(String provider) {
        mGpsStatus = GpsStatus.ON;
        setUpUi();
    }

    @Override
    public void onProviderDisabled(String provider) {
        mGpsStatus = GpsStatus.OFF;
        setUpUi();
    }
}
