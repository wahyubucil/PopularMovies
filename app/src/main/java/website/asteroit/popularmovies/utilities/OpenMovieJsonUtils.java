package website.asteroit.popularmovies.utilities;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import website.asteroit.popularmovies.R;
import website.asteroit.popularmovies.objects.Movie;
import website.asteroit.popularmovies.objects.Review;
import website.asteroit.popularmovies.objects.Video;

/**
 * Created by Wahyu on 22/06/2017.
 */

public final class OpenMovieJsonUtils {

    private static final String LOG_TAG = OpenMovieJsonUtils.class.getSimpleName();

    public static List<Movie> getMovieListFromJson(String movieListJsonStr) {
        if (TextUtils.isEmpty(movieListJsonStr)) return null;

        final String OWM_RESULTS = "results";

        final String OWM_ID = "id";
        final String OWM_TITLE = "title";
        final String OWM_POSTER = "poster_path";
        final String OWM_RATING = "vote_average";

        List<Movie> parsedMovieList = new ArrayList<>();

        try {
            JSONObject movieListJson = new JSONObject(movieListJsonStr);
            JSONArray movieListArray = movieListJson.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < movieListArray.length(); i++) {
                JSONObject movieListResults = movieListArray.getJSONObject(i);
                int id = movieListResults.getInt(OWM_ID);
                String title = movieListResults.getString(OWM_TITLE);
                String poster = movieListResults.getString(OWM_POSTER);
                double rating = movieListResults.getDouble(OWM_RATING);
                parsedMovieList.add(new Movie(id, title, poster, rating));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON results", e);
        }

        return parsedMovieList;
    }

    public static Movie getMovieDetailFromJson(Context context,
                                               String movieDetailJsonStr,
                                               String movieVideoJsonStr,
                                               String movieReviewJsonStr) {
        if (TextUtils.isEmpty(movieDetailJsonStr)
                || TextUtils.isEmpty(movieReviewJsonStr)
                || TextUtils.isEmpty(movieVideoJsonStr)) return null;

        final String OWM_RESULTS = "results";

        // OWM for Movie Detail
        final String OWM_BACKDROP = "backdrop_path";
        final String OWM_DATE = "release_date";
        final String OWM_RUNTIME = "runtime";
        final String OWM_SYNOPSIS = "overview";

        //OWM for Movie Video
        final String OWM_KEY = "key";
        final String OWM_VIDEO_NAME = "name";
        final String OWM_SITE = "site";
        final String OWM_TYPE = "type";

        // OWM for Movie Review
        final String OWM_AUTHOR = "author";
        final String OWM_CONTENT = "content";
        final String OWM_TOTAL_RESULTS = "total_results";

        Movie parsedMovieDetail = null;

        try {
            // parse json movie detail
            JSONObject movieDetailJson = new JSONObject(movieDetailJsonStr);
            String backdrop = movieDetailJson.getString(OWM_BACKDROP);
            String date = movieDetailJson.getString(OWM_DATE);
            int runtime = movieDetailJson.getInt(OWM_RUNTIME);
            String synopsis = movieDetailJson.getString(OWM_SYNOPSIS);


            // parse json movie video
            JSONObject movieVideoJson = new JSONObject(movieVideoJsonStr);
            JSONArray movieVideoArray = movieVideoJson.getJSONArray(OWM_RESULTS);
            int movieVideoArrayLength = movieVideoArray.length();
            List<Video> videos = new ArrayList<Video>();
            if (movieVideoArrayLength > 0) {
                for (int i = 0; i < movieVideoArrayLength; i++) {
                    JSONObject movieVideoResults = movieVideoArray.getJSONObject(i);
                    String site = movieVideoResults.getString(OWM_SITE);
                    if (site.toLowerCase().equals("youtube")) {
                        String videoName = movieVideoResults.getString(OWM_VIDEO_NAME);
                        String type = movieVideoResults.getString(OWM_TYPE);
                        String key = movieVideoResults.getString(OWM_KEY);
                        String videoUrl = context.getString(R.string.youtube_url) + key;

                        videos.add(new Video(videoName, videoUrl, type));
                    }
                }
            } else {
                videos = null;
            }


            // parse json movie review
            JSONObject movieReviewJson = new JSONObject(movieReviewJsonStr);
            int total_results = movieReviewJson.getInt(OWM_TOTAL_RESULTS);
            List<Review> reviews = new ArrayList<Review>();
            if (total_results > 0) {
                JSONArray movieReviewArray = movieReviewJson.getJSONArray(OWM_RESULTS);
                for (int i = 0; i < movieReviewArray.length(); i++) {
                    JSONObject movieReviewResults = movieReviewArray.getJSONObject(i);
                    String author = movieReviewResults.getString(OWM_AUTHOR);
                    String content = movieReviewResults.getString(OWM_CONTENT);
                    reviews.add(new Review(author, content));
                }
            } else {
                reviews = null;
            }


            parsedMovieDetail = new Movie(backdrop, date, runtime, synopsis, videos, reviews);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON results", e);
        }

        return parsedMovieDetail;
    }
}
