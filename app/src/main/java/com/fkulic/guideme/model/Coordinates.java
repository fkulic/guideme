package com.fkulic.guideme.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Filip on 24.8.2017..
 */

@IgnoreExtraProperties
public class Coordinates implements Parcelable {
    public double latitude;
    public double longitude;

    public Coordinates() {} // Empty constructor for Firebase serialization

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinates(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public boolean areValid() {
        if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude < 180) {
            return true;
        }
        return false;
    }

    @Exclude
    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    @Exclude
    public String getLatLngStringForDB() {
        StringBuilder sb = new StringBuilder();
        sb.append(Math.round(this.latitude * 1000000)).append("_")
                .append(Math.round(this.longitude * 1000000));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "(" + this.latitude + ", " + this.longitude + ")";
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected Coordinates(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Parcelable.Creator<Coordinates> CREATOR = new Parcelable.Creator<Coordinates>() {
        @Override
        public Coordinates createFromParcel(Parcel source) {
            return new Coordinates(source);
        }

        @Override
        public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }
    };
}
