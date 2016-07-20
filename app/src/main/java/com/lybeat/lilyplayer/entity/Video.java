package com.lybeat.lilyplayer.entity;

import android.graphics.Bitmap;

/**
 * Author: lybeat
 * Date: 2015/12/12
 */
public class Video {

    private String path;
    private String name;
    private String format;
    private long size;
    private long duration;
    private Bitmap thumbnail;

    public Video() {}

    public Video(String path,
                 String name,
                 String format,
                 long size,
                 long duration,
                 Bitmap thumbnail) {
        this.path = path;
        this.name = name;
        this.format = format;
        this.size = size;
        this.duration = duration;
        this.thumbnail = thumbnail;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public long getSize() {
        return size;
    }

    public long getDuration() {
        return duration;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}
