package com.warmrecipes.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.Button;

/** 底部菜单栏：主页 / 预定 两个 Tab 的跳转与高亮。 */
public class Nav {
    public static void wire(Activity a, boolean planActive) {
        Button home = a.findViewById(R.id.nav_home);
        Button plan = a.findViewById(R.id.nav_plan);
        if (home == null || plan == null) return;
        int accent = colorAttr(a, android.R.attr.colorAccent);
        int secondary = colorAttr(a, android.R.attr.textColorSecondary);
        home.setTextColor(planActive ? secondary : accent);
        plan.setTextColor(planActive ? accent : secondary);
        home.setOnClickListener(v -> { if (planActive) goHome(a); });
        plan.setOnClickListener(v -> { if (!planActive) goPlan(a); });
    }

    public static void goHome(Activity a) {
        Intent i = new Intent(a, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
    }

    public static void goPlan(Activity a) {
        a.startActivity(planIntent(a));
    }

    public static Intent planIntent(Context c) {
        int phase = PlanStore.get(c).phase;
        if (phase == 2) return new Intent(c, PlanCookingActivity.class);
        if (phase == 1) return new Intent(c, PlanShoppingActivity.class);
        return new Intent(c, PlanSelectActivity.class).putExtra("initial", true);
    }

    private static int colorAttr(Context c, int res) {
        TypedValue tv = new TypedValue();
        c.getTheme().resolveAttribute(res, tv, true);
        return tv.data;
    }
}
