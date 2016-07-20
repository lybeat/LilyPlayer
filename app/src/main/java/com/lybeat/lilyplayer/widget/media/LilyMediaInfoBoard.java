package com.lybeat.lilyplayer.widget.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lybeat.lilyplayer.R;
import com.lybeat.lilyplayer.util.DateUtil;

/**
 * Author: lybeat
 * Date: 2016/7/15
 */
public class LilyMediaInfoBoard extends FrameLayout implements IMediaInfoBoard {

    private static final String TAG = "LilyMediaInfoBoard";

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;

    private MediaInfoControl mMediaInfoController;
    private Context mContext;
    private View mAnchor;
    private View mRoot;
    private WindowManager mWindowManager;
    private Window mWindow;
    private View mDecor;
    private WindowManager.LayoutParams mDecorLayoutParams;
    private TextView mMediaTitleTxt;
    private TextView mPhoneTimeTxt;
    private ImageButton mMediaInfoIbtn;

    private boolean mShowing;

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initInfoBoardView(mRoot);
    }

    public LilyMediaInfoBoard(Context context) {
        super(context);
        this.mContext = context;
        initFloatingWindowLayout();
        initFloatingWindow();
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        p.y = anchorPos[1] + mAnchor.getHeight() - mAnchor.getMeasuredHeight();
    }

    private OnLayoutChangeListener mLayoutChangeListener =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ?
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

    @Override
    public void setAnchorView(View view) {
        if (mAnchor != null) {
            mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mAnchor = view;
        if (mAnchor != null) {
            mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeInfoBoardView();
        addView(v, frameParams);
    }

    private View makeInfoBoardView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_info_board, null);

        initInfoBoardView(mRoot);

        return mRoot;
    }

    private void initInfoBoardView(View mRoot) {
        mMediaTitleTxt = (TextView) mRoot.findViewById(R.id.media_title_txt);
        mPhoneTimeTxt = (TextView) mRoot.findViewById(R.id.phone_time_txt);
        mMediaInfoIbtn = (ImageButton) mRoot.findViewById(R.id.media_info_ib);

        showPhoneInfo();
        mMediaInfoIbtn.setOnClickListener(mediaInfoListener);
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            updateFloatingWindowLayout();
            mWindowManager.addView(mDecor, mDecorLayoutParams);
            mShowing = true;
        }

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            Message msg = mHandler.obtainMessage(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void hide() {
        if (mAnchor == null)
            return;

        if (mShowing) {
            try {
                mWindowManager.removeView(mDecor);
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "already removed");
            }
            mShowing = false;
        }

        mMediaInfoController.hideNavigation();
    }

    public void setMediaInfoController(MediaInfoControl controller) {
        mMediaInfoController = controller;
    }

    public void showMediaTitle(String title) {
        mMediaTitleTxt.setText(title);
    }

    public void showPhoneInfo() {
        mPhoneTimeTxt.setText(DateUtil.getCurrentTime("HH:mm"));
    }

    public void showMediaInfo() {
        mMediaInfoController.showMediaInfo();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0); // show until hide is called
                break;
            case MotionEvent.ACTION_UP:
                show(sDefaultTimeout); // start timeout
                mMediaInfoController.showAllBoard();
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown) {
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown) {
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
                mMediaInfoController.hideAllBoard();
            }
            return super.dispatchKeyEvent(event);
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mediaInfoListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            showMediaInfo();
            mMediaInfoController.showAllBoard();
        }
    };

    public interface MediaInfoControl {
        void showMediaTitle(String title);

        void showPhoneInfo();

        void showMediaInfo();

        void showAllBoard();

        void hideAllBoard();

        void hideNavigation();
    }
}
