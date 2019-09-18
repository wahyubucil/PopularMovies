package website.asteroit.popularmovies;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import website.asteroit.popularmovies.data.MovieContract;
import website.asteroit.popularmovies.objects.Movie;
import website.asteroit.popularmovies.utilities.ExtraUtils;
import website.asteroit.popularmovies.utilities.NetworkUtils;
import website.asteroit.popularmovies.utilities.OpenMovieCursorUtils;
import website.asteroit.popularmovies.utilities.OpenMovieJsonUtils;

/**
 * A simple {@link Fragment} subclass.
 */

public class MovieListFragment extends Fragment implements
        MovieListAdapter.MovieListAdapterOnClickHandler,
        SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String LOG_TAG = MovieListFragment.class.getSimpleName();

    private int mMovieListTag;
    public static final int MOST_POPULAR_TAG = 1;
    public static final int TOP_RATED_TAG = 2;
    public static final int MY_FAVORITES_TAG = 3;
    private static final String MOVIE_LIST_TAG_KEY = "movie-list-tag";

    private static final String MOVIE_LIST_DATA_STATE = "movie-list-data-state";
    private ArrayList<Movie> mMovieListData = new ArrayList<>();

    private String mMovieListUrl;
    private static final String MOVIE_LIST_URL_KEY = "movie-list-url";

    private int mMovieListUrlPage = 1;

    private int mMovieListLoaderId;
    private SwipeRefreshLayout mSrlMovieList;

    private TextView mTvErrorList;
    private RecyclerView mRvMovieList;
    private MovieListAdapter mMovieListAdapter;

    private EndlessRecyclerViewScrollListener mRvScrollListener;

    public static MovieListFragment newInstance(int movieListTag, String movieListUrl) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_LIST_TAG_KEY, movieListTag);
        args.putString(MOVIE_LIST_URL_KEY, movieListUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = this.getArguments();
        mMovieListTag = args.getInt(MOVIE_LIST_TAG_KEY);
        mMovieListUrl = args.getString(MOVIE_LIST_URL_KEY);

        mMovieListLoaderId = mMovieListTag;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_list, container, false);

        mSrlMovieList = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_movie_list);

        mTvErrorList = (TextView) rootView.findViewById(R.id.tv_error_list);

        mRvMovieList = (RecyclerView) rootView.findViewById(R.id.rv_movie_list);

        int spanCount = ExtraUtils.calculateNoOfColumns(getActivity());

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount, GridLayoutManager.VERTICAL, false);

        mRvMovieList.setLayoutManager(layoutManager);

        if (mMovieListTag != MY_FAVORITES_TAG) {
            mRvScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    mMovieListUrlPage = page + 1;
                    loadMovieListData();
                }
            };

            mRvMovieList.addOnScrollListener(mRvScrollListener);
        }

        mRvMovieList.setHasFixedSize(true);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_layout_margin);
        mRvMovieList.addItemDecoration(new MovieListAdapter.GridSpacingItemDecoration(spanCount, spacingInPixels, false, 0));

        mMovieListAdapter = new MovieListAdapter(this);

        mRvMovieList.setAdapter(mMovieListAdapter);

        mSrlMovieList.setOnRefreshListener(this);

        if (savedInstanceState != null) {
            mMovieListData = savedInstanceState.getParcelableArrayList(MOVIE_LIST_DATA_STATE);
            mMovieListAdapter.setMovieListData(mMovieListData);
        } else {
            if (mMovieListTag != MY_FAVORITES_TAG) {
                mSrlMovieList.setRefreshing(true);
                loadMovieListData();
            }
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovieListTag == MY_FAVORITES_TAG) {
            mMovieListData.clear();
            mMovieListAdapter.setMovieListData(null);
            loadMovieListData();
        }
    }

    @Override
    public void onRefresh() {
        if (mMovieListTag != MY_FAVORITES_TAG) {
            mMovieListUrlPage = 1;
        }
        mMovieListData.clear();
        mMovieListAdapter.setMovieListData(null);
        loadMovieListData();
    }

    public void setPositionToTop() {
        mRvMovieList.smoothScrollToPosition(0);
        ((MainFragment) getParentFragment()).turnOnToolbarScrolling();
    }

    private void loadMovieListData() {
        if (mMovieListTag != MY_FAVORITES_TAG) {
            if (ExtraUtils.isNetworkAvailable(getActivity())) {
                setupLoader();
            } else {
                showErrorMessage(getString(R.string.no_internet_connection));
            }
        } else {
            setupLoader();
        }
    }

    private void setupLoader() {
        LoaderManager loaderManager = getLoaderManager();
        Loader<List<Movie>> moviesLoader = loaderManager.getLoader(mMovieListLoaderId);
        if (moviesLoader == null) {
            loaderManager.initLoader(mMovieListLoaderId, null, this);
        } else {
            loaderManager.restartLoader(mMovieListLoaderId, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_LIST_DATA_STATE, mMovieListData);
    }

    @Override
    public void onItemClick(int movieId, String movieTitle, String moviePoster, double movieRating) {
        Intent toDetailActivity = new Intent(getActivity(), DetailActivity.class);
        toDetailActivity.putExtra("id", movieId);
        toDetailActivity.putExtra("title", movieTitle);
        toDetailActivity.putExtra("poster", moviePoster);
        toDetailActivity.putExtra("rating", movieRating);
        startActivity(toDetailActivity);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(getActivity()) {

            List<Movie> mData;

            @Override
            protected void onStartLoading() {
                if (mData != null) {
                    deliverResult(mData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                List<Movie> movieListData;
                if (id != MY_FAVORITES_TAG) {
                    String stringUrl = mMovieListUrl + "&page=" + mMovieListUrlPage;
                    URL url = NetworkUtils.buildUrl(stringUrl);

                    try {
                        String jsonMovieListResponse = NetworkUtils.getResponseFromHttpUrl(url);

                        movieListData = OpenMovieJsonUtils.getMovieListFromJson(jsonMovieListResponse);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing input streams", e);
                        movieListData = null;
                    }
                } else {
                    String[] projection = new String[]{
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            MovieContract.MovieEntry.COLUMN_TITLE,
                            MovieContract.MovieEntry.COLUMN_POSTER,
                            MovieContract.MovieEntry.COLUMN_RATING
                    };
                    String sortOrder = MovieContract.MovieEntry._ID + " DESC";
                    Cursor cursor = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            projection, null, null, sortOrder);
                    movieListData = OpenMovieCursorUtils.getMovieListFromCursor(cursor);
                    cursor.close();
                }

                return movieListData;
            }

            @Override
            public void deliverResult(List<Movie> data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        if (data != null && !data.isEmpty()) {
            showMovieList();
            mMovieListData.addAll(data);
            mMovieListAdapter.setMovieListData(mMovieListData);
        } else {
            showErrorMessage(getString(R.string.no_movies));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mMovieListData.clear();
        mMovieListAdapter.setMovieListData(null);
    }

    private void showMovieList() {
        if (mSrlMovieList.isRefreshing()) mSrlMovieList.setRefreshing(false);
        mTvErrorList.setVisibility(View.GONE);
        mRvMovieList.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String errorMessage) {
        if (mSrlMovieList.isRefreshing()) mSrlMovieList.setRefreshing(false);
        mRvMovieList.setVisibility(View.GONE);

        mTvErrorList.setText(errorMessage);
        mTvErrorList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_favorites_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mMovieListTag != MY_FAVORITES_TAG) {
            MenuItem menuItem = menu.findItem(R.id.action_remove_all_my_favorites);
            menuItem.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove_all_my_favorites:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        if (mMovieListData.isEmpty()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.remove_all_my_favorites);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeAllMyFavorites();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeAllMyFavorites() {
        getActivity().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        getActivity().getContentResolver().delete(MovieContract.VideoEntry.CONTENT_URI, null, null);
        getActivity().getContentResolver().delete(MovieContract.ReviewEntry.CONTENT_URI, null, null);
        loadMovieListData();
    }
}
