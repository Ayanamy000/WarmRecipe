package com.warmrecipes.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/** 本机存储：用框架自带 org.json 序列化到内部存储 recipes.json。 */
public class RecipeStore {
    private static RecipeStore instance;
    private final Context ctx;
    private final ArrayList<Recipe> recipes = new ArrayList<>();
    private final File file;

    private RecipeStore(Context c) {
        ctx = c.getApplicationContext();
        file = new File(ctx.getFilesDir(), "recipes.json");
        load();
    }

    public static synchronized RecipeStore get(Context c) {
        if (instance == null) instance = new RecipeStore(c);
        return instance;
    }

    public synchronized ArrayList<Recipe> all() {
        return new ArrayList<>(recipes);
    }

    public synchronized Recipe byId(long id) {
        for (Recipe r : recipes) if (r.id == id) return r;
        return null;
    }

    /** 新增（id==0）或更新。 */
    public synchronized void save(Recipe r) {
        if (r.id == 0) {
            r.id = System.currentTimeMillis();
            recipes.add(0, r);
        } else {
            for (int i = 0; i < recipes.size(); i++) {
                if (recipes.get(i).id == r.id) { recipes.set(i, r); break; }
            }
        }
        r.updatedAt = System.currentTimeMillis();
        persist();
    }

    public synchronized void delete(long id) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).id == id) { recipes.remove(i); break; }
        }
        persist();
    }

    public synchronized void toggleFavorite(long id) {
        Recipe r = byId(id);
        if (r != null) { r.favorite = !r.favorite; persist(); }
    }

    private void persist() {
        try {
            JSONArray arr = new JSONArray();
            for (Recipe r : recipes) arr.put(toJson(r));
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(arr.toString(2).getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() {
        recipes.clear();
        if (!file.exists()) return;
        try {
            byte[] b = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(b);
            fis.close();
            JSONArray arr = new JSONArray(new String(b, "UTF-8"));
            for (int i = 0; i < arr.length(); i++) recipes.add(fromJson(arr.getJSONObject(i)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject toJson(Recipe r) throws Exception {
        JSONObject o = new JSONObject();
        o.put("id", r.id);
        o.put("name", r.name);
        o.put("category", r.category);
        o.put("emoji", r.emoji);
        JSONArray ing = new JSONArray();
        for (Recipe.Ingredient x : r.ingredients) {
            JSONObject j = new JSONObject();
            j.put("name", x.name);
            j.put("amount", x.amount);
            ing.put(j);
        }
        o.put("ingredients", ing);
        JSONArray st = new JSONArray();
        for (Recipe.Step x : r.steps) {
            JSONObject j = new JSONObject();
            j.put("instruction", x.instruction);
            j.put("duration", x.duration);
            st.put(j);
        }
        o.put("steps", st);
        o.put("notes", r.notes);
        o.put("favorite", r.favorite);
        o.put("createdAt", r.createdAt);
        o.put("updatedAt", r.updatedAt);
        return o;
    }

    private Recipe fromJson(JSONObject o) throws Exception {
        Recipe r = new Recipe();
        r.id = o.optLong("id");
        r.name = o.optString("name");
        r.category = o.optString("category", "其他");
        r.emoji = o.optString("emoji", Recipe.emojiFor(r.category));
        JSONArray ing = o.optJSONArray("ingredients");
        if (ing != null) {
            for (int i = 0; i < ing.length(); i++) {
                JSONObject j = ing.getJSONObject(i);
                Recipe.Ingredient x = new Recipe.Ingredient();
                x.name = j.optString("name");
                x.amount = j.optString("amount");
                r.ingredients.add(x);
            }
        }
        JSONArray st = o.optJSONArray("steps");
        if (st != null) {
            for (int i = 0; i < st.length(); i++) {
                JSONObject j = st.getJSONObject(i);
                Recipe.Step x = new Recipe.Step();
                x.instruction = j.optString("instruction");
                x.duration = j.optString("duration");
                r.steps.add(x);
            }
        }
        r.notes = o.optString("notes");
        r.favorite = o.optBoolean("favorite");
        r.createdAt = o.optLong("createdAt", System.currentTimeMillis());
        r.updatedAt = o.optLong("updatedAt", System.currentTimeMillis());
        return r;
    }
}
