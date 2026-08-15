package com.warmrecipes.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** 烹饪页：只显示所选定的菜谱，点任意菜谱看做法；制作完成后清空全部内容。 */
public class PlanCookingActivity extends Activity {
    private ListView list;

    @Override
    protected void onCreate(Bundle b) {
        ThemeManager.apply(this);
        super.onCreate(b);
        setContentView(R.layout.activity_plan_cooking);
        Nav.wire(this, true);

        Button back = findViewById(R.id.back);
        Button edit = findViewById(R.id.edit_btn);
        Button done = findViewById(R.id.done_btn);
        list = findViewById(R.id.list);

        back.setOnClickListener(v -> finish());
        edit.setOnClickListener(v -> {
            Intent i = new Intent(this, PlanSelectActivity.class);
            i.putExtra("initial", false);
            startActivity(i);
        });
        done.setOnClickListener(v -> {
            PlanStore.get(this).clear();
            Toast.makeText(this, "全部完成，清单已清空 🎉", Toast.LENGTH_SHORT).show();
            finish();
        });

        list.setOnItemClickListener((p, v, pos, id) -> {
            Recipe r = (Recipe) p.getItemAtPosition(pos);
            Intent i = new Intent(this, DetailActivity.class);
            i.putExtra("id", r.id);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        list.setAdapter(new RecipeAdapter(PlanStore.get(this).selectedRecipes()));
    }

    private class RecipeAdapter extends BaseAdapter {
        private final List<Recipe> data;

        RecipeAdapter(List<Recipe> d) { data = d; }
        @Override public int getCount() { return data.size(); }
        @Override public Object getItem(int i) { return data.get(i); }
        @Override public long getItemId(int i) { return data.get(i).id; }

        @Override
        public View getView(int i, View cv, ViewGroup parent) {
            View v = cv;
            if (v == null) {
                v = LayoutInflater.from(PlanCookingActivity.this)
                        .inflate(R.layout.item_recipe, parent, false);
            }
            Recipe r = data.get(i);
            ((TextView) v.findViewById(R.id.emoji)).setText(r.emoji);
            ((TextView) v.findViewById(R.id.name)).setText(r.name);
            ((TextView) v.findViewById(R.id.subtitle)).setText(r.subtitle());
            ((TextView) v.findViewById(R.id.star)).setText(r.favorite ? "★" : "");
            return v;
        }
    }
}
