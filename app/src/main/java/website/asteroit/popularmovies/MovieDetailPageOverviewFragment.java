package website.asteroit.popularmovies;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import website.asteroit.popularmovies.utilities.NetworkUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailPageOverviewFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailPageOverviewFragment.class.getSimpleName();

    private static final String TITLE_ARGS_KEY = "title";
    private static final String POSTER_ARGS_KEY = "poster";
    private static final String RUNTIME_ARGS_KEY = "runtime";
    private static final String RATING_ARGS_KEY = "rating";
    private static final String SYNOPSIS_ARGS_KEY = "synopsis";

    private String mTitle, mPoster, mRuntime, mRating, mSynopsis;

    public static MovieDetailPageOverviewFragment newInstance(String poster, String title, int runtime, double rating, String synopsis) {
        MovieDetailPageOverviewFragment fragment = new MovieDetailPageOverviewFragment();
        Bundle args = new Bundle();
        args.putString(POSTER_ARGS_KEY, poster);
        args.putString(TITLE_ARGS_KEY, title);
        args.putInt(RUNTIME_ARGS_KEY, runtime);
        args.putDouble(RATING_ARGS_KEY, rating);
        args.putString(SYNOPSIS_ARGS_KEY, synopsis);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieDetailPageOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = this.getArguments();
        mPoster = args.getString(POSTER_ARGS_KEY);
        mTitle = args.getString(TITLE_ARGS_KEY);
        mRuntime = getDuration(args.getInt(RUNTIME_ARGS_KEY));
        mRating = getActivity().getString(R.string.rating_format, args.getDouble(RATING_ARGS_KEY));
        mSynopsis = args.getString(SYNOPSIS_ARGS_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail_page_overview, container, false);

        final FloatingActionButton fabFavorite = ((DetailFragment) getParentFragment()).getFabFavorite();
        NestedScrollView nsvPageOverview = (NestedScrollView) rootView.findViewById(R.id.nsv_page_overview);
        nsvPageOverview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    fabFavorite.hide();
                } else if (scrollY < oldScrollY) {
                    fabFavorite.show();
                }
            }
        });

        ImageView ivDetailPoster = (ImageView) rootView.findViewById(R.id.iv_detail_poster);
        Picasso.with(getActivity())
                .load(NetworkUtils.URL_IMAGE_342 + mPoster)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_error)
                .into(ivDetailPoster);

        TextView tvDetailTitle = (TextView) rootView.findViewById(R.id.tv_detail_title);
        tvDetailTitle.setText(mTitle);

        TextView tvDetailRuntime = (TextView) rootView.findViewById(R.id.tv_detail_runtime);
        tvDetailRuntime.setText(mRuntime);

        TextView tvDetailRating = (TextView) rootView.findViewById(R.id.tv_detail_rating);
        tvDetailRating.setText(mRating);

        TextView tvDetailSynopsis = (TextView) rootView.findViewById(R.id.tv_detail_synopsis);
        tvDetailSynopsis.setText(mSynopsis);

        return rootView;
    }

    private String getDuration(int runtimeMinutes) {
        int hours = runtimeMinutes / 60;
        int minutes = runtimeMinutes % 60;
        if (minutes < 1) {
            return hours + "h ";
        } else {
            return hours + "h " + minutes + "m";
        }
    }

}
