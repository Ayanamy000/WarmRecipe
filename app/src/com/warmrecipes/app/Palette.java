package com.warmrecipes.app;

/**
 * 温馨配色方案。每种配色对应 styles.xml 里的一个子主题，
 * accent 仅用于主题选择页的色块预览。
 */
public class Palette {
    public final String id;
    public final String name;
    public final int themeResId;
    public final int accent;

    public Palette(String id, String name, int themeResId, int accent) {
        this.id = id;
        this.name = name;
        this.themeResId = themeResId;
        this.accent = accent;
    }

    public static final Palette[] ALL = {
        new Palette("cream",    "奶油杏",  R.style.Theme_WarmRecipes_Cream,    0xFFE8A87C),
        new Palette("peach",    "蜜桃粉",  R.style.Theme_WarmRecipes_Peach,    0xFFF29492),
        new Palette("matcha",   "抹茶绿",  R.style.Theme_WarmRecipes_Matcha,   0xFF93A96B),
        new Palette("orange",   "暖橙",    R.style.Theme_WarmRecipes_Orange,   0xFFF2A65A),
        new Palette("blue",     "雾蓝",    R.style.Theme_WarmRecipes_Blue,     0xFF8FB5C9),
        new Palette("lavender", "薰衣草",  R.style.Theme_WarmRecipes_Lavender, 0xFFB39DDB),
    };

    public static Palette byId(String id) {
        for (Palette p : ALL) {
            if (p.id.equals(id)) return p;
        }
        return ALL[0];
    }
}
