package com.lybeat.lilyplayer.widget.media;

import android.view.View;

/**
 * Author: lybeat
 * Date: 2016/7/15
 */
public interface IMediaInfoBoard {

    void setAnchorView(View view);

    boolean isShowing();

    void show();

    void show(int timeout);

    void hide();

    void setMediaInfoController(LilyMediaInfoBoard.MediaInfoControl controller);

    void showMediaTitle(String title);

    void showPhoneInfo();

    void showMediaInfo();

//    void hideNavigation();
}
