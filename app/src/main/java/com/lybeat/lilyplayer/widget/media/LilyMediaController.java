package com.lybeat.lilyplayer.widget.media;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lybeat.lilyplayer.R;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

/**
 * Author: lybeat
 * Date: 2016/7/14
 */
public class LilyMediaController extends FrameLayout implements IMediaController {

    private static final String TAG = "LilyMediaController";

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private MediaPlayerControl mPlayer;
    private Context mContext;
    private View mAnchor;
    private View mRoot;
    private WindowManager mWindowManager;
    private Window mWindow;
    private View mDecor;
    private WindowManager.LayoutParams mDecorLayoutParams;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mUseFastForward;
    private boolean mFromXml;
    private OnClickListener mNextListener, mPrevListener, mSelectSetListener;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private SeekBar mSeekBar;
    private TextView mTimeTxt;
    private ImageButton mPauseIbtn;
    private ImageButton mFfwdIbtn;
    private ImageButton mRewIbtn;
    private ImageButton mNextIbtn;
    private ImageButton mPrevIbtn;
    private ImageButton mSelectSetIbtn;

    //----------
    // Extends
    //----------
    private ArrayList<View> mShowOnceArray = new ArrayList<>();

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public LilyMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
    }

    public LilyMediaController(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
        initFloatingWindowLayout();
        initFloatingWindow();
    }

    public LilyMediaController(Context context) {
        this(context, true);
    }

    private void initFloatingWindow() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindow = PolicyCompat.createWindow(mContext);
        mWindow.setWindowManager(mWindowManager, null, null);
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
        mDecor = mWindow.getDecorView();
        mDecor.setOnTouchListener(mTouchListener);
        mWindow.setContentView(this);
        mWindow.setBackgroundDrawableResource(android.R.color.transparent);

        // While the media controller is up, the volume control keys should
        // affect the media stream type
        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();
    }

    // Allocate and initialize the static parts of mDecorLayoutParams. Must
    // also call updateFloatingWindowLayout() to fill in the dynamic parts
    // (y and width) before mDecorLayoutParams can be used.
    @TargetApi(VERSION_CODES.HONEYCOMB)
    private void initFloatingWindowLayout() {
        mDecorLayoutParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.height = LayoutParams.WRAP_CONTENT;
        p.x = 0;
        p.format = PixelFormat.TRANSLUCENT;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
        p.token = null;
        p.windowAnimations = 0; // android.R.style.DropDownAnimationDown;
    }

    // Update the dynamic parts of mDecorLayoutParams
    // Must be called with mAnchor != NULL.
    private void updateFloatingWindowLayout() {
        int[] anchorPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);

        // we need to know the size of the controller so we can properly position it
        // within its space
        mDecor.measure(MeasureSpec.makeMeasureSpec(mAnchor.getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(mAnchor.getHeight(), MeasureSpec.AT_MOST));

        WindowManager.LayoutParams p = mDecorLayoutParams;
        p.width = mAnchor.getWidth();
        p.x = anchorPos[0] + (mAnchor.getWidth() - p.width) / 2;
        p.y = anchorPos[1] + mAnchor.getHeight() - mDecor.getMeasuredHeight();
    }

    // This is called whenever mAnchor's layout bound changes
    private OnLayoutChangeListener mLayoutChangeListener =
            (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) ?
                    new OnLayoutChangeListener() {
                        public void onLayoutChange(View v, int left, int top, int right,
                                                   int bottom, int oldLeft, int oldTop, int oldRight,
                                                   int oldBottom) {
                            updateFloatingWindowLayout();
                            if (mShowing) {
                                mWindowManager.updateViewLayout(mDecor, mDecorLayoutParams);
                            }
                        }
                    } :
                    null;

    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                }
            }
            return false;
        }
    };

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setAnchorView(View view) {
        boolean hasOnLayoutChangeListener = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);

        if (hasOnLayoutChangeListener && mAnchor != null) {
            mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mAnchor = view;
        if (hasOnLayoutChangeListener && mAnchor != null) {
            mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        mRoot = makeControllerView();
        initControllerView(mRoot);
        addView(mRoot, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = LayoutInflater.from(getContext());
        return inflate.inflate(R.layout.media_controller, null);
    }

    private void initControllerView(View v) {
        mPauseIbtn = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseIbtn != null) {
            mPauseIbtn.requestFocus();
            mPauseIbtn.setOnClickListener(mPauseListener);
        }

        mFfwdIbtn = (ImageButton) v.findViewById(R.id.ffwd);
        if (mFfwdIbtn != null) {
            mFfwdIbtn.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdIbtn.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewIbtn = (ImageButton) v.findViewById(R.id.rew);
        if (mRewIbtn != null) {
            mRewIbtn.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewIbtn.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextIbtn = (ImageButton) v.findViewById(R.id.next);
        mPrevIbtn = (ImageButton) v.findViewById(R.id.prev);

        mSelectSetIbtn = (ImageButton) v.findViewById(R.id.select_set_ibtn);
        if (mSelectSetIbtn != null && !mFromXml) {
            mSelectSetIbtn.setOnClickListener(mSelectSetListener);
        }

        mSeekBar = (SeekBar) v.findViewById(R.id.media_controller_seek);
        if (mSeekBar != null) {
            SeekBar seeker = mSeekBar;
            seeker.setOnSeekBarChangeListener(mSeekListener);
            mSeekBar.setMax(1000);
        }

        mTimeTxt = (TextView) v.findViewById(R.id.time_txt);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void showOnce(View view) {
        mShowOnceArray.add(view);
        view.setVisibility(View.VISIBLE);
        show();
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPauseIbtn != null && !mPlayer.canPause()) {
                mPauseIbtn.setEnabled(false);
            }
            if (mRewIbtn != null && !mPlayer.canSeekBackward()) {
                mRewIbtn.setEnabled(false);
            }
            if (mFfwdIbtn != null && !mPlayer.canSeekForward()) {
                mFfwdIbtn.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseIbtn != null) {
                mPauseIbtn.requestFocus();
            }
            disableUnsupportedButtons();
            updateFloatingWindowLayout();
            mWindowManager.addView(mDecor, mDecorLayoutParams);
            mShowing = true;
        }
        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null)
            return;

        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                mWindowManager.removeView(mDecor);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
        for (View view : mShowOnceArray)
            view.setVisibility(View.GONE);
        mShowOnceArray.clear();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mTimeTxt != null)
            mTimeTxt.setText(String.format("%s/%s", stringForTime(position), stringForTime(duration)));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0); // show until hide is called
                break;
            case MotionEvent.ACTION_UP:
                show(sDefaultTimeout); // start timeout
                mPlayer.showAllBoard();
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseIbtn != null) {
                    mPauseIbtn.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return super.dispatchKeyEvent(event);
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
            mPlayer.showAllBoard();
        }
    };

    private void updatePausePlay() {
        if (mRoot != null && mPauseIbtn != null)
            updatePausePlay(mPlayer.isPlaying(), mPauseIbtn);
    }

    protected void updatePausePlay(boolean isPlaying, ImageButton pauseButton) {
        if (isPlaying) {
            pauseButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            pauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);

            mPlayer.showAllBoard();
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newPosition);
            if (mTimeTxt != null)
                mTimeTxt.setText(String.format("%s/%s",
                        stringForTime((int) newPosition), stringForTime((int) duration)));

            mPlayer.showAllBoard();
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mPlayer.showAllBoard();

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseIbtn != null) {
            mPauseIbtn.setEnabled(enabled);
        }
        if (mFfwdIbtn != null) {
            mFfwdIbtn.setEnabled(enabled);
        }
        if (mRewIbtn != null) {
            mRewIbtn.setEnabled(enabled);
        }
        if (mNextIbtn != null) {
            mNextIbtn.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevIbtn != null) {
            mPrevIbtn.setEnabled(enabled && mPrevListener != null);
        }
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LilyMediaController.class.getName());
    }

    @TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(LilyMediaController.class.getName());
    }

    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private void installPrevNextListeners() {
        if (mNextIbtn != null) {
            mNextIbtn.setOnClickListener(mNextListener);
            mNextIbtn.setEnabled(mNextListener != null);
        }

        if (mPrevIbtn != null) {
            mPrevIbtn.setOnClickListener(mPrevListener);
            mPrevIbtn.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevListener(OnClickListener prev) {
        mPrevListener = prev;
        if (mPrevIbtn != null && !mFromXml) {
            mPrevIbtn.setOnClickListener(mPrevListener);
            mPrevIbtn.setEnabled(mPrevListener != null);
            mPrevIbtn.setVisibility(View.VISIBLE);
        }
    }

    public void setNextListener(OnClickListener next) {
        mNextListener = next;
        if (mNextIbtn != null && !mFromXml) {
            mNextIbtn.setOnClickListener(mNextListener);
            mNextIbtn.setEnabled(mNextListener != null);
            mNextIbtn.setVisibility(View.VISIBLE);
        }
    }

    public void setSelectSetListener(OnClickListener selectSet) {
        mSelectSetListener = selectSet;
        if (mSelectSetIbtn != null && !mFromXml) {
            mSelectSetIbtn.setOnClickListener(mSelectSetListener);
            mSelectSetIbtn.setEnabled(true);
            mSelectSetIbtn.setVisibility(View.VISIBLE);
        }
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        /**
         * Get the audio session id for the player used by this VideoView. This can be used to
         * apply audio effects to the audio track of a video.
         *
         * @return The audio session, or 0 if there was an error.
         */
        int getAudioSessionId();

        void showAllBoard();

        void hideAllBoard();
    }
}
