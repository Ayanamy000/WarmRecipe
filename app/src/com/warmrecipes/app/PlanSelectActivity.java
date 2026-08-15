package com.warmrecipes.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** 选择要预定的食谱（多选）；也用于「修改」增删食谱。 */
public class PlanSelectActivity extends Activity {
    private ListView list;
    private List<Recipe> recipes;

    @Override
    protected void onCreate(Bundle b) {
        ThemeManager.apply(this);
        super.onCreate(b);
        setContentView(R.layout.activity_plan_select);
        Nav.wire(this, true);

        Button back = findViewById(R.id.back);
        Button ok = findViewById(R.id.ok);
        list = findViewById(R.id.list);

        back.setOnClickListener(v -> finish());

        recipes = RecipeStore.get(this).all();
        List<String> labels = new ArrayList<>();
        for (Recipe r : recipes) labels.add(r.emoji + "  " + r.name);
        list.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, labels));
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        List<Long> current = PlanStore.get(this).recipeIds;
        for (int i = 0; i < recipes.size(); i++) {
            if (current.contains(recipes.get(i).id)) list.setItemChecked(i, true);
        }

        ok.setOnClickListener(v -> {
            ArrayList<Long> ids = new ArrayList<>();
            for (int i = 0; i < recipes.size(); i++) {
                if (list.isItemChecked(i)) ids.add(recipes.get(i).id);
            }
            if (ids.isEmpty()) {
                Toast.makeText(this, "请至少选择一道食谱", Toast.LENGTH_SHORT).show();
                return;
            }
            PlanStore.get(this).setRecipeIds(ids, getIntent().getBooleanExtra("initial", false));
            if (getIntent().getBooleanExtra("initial", false)) {
                startActivity(new Intent(this, PlanShoppingActivity.class));
            }
            finish();
        });
    }
}
