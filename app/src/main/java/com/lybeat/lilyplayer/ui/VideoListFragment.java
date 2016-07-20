package com.lybeat.lilyplayer.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.adapter.BaseAdapter;
import com.lybeat.lilyplayer.adapter.VideoAdapter;
import com.lybeat.lilyplayer.entity.Video;

import java.util.ArrayList;

/**
 * Author: lybeat
 * Date: 2016/7/15
 */
public class VideoListFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        BaseAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

    private ArrayList<Video> videos;
    private VideoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);

        initViews(view);
        initData();

        return view;
    }

    @Override
    protected void initViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.video_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.video_refresh);
        refreshLayout.setColorSchemeColors(0xff64B5F6);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        videos = queryVideoInfo();
        adapter = new VideoAdapter(getActivity(), videos);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {

    }

    private ArrayList<Video> queryVideoInfo() {
        ArrayList<Video> videos = new ArrayList<>();
        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID
        };
        Cursor cursor = MediaStore.Video.query(cr, uri, projection);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Video.Media.DATA));
                String name = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Video.Media.DISPLAY_NAME));
                String format = cursor.getString(cursor.getColumnIndex(
                        MediaStore.Video.Media.MIME_TYPE));
                long duration = cursor.getLong(cursor.getColumnIndex(
                        MediaStore.Video.Media.DURATION));
                long size = cursor.getInt(cursor.getColumnIndex(
                        MediaStore.Video.Media.SIZE));
                long id = cursor.getLong(cursor.getColumnIndex(
                        MediaStore.Video.Media._ID));

                Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        cr, id, MediaStore.Video.Thumbnails.MINI_KIND, new BitmapFactory.Options());

                Video video = new Video(path,
                        name,
                        format,
                        size,
                        duration,
                        thumbnail);
                videos.add(video);

//                Log.i(TAG, "video name: " + name);
//                Log.i(TAG, "video format: " + format);
//                Log.i(TAG, "video duration: " + duration);
//                Log.i(TAG, "video size: " + size);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return videos;
    }

    @Override
    public void onItemClick(View view, int position) {
        Video video = videos.get(position);
        Intent intent = new Intent();
        intent.setClass(getActivity(), VideoActivity.class);
        intent.putExtra("video_path", video.getPath());
        intent.putExtra("video_title", video.getName());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
