package com.warmrecipes.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/** 品类管理：支持自定义图标（emoji 或相册图片），可增删改名（持久化到 categories.json）。 */
public class CategoryStore {
    private static CategoryStore instance;
    private final File file;
    private final File iconDir;
    private final ArrayList<Category> categories = new ArrayList<>();

    public static class Category {
        public String name = "";
        public String emoji = "";
        public String image = ""; // 图片文件路径，空 = 无
    }

    public static final String[][] DEFAULT = {
        {"炒菜", "🥘"}, {"炖菜", "🍲"}, {"蒸菜", "🍥"}, {"油炸", "🍤"},
        {"烤肉", "🍖"}, {"凉菜", "🥗"}, {"汤", "🥣"}, {"主食", "🍚"},
        {"烘焙", "🍞"}, {"甜品", "🍰"}, {"小吃", "🥟"}, {"饮品", "🍹"}
    };

    public static synchronized CategoryStore get(Context c) {
        if (instance == null) instance = new CategoryStore(c.getApplicationContext());
        return instance;
    }

    private CategoryStore(Context appCtx) {
        file = new File(appCtx.getFilesDir(), "categories.json");
        iconDir = new File(appCtx.getFilesDir(), "category_icons");
        iconDir.mkdirs();
        load();
    }

    public synchronized ArrayList<Category> all() {
        return new ArrayList<>(categories);
    }

    public synchronized Category byName(String name) {
        for (Category c : categories) if (c.name.equals(name)) return c;
        return null;
    }

    public synchronized void add(String name, String emoji, String image) {
        if (name == null) return;
        String n = name.trim();
        if (n.isEmpty() || byName(n) != null) return;
        Category c = new Category();
        c.name = n;
        c.emoji = emoji == null ? "" : emoji;
        c.image = image == null ? "" : image;
        categories.add(c);
        save();
    }

    public synchronized void remove(String name) {
        Category c = byName(name);
        if (c == null) return;
        deleteImage(c.image);
        categories.remove(c);
        save();
    }

    public synchronized void rename(String oldName, String newName) {
        if (newName == null) return;
        String n = newName.trim();
        if (n.isEmpty() || n.equals(oldName) || byName(n) != null) return;
        Category c = byName(oldName);
        if (c != null) { c.name = n; save(); }
    }

    public synchronized void setEmoji(String name, String emoji) {
        Category c = byName(name);
        if (c != null) { c.emoji = emoji == null ? "" : emoji; save(); }
    }

    public synchronized void setImage(String name, String image) {
        Category c = byName(name);
        if (c == null) return;
        if (!c.image.isEmpty() && !c.image.equals(image)) deleteImage(c.image);
        c.image = image == null ? "" : image;
        save();
    }

    public File newIconFile() {
        return new File(iconDir, "cat_" + System.currentTimeMillis() + ".jpg");
    }

    private void deleteImage(String path) {
        if (path == null || path.isEmpty()) return;
        try { new File(path).delete(); } catch (Exception ignored) {}
    }

    private void load() {
        categories.clear();
        if (!file.exists()) {
            for (String[] d : DEFAULT) {
                Category c = new Category();
                c.name = d[0];
                c.emoji = d[1];
                categories.add(c);
            }
            return;
        }
        try {
            byte[] b = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(b);
            fis.close();
            JSONArray arr = new JSONArray(new String(b, "UTF-8"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                Category c = new Category();
                c.name = o.optString("name", "").trim();
                if (c.name.isEmpty()) continue;
                c.emoji = o.optString("emoji", "");
                c.image = o.optString("image", "");
                categories.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
            for (String[] d : DEFAULT) {
                Category c = new Category();
                c.name = d[0];
                c.emoji = d[1];
                categories.add(c);
            }
        }
    }

    private void save() {
        try {
            JSONArray arr = new JSONArray();
            for (Category c : categories) {
                JSONObject o = new JSONObject();
                o.put("name", c.name);
                o.put("emoji", c.emoji);
                o.put("image", c.image);
                arr.put(o);
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(arr.toString().getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
