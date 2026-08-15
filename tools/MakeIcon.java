import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** 生成温馨风格的启动图标：暖色渐变圆角底 + 白色爱心。 */
public class MakeIcon {
    public static void main(String[] args) throws Exception {
        int[][] sizes = {
            {48, 0}, {72, 1}, {96, 2}, {144, 3}, {192, 4}
        };
        String[] dirs = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};
        String base = "app/res/mipmap-";
        for (int i = 0; i < sizes.length; i++) {
            int s = sizes[i][0];
            BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 暖色渐变背景（圆角）
            g.setPaint(new GradientPaint(0, 0, new Color(0xF3C69A), 0, s, new Color(0xE3956B)));
            int arc = Math.round(s * 0.22f);
            g.fillRoundRect(0, 0, s, s, arc, arc);

            // 白色爱心
            GeneralPath heart = heartPath();
            double scale = s * 0.72 / 20.0;
            double ox = (s - 20 * scale) / 2.0 - 2 * scale;
            double oy = (s - 18.35 * scale) / 2.0 - 3 * scale;
            AffineTransform at = new AffineTransform();
            at.translate(ox, oy);
            at.scale(scale, scale);
            heart.transform(at);
            g.setColor(Color.WHITE);
            g.fill(heart);
            g.dispose();

            File out = new File(base + dirs[i], "ic_launcher.png");
            out.getParentFile().mkdirs();
            ImageIO.write(img, "png", out);
            System.out.println("wrote " + out + " (" + s + "x" + s + ")");
        }
    }

    private static GeneralPath heartPath() {
        GeneralPath p = new GeneralPath();
        p.moveTo(12, 21.35);
        p.lineTo(10.55, 20.03);
        p.curveTo(5.4, 15.36, 2, 12.28, 2, 8.5);
        p.curveTo(2, 5.42, 4.42, 3, 7.5, 3);
        p.curveTo(9.24, 3, 10.91, 3.81, 12, 5.09);
        p.curveTo(13.09, 3.81, 14.76, 3, 16.5, 3);
        p.curveTo(19.58, 3, 22, 5.42, 22, 8.5);
        p.curveTo(22, 12.28, 18.6, 15.36, 13.45, 20.04);
        p.lineTo(12, 21.35);
        p.closePath();
        return p;
    }
}
