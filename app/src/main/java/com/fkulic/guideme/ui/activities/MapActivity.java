package com.fkulic.guideme.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.FirebaseDbHelper;
import com.fkulic.guideme.model.City;
import com.fkulic.guideme.model.Coordinates;
import com.fkulic.guideme.model.Landmark;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

import static com.fkulic.guideme.Constants.KEY_CITY;
import static com.fkulic.guideme.Constants.KEY_CITY_LATLNG;
import static com.fkulic.guideme.Constants.KEY_LANDMARK_DIRECTIONS;
import static com.fkulic.guideme.Constants.PERMISSION_REQ_FINE_LOC;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, FirebaseDbHelper.FirebaseDataService {
    private static final String TAG = "MapActivity";


    private City mCity;
    private SupportMapFragment fMap;
    private GoogleMap mGoogleMap;
    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private Landmark mGoToLandmark;
    private Marker mGoToMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fMap);
        this.fMap.getMapAsync(this);
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_LANDMARK_DIRECTIONS)) {
            // TODO: 14.9.2017. directions using googles directions api
            this.mGoToLandmark = intent.getParcelableExtra(KEY_LANDMARK_DIRECTIONS);
        } else if (intent.hasExtra(KEY_CITY_LATLNG)) {
//            Log.d(TAG, "onCreate: " + mCity.toString());
            FirebaseDbHelper fb = new FirebaseDbHelper(this);
            fb.getLandmarks(intent.getStringExtra(KEY_CITY_LATLNG));
        }
    }

    private void drawLandmarkMarkers() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        for (Map.Entry<String, Landmark> landmarkEntry : mCity.landmarks.entrySet()) {
            markerOptions.position(landmarkEntry.getValue().coordinates.getLatLng());
            markerOptions.title(landmarkEntry.getValue().name);
            mGoToMarker = mGoogleMap.addMarker(markerOptions);
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        UiSettings settings = mGoogleMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
        settings.setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_FINE_LOC);
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, this);

        if (mGoToLandmark != null) {
            createDestinationMarker(mGoToLandmark.coordinates.getLatLng());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
//        markerOptions.title("You");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        mGoToMarker = mGoogleMap.addMarker(markerOptions);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    private void createDestinationMarker(LatLng latLng) {
        if (mGoToMarker != null) {
            mGoToMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(mGoToLandmark.name);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        mGoToMarker = mGoogleMap.addMarker(markerOptions);
        animateToLocation(latLng);
    }

    private void createDestinationMarker(Coordinates coordinates) {
        createDestinationMarker(coordinates.getLatLng());
    }

    private void animateToLocation(LatLng latLng) {
//        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(13)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void citiesListDataReady(ArrayList<City> cities) {
    }

    @Override
    public void cityDataReady(City city) {
        mCity = city;
        animateToLocation(city.coordinates.getLatLng());
        drawLandmarkMarkers();
    }
}
