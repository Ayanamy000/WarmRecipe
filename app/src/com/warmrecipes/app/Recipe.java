package com.warmrecipes.app;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 一条食谱：先食材（含重量/数量），后步骤（含时长）。 */
public class Recipe {
    public long id = 0;
    public String name = "";
    public String category = "";
    public String emoji = "\uD83C\uDF72"; // 🍲
    public final ArrayList<Ingredient> ingredients = new ArrayList<>();
    public final ArrayList<Step> steps = new ArrayList<>();
    public String notes = "";
    public boolean favorite = false;
    public long createdAt = System.currentTimeMillis();
    public long updatedAt = System.currentTimeMillis();

    public static class Ingredient {
        public String name = "";
        public String amount = "";
    }

    public static class Step {
        public String instruction = "";
        public String duration = "";
    }

    public static String emojiFor(String category) {
        switch (category) {
            case "炒菜": return "🥘";
            case "炖菜": return "🍲";
            case "蒸菜": return "🍥";
            case "油炸": return "🍤";
            case "烤肉": return "🍖";
            case "凉菜": return "🥗";
            case "汤":   return "🥣";
            case "主食": return "🍚";
            case "烘焙": return "🍞";
            case "甜品": return "🍰";
            case "小吃": return "🥟";
            case "饮品": return "🍹";
            default:     return "🍽️";
        }
    }

    private static final Pattern RE_MIN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:分钟|分|min|m)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_HR = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:小时|钟头|h|hr|hour)", Pattern.CASE_INSENSITIVE);

    /** 解析所有步骤的时长，估算总分钟数（无法识别的忽略）。 */
    public int totalMinutes() {
        double sum = 0;
        for (Step s : steps) {
            Matcher mh = RE_HR.matcher(s.duration);
            if (mh.find()) sum += Double.parseDouble(mh.group(1)) * 60;
            Matcher mm = RE_MIN.matcher(s.duration);
            if (mm.find()) sum += Double.parseDouble(mm.group(1));
        }
        return (int) Math.round(sum);
    }

    /** 总时长的人类可读标签，例如「约 1 小时 20 分钟」。无有效时长返回空串。 */
    public String totalLabel() {
        int t = totalMinutes();
        if (t <= 0) return "";
        int h = t / 60;
        int m = t % 60;
        if (h > 0 && m > 0) return "约 " + h + " 小时 " + m + " 分钟";
        if (h > 0) return "约 " + h + " 小时";
        return "约 " + m + " 分钟";
    }

    /** 列表副标题：分类 · N 步 · 约总时长（分类为空时省略）。 */
    public String subtitle() {
        String total = totalLabel();
        StringBuilder sb = new StringBuilder();
        if (!category.isEmpty()) sb.append(category).append(" · ");
        sb.append(steps.size()).append(" 步");
        if (!total.isEmpty()) sb.append(" · ").append(total);
        return sb.toString();
    }

    /** 生成用于分享的可读文本。 */
    public String toShareText() {
        StringBuilder sb = new StringBuilder();
        sb.append(emoji).append(' ').append(name).append('\n');
        sb.append("分类：").append(category).append('\n');
        String t = totalLabel();
        if (!t.isEmpty()) sb.append("总时长：").append(t).append('\n');
        sb.append('\n').append("【食材与调料】\n");
        for (Ingredient x : ingredients) {
            sb.append("· ").append(x.name);
            if (!x.amount.trim().isEmpty()) sb.append("　").append(x.amount);
            sb.append('\n');
        }
        sb.append('\n').append("【步骤】\n");
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            sb.append(i + 1).append(". ").append(s.instruction);
            String d = s.duration.trim();
            if (!d.isEmpty()) sb.append("（").append(d).append("）");
            sb.append('\n');
        }
        if (!notes.trim().isEmpty()) {
            sb.append('\n').append("【备注】\n").append(notes.trim()).append('\n');
        }
        return sb.toString().trim();
    }
}
