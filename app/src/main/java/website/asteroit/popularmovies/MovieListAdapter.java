package website.asteroit.popularmovies;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import website.asteroit.popularmovies.objects.Movie;
import website.asteroit.popularmovies.utilities.NetworkUtils;

/**
 * Created by Wahyu on 22/06/2017.
 */

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListAdapterViewHolder> {

    private List<Movie> mMovieListData = new ArrayList<>();

    private Context mContext;

    private final MovieListAdapterOnClickHandler mClickHandler;

    public interface MovieListAdapterOnClickHandler {
        void onItemClick(int movieId, String movieTitle, String moviePoster, double movieRating);
    }

    public MovieListAdapter(MovieListAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public MovieListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new MovieListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieListAdapterViewHolder holder, int position) {
        Movie currentMovie = mMovieListData.get(position);
        ImageView imageViewList = holder.mIvImageList;
        Picasso.with(mContext)
                .load(NetworkUtils.URL_IMAGE_342 + currentMovie.getPoster())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_error)
                .into(imageViewList);

        holder.mTvTitleList.setText(currentMovie.getTitle());
        
        double rating = currentMovie.getRating();
        String ratingString;
        if (rating == 0) {
            ratingString = mContext.getString(R.string.no_rating);
        } else {
            ratingString = mContext.getString(R.string.rating_format, rating);
        }
        holder.mTvRatingList.setText(ratingString);
    }

    @Override
    public int getItemCount() {
        if (mMovieListData == null) return 0;
        return mMovieListData.size();
    }

    public void setMovieListData(List<Movie> movieListData) {
        mMovieListData = movieListData;
        notifyDataSetChanged();
    }

    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mIvImageList;
        public final TextView mTvTitleList;
        public final TextView mTvRatingList;

        public MovieListAdapterViewHolder(View itemView) {
            super(itemView);

            mIvImageList = (ImageView) itemView.findViewById(R.id.iv_image_list);
            mTvTitleList = (TextView) itemView.findViewById(R.id.tv_title_list);
            mTvRatingList = (TextView) itemView.findViewById(R.id.tv_rating_list);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Movie currentMovie = mMovieListData.get(adapterPosition);
            int id = currentMovie.getId();
            String title = currentMovie.getTitle();
            String poster = currentMovie.getPoster();
            double rating = currentMovie.getRating();
            mClickHandler.onItemClick(id, title, poster, rating);
        }
    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;
        private int headerNum;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, int headerNum) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
            this.headerNum = headerNum;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view) - headerNum; // item position

            if (position >= 0) {
                int column = position % spanCount; // item column

                if (includeEdge) {
                    outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                    if (position < spanCount) { // top edge
                        outRect.top = spacing;
                    }
                    outRect.bottom = spacing; // item bottom
                } else {
                    outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                    if (position >= spanCount) {
                        outRect.top = spacing; // item top
                    }
                }
            } else {
                outRect.left = 0;
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            }
        }
    }
}
