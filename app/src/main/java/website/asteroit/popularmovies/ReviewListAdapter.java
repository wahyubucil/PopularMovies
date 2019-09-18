package website.asteroit.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import website.asteroit.popularmovies.objects.Review;

/**
 * Created by Wahyu on 30/07/2017.
 */

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewListAdapterViewHolder> {

    private List<Review> mReviewListData;

    @Override
    public ReviewListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, parent, false);
        return new ReviewListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewListAdapterViewHolder holder, int position) {
        Review currentReview = mReviewListData.get(position);
        holder.mTvReviewAuthor.setText(currentReview.getAuthor());
        holder.mTvReviewContent.setText('"' + currentReview.getContent() + '"');
    }

    @Override
    public int getItemCount() {
        if (mReviewListData == null) return 0;
        return mReviewListData.size();
    }

    public void setReviewListData(List<Review> reviewListData) {
        mReviewListData = reviewListData;
        notifyDataSetChanged();
    }

    public class ReviewListAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTvReviewAuthor;
        public final TextView mTvReviewContent;

        public ReviewListAdapterViewHolder(View itemView) {
            super(itemView);

            mTvReviewAuthor = itemView.findViewById(R.id.tv_review_author);
            mTvReviewContent = itemView.findViewById(R.id.tv_review_content);
        }
    }
}
