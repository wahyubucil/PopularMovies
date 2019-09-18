package website.asteroit.popularmovies.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Wahyu on 22/06/2017.
 */

public class Movie implements Parcelable {
    private int mId;
    private String mTitle, mPoster;
    private double mRating;

    private String mBackdrop, mDate, mSynopsis;
    private int mRuntime;
    private List<Video> mVideo;
    private List<Review> mReview;

    public Movie(int id, String title, String poster, double rating) {
        this.mId = id;
        this.mPoster = poster;
        this.mTitle = title;
        this.mRating = rating;
    }

    private Movie(Parcel in) {
        mId = in.readInt();
        mPoster = in.readString();
        mTitle = in.readString();
        mRating = in.readDouble();
    }

    public Movie(String backdrop, String date, int runtime, String synopsis, List<Video> video, List<Review> review) {
        this.mBackdrop = backdrop;
        this.mDate = date;
        this.mRuntime = runtime;
        this.mSynopsis = synopsis;
        this.mVideo = video;
        this.mReview = review;
    }

    public int getId() {
        return this.mId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getPoster() {
        return this.mPoster;
    }

    public double getRating() {
        return this.mRating;
    }

    public String getBackdrop() {
        return this.mBackdrop;
    }

    public String getDate() {
        return this.mDate;
    }

    public int getRuntime() {
        return this.mRuntime;
    }

    public String getSynopsis() {
        return this.mSynopsis;
    }

    public List<Review> getReview() {
        return this.mReview;
    }

    public List<Video> getVideo() {
        return this.mVideo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mId);
        parcel.writeString(mPoster);
        parcel.writeString(mTitle);
        parcel.writeDouble(mRating);
    }

    public final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };
}
