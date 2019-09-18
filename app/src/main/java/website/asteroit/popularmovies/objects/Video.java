package website.asteroit.popularmovies.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Wahyu on 26/07/2017.
 */

public class Video implements Parcelable {
    private String mVideoName, mVideoUrl, mVideoType;

    public Video(String videoName, String videoUrl, String videoType) {
        this.mVideoName = videoName;
        this.mVideoUrl = videoUrl;
        this.mVideoType = videoType;
    }

    protected Video(Parcel in) {
        mVideoName = in.readString();
        mVideoUrl = in.readString();
        mVideoType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mVideoName);
        dest.writeString(mVideoUrl);
        dest.writeString(mVideoType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getVideoName() {
        return this.mVideoName;
    }

    public String getVideoUrl() {
        return this.mVideoUrl;
    }

    public String getVideoType() {
        return this.mVideoType;
    }
}
