package com.fkulic.guideme.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Filip on 19.8.2017..
 */
@IgnoreExtraProperties
public class Landmark implements Parcelable {
    private static final String TAG = "Landmark";
    public String name;
    public Coordinates coordinates;
    public String description;
    public String imgUrl;
    public Map<String, String> audioUrls;


    public Landmark() {}  // Empty constructor for Firebase serialization

    public Landmark(String name, Coordinates coordinates, String description, String imgUrl) {
        this.name = name;
        this.coordinates = coordinates;
        this.description = description;
        this.imgUrl = imgUrl;
        this.audioUrls = new HashMap<>();
    }

    public Landmark(String name, Coordinates coordinates, String description, String imgUrl, Map<String, String> audioUrls) {
        this.name = name;
        this.coordinates = coordinates;
        this.description = description;
        this.imgUrl = imgUrl;
        this.audioUrls = audioUrls;
    }

    public void addAudio(String name, String url) {
        if (audioUrls == null) {
            this.audioUrls = new HashMap<>();
        }
        this.audioUrls.put(name, url);
    }

    @Exclude
    public int getFileCount() {
        return audioUrls.size() + 1;
    }

    public boolean isValid() {
        if (name.trim().length() < 5) {
            Log.d(TAG, "isValid: " + name.trim());
            return false;
        } else if (!coordinates.areValid()) {
            Log.d(TAG, "isValid: " + coordinates.toString());
            return false;
        } else if (description.trim().length() < 20) {
            Log.d(TAG, "isValid: " + description.trim());
            return false;
        } else if (imgUrl == null) {
            Log.d(TAG, "isValid: img");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("\nName: ")
                .append(name).append("\nCoordinates: ")
                .append(coordinates.toString()).append("\nImage url: ")
                .append(imgUrl).append("\nAudio files:\n");
        if (audioUrls != null) {
            for (Map.Entry<String, String> audioEntry : this.audioUrls.entrySet()) {
                builder.append(audioEntry.getKey()).append(" -> ").append(audioEntry.getValue()).append("\n");
            }
        }
        return builder.toString();
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeParcelable(this.coordinates, flags);
        dest.writeString(this.description);
        dest.writeString(this.imgUrl);
        if (this.audioUrls != null) {
            dest.writeInt(this.audioUrls.size());
            for (Map.Entry<String, String> entry : this.audioUrls.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        } else {
            this.audioUrls = new HashMap<>();
        }
    }

    protected Landmark(Parcel in) {
        this.name = in.readString();
        this.coordinates = in.readParcelable(Coordinates.class.getClassLoader());
        this.description = in.readString();
        this.imgUrl = in.readString();
        int audioUrlsSize = in.readInt();
        this.audioUrls = new HashMap<>(audioUrlsSize);
        for (int i = 0; i < audioUrlsSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.audioUrls.put(key, value);
        }
    }

    public static final Creator<Landmark> CREATOR = new Creator<Landmark>() {
        @Override
        public Landmark createFromParcel(Parcel source) {
            return new Landmark(source);
        }

        @Override
        public Landmark[] newArray(int size) {
            return new Landmark[size];
        }
    };
}
