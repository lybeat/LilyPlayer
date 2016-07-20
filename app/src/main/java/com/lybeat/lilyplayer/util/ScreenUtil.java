package com.lybeat.lilyplayer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ScreenUtil {

	private ScreenUtil() {
		throw new UnsupportedOperationException("Cannot be instantiated");
	}

	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		return outMetrics.widthPixels;
	}

	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		return outMetrics.heightPixels;
	}

	public static int getStatusBarHeight(Context context) {
		int statusHeight = -1;

		try {
			Class<?> cls = Class.forName("com.android.internal.R$dimen");
			Object object = cls.newInstance();
			int height = Integer.parseInt(cls.getField("status_bar_height")
					.get(object).toString());
			statusHeight = context.getResources().getDimensionPixelSize(height);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return statusHeight;
	}

	public static int getStatusBarHeight(Activity activity) {
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return frame.top;
	}

    /**
     * 获取状态栏高度＋标题栏(ActionBar)高度
     * @param activity
     * @return
     */
    public static int getTopBarHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }

	/**
	 * 获取当前屏幕截图，包含状态栏
	 * @param activity
	 * @return
	 */
	public static Bitmap snapshotWithStatusBar(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmpCache = view.getDrawingCache();

		int width = getScreenWidth(activity);
		int height = getScreenHeight(activity);
		Bitmap bmp = Bitmap.createBitmap(bmpCache, 0, 0, width, height);
		view.destroyDrawingCache();

		return bmp;
	}

	/**
	 * 获取当前屏幕截图，不包含状态栏
	 * @param activity
	 * @return
	 */
	public static Bitmap snapshotWithoutStatusBar(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmpCache = view.getDrawingCache();

		int statusBarHeight = getStatusBarHeight(activity);

		int width = getScreenWidth(activity);
		int height = getScreenHeight(activity);
		Bitmap bmp = Bitmap.createBitmap(bmpCache, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();

		return bmp;
	}

	public static float getScreenBrightness(Activity activity) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		return lp.screenBrightness;
	}

	public static void setScreenBrightness(Activity activity, float brightness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = brightness;
		activity.getWindow().setAttributes(lp);
	}

}
