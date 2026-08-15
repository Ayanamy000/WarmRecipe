package com.warmrecipes.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class EditActivity extends Activity {
    private static final String[] EMOJIS = {
        "🥘", "🍲", "🍥", "🍤", "🍖", "🥗", "🥣", "🍚", "🍞", "🍰",
        "🥟", "🍹", "🍳", "🍜", "🍱", "🍝", "🍮", "🍛", "🍕", "🍵", "🍪", "🥩"
    };

    private long id;
    private boolean editing = false;
    private EditText nameInput, notesInput;
    private LinearLayout categoryContainer, emojiContainer, ingContainer, stepContainer;
    private String category = "";
    private String emoji = "🍽️";

    private String pendingCatName = "";
    private String pendingCatEmoji = "🍽️";
    private int pendingCatMode = 0; // 0=添加 1=换图标
    private AlertDialog catDialog;
    private static final int REQ_CAT_IMAGE = 2001;

    @Override
    protected void onCreate(Bundle b) {
        ThemeManager.apply(this);
        super.onCreate(b);
        setContentView(R.layout.activity_edit);

        id = getIntent().getLongExtra("id", 0);

        Button back = findViewById(R.id.back);
        TextView title = findViewById(R.id.title);
        nameInput = findViewById(R.id.name_input);
        notesInput = findViewById(R.id.notes_input);
        categoryContainer = findViewById(R.id.category_container);
        emojiContainer = findViewById(R.id.emoji_container);
        ingContainer = findViewById(R.id.ing_container);
        stepContainer = findViewById(R.id.step_container);
        Button addIng = findViewById(R.id.add_ing);
        Button addStep = findViewById(R.id.add_step);
        Button save = findViewById(R.id.save);

        back.setOnClickListener(v -> finish());
        addIng.setOnClickListener(v -> addIngredientRow(null, null));
        addStep.setOnClickListener(v -> addStepRow(null, null));
        save.setOnClickListener(v -> doSave());

        buildCategories();
        buildEmojis();

        if (id != 0) {
            Recipe r = RecipeStore.get(this).byId(id);
            if (r != null) {
                editing = true;
                title.setText("编辑食谱");
                nameInput.setText(r.name);
                notesInput.setText(r.notes);
                category = r.category;
                emoji = r.emoji;
                for (Recipe.Ingredient x : r.ingredients) addIngredientRow(x.name, x.amount);
                for (Recipe.Step s : r.steps) addStepRow(s.instruction, s.duration);
                updateCategoryChips();
                updateEmoji();
            }
        } else {
            title.setText("新建食谱");
            List<CategoryStore.Category> cats = CategoryStore.get(this).all();
            category = cats.isEmpty() ? "" : cats.get(0).name;
            CategoryStore.Category first = CategoryStore.get(this).byName(category);
            emoji = (first != null && !first.emoji.isEmpty()) ? first.emoji : "🍽️";
            addIngredientRow(null, null);
            addStepRow(null, null);
            updateCategoryChips();
            updateEmoji();
        }
    }

    private void buildCategories() {
        categoryContainer.removeAllViews();
        for (CategoryStore.Category c : CategoryStore.get(this).all()) {
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setBackgroundResource(R.drawable.bg_chip);
            b.setTag(c.name);
            CategoryUi.styleChip(this, b, c, dp(24));
            b.setOnClickListener(v -> {
                category = (String) v.getTag();
                CategoryStore.Category cat = CategoryStore.get(this).byName(category);
                emoji = (cat != null && !cat.emoji.isEmpty()) ? cat.emoji : "🍽️";
                updateCategoryChips();
                updateEmoji();
            });
            b.setOnLongClickListener(v -> {
                promptCategoryMenu((String) v.getTag());
                return true;
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int m = dp(4);
            lp.setMargins(m, m, m, m);
            b.setPadding(dp(12), dp(6), dp(12), dp(6));
            categoryContainer.addView(b, lp);
        }
        Button add = new Button(this);
        add.setText("＋ 添加品类");
        add.setAllCaps(false);
        add.setBackgroundResource(R.drawable.bg_chip);
        add.setTextColor(colorAttr(android.R.attr.colorAccent));
        add.setOnClickListener(v -> promptAddCategory());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = dp(4);
        lp.setMargins(m, m, m, m);
        add.setPadding(dp(14), dp(6), dp(14), dp(6));
        categoryContainer.addView(add, lp);
        updateCategoryChips();
    }

    private void promptCategoryMenu(String name) {
        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(new String[]{"改名", "换图标", "删除"}, (d, which) -> {
                    if (which == 0) promptRenameCategory(name);
                    else if (which == 1) promptChangeIcon(name);
                    else promptDeleteCategory(name);
                })
                .show();
    }

    private void promptRenameCategory(String name) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(24), dp(12), dp(24), dp(4));
        EditText input = new EditText(this);
        input.setText(name);
        input.setSingleLine(true);
        input.setBackgroundResource(R.drawable.bg_input);
        input.setPadding(dp(14), dp(10), dp(14), dp(10));
        wrap.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(this)
                .setTitle("改名")
                .setView(wrap)
                .setPositiveButton("确定", (d, w) -> {
                    String nn = input.getText().toString().trim();
                    if (nn.isEmpty() || nn.equals(name)) return;
                    CategoryStore.get(this).rename(name, nn);
                    if (name.equals(category)) category = nn;
                    buildCategories();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void promptAddCategory() {
        pendingCatName = "";
        pendingCatEmoji = "🍽️";
        pendingCatMode = 0;
        showIconPanel("添加品类");
    }

    private void promptChangeIcon(String name) {
        pendingCatName = name;
        CategoryStore.Category c = CategoryStore.get(this).byName(name);
        pendingCatEmoji = (c != null && !c.emoji.isEmpty()) ? c.emoji : "🍽️";
        pendingCatMode = 1;
        showIconPanel("换图标");
    }

    private void showIconPanel(String dialogTitle) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(8), dp(24), dp(4));

        final EditText nameInput2 = new EditText(this);
        nameInput2.setHint("品类名称，如：火锅");
        nameInput2.setSingleLine(true);
        nameInput2.setBackgroundResource(R.drawable.bg_input);
        nameInput2.setPadding(dp(14), dp(10), dp(14), dp(10));
        if (pendingCatMode == 0) {
            nameInput2.setText(pendingCatName);
            panel.addView(nameInput2, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        TextView label = new TextView(this);
        label.setText("选择图标");
        label.setTextSize(13);
        label.setTextColor(colorAttr(android.R.attr.textColorSecondary));
        label.setPadding(0, dp(12), 0, dp(4));
        panel.addView(label);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        final LinearLayout emojiRow = buildEmojiRow();
        hsv.addView(emojiRow);
        panel.addView(hsv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button album = new Button(this);
        album.setText("📷 从相册选择图片");
        album.setAllCaps(false);
        album.setBackgroundResource(R.drawable.bg_button_outline);
        album.setTextColor(colorAttr(android.R.attr.colorAccent));
        album.setStateListAnimator(null);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alp.setMargins(0, dp(12), 0, 0);
        panel.addView(album, alp);

        album.setOnClickListener(v -> {
            if (pendingCatMode == 0) pendingCatName = nameInput2.getText().toString().trim();
            if (catDialog != null) catDialog.dismiss();
            launchAlbumPicker();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(dialogTitle)
                .setView(panel)
                .setNegativeButton("取消", null);
        if (pendingCatMode == 0) {
            builder.setPositiveButton("添加", (d, w) -> {
                String nm = nameInput2.getText().toString().trim();
                if (nm.isEmpty()) {
                    Toast.makeText(this, "请输入品类名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                CategoryStore.get(this).add(nm, pendingCatEmoji, "");
                buildCategories();
            });
        } else {
            builder.setPositiveButton("确定", (d, w) -> {
                CategoryStore.get(this).setEmoji(pendingCatName, pendingCatEmoji);
                buildCategories();
            });
        }
        catDialog = builder.show();
    }

    private LinearLayout buildEmojiRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (String e : EMOJIS) {
            TextView t = new TextView(this);
            t.setText(e);
            t.setTextSize(22);
            t.setTag(e);
            t.setPadding(dp(8), dp(4), dp(8), dp(4));
            t.setOnClickListener(v -> { pendingCatEmoji = (String) v.getTag(); highlightEmoji(row); });
            row.addView(t);
        }
        highlightEmoji(row);
        return row;
    }

    private void highlightEmoji(LinearLayout row) {
        for (int i = 0; i < row.getChildCount(); i++) {
            View v = row.getChildAt(i);
            v.setSelected(v.getTag() != null && v.getTag().equals(pendingCatEmoji));
        }
    }

    private void launchAlbumPicker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(i, REQ_CAT_IMAGE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_CAT_IMAGE || res != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            File dest = CategoryStore.get(this).newIconFile();
            InputStream is = getContentResolver().openInputStream(uri);
            OutputStream os = new FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) os.write(buf, 0, n);
            is.close();
            os.close();
            if (pendingCatMode == 0) {
                String nm = pendingCatName.isEmpty() ? "新品类" : pendingCatName;
                CategoryStore.get(this).add(nm, pendingCatEmoji, dest.getAbsolutePath());
            } else {
                CategoryStore.get(this).setImage(pendingCatName, dest.getAbsolutePath());
            }
            buildCategories();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "图片导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptDeleteCategory(String name) {
        new AlertDialog.Builder(this)
                .setTitle("删除品类")
                .setMessage("确定删除「" + name + "」吗？已有食谱不受影响。")
                .setPositiveButton("删除", (d, w) -> {
                    CategoryStore.get(this).remove(name);
                    if (name.equals(category)) category = "";
                    buildCategories();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateCategoryChips() {
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            Button b = (Button) categoryContainer.getChildAt(i);
            if (b.getTag() == null) continue; // 「＋ 添加品类」
            boolean sel = category.equals(b.getTag());
            b.setSelected(sel);
            b.setTextColor(sel ? colorAttr(R.attr.colorOnAccent)
                    : colorAttr(android.R.attr.textColorSecondary));
        }
    }

    private void buildEmojis() {
        for (String e : EMOJIS) {
            TextView t = new TextView(this);
            t.setText(e);
            t.setTextSize(22);
            t.setTag(e);
            t.setPadding(dp(8), dp(4), dp(8), dp(4));
            t.setOnClickListener(v -> { emoji = (String) v.getTag(); updateEmoji(); });
            emojiContainer.addView(t);
        }
        updateEmoji();
    }

    private void updateEmoji() {
        for (int i = 0; i < emojiContainer.getChildCount(); i++) {
            View v = emojiContainer.getChildAt(i);
            v.setSelected(v.getTag() != null && v.getTag().equals(emoji));
        }
    }

    private void addIngredientRow(String name, String amount) {
        View v = LayoutInflater.from(this).inflate(R.layout.item_ingredient_edit, ingContainer, false);
        EditText n = v.findViewById(R.id.ing_name_input);
        EditText a = v.findViewById(R.id.ing_amount_input);
        Button rm = v.findViewById(R.id.ing_remove);
        if (name != null) n.setText(name);
        if (amount != null) a.setText(amount);
        rm.setOnClickListener(x -> ingContainer.removeView(v));
        ingContainer.addView(v);
    }

    private void addStepRow(String instruction, String duration) {
        View v = LayoutInflater.from(this).inflate(R.layout.item_step_edit, stepContainer, false);
        EditText ins = v.findViewById(R.id.step_text_input);
        EditText dur = v.findViewById(R.id.step_time_input);
        Button rm = v.findViewById(R.id.step_remove);
        if (instruction != null) ins.setText(instruction);
        if (duration != null) dur.setText(duration);
        rm.setOnClickListener(x -> { stepContainer.removeView(v); renumberSteps(); });
        stepContainer.addView(v);
        renumberSteps();
    }

    private void renumberSteps() {
        for (int i = 0; i < stepContainer.getChildCount(); i++) {
            View v = stepContainer.getChildAt(i);
            ((TextView) v.findViewById(R.id.step_label)).setText("步骤 " + (i + 1));
        }
    }

    private void doSave() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "请填写食谱名称", Toast.LENGTH_SHORT).show();
            return;
        }
        Recipe r = editing ? RecipeStore.get(this).byId(id) : new Recipe();
        if (r == null) r = new Recipe();
        r.name = name;
        r.category = category;
        r.emoji = emoji;
        r.notes = notesInput.getText().toString().trim();

        r.ingredients.clear();
        for (int i = 0; i < ingContainer.getChildCount(); i++) {
            View v = ingContainer.getChildAt(i);
            String nm = ((EditText) v.findViewById(R.id.ing_name_input)).getText().toString().trim();
            String am = ((EditText) v.findViewById(R.id.ing_amount_input)).getText().toString().trim();
            if (!nm.isEmpty() || !am.isEmpty()) {
                Recipe.Ingredient x = new Recipe.Ingredient();
                x.name = nm;
                x.amount = am;
                r.ingredients.add(x);
            }
        }

        r.steps.clear();
        for (int i = 0; i < stepContainer.getChildCount(); i++) {
            View v = stepContainer.getChildAt(i);
            String ins = ((EditText) v.findViewById(R.id.step_text_input)).getText().toString().trim();
            String dur = ((EditText) v.findViewById(R.id.step_time_input)).getText().toString().trim();
            if (!ins.isEmpty() || !dur.isEmpty()) {
                Recipe.Step s = new Recipe.Step();
                s.instruction = ins;
                s.duration = dur;
                r.steps.add(s);
            }
        }

        RecipeStore.get(this).save(r);
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
        finish();
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
