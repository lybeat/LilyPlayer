package com.lybeat.lilyplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.entity.Video;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Author: lybeat
 * Date: 2016/7/15
 */
public class VideoAdapter extends BaseAdapter {

    private Context context;
    private List<Video> videos;

    public VideoAdapter(Context context, List<Video> videos) {
        this.context = context;
        this.videos = videos;
    }

    @Override
    public BaseHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindHolder(BaseHolder holder, int position) {
        if (holder instanceof VideoHolder) {
            Video video = videos.get(position);
            ((VideoHolder) holder).thumbImg.setImageBitmap(video.getThumbnail());
            ((VideoHolder) holder).nameTxt.setText(video.getName());
            ((VideoHolder) holder).durationTxt.setText(stringForTime(video.getDuration()));
            ((VideoHolder) holder).sizeTxt.setText(String.format(Locale.CHINA, "%dM", byteToM(video.getSize())));
        }
    }

    private long byteToM(long b) {
        return b / 1024 / 1024;
    }

    private String stringForTime(long timeMs) {
        int totalSeconds = (int) (timeMs / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    private class VideoHolder extends BaseHolder {

        ImageView thumbImg;
        TextView nameTxt;
        TextView durationTxt;
        TextView sizeTxt;

        public VideoHolder(View itemView) {
            super(itemView);
            thumbImg = (ImageView) itemView.findViewById(R.id.thumb_img);
            nameTxt = (TextView) itemView.findViewById(R.id.name_txt);
            durationTxt = (TextView) itemView.findViewById(R.id.duration_txt);
            sizeTxt = (TextView) itemView.findViewById(R.id.size_txt);
        }
    }
}
