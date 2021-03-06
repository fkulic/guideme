package com.fkulic.guideme.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Filip on 7.9.2017..
 */

public class SharedPrefsHelper {
    private static final String FILE_NAME = "GuideMePrefs";
    private static final String DEVICE_HEIGHT = "device_height";
    private static final String DEVICE_WIDTH = "device_width";
    private static final String CURRENT_CITY = "current_city";
    private static final String PERMISSION_ASKED_LOCATION = "permission_asked_loc";
    private static final String NEW_LAND_CITY_COORDINATES = "new_land_city_coo";

    private static SharedPrefsHelper instance = null;
    private SharedPreferences preferences;

    private SharedPrefsHelper(Context context) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsHelper(context);
        }
        return instance;
    }

    public void setCurrentCity(String latLngStringForDB) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CURRENT_CITY, latLngStringForDB);
        editor.apply();
    }

    public void setWidth(int width) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(DEVICE_WIDTH, width);
        editor.apply();
    }

    public void setHeight(int height) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(DEVICE_HEIGHT, height);
        editor.apply();
    }

    public void setPermissionAskedLocation(boolean asked) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PERMISSION_ASKED_LOCATION, asked);
        editor.apply();
    }

    public void setNewLandmarkCityCoordinates(String latLngString) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NEW_LAND_CITY_COORDINATES, latLngString);
        editor.apply();
    }

    public String getCurrentCity() {
        return preferences.getString(CURRENT_CITY, null);
    }

    public int getWidth() {
        return preferences.getInt(DEVICE_WIDTH, 1080);
    }

    public int getHeight() {
        return preferences.getInt(DEVICE_HEIGHT, 1920);
    }

    public boolean getPermissionAskedLocation() {
        return preferences.getBoolean(PERMISSION_ASKED_LOCATION, false);
    }

    public String getNewLandmarkCityCoordinates() {
        return preferences.getString(NEW_LAND_CITY_COORDINATES, null);
    }
}
