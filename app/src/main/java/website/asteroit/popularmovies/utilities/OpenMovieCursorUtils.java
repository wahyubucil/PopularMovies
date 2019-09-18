package website.asteroit.popularmovies.utilities;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import website.asteroit.popularmovies.data.MovieContract;
import website.asteroit.popularmovies.objects.Movie;
import website.asteroit.popularmovies.objects.Review;
import website.asteroit.popularmovies.objects.Video;

/**
 * Created by Wahyu on 27/07/2017.
 */

public final class OpenMovieCursorUtils {

    public static List<Movie> getMovieListFromCursor(Cursor cursor) {
        if (cursor == null) return null;

        List<Movie> parsedMovieList = new ArrayList<>();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            int movieId = cursor.getInt(
                    cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)
            );
            String title = cursor.getString(
                    cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)
            );
            String poster = cursor.getString(
                    cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER)
            );
            double rating = cursor.getDouble(
                    cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING)
            );

            parsedMovieList.add(new Movie(movieId, title, poster, rating));
        }

        return parsedMovieList;
    }

    public static Movie getMovieDetailFromCursor(Cursor movieCursor, Cursor videoCursor, Cursor reviewCursor) {
        if (movieCursor == null) return null;

        Movie parsedMovieDetail = null;

        if(movieCursor.moveToFirst()) {
            String backdrop = movieCursor.getString(
                    movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP)
            );
            String date = movieCursor.getString(
                    movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE)
            );
            int runtime = movieCursor.getInt(
                    movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME)
            );
            String synopsis = movieCursor.getString(
                    movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS)
            );

            List<Video> videos = new ArrayList<>();
            if (videoCursor != null && videoCursor.getCount() > 0) {
                videoCursor.moveToPosition(-1);
                while (videoCursor.moveToNext()) {
                    String videoName = videoCursor.getString(
                            videoCursor.getColumnIndex(MovieContract.VideoEntry.COLUMN_VIDEO_NAME)
                    );
                    String videoUrl = videoCursor.getString(
                            videoCursor.getColumnIndex(MovieContract.VideoEntry.COLUMN_VIDEO_URL)
                    );
                    String videoType = videoCursor.getString(
                            videoCursor.getColumnIndex(MovieContract.VideoEntry.COLUMN_VIDEO_TYPE)
                    );
                    videos.add(new Video(videoName, videoUrl, videoType));
                }
            } else {
                videos = null;
            }

            List<Review> reviews = new ArrayList<>();
            if (reviewCursor != null && reviewCursor.getCount() > 0) {
                reviewCursor.moveToPosition(-1);
                while (reviewCursor.moveToNext()) {
                    String author = reviewCursor.getString(
                            reviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR)
                    );
                    String content = reviewCursor.getString(
                            reviewCursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT)
                    );
                    reviews.add(new Review(author, content));
                }
            } else {
                reviews = null;
            }

            parsedMovieDetail = new Movie(backdrop, date, runtime, synopsis, videos, reviews);
        }

        return parsedMovieDetail;
    }
}
