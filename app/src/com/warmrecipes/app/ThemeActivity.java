package com.warmrecipes.app;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThemeActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        ThemeManager.apply(this);
        super.onCreate(b);
        setContentView(R.layout.activity_theme);

        TextView title = findViewById(R.id.title);
        Button back = findViewById(R.id.back);
        LinearLayout container = findViewById(R.id.theme_container);
        title.setText("主题配色");
        back.setOnClickListener(v -> finish());

        String current = ThemeManager.current(this).id;
        for (Palette p : Palette.ALL) {
            container.addView(buildRow(p, p.id.equals(current)));
        }
    }

    private View buildRow(Palette p, boolean selected) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(lp);

        TextView swatch = new TextView(this);
        swatch.setBackground(makeOval(p.accent));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(30), dp(30));
        sp.setMargins(dp(2), 0, dp(16), 0);
        row.addView(swatch, sp);

        TextView name = new TextView(this);
        name.setText(p.name);
        name.setTextSize(16);
        name.setTextColor(colorAttr(android.R.attr.textColorPrimary));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(name, np);

        TextView check = new TextView(this);
        check.setText(selected ? "✓" : "");
        check.setTextSize(18);
        check.setTextColor(p.accent);
        row.addView(check);

        row.setOnClickListener(v -> {
            ThemeManager.set(this, p.id);
            recreate();
        });
        return row;
    }

    private GradientDrawable makeOval(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    private int colorAttr(int res) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(res, tv, true);
        return tv.data;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
