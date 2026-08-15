package com.warmrecipes.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailActivity extends Activity {
    private Recipe recipe;
    private TextView starView;
    private LinearLayout ingContainer, stepContainer;
    private TextView notesSection, notesText;

    @Override
    protected void onCreate(Bundle b) {
        ThemeManager.apply(this);
        super.onCreate(b);
        setContentView(R.layout.activity_detail);

        long id = getIntent().getLongExtra("id", 0);
        recipe = RecipeStore.get(this).byId(id);
        if (recipe == null) { finish(); return; }

        Button back = findViewById(R.id.back);
        TextView title = findViewById(R.id.title);
        TextView emoji = findViewById(R.id.emoji);
        TextView name = findViewById(R.id.name);
        TextView meta = findViewById(R.id.meta);
        TextView time = findViewById(R.id.time);
        starView = findViewById(R.id.star);
        Button editBtn = findViewById(R.id.edit_btn);
        Button shareBtn = findViewById(R.id.share_btn);
        Button deleteBtn = findViewById(R.id.delete_btn);
        TextView ingTitle = findViewById(R.id.ing_title);
        TextView stepTitle = findViewById(R.id.step_title);
        ingContainer = findViewById(R.id.ing_container);
        stepContainer = findViewById(R.id.step_container);
        notesSection = findViewById(R.id.notes_section);
        notesText = findViewById(R.id.notes_text);

        back.setOnClickListener(v -> finish());
        title.setText(recipe.name);
        emoji.setText(recipe.emoji);
        name.setText(recipe.name);
        meta.setText(recipe.category.isEmpty() ? recipe.steps.size() + " 步"
                : recipe.category + " · " + recipe.steps.size() + " 步");
        String t = recipe.totalLabel();
        time.setText(t.isEmpty() ? "时长：—" : "总时长：" + t);
        ingTitle.setText("食材与调料（" + recipe.ingredients.size() + "）");
        stepTitle.setText("步骤（" + recipe.steps.size() + "）");

        renderFavorite();
        starView.setOnClickListener(v -> {
            RecipeStore.get(this).toggleFavorite(recipe.id);
            recipe.favorite = !recipe.favorite;
            renderFavorite();
        });

        editBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, EditActivity.class);
            i.putExtra("id", recipe.id);
            startActivity(i);
        });

        shareBtn.setOnClickListener(v -> shareRecipe());

        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("删除食谱")
                .setMessage("确定要删除「" + recipe.name + "」吗？此操作无法撤销。")
                .setPositiveButton("删除", (d, w) -> {
                    RecipeStore.get(this).delete(recipe.id);
                    finish();
                })
                .setNegativeButton("取消", null)
                .show());

        renderIngredients();
        renderSteps();
        renderNotes();
    }

    private void shareRecipe() {
        String text = recipe.toShareText();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, recipe.name);
        send.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, "分享食谱"));
    }

    private void renderFavorite() {
        starView.setText(recipe.favorite ? "★" : "☆");
        starView.setTextColor(recipe.favorite ? 0xFFF2B33D
                : colorAttr(android.R.attr.textColorSecondary));
    }

    private void renderIngredients() {
        ingContainer.removeAllViews();
        for (Recipe.Ingredient x : recipe.ingredients) {
            View v = LayoutInflater.from(this).inflate(R.layout.item_ingredient, ingContainer, false);
            ((TextView) v.findViewById(R.id.ing_name)).setText(x.name);
            ((TextView) v.findViewById(R.id.ing_amount)).setText(x.amount);
            ingContainer.addView(v);
        }
    }

    private void renderSteps() {
        stepContainer.removeAllViews();
        for (int i = 0; i < recipe.steps.size(); i++) {
            Recipe.Step s = recipe.steps.get(i);
            View v = LayoutInflater.from(this).inflate(R.layout.item_step, stepContainer, false);
            ((TextView) v.findViewById(R.id.step_num)).setText(String.valueOf(i + 1));
            ((TextView) v.findViewById(R.id.step_text)).setText(s.instruction);
            TextView tt = v.findViewById(R.id.step_time);
            String d = s.duration == null ? "" : s.duration.trim();
            tt.setText(d.isEmpty() ? "" : "⏱ " + d);
            tt.setVisibility(d.isEmpty() ? View.GONE : View.VISIBLE);
            stepContainer.addView(v);
        }
    }

    private void renderNotes() {
        String n = recipe.notes == null ? "" : recipe.notes.trim();
        if (n.isEmpty()) {
            notesSection.setVisibility(View.GONE);
            notesText.setVisibility(View.GONE);
        } else {
            notesSection.setVisibility(View.VISIBLE);
            notesText.setVisibility(View.VISIBLE);
            notesText.setText(n);
        }
    }

    private int colorAttr(int res) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(res, tv, true);
        return tv.data;
    }
}
