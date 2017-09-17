package com.fkulic.guideme.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Filip on 19.8.2017..
 */
@IgnoreExtraProperties
public class City implements Parcelable {
    public String name;
    public String adminArea;
    public String country;
    public Coordinates coordinates;
    public Map<String, Landmark> landmarks;

    public City() {} // Empty constructor for Firebase serialization


    public City(String name, String adminArea, String country, Coordinates coordinates) {
        this.name = name;
        this.adminArea = adminArea;
        this.country = country;
        this.coordinates = coordinates;
        this.landmarks = new HashMap<>();
    }

    public City(String name, String adminArea, String country, LatLng latLng) {
        this.name = name;
        this.adminArea = adminArea;
        this.country = country;
        this.coordinates = new Coordinates(latLng);
        this.landmarks = new HashMap<>();
    }

    public City(String name, String adminArea, String country, String latLngString) {
        this.name = name;
        this.adminArea = adminArea;
        this.country = country;
        this.coordinates = new Coordinates(latLngString);
        this.landmarks = new HashMap<>();
    }

    public void addLandmark(Landmark landmark) {
        this.landmarks.put(landmark.coordinates.getLatLngStringForDB(), landmark);
    }

    @Override
    public String toString() {
        return name + "\n" + adminArea + "\n" + country;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.adminArea);
        dest.writeString(this.country);
        dest.writeParcelable(this.coordinates, flags);
        if (this.landmarks != null) {
            dest.writeInt(this.landmarks.size());
            for (Map.Entry<String, Landmark> entry : this.landmarks.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeParcelable(entry.getValue(), flags);
            }
        } else {
            this.landmarks = new HashMap<>();
        }

    }

    protected City(Parcel in) {
        this.name = in.readString();
        this.adminArea = in.readString();
        this.country = in.readString();
        this.coordinates = in.readParcelable(Coordinates.class.getClassLoader());
        int landmarksSize = in.readInt();
        this.landmarks = new HashMap<>(landmarksSize);
        for (int i = 0; i < landmarksSize; i++) {
            String key = in.readString();
            Landmark value = in.readParcelable(Landmark.class.getClassLoader());
            this.landmarks.put(key, value);
        }
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
