package com.lybeat.lilyplayer.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.content.RecentMediaStorage;
import com.lybeat.lilyplayer.db.DBManager;
import com.lybeat.lilyplayer.entity.PlayRecord;
import com.lybeat.lilyplayer.widget.media.IjkVideoView;
import com.lybeat.lilyplayer.widget.media.LilyMediaController;
import com.lybeat.lilyplayer.widget.media.LilyMediaInfoBoard;
import com.lybeat.lilyplayer.widget.media.MeasureHelper;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Author: lybeat
 * Date: 2016/7/14
 */
public class VideoActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {

    private static final String TAG = "VideoActivity";

    private String videoPath;
    private Uri videoUri;
    private String videoTitle;

    private LilyMediaController mediaController;
    private LilyMediaInfoBoard mediaInfoBoard;
    private IjkVideoView videoView;
    private DrawerLayout drawerLayout;
    private ViewGroup rightDrawer;

    private Settings settings;
    private boolean backPressed;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_player);

        initData();
        initView();
        initPlayer();
        start();
    }

    private void initData() {
        Intent intent = getIntent();
        videoPath = intent.getStringExtra("video_path");
        videoTitle = intent.getStringExtra("video_title");
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(Intent.ACTION_VIEW)) {
                videoPath = intent.getDataString();
            } else if (action.equals(Intent.ACTION_SEND)) {
                videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = videoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Unknown scheme");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        videoPath = videoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme);
                        finish();
                        return;
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(videoPath)) {
            new RecentMediaStorage(this).saveUrlAsync(videoPath);
        }
    }

    private void initView() {
        mediaController = new LilyMediaController(this, false);
        mediaInfoBoard = new LilyMediaInfoBoard(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        rightDrawer = (ViewGroup) findViewById(R.id.right_drawer);

        drawerLayout.setScrimColor(Color.TRANSPARENT);

        videoView = (IjkVideoView) findViewById(R.id.video_view);
        videoView.setMediaController(mediaController);
        videoView.setMediaInfoBoard(mediaInfoBoard);
        videoView.showMediaTitle(videoTitle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) return;
        mediaController.setNextListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VideoActivity.this, "Next", Toast.LENGTH_SHORT).show();
                videoView.showAllBoard();
            }
        });
        mediaController.setSelectSetListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
                videoView.hideAllBoard();
            }
        });
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void initPlayer() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    private void start() {
        if (videoPath != null) {
            videoView.setVideoPath(videoPath);
        } else if (videoUri != null) {
            videoView.setVideoURI(videoUri);
        } else {
            Log.e(TAG, "Null data source");
            finish();
        }
        videoView.start();
    }

    @Override
    public void onBackPressed() {
        backPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (backPressed || !videoView.isBackgroundPlayEnabled()) {
            videoView.stopPlayback();
            videoView.release(true);
            videoView.stopBackgroundPlay();
        } else {
            videoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();

        DBManager dbManager = DBManager.getInstance(this);
        dbManager.insertPlayRecord(new PlayRecord(0, videoPath, videoTitle,
                videoView.getCurrentPosition(),
                videoView.getDuration(), System.currentTimeMillis()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_ratio) {
            int aspectRatio = videoView.toggleAspectRatio();
            String aspectRatioText = MeasureHelper.getAspectRatioText(this, aspectRatio);
            return true;
        } else if (id == R.id.action_toggle_player) {
            int player = videoView.togglePlayer();
            String playerText = IjkVideoView.getPlayerText(this, player);
            return true;
        } else if (id == R.id.action_toggle_render) {
            int render = videoView.toggleRender();
            String renderText = IjkVideoView.getRenderText(this, render);
            return true;
        } else if (id == R.id.action_show_info) {
            videoView.showMediaInfo();
        } else if (id == R.id.action_show_tracks) {
            if (drawerLayout.isDrawerOpen(rightDrawer)) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.right_drawer);
                if (fragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.remove(fragment);
                    transaction.commit();
                }
                drawerLayout.closeDrawer(rightDrawer);
            } else {
                Fragment f = TracksFragment.newInstance();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.right_drawer, f);
                transaction.commit();
                drawerLayout.openDrawer(rightDrawer);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        return new ITrackInfo[0];
    }

    @Override
    public int getSelectedTrack(int trackType) {
        return videoView.getSelectedTrack(trackType);
    }

    @Override
    public void selectTrack(int stream) {
        videoView.selectTrack(stream);
    }

    @Override
    public void deselectTrack(int stream) {
        videoView.deselectTrack(stream);
    }
}
