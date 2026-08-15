package com.warmrecipes.app;

import android.content.Context;
import org.json.JSONArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/** 品类管理：用户可自行增删的餐品品类（持久化到 categories.json）。 */
public class CategoryStore {
    private static CategoryStore instance;
    private final File file;
    private final ArrayList<String> categories = new ArrayList<>();

    public static final String[] DEFAULT = {
        "炒菜", "炖菜", "蒸菜", "油炸", "烤肉", "凉菜", "汤", "主食", "烘焙", "甜品", "小吃", "饮品"
    };

    public static synchronized CategoryStore get(Context c) {
        if (instance == null) instance = new CategoryStore(c.getApplicationContext());
        return instance;
    }

    private CategoryStore(Context appCtx) {
        file = new File(appCtx.getFilesDir(), "categories.json");
        load();
    }

    public synchronized ArrayList<String> all() {
        return new ArrayList<>(categories);
    }

    public synchronized void add(String name) {
        if (name == null) return;
        String n = name.trim();
        if (n.isEmpty() || categories.contains(n)) return;
        categories.add(n);
        save();
    }

    public synchronized void remove(String name) {
        if (categories.remove(name)) save();
    }

    private void load() {
        categories.clear();
        if (!file.exists()) {
            for (String d : DEFAULT) categories.add(d);
            return;
        }
        try {
            byte[] b = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(b);
            fis.close();
            JSONArray arr = new JSONArray(new String(b, "UTF-8"));
            for (int i = 0; i < arr.length(); i++) {
                String s = arr.optString(i).trim();
                if (!s.isEmpty() && !categories.contains(s)) categories.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            for (String d : DEFAULT) categories.add(d);
        }
    }

    private void save() {
        try {
            JSONArray arr = new JSONArray();
            for (String c : categories) arr.put(c);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(arr.toString().getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
