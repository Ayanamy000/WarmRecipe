package com.warmrecipes.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Button;

/** 品类图标渲染：emoji 或相册图片缩略图。 */
public class CategoryUi {
    public static void styleChip(Context c, Button b, CategoryStore.Category cat, int thumbPx) {
        b.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        if (cat == null) return;
        if (cat.image != null && !cat.image.isEmpty()) {
            Bitmap bmp = loadThumb(cat.image, thumbPx);
            if (bmp != null) {
                b.setCompoundDrawablesWithIntrinsicBounds(
                        new BitmapDrawable(c.getResources(), bmp), null, null, null);
            }
            b.setText(" " + cat.name);
        } else if (cat.emoji != null && !cat.emoji.isEmpty()) {
            b.setText(cat.emoji + " " + cat.name);
        } else {
            b.setText(cat.name);
        }
    }

    public static Bitmap loadThumb(String path, int px) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            int sample = 1;
            while (o.outWidth / sample > px * 2 || o.outHeight / sample > px * 2) sample *= 2;
            o.inSampleSize = sample;
            o.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, o);
        } catch (Exception e) {
            return null;
        }
    }
}
