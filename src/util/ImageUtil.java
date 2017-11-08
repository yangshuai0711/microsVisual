package util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Created by Administrator on 2017/10/30.
 */
public class ImageUtil {
    public static void writeColorImage(double[][] rgbs, String[] strs, String filepath) throws IOException {
        BufferedImage image = new BufferedImage(rgbs.length * 25 + 5, 30, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        int lastX = 5;
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        for (int i = 0; i < rgbs.length; i++) {
            double[] rgb = rgbs[i];
            if (rgb == null) {
                break;
            }
            g.setColor(new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]));
            g.fillRect(lastX, 5, 20, 20);
            g.setColor(Color.white);
            g.drawString(strs[i], lastX, 5);
            lastX += 25;
        }
        ImageIO.write(image, "png", new File(filepath));
    }

    public static void main(String[] args) throws IOException {
        double[] rgb = ColorUtil.HSL2RGB(new double[] {30, 85, 253});
        ImageUtil.writeColorImage(new double[][] {rgb}, new String[] {"test"}, "C:\\30_85_253.png");
    }
}
