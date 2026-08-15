package com.warmrecipes.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 预定食材：当前选中的食谱 + 生成的购物清单（同名食材合并，状态持久化到 plan.json）。 */
public class PlanStore {
    private static PlanStore instance;
    private static final Pattern NUM_UNIT = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*(.*)$");

    private final Context ctx;
    private final File file;
    public final ArrayList<Long> recipeIds = new ArrayList<>();
    public final ArrayList<PlanItem> items = new ArrayList<>();

    public static class PlanItem {
        public String name = "";
        public String amount = "";
        public boolean have = false; // true = 家里已有（已从待购清单划掉）
    }

    public static synchronized PlanStore get(Context c) {
        if (instance == null) instance = new PlanStore(c.getApplicationContext());
        return instance;
    }

    private PlanStore(Context appCtx) {
        ctx = appCtx;
        file = new File(ctx.getFilesDir(), "plan.json");
        load();
    }

    public synchronized void setRecipeIds(List<Long> ids) {
        recipeIds.clear();
        recipeIds.addAll(ids);
        rebuild();
        save();
    }

    public synchronized List<Recipe> selectedRecipes() {
        List<Recipe> out = new ArrayList<>();
        RecipeStore store = RecipeStore.get(ctx);
        for (long id : recipeIds) {
            Recipe r = store.byId(id);
            if (r != null) out.add(r);
        }
        return out;
    }

    public synchronized void toggleHave(PlanItem it) {
        it.have = !it.have;
        save();
    }

    public synchronized void clear() {
        recipeIds.clear();
        items.clear();
        file.delete();
    }

    /** 依据选中食谱重建清单；按食材名合并，并保留「家里已有」标记。 */
    public synchronized void rebuild() {
        LinkedHashMap<String, Boolean> oldHave = new LinkedHashMap<>();
        for (PlanItem it : items) oldHave.put(it.name, it.have);

        items.clear();
        LinkedHashMap<String, PlanItem> map = new LinkedHashMap<>();
        RecipeStore store = RecipeStore.get(ctx);
        for (long id : recipeIds) {
            Recipe r = store.byId(id);
            if (r == null) continue;
            for (Recipe.Ingredient ing : r.ingredients) {
                String key = ing.name.trim();
                if (key.isEmpty() && ing.amount.trim().isEmpty()) continue;
                if (key.isEmpty()) key = "（未命名）";
                PlanItem item = map.get(key);
                if (item == null) {
                    item = new PlanItem();
                    item.name = key;
                    item.amount = ing.amount.trim();
                    Boolean h = oldHave.get(key);
                    item.have = h != null && h;
                    map.put(key, item);
                } else {
                    item.amount = mergeAmount(item.amount, ing.amount.trim());
                }
            }
        }
        items.addAll(map.values());
    }

    private static String mergeAmount(String a, String b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        if (a.equals(b)) return a;
        Matcher ma = NUM_UNIT.matcher(a);
        Matcher mb = NUM_UNIT.matcher(b);
        if (ma.matches() && mb.matches() && ma.group(2).equals(mb.group(2))) {
            double sum = Double.parseDouble(ma.group(1)) + Double.parseDouble(mb.group(1));
            String num = (sum == Math.floor(sum) && !Double.isInfinite(sum))
                    ? String.valueOf((long) sum) : String.valueOf(sum);
            return num + ma.group(2);
        }
        return a + " + " + b;
    }

    private void save() {
        try {
            JSONObject o = new JSONObject();
            JSONArray ids = new JSONArray();
            for (long id : recipeIds) ids.put(id);
            o.put("recipeIds", ids);
            JSONArray have = new JSONArray();
            for (PlanItem it : items) if (it.have) have.put(it.name);
            o.put("have", have);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(o.toString().getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!file.exists()) return;
        try {
            byte[] b = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(b);
            fis.close();
            JSONObject o = new JSONObject(new String(b, "UTF-8"));
            JSONArray ids = o.optJSONArray("recipeIds");
            if (ids != null) {
                recipeIds.clear();
                for (int i = 0; i < ids.length(); i++) recipeIds.add(ids.getLong(i));
            }
            JSONArray have = o.optJSONArray("have");
            HashSet<String> haveSet = new HashSet<>();
            if (have != null) for (int i = 0; i < have.length(); i++) haveSet.add(have.getString(i));
            rebuild();
            for (PlanItem it : items) if (haveSet.contains(it.name)) it.have = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
