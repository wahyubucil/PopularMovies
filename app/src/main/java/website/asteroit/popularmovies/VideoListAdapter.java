package website.asteroit.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import website.asteroit.popularmovies.objects.Video;

/**
 * Created by Wahyu on 30/07/2017.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListAdapterViewHolder> {

    private List<Video> mVideoListData = new ArrayList<>();

    private OnVideoClickHandler mClickHandler;

    public interface OnVideoClickHandler {
        void OnVideoClick(String videoUrl);
    }

    public void setOnVideoClickHandler(OnVideoClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public VideoListAdapter.VideoListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
        return new VideoListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoListAdapter.VideoListAdapterViewHolder holder, int position) {
        Video currentVideo = mVideoListData.get(position);
        holder.mTvVideoName.setText(currentVideo.getVideoName());
        String videoType = currentVideo.getVideoType();
        holder.mTvVideoType.setText(currentVideo.getVideoType());
        if (videoType.equals("Trailer")) {
            holder.mIvVideoItem.setImageResource(R.drawable.ic_local_movies_white_48dp);
        } else if (videoType.equals("Clip")) {
            holder.mIvVideoItem.setImageResource(R.drawable.ic_movie_filter_white_48dp);
        } else if (videoType.equals("Featurette")) {
            holder.mIvVideoItem.setImageResource(R.drawable.ic_movie_roll_white_48dp);
        } else {
            holder.mIvVideoItem.setImageResource(R.drawable.ic_movie_creation_white_48dp);
        }
    }

    @Override
    public int getItemCount() {
        if (mVideoListData == null) return 0;
        return mVideoListData.size();
    }

    public void setVideoListData(List<Video> videoListData) {
        mVideoListData = videoListData;
        notifyDataSetChanged();
    }

    public class VideoListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mIvVideoItem;
        public final TextView mTvVideoName, mTvVideoType;

        public VideoListAdapterViewHolder(View itemView) {
            super(itemView);

            mIvVideoItem = itemView.findViewById(R.id.iv_video_item);
            mTvVideoName = itemView.findViewById(R.id.tv_video_name);
            mTvVideoType = itemView.findViewById(R.id.tv_video_type);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Video currentVideo = mVideoListData.get(adapterPosition);
            String videoUrl = currentVideo.getVideoUrl();
            mClickHandler.OnVideoClick(videoUrl);
        }
    }
}