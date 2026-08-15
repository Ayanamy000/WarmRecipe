package com.warmrecipes.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** 购物清单页：列出所选食谱的食材，可删除（标记已有）/恢复，购买完成后进入烹饪页。 */
public class PlanShoppingActivity extends Activity {
    private LinearLayout toBuyContainer, haveContainer;
    private TextView haveSection, countView;

    @Override
    protected void onCreate(Bundle b) {
        ThemeManager.apply(this);
        super.onCreate(b);
        setContentView(R.layout.activity_plan_shopping);
        Nav.wire(this, true);

        Button back = findViewById(R.id.back);
        Button edit = findViewById(R.id.edit_btn);
        Button done = findViewById(R.id.done_btn);
        toBuyContainer = findViewById(R.id.to_buy_container);
        haveContainer = findViewById(R.id.have_container);
        haveSection = findViewById(R.id.have_section);
        countView = findViewById(R.id.count);

        back.setOnClickListener(v -> finish());
        edit.setOnClickListener(v -> {
            Intent i = new Intent(this, PlanSelectActivity.class);
            i.putExtra("initial", false);
            startActivity(i);
        });
        done.setOnClickListener(v -> {
            if (PlanStore.get(this).recipeIds.isEmpty()) {
                Toast.makeText(this, "清单为空", Toast.LENGTH_SHORT).show();
                return;
            }
            PlanStore.get(this).markCooking();
            startActivity(new Intent(this, PlanCookingActivity.class));
            finish();
        });

        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        List<PlanStore.PlanItem> items = PlanStore.get(this).items;
        toBuyContainer.removeAllViews();
        haveContainer.removeAllViews();
        int toBuy = 0;
        for (final PlanStore.PlanItem it : items) {
            View v = LayoutInflater.from(this).inflate(R.layout.item_plan_ingredient, toBuyContainer, false);
            TextView name = v.findViewById(R.id.pi_name);
            TextView amount = v.findViewById(R.id.pi_amount);
            Button act = v.findViewById(R.id.pi_action);
            name.setText(it.name);
            amount.setText(it.amount);
            act.setText(it.have ? "恢复" : "删除");
            act.setOnClickListener(x -> { PlanStore.get(this).toggleHave(it); render(); });
            if (it.have) {
                haveContainer.addView(v);
            } else {
                toBuyContainer.addView(v);
                toBuy++;
            }
        }
        int haveCount = items.size() - toBuy;
        countView.setText("共 " + items.size() + " 种食材 · 待购买 " + toBuy + " 种");
        boolean anyHave = haveCount > 0;
        haveSection.setVisibility(anyHave ? View.VISIBLE : View.GONE);
        haveContainer.setVisibility(anyHave ? View.VISIBLE : View.GONE);
    }
}
