package com.warmrecipes.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private ListView list;
    private EditText searchInput;
    private LinearLayout chipContainer;
    private RecipeAdapter adapter;
    private String selectedCategory = null; // null = 全部
    private String appliedThemeId;
    private static final int REQ_EXPORT = 1001;
    private static final int REQ_IMPORT = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appliedThemeId = ThemeManager.current(this).id;

        TextView title = findViewById(R.id.title);
        title.setText(R.string.app_name);
        Button themeBtn = findViewById(R.id.btn_theme);
        themeBtn.setOnClickListener(v -> startActivity(new Intent(this, ThemeActivity.class)));
        Button moreBtn = findViewById(R.id.btn_more);
        moreBtn.setOnClickListener(v -> showMoreMenu());

        searchInput = findViewById(R.id.search_input);
        chipContainer = findViewById(R.id.chip_container);
        list = findViewById(R.id.list);
        TextView emptyView = findViewById(R.id.empty_view);
        Button fab = findViewById(R.id.fab_add);

        adapter = new RecipeAdapter();
        list.setAdapter(adapter);
        list.setEmptyView(emptyView);

        buildChips();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { refresh(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, EditActivity.class)));

        list.setOnItemClickListener((parent, view, position, id) -> {
            Recipe r = (Recipe) parent.getItemAtPosition(position);
            Intent i = new Intent(this, DetailActivity.class);
            i.putExtra("id", r.id);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appliedThemeId != null && !appliedThemeId.equals(ThemeManager.current(this).id)) {
            recreate();
            return;
        }
        refresh();
    }

    private void showMoreMenu() {
        new AlertDialog.Builder(this)
                .setTitle("导入 / 备份")
                .setItems(new String[]{"导出全部备份", "导入食谱"}, (d, which) -> {
                    if (which == 0) exportBackup();
                    else importRecipes();
                })
                .show();
    }

    private void exportBackup() {
        String json = RecipeStore.get(this).exportAllJson();
        if (json == null || json.isEmpty()) {
            Toast.makeText(this, "还没有可导出的食谱", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_TITLE, "recipes-backup.json");
        startActivityForResult(i, REQ_EXPORT);
    }

    private void importRecipes() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, REQ_IMPORT);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (req == REQ_EXPORT) {
            try {
                String json = RecipeStore.get(this).exportAllJson();
                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os == null) throw new RuntimeException("null stream");
                os.write(json.getBytes("UTF-8"));
                os.close();
                Toast.makeText(this, "备份已导出", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
            }
        } else if (req == REQ_IMPORT) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if (is == null) throw new RuntimeException("null stream");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                is.close();
                String json = new String(baos.toByteArray(), "UTF-8");
                int count = RecipeStore.get(this).importJson(json);
                Toast.makeText(this, count > 0 ? "已导入 " + count + " 条食谱" : "未识别到食谱",
                        Toast.LENGTH_SHORT).show();
                refresh();
            } catch (Exception e) {
                Toast.makeText(this, "导入失败，请选择由本应用导出的 JSON 文件", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void buildChips() {
        addChip("全部", null);
        for (String c : Recipe.CATEGORIES) addChip(c, c);
        updateChips();
    }

    private void addChip(String label, String value) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setBackgroundResource(R.drawable.bg_chip);
        b.setTag(value);
        b.setOnClickListener(v -> { selectedCategory = (String) v.getTag(); updateChips(); refresh(); });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = dp(4);
        lp.setMargins(m, m, m, m);
        b.setPadding(dp(14), dp(6), dp(14), dp(6));
        chipContainer.addView(b, lp);
    }

    private void updateChips() {
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            Button b = (Button) chipContainer.getChildAt(i);
            boolean sel = b.getTag() == null ? selectedCategory == null
                    : b.getTag().equals(selectedCategory);
            b.setSelected(sel);
            b.setTextColor(sel ? colorAttr(R.attr.colorOnAccent)
                    : colorAttr(android.R.attr.textColorSecondary));
        }
    }

    private void refresh() {
        String q = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());
        List<Recipe> all = RecipeStore.get(this).all();
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : all) {
            if (selectedCategory != null && !selectedCategory.equals(r.category)) continue;
            if (!q.isEmpty()
                    && !r.name.toLowerCase(Locale.getDefault()).contains(q)
                    && !containsIngredient(r, q)) continue;
            out.add(r);
        }
        adapter.setData(out);
    }

    private boolean containsIngredient(Recipe r, String q) {
        for (Recipe.Ingredient ing : r.ingredients) {
            if (ing.name.toLowerCase(Locale.getDefault()).contains(q)) return true;
        }
        return false;
    }

    private int colorAttr(int res) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(res, tv, true);
        return tv.data;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private class RecipeAdapter extends BaseAdapter {
        private List<Recipe> data = new ArrayList<>();

        void setData(List<Recipe> d) {
            data = d;
            notifyDataSetChanged();
        }

        @Override public int getCount() { return data.size(); }
        @Override public Object getItem(int i) { return data.get(i); }
        @Override public long getItemId(int i) { return data.get(i).id; }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_recipe, parent, false);
            }
            Recipe r = data.get(i);
            ((TextView) v.findViewById(R.id.emoji)).setText(r.emoji);
            ((TextView) v.findViewById(R.id.name)).setText(r.name);
            String total = r.totalLabel();
            String meta = r.category + " · " + r.steps.size() + " 步"
                    + (total.isEmpty() ? "" : " · " + total);
            ((TextView) v.findViewById(R.id.subtitle)).setText(meta);
            ((TextView) v.findViewById(R.id.star)).setText(r.favorite ? "★" : "");
            return v;
        }
    }
}
