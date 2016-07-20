package com.lybeat.lilyplayer.application;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: lybeat
 * Date: 2016/4/10
 */
public class MyApplication extends Application {

    private static MyApplication application;
    private List<Activity> activities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
    }

    public synchronized static MyApplication create() {
        if (application == null) {
            application = new MyApplication();
        }
        return application;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public void quitAllActivity() {
        for (Activity activity : activities) {
            if (activity != null) {
                activity.finish();
            }
        }
    }
}
