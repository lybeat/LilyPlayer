package com.lybeat.lilyplayer.ui;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Author: lybeat
 * Date: 2016/3/2
 */
public abstract class BaseFragment extends Fragment {

    protected abstract void initViews(View view);
    protected abstract void initData();
}