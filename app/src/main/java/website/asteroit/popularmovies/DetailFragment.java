package website.asteroit.popularmovies;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import website.asteroit.popularmovies.data.MovieContract;
import website.asteroit.popularmovies.objects.Movie;
import website.asteroit.popularmovies.objects.Review;
import website.asteroit.popularmovies.objects.Video;
import website.asteroit.popularmovies.utilities.ExtraUtils;
import website.asteroit.popularmovies.utilities.NetworkUtils;
import website.asteroit.popularmovies.utilities.OpenMovieCursorUtils;
import website.asteroit.popularmovies.utilities.OpenMovieJsonUtils;

/**
 * Created by Wahyu on 28/07/2017.
 */

public class DetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Movie>, SwipeRefreshLayout.OnRefreshListener {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private CoordinatorLayout mMovieDetailContainer;
    private ImageView mIvDetailBackdrop;

    private SwipeRefreshLayout mSrlMovieDetail;
    private ViewPager mMovieDetailPager;
    private ProgressBar mLoadingIndicator;

    private FloatingActionButton mFabFavorite;
    private boolean mFabFavoriteIsChecked;

    private Snackbar mErrorSnackBar;

    private int mId;
    private String mTitle, mPoster;
    private double mRating;

    private static final String ID_ARGS_KEY = "id";
    private static final String TITLE_ARGS_KEY = "title";
    private static final String POSTER_ARGS_KEY = "poster";
    private static final String RATING_ARGS_KEY = "rating";

    private String mDetailUrl, mVideoUrl, mReviewUrl;
    private static final int MOVIE_DETAIL_JSON_LOADER_ID = 5;
    private static final int MOVIE_DETAIL_CURSOR_LOADER_ID = 6;

    private Movie mMovieDetailData;
    private List<Video> mVideos;
    private List<Review> mReviews;

    private String mFullTitle;

    private Toast mToastFavorite;

    public static DetailFragment newInstance(int id, String title, String poster, double rating) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(ID_ARGS_KEY, id);
        args.putString(TITLE_ARGS_KEY, title);
        args.putString(POSTER_ARGS_KEY, poster);
        args.putDouble(RATING_ARGS_KEY, rating);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = this.getArguments();
        if (args != null) {
            mId = args.getInt(ID_ARGS_KEY);
            mTitle = args.getString(TITLE_ARGS_KEY);
            mPoster = args.getString(POSTER_ARGS_KEY);
            mRating = args.getDouble(RATING_ARGS_KEY);
            mDetailUrl = "https://api.themoviedb.org/3/movie/" + mId + "?api_key=" + NetworkUtils.API_KEY;
            mVideoUrl = "https://api.themoviedb.org/3/movie/" + mId + "/videos?api_key=" + NetworkUtils.API_KEY;
            mReviewUrl = "https://api.themoviedb.org/3/movie/" + mId + "/reviews?api_key=" + NetworkUtils.API_KEY;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        final Toolbar movieDetailToolbar = (Toolbar) rootView.findViewById(R.id.movie_detail_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(movieDetailToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovieDetailContainer = (CoordinatorLayout) rootView.findViewById(R.id.movie_detail_container);
        mIvDetailBackdrop = (ImageView) rootView.findViewById(R.id.iv_detail_backdrop);

        mSrlMovieDetail = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_movie_detail);

        mMovieDetailPager = (ViewPager) rootView.findViewById(R.id.movie_detail_pager);
        mMovieDetailPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        mSrlMovieDetail.setOnRefreshListener(this);

        TabLayout movieDetailTabs = (TabLayout) rootView.findViewById(R.id.movie_detail_tabs);
        movieDetailTabs.setupWithViewPager(mMovieDetailPager);

        mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator_detail);
        mFabFavorite = (FloatingActionButton) rootView.findViewById(R.id.fab_favorite);

        mFabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMovieDetailData != null) {
                    if (!mFabFavoriteIsChecked) {
                        addToFavorite();
                        setCheckedFabFavorite(true);
                    } else {
                        removeFromFavorite();
                        setCheckedFabFavorite(false);
                    }
                }
            }
        });

        loadMovieDetailData();

        return rootView;
    }

    private void enableDisableSwipeRefresh(boolean enable) {
        if (mSrlMovieDetail != null) {
            mSrlMovieDetail.setEnabled(enable);
        }
    }

    public FloatingActionButton getFabFavorite() {
        return mFabFavorite;
    }

    private void setCheckedFabFavorite(boolean checked) {
        if (checked) {
            mFabFavorite.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            mFabFavorite.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }
        mFabFavoriteIsChecked = checked;
    }

    private void addToFavorite() {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mId);
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, mTitle);
        values.put(MovieContract.MovieEntry.COLUMN_POSTER, mPoster);
        values.put(MovieContract.MovieEntry.COLUMN_BACKDROP, mMovieDetailData.getBackdrop());
        values.put(MovieContract.MovieEntry.COLUMN_DATE, mMovieDetailData.getDate());
        values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, mMovieDetailData.getRuntime());
        values.put(MovieContract.MovieEntry.COLUMN_RATING, mRating);
        values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, mMovieDetailData.getSynopsis());
        getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);

        if (mVideos != null && !mVideos.isEmpty()) {
            ContentValues[] videoValuesArr = new ContentValues[mVideos.size()];
            for (int i = 0; i < mVideos.size(); i++) {
                Video currentVideo = mVideos.get(i);
                videoValuesArr[i] = new ContentValues();
                videoValuesArr[i].put(MovieContract.VideoEntry.COLUMN_MOVIE_ID, mId);
                videoValuesArr[i].put(MovieContract.VideoEntry.COLUMN_VIDEO_NAME, currentVideo.getVideoName());
                videoValuesArr[i].put(MovieContract.VideoEntry.COLUMN_VIDEO_URL, currentVideo.getVideoUrl());
                videoValuesArr[i].put(MovieContract.VideoEntry.COLUMN_VIDEO_TYPE, currentVideo.getVideoType());
            }
            getActivity().getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI, videoValuesArr);
        }

        if (mReviews != null && !mReviews.isEmpty()) {
            ContentValues[] reviewValuesArr = new ContentValues[mReviews.size()];
            for (int i = 0; i < mReviews.size(); i++) {
                Review currentReview = mReviews.get(i);
                reviewValuesArr[i] = new ContentValues();
                reviewValuesArr[i].put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, mId);
                reviewValuesArr[i].put(MovieContract.ReviewEntry.COLUMN_AUTHOR, currentReview.getAuthor());
                reviewValuesArr[i].put(MovieContract.ReviewEntry.COLUMN_CONTENT, currentReview.getContent());
            }
            getActivity().getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, reviewValuesArr);
        }

        if (mToastFavorite != null) {
            mToastFavorite.cancel();
        }
        mToastFavorite = Toast.makeText(getActivity(), getString(R.string.added_to_my_favorites), Toast.LENGTH_SHORT);
        mToastFavorite.show();
    }

    private void removeFromFavorite() {
        Uri uriMovie = MovieContract.MovieEntry.buildMovieIdUri(mId);
        int rowsDeleted = getActivity().getContentResolver().delete(uriMovie, null, null);

        if (mVideos != null && !mVideos.isEmpty()) {
            Uri uriVideo = ContentUris.withAppendedId(MovieContract.VideoEntry.CONTENT_URI, mId);
            getActivity().getContentResolver().delete(uriVideo, null, null);
        }

        if (mReviews != null && !mReviews.isEmpty()) {
            Uri uriReview = ContentUris.withAppendedId(MovieContract.ReviewEntry.CONTENT_URI, mId);
            getActivity().getContentResolver().delete(uriReview, null, null);
        }

        if (rowsDeleted > 0) {
            if (mToastFavorite != null) {
                mToastFavorite.cancel();
            }
            mToastFavorite = Toast.makeText(getActivity(), getString(R.string.removed_from_my_favorites), Toast.LENGTH_SHORT);
            mToastFavorite.show();
        }
    }

    @Override
    public void onRefresh() {
        loadMovieDetailData();
    }

    private void loadMovieDetailData() {
        Uri uri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI_COUNT, mId);
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int countAvailable = cursor.getInt(0);
        cursor.close();

        if (countAvailable < 1) {
            setCheckedFabFavorite(false);

            if (ExtraUtils.isNetworkAvailable(getActivity())) {
                setupLoader(MOVIE_DETAIL_JSON_LOADER_ID);
            } else {
                showErrorMessage(getString(R.string.no_internet_connection));
            }
        } else {
            setCheckedFabFavorite(true);
            setupLoader(MOVIE_DETAIL_CURSOR_LOADER_ID);
        }
    }

    private void setupLoader(int loaderId) {
        LoaderManager loaderManager = getLoaderManager();
        Loader<List<Movie>> popularMoviesLoader = loaderManager.getLoader(loaderId);
        if (popularMoviesLoader == null) {
            loaderManager.initLoader(loaderId, null, this);
        } else {
            loaderManager.restartLoader(loaderId, null, this);
        }
    }

    @Override
    public Loader<Movie> onCreateLoader(final int id, Bundle bundle) {
        return new AsyncTaskLoader<Movie>(getActivity()) {
            Movie mData;

            @Override
            protected void onStartLoading() {
                if (mData != null) {
                    deliverResult(mData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Movie loadInBackground() {
                Movie movieDetailData;
                if (id == MOVIE_DETAIL_JSON_LOADER_ID) {
                    if (mDetailUrl == null) return null;

                    URL detailMovieUrl = NetworkUtils.buildUrl(mDetailUrl);
                    URL videoMovieUrl = NetworkUtils.buildUrl(mVideoUrl);
                    URL reviewMovieUrl = NetworkUtils.buildUrl(mReviewUrl);

                    try {
                        String jsonMovieDetailResponse = NetworkUtils.getResponseFromHttpUrl(detailMovieUrl);
                        String jsonMovieVideoResponse = NetworkUtils.getResponseFromHttpUrl(videoMovieUrl);
                        String jsonMovieReviewResponse = NetworkUtils.getResponseFromHttpUrl(reviewMovieUrl);

                        movieDetailData = OpenMovieJsonUtils.getMovieDetailFromJson(getActivity(),
                                jsonMovieDetailResponse, jsonMovieVideoResponse, jsonMovieReviewResponse);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing input streams", e);
                        movieDetailData = null;
                    }
                } else {
                    String[] movieProjection = new String[]{
                            MovieContract.MovieEntry.COLUMN_BACKDROP,
                            MovieContract.MovieEntry.COLUMN_DATE,
                            MovieContract.MovieEntry.COLUMN_RUNTIME,
                            MovieContract.MovieEntry.COLUMN_RATING,
                            MovieContract.MovieEntry.COLUMN_SYNOPSIS
                    };
                    Uri movieUri = MovieContract.MovieEntry.buildMovieIdUri(mId);
                    Cursor movieCursor = getActivity().getContentResolver().query(
                            movieUri,
                            movieProjection,
                            null,
                            null,
                            null);

                    String[] videoProjection = new String[]{
                            MovieContract.VideoEntry.COLUMN_VIDEO_NAME,
                            MovieContract.VideoEntry.COLUMN_VIDEO_URL,
                            MovieContract.VideoEntry.COLUMN_VIDEO_TYPE
                    };
                    Uri videoUri = ContentUris.withAppendedId(MovieContract.VideoEntry.CONTENT_URI, mId);
                    Cursor videoCursor = getActivity().getContentResolver().query(
                            videoUri,
                            videoProjection,
                            null,
                            null,
                            null);

                    String[] reviewProjection = new String[]{
                            MovieContract.ReviewEntry.COLUMN_AUTHOR,
                            MovieContract.ReviewEntry.COLUMN_CONTENT
                    };
                    Uri reviewUri = ContentUris.withAppendedId(MovieContract.ReviewEntry.CONTENT_URI, mId);
                    Cursor reviewCursor = getActivity().getContentResolver().query(
                            reviewUri,
                            reviewProjection,
                            null,
                            null,
                            null);

                    movieDetailData = OpenMovieCursorUtils.getMovieDetailFromCursor(movieCursor, videoCursor, reviewCursor);
                    movieCursor.close();
                    videoCursor.close();
                    reviewCursor.close();
                }
                return movieDetailData;
            }

            @Override
            public void deliverResult(Movie data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Movie> loader, Movie movie) {
        mMovieDetailData = null;

        if (movie != null) {
            showMovieDetail();
            returnMovieDetailData(movie);
        } else {
            showErrorMessage(getString(R.string.movie_not_found));
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {
        mMovieDetailData = null;
    }

    private void showMovieDetail() {
        if (mLoadingIndicator.isShown()) mLoadingIndicator.setVisibility(View.GONE);
        if (mSrlMovieDetail.isRefreshing()) mSrlMovieDetail.setRefreshing(false);
        mFabFavorite.setVisibility(View.VISIBLE);
        if (mErrorSnackBar != null) mErrorSnackBar.dismiss();
    }

    private void showErrorMessage(String errorMessage) {
        if (mLoadingIndicator.isShown()) mLoadingIndicator.setVisibility(View.GONE);
        if (mSrlMovieDetail.isRefreshing()) mSrlMovieDetail.setRefreshing(false);

        mErrorSnackBar = Snackbar.make(mMovieDetailContainer, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.reload).toUpperCase(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadMovieDetailData();
                        if (!mLoadingIndicator.isShown()) mLoadingIndicator.setVisibility(View.VISIBLE);
                    }
                });
        mErrorSnackBar.show();
    }

    private void returnMovieDetailData(Movie movie) {
        mMovieDetailData = movie;

        Picasso.with(getActivity())
                .load(NetworkUtils.URL_IMAGE_780 + mMovieDetailData.getBackdrop())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_error)
                .into(mIvDetailBackdrop);

        String yearDate = getYearOfDate(mMovieDetailData.getDate());
        mFullTitle = mTitle + " (" + yearDate + ")";
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(mFullTitle);

        mVideos = mMovieDetailData.getVideo();
        mReviews = mMovieDetailData.getReview();

        mMovieDetailPager.setAdapter(new MovieDetailPagerAdapter(getChildFragmentManager()));
    }

    private String getYearOfDate(String fullDate) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = fullDateFormat.parse(fullDate);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
            String year = yearFormat.format(date);
            return year;
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date to year", e);
            return fullDate;
        }
    }

    private class MovieDetailPagerAdapter extends FragmentPagerAdapter {

        public MovieDetailPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MovieDetailPageOverviewFragment.newInstance(mPoster, mFullTitle,
                            mMovieDetailData.getRuntime(), mRating,
                            mMovieDetailData.getSynopsis());
                case 1:
                    return MovieDetailPageListFragment.newInstance(MovieDetailPageListFragment.VIDEO_TAG,
                            mVideos, null);
                case 2:
                    return MovieDetailPageListFragment.newInstance(MovieDetailPageListFragment.REVIEW_TAG,
                            null, mReviews);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.overview);
                case 1:
                    return getString(R.string.videos);
                case 2:
                    return getString(R.string.reviews);
                default:
                    return super.getPageTitle(position);
            }
        }
    }
}
