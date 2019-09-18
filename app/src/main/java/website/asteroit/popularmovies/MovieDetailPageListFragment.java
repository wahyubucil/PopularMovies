package website.asteroit.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import website.asteroit.popularmovies.objects.Review;
import website.asteroit.popularmovies.objects.Video;

/**
 * Created by Wahyu on 29/07/2017.
 */

public class MovieDetailPageListFragment extends Fragment {

    public static final int VIDEO_TAG = 1;
    public static final int REVIEW_TAG = 2;

    private List<Video> mVideoData = new ArrayList<>();
    private List<Review> mReviewData = new ArrayList<>();
    private int mTag;

    private static final String TAG_KEY = "tag";
    private static final String DATA_KEY = "data";

    NestedScrollView mNoDataContainer;
    TextView mTvNoData;

    public static MovieDetailPageListFragment newInstance(int tag, List<Video> videos, List<Review> reviews) {
        MovieDetailPageListFragment fragment = new MovieDetailPageListFragment();
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, tag);
        if (tag == VIDEO_TAG) {
            ArrayList<Video> videoData = new ArrayList<>();
            if (videos != null) {
                videoData.addAll(videos);
            } else {
                videoData = null;
            }
            args.putParcelableArrayList(DATA_KEY, videoData);
        } else {
            ArrayList<Review> reviewData = new ArrayList<>();
            if (reviews != null) {
                reviewData.addAll(reviews);
            } else {
                reviewData = null;
            }
            args.putParcelableArrayList(DATA_KEY, reviewData);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = this.getArguments();
        mTag = args.getInt(TAG_KEY);
        if (mTag == VIDEO_TAG) {
            mVideoData = args.getParcelableArrayList(DATA_KEY);
        } else {
            mReviewData = args.getParcelableArrayList(DATA_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail_page_list, container, false);

        RecyclerView rvMovieDetailPage = (RecyclerView) rootView.findViewById(R.id.rv_movie_detail_page);
        final FloatingActionButton fabFavorite = ((DetailFragment) getParentFragment()).getFabFavorite();
        mNoDataContainer = (NestedScrollView) rootView.findViewById(R.id.no_data_container);
        mTvNoData = (TextView) rootView.findViewById(R.id.tv_no_data);

        rvMovieDetailPage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fabFavorite.hide();
                }
                else if (dy < 0) {
                    fabFavorite.show();
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvMovieDetailPage.setLayoutManager(layoutManager);
        rvMovieDetailPage.setHasFixedSize(true);
        if (mTag == VIDEO_TAG) {
            if (mVideoData != null) {
                VideoListAdapter adapter = new VideoListAdapter();
                rvMovieDetailPage.setAdapter(adapter);
                adapter.setVideoListData(mVideoData);
                adapter.setOnVideoClickHandler(new VideoListAdapter.OnVideoClickHandler() {
                    @Override
                    public void OnVideoClick(String videoUrl) {
                        Intent openVideoUrlIntent = new Intent(Intent.ACTION_VIEW);
                        openVideoUrlIntent.setData(Uri.parse(videoUrl));
                        startActivity(openVideoUrlIntent);
                    }
                });
            } else {
                showNoDataMessage(getString(R.string.no_video));
            }
        } else {
            if (mReviewData != null) {
                ReviewListAdapter adapter = new ReviewListAdapter();
                rvMovieDetailPage.setAdapter(adapter);
                adapter.setReviewListData(mReviewData);
            }
            else {
                showNoDataMessage(getString(R.string.no_review));
            }
        }

        return rootView;
    }

    private void showNoDataMessage(String noDataMessage) {
        mNoDataContainer.setVisibility(View.VISIBLE);
        mTvNoData.setText(noDataMessage);
    }
}
