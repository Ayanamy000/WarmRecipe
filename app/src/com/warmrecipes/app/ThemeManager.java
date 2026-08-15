package com.warmrecipes.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/** 主题配色读写 + 在 Activity 里应用（必须在 super.onCreate 前调用）。 */
public class ThemeManager {
    private static final String PREFS = "warm_recipes_prefs";
    private static final String KEY_THEME = "theme";

    public static Palette current(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return Palette.byId(sp.getString(KEY_THEME, "cream"));
    }

    public static void set(Context c, String id) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THEME, id).apply();
    }

    /** 在 onCreate 的 super.onCreate() 之前调用。 */
    public static void apply(Activity a) {
        a.setTheme(current(a).themeResId);
    }
}
