package util;

/**
 * Created by Administrator on 2017/10/30.
 */
public class ColorUtil {
    public static double[] RGB2HSL(double[] rgb) {
        if (rgb == null) {
            return null;
        }

        double H, S, L, var_Min, var_Max, del_Max, del_R, del_G, del_B;
        H = 0;
        var_Min = Math.min(rgb[0], Math.min(rgb[2], rgb[1]));
        var_Max = Math.max(rgb[0], Math.max(rgb[2], rgb[1]));
        del_Max = var_Max - var_Min;
        L = (var_Max + var_Min) / 2;
        if (del_Max == 0) {
            H = 0;
            S = 0;

        } else {
            if (L < 128) {
                S = 256 * del_Max / (var_Max + var_Min);
            } else {
                S = 256 * del_Max / (512 - var_Max - var_Min);
            }
            del_R = ((360 * (var_Max - rgb[0]) / 6) + (360 * del_Max / 2))
                / del_Max;
            del_G = ((360 * (var_Max - rgb[1]) / 6) + (360 * del_Max / 2))
                / del_Max;
            del_B = ((360 * (var_Max - rgb[2]) / 6) + (360 * del_Max / 2))
                / del_Max;
            if (rgb[0] == var_Max) {
                H = del_B - del_G;
            } else if (rgb[1] == var_Max) {
                H = 120 + del_R - del_B;
            } else if (rgb[2] == var_Max) {
                H = 240 + del_G - del_R;
            }
            if (H < 0) {
                H += 360;
            }
            if (H >= 360) {
                H -= 360;
            }
            if (L >= 256) {
                L = 255;
            }
            if (S >= 256) {
                S = 255;
            }
        }
        return new double[] {H, S, L};
    }

    public static double[] HSL2RGB(double[] hsl) {
        if (hsl == null) {
            return null;
        }
        double H = hsl[0];
        double S = hsl[1];
        double L = hsl[2];

        double R, G, B, var_1, var_2;
        if (S == 0) {
            R = L;
            G = L;
            B = L;
        } else {
            if (L < 128) {
                var_2 = (L * (256 + S)) / 256;
            } else {
                var_2 = (L + S) - (S * L) / 256;
            }

            if (var_2 > 255) {
                var_2 = Math.round(var_2);
            }

            if (var_2 > 254) {
                var_2 = 255;
            }

            var_1 = 2 * L - var_2;
            R = RGBFromHue(var_1, var_2, H + 120);
            G = RGBFromHue(var_1, var_2, H);
            B = RGBFromHue(var_1, var_2, H - 120);
        }
        R = R < 0 ? 0 : R;
        R = R > 255 ? 255 : R;
        G = G < 0 ? 0 : G;
        G = G > 255 ? 255 : G;
        B = B < 0 ? 0 : B;
        B = B > 255 ? 255 : B;
        return new double[] {(int)Math.round(R), (int)Math.round(G), (int)Math
            .round(B)};
    }

    public static double RGBFromHue(double a, double b, double h) {
        if (h < 0) {
            h += 360;
        }
        if (h >= 360) {
            h -= 360;
        }
        if (h < 60) {
            return a + ((b - a) * h) / 60;
        }
        if (h < 180) {
            return b;
        }

        if (h < 240) {
            return a + ((b - a) * (240 - h)) / 60;
        }
        return a;
    }

    public static double[] rgbFromInt(int rgb) {
        int blue = rgb & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int red = (rgb >> 16) & 0xFF;
        return new double[] {red, green, blue};
    }

    public static void main(String[] args) {
        double[][] rgbs = new double[][] {new double[] {134,102,109}, new double[] {181,120,119}, new double[] {137,97,117},new double[] {154,126,117},new double[] {149,120,111}
            ,new double[] {155,118,131}};
        for (int i = 0; i < rgbs.length; i++) {
            double[] hsl = ColorUtil.RGB2HSL(rgbs[i]);
            System.out.println((int)hsl[0] + "," + (int)hsl[1] + "," + (int)hsl[2]);
        }
    }

}
