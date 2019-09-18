package website.asteroit.popularmovies.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Wahyu on 26/07/2017.
 */

public class Review implements Parcelable {
    private String mAuthor, mContent;

    public Review(String author, String content) {
        this.mAuthor = author;
        this.mContent = content;
    }

    protected Review(Parcel in) {
        mAuthor = in.readString();
        mContent = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getAuthor() {
        return this.mAuthor;
    }

    public String getContent() {
        return this.mContent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mAuthor);
        parcel.writeString(mContent);
    }
}
