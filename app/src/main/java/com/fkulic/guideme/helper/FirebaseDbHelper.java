package com.fkulic.guideme.helper;

import android.util.Log;

import com.fkulic.guideme.model.City;
import com.fkulic.guideme.model.Landmark;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Filip on 20.8.2017..
 */

public class FirebaseDbHelper {
    private static final String TAG = "FirebaseDbHelper";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseDataService mCallback;

    public FirebaseDbHelper(FirebaseDataService callback) {
        mCallback = callback;
    }

    public interface FirebaseDataService {
        void citiesListDataReady(ArrayList<City> cities);
        void cityDataReady(City city);
    }

    public static void addCity(City city) {
        // Add simultaneously to list of cities and create new city using latitude and longitude
//        Log.d(TAG, "addCity: " + city.toString());
        Map<String, Object> newCity = new HashMap<>();
        String coordinates = city.coordinates.getLatLngStringForDB(); //getLatLngString(city.getPosition());
        newCity.put("/cities/" + city.name + "/" + coordinates, city.adminArea + "_" + city.country);
        newCity.put("/" + coordinates, city);
        databaseReference.updateChildren(newCity);
    }

    public static void addLandmark(String cityLatLng, Landmark landmark) {
        Log.d(TAG, "Adding landmark to database: " + landmark.toString());
        databaseReference.child(cityLatLng).child("landmarks")
                .child(landmark.coordinates.getLatLngStringForDB()).setValue(landmark);
    }

    public static void addAudioRef(String cityLatLng, Landmark landmark, String audioName, String audioUrl) {
        landmark.addAudio(audioName, audioUrl);
        databaseReference.child(cityLatLng).child("landmarks")
                .child(landmark.coordinates.getLatLngStringForDB()).child("audioUrls").setValue(landmark.audioUrls);
    }

    public void getCities() {
        Query query = databaseReference.child("cities").orderByValue();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<City> cities = new ArrayList<>();
                for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) { // iterate over cities by name
//                    Log.d(TAG, "name:" + citySnapshot.getKey());
                    String name = citySnapshot.getKey();
                    for (DataSnapshot cityData : citySnapshot.getChildren()) { // iterate over name by mCoordinates (cities might have same name)
//                        Log.d(TAG, "mCoordinates:" + cityData.getKey());
                        String[] adminArea_County = cityData.getValue(String.class).split("_");
                        String latLngString = cityData.getKey();
                        cities.add(new City(name, adminArea_County[0], adminArea_County[1], latLngString));
                    }
                }
                mCallback.citiesListDataReady(cities);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    public void getLandmarks(String latLngString) {
        databaseReference.child(latLngString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                    City city = dataSnapshot.getValue(City.class);
                    mCallback.cityDataReady(city);
//                Log.d(TAG, "onDataChange: " + city.toString());
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }
}
