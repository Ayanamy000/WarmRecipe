package com.warmrecipes.app;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** 预定页：在同一个容器内切换「选菜 / 购物 / 烹饪」三个子状态，底部固定动作条。 */
public class PlanPage {
    private final Activity a;
    private final View root;
    private final FrameLayout content;
    private final Button actionBtn, backBtn, modifyBtn;
    private final TextView title;

    private ListView selectList;
    private List<Recipe> selectRecipes;
    private boolean modifying = false;

    public PlanPage(Activity a) {
        this.a = a;
        root = LayoutInflater.from(a).inflate(R.layout.fragment_plan, null);
        content = root.findViewById(R.id.plan_content);
        actionBtn = root.findViewById(R.id.plan_action);
        backBtn = root.findViewById(R.id.plan_back);
        modifyBtn = root.findViewById(R.id.plan_modify);
        title = root.findViewById(R.id.plan_title);

        backBtn.setOnClickListener(v -> { modifying = false; refresh(); });
        modifyBtn.setOnClickListener(v -> { modifying = true; refresh(); });
        actionBtn.setOnClickListener(v -> onAction());

        refresh();
    }

    public View getView() { return root; }

    public void refresh() {
        int phase = PlanStore.get(a).phase;
        backBtn.setVisibility(modifying ? View.VISIBLE : View.GONE);
        if (modifying) {
            title.setText("修改食谱");
            actionBtn.setText("确定");
            modifyBtn.setVisibility(View.GONE);
            renderSelect();
        } else if (phase == 2) {
            title.setText("开始烹饪");
            actionBtn.setText("制作完成");
            modifyBtn.setVisibility(View.VISIBLE);
            renderCooking();
        } else if (phase == 1) {
            title.setText("购物清单");
            actionBtn.setText("购买完成");
            modifyBtn.setVisibility(View.VISIBLE);
            renderShopping();
        } else {
            title.setText("选择食谱");
            actionBtn.setText("确定");
            modifyBtn.setVisibility(View.GONE);
            renderSelect();
        }
    }

    private void onAction() {
        if (modifying) {
            if (applySelection(false)) {
                modifying = false;
                refresh();
            }
            return;
        }
        int phase = PlanStore.get(a).phase;
        if (phase == 2) {
            PlanStore.get(a).clear();
            Toast.makeText(a, "全部完成，清单已清空 🎉", Toast.LENGTH_SHORT).show();
            refresh();
        } else if (phase == 1) {
            PlanStore.get(a).markCooking();
            refresh();
        } else {
            if (applySelection(true)) refresh();
        }
    }

    private boolean applySelection(boolean start) {
        if (selectList == null || selectRecipes == null) return false;
        ArrayList<Long> ids = new ArrayList<>();
        for (int i = 0; i < selectRecipes.size(); i++) {
            if (selectList.isItemChecked(i)) ids.add(selectRecipes.get(i).id);
        }
        if (ids.isEmpty()) {
            Toast.makeText(a, "请至少选择一道食谱", Toast.LENGTH_SHORT).show();
            return false;
        }
        PlanStore.get(a).setRecipeIds(ids, start);
        return true;
    }

    private void renderSelect() {
        content.removeAllViews();
        View v = LayoutInflater.from(a).inflate(R.layout.plan_select, content, false);
        content.addView(v);
        selectList = v.findViewById(R.id.select_list);
        selectRecipes = RecipeStore.get(a).all();
        List<String> labels = new ArrayList<>();
        for (Recipe r : selectRecipes) labels.add(r.emoji + "  " + r.name);
        selectList.setAdapter(new ArrayAdapter<>(a, android.R.layout.simple_list_item_multiple_choice, labels));
        selectList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        List<Long> current = PlanStore.get(a).recipeIds;
        for (int i = 0; i < selectRecipes.size(); i++) {
            if (current.contains(selectRecipes.get(i).id)) selectList.setItemChecked(i, true);
        }
    }

    private void renderShopping() {
        content.removeAllViews();
        View v = LayoutInflater.from(a).inflate(R.layout.plan_shopping, content, false);
        content.addView(v);
        LinearLayout toBuy = v.findViewById(R.id.to_buy_container);
        LinearLayout have = v.findViewById(R.id.have_container);
        TextView haveSection = v.findViewById(R.id.have_section);
        TextView countView = v.findViewById(R.id.shop_count);

        List<PlanStore.PlanItem> items = PlanStore.get(a).items;
        toBuy.removeAllViews();
        have.removeAllViews();
        int toBuyCount = 0;
        for (final PlanStore.PlanItem it : items) {
            View row = LayoutInflater.from(a).inflate(R.layout.item_plan_ingredient, toBuy, false);
            TextView name = row.findViewById(R.id.pi_name);
            TextView amount = row.findViewById(R.id.pi_amount);
            Button act = row.findViewById(R.id.pi_action);
            name.setText(it.name);
            amount.setText(it.amount);
            act.setText(it.have ? "恢复" : "删除");
            act.setOnClickListener(x -> { PlanStore.get(a).toggleHave(it); renderShopping(); });
            if (it.have) {
                have.addView(row);
            } else {
                toBuy.addView(row);
                toBuyCount++;
            }
        }
        int haveCount = items.size() - toBuyCount;
        countView.setText("共 " + items.size() + " 种食材 · 待购买 " + toBuyCount + " 种");
        boolean anyHave = haveCount > 0;
        haveSection.setVisibility(anyHave ? View.VISIBLE : View.GONE);
        have.setVisibility(anyHave ? View.VISIBLE : View.GONE);
    }

    private void renderCooking() {
        content.removeAllViews();
        View v = LayoutInflater.from(a).inflate(R.layout.plan_cooking, content, false);
        content.addView(v);
        ListView list = v.findViewById(R.id.cooking_list);
        list.setAdapter(new RecipeAdapter(PlanStore.get(a).selectedRecipes()));
        list.setOnItemClickListener((p, view, pos, id) -> {
            Recipe r = (Recipe) p.getItemAtPosition(pos);
            Intent i = new Intent(a, DetailActivity.class);
            i.putExtra("id", r.id);
            a.startActivity(i);
        });
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
            if (v == null) v = LayoutInflater.from(a).inflate(R.layout.item_recipe, parent, false);
            Recipe r = data.get(i);
            ((TextView) v.findViewById(R.id.emoji)).setText(r.emoji);
            ((TextView) v.findViewById(R.id.name)).setText(r.name);
            ((TextView) v.findViewById(R.id.subtitle)).setText(r.subtitle());
            ((TextView) v.findViewById(R.id.star)).setText(r.favorite ? "★" : "");
            return v;
        }
    }
}
