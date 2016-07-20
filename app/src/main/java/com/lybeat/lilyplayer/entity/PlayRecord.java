package com.lybeat.lilyplayer.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Author: lybeat
 * Date: 2016/7/18
 */
@Entity
public class PlayRecord {

    @Id
    private long id;
    private String path;
    private String name;
    private long progress;
    private long duration;
    private long time;

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getProgress() {
        return this.progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Generated(hash = 1554485148)
    public PlayRecord(long id, String path, String name, long progress,
                      long duration, long time) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.progress = progress;
        this.duration = duration;
        this.time = time;
    }

    @Generated(hash = 1408486030)
    public PlayRecord() {
    }
}
