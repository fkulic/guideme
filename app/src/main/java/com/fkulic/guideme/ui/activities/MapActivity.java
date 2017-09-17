package com.fkulic.guideme.ui.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.fkulic.guideme.R;
import com.fkulic.guideme.helper.FirebaseDbHelper;
import com.fkulic.guideme.helper.SharedPrefsHelper;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Map;

import static com.fkulic.guideme.Constants.KEY_CITY;
import static com.fkulic.guideme.Constants.KEY_CITY_LATLNG;
import static com.fkulic.guideme.Constants.KEY_COORDINATES;
import static com.fkulic.guideme.Constants.KEY_LANDMARK;
import static com.fkulic.guideme.Constants.KEY_LANDMARK_DIRECTIONS;
import static com.fkulic.guideme.Constants.PERMISSION_REQ_FINE_LOC;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, LocationListener,
        FirebaseDbHelper.FirebaseDataService, RoutingListener {

    private static final String TAG = "MapActivity";

    private City mCity;
    private SupportMapFragment fMap;
    private GoogleMap mGoogleMap;
    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private Location mDestination;
    private Landmark mGoToLandmark;
    private Marker mMarker;
    private ProgressDialog mProgressDialog;
    private Polyline mPolyline;
    private boolean mNotifiedApproaching = false;
    private boolean mNewLandmarkSetLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fMap);
        this.fMap.getMapAsync(this);
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_LANDMARK_DIRECTIONS)) {
            this.mGoToLandmark = intent.getParcelableExtra(KEY_LANDMARK_DIRECTIONS);
            this.mDestination = new Location("destination");
            this.mDestination.setLatitude(mGoToLandmark.coordinates.latitude);
            this.mDestination.setLongitude(mGoToLandmark.coordinates.longitude);
        } else if (intent.hasExtra(KEY_CITY_LATLNG)) {
//            Log.d(TAG, "onCreate: " + mCity.toString());
            FirebaseDbHelper fb = new FirebaseDbHelper(this);
            fb.getLandmarks(intent.getStringExtra(KEY_CITY_LATLNG));
        } else {
            mNewLandmarkSetLocation = true;
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
        if (mGoToLandmark != null) {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mProgressDialog = ProgressDialog.show(this, getString(R.string.please_wait), getString(R.string.searcing_gps_signal), true);
            } else if (this.mGoToLandmark != null) {
                Toast.makeText(this, R.string.warning_gps_off_directions_map_activity, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        UiSettings settings = mGoogleMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
        settings.setMapToolbarEnabled(false);
        settings.setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_FINE_LOC);
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 20, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 20, this);

        if (mGoToLandmark != null) {
            createDestinationMarker(mGoToLandmark.coordinates.getLatLng());
        }
    }

    private void drawLandmarkMarkers() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
        for (Map.Entry<String, Landmark> landmarkEntry : mCity.landmarks.entrySet()) {
            markerOptions.position(landmarkEntry.getValue().coordinates.getLatLng());
            markerOptions.title(landmarkEntry.getValue().name);
            mMarker = mGoogleMap.addMarker(markerOptions);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                if (this.mGoToLandmark == null) {
                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.setMessage("Fetching route information.");
                }
            }
        }

        mCurrentLocation = location;
        if (mGoToLandmark != null) {
            if (mPolyline == null) {
                animateToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                requestDirections();
            }
            // TODO: 15.9.2017.  switch to geofencing in (possible) future & create location service
            if (location.distanceTo(mDestination) < 20 && !mNotifiedApproaching) {
                createApproachingNotification();
            }
        }

        if (mNewLandmarkSetLocation) {
            setUpUiForCoordinateSelection();
        }
    }

    private void setUpUiForCoordinateSelection() {
        // Center map on city
        Coordinates centerOfCity = new Coordinates(SharedPrefsHelper.getInstance(this).getNewLandmarkCityCoordinates());
        animateToLocation(centerOfCity.getLatLng());

        // Display instructions and fab
        Toast.makeText(this, R.string.info_click_to_place_marker, Toast.LENGTH_LONG).show();
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fabSaveCoordinates);
        floatingActionButton.setVisibility(View.VISIBLE);


        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMarker != null) {
                    mMarker.remove();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.draggable(true);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
                mMarker = mGoogleMap.addMarker(markerOptions);
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMarker != null) {
                    LatLng latLng = mMarker.getPosition();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(KEY_COORDINATES, new Coordinates(mMarker.getPosition()));
                    MapActivity.this.setResult(RESULT_OK, resultIntent);
                    MapActivity.this.finish();

                } else {
                    Toast.makeText(MapActivity.this, R.string.info_click_to_place_marker, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createApproachingNotification() {
        Intent landmarkIntent = new Intent(MapActivity.this, LandmarkDetailsActivity.class);
        landmarkIntent.putExtra(KEY_LANDMARK, mGoToLandmark);

        PendingIntent piLandmark = PendingIntent.getActivity(this, 0, landmarkIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.approaching_landmark_title))
                .setContentText(getString(R.string.approaching_landmark_message))
                .setContentIntent(piLandmark)
                .setAutoCancel(true)
                .setLights(Color.CYAN, 1000, 2000)
                .setVibrate(new long[]{2000,1000,2000})
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, notification);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }


    private void createDestinationMarker(LatLng latLng) {
        if (mMarker != null) {
            mMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(mGoToLandmark.name);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
        mMarker = mGoogleMap.addMarker(markerOptions);
    }

    private void animateToLocation(LatLng latLng) {
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

    private void requestDirections() {
        LatLng origin = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        String apiKey = "AIzaSyDhyVfYY8S9DYvBfMN7Zbb9WA8sbs6Ns5U";

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(origin, mGoToLandmark.coordinates.getLatLng())
                .key(apiKey)
                .build();

        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        mProgressDialog.dismiss();
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        mProgressDialog.dismiss();

        if (routes.size() > 0) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(getResources().getColor(R.color.secondary));
            polylineOptions.width(20);
            polylineOptions.addAll(routes.get(0).getPoints());
            mPolyline = mGoogleMap.addPolyline(polylineOptions);
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
}
