package com.lybeat.lilyplayer.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;

import com.lybeat.lilyplayer.R;

/**
 * Author: lybeat
 * Date: 2016/4/29
 */
public class ThemeUtil {

    static final int FALLBACK_COLOR = Color.parseColor("#009688");

    private ThemeUtil() {
    }

    static boolean isAtLeastL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int resolveAccentColor(Context context) {
        Resources.Theme theme = context.getTheme();

        // on Lollipop, grab system colorAccent attribute
        // pre-Lollipop, grab AppCompat colorAccent attribute
        // finally, check for custom mp_colorAccent attribute
        int attr = isAtLeastL() ? android.R.attr.colorAccent : R.attr.colorAccent;
        TypedArray typedArray = theme.obtainStyledAttributes(new int[] { attr });

        int accentColor = typedArray.getColor(0, FALLBACK_COLOR);
        typedArray.recycle();

        return accentColor;
    }
}
