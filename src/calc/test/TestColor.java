//package calc.test;
//
//import java.awt.image.BufferedImage;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map.Entry;
//
//import javax.imageio.ImageIO;
//
//import alg.DBSCAN;
//import util.ColorUtil;
//import model.Cluster;
//import model.Point;
//
///**
// * Created by Administrator on 2017/10/29.
// */
//public class TestColor {
//    public static void main(String[] args) throws IOException {
//        BufferedImage result = ImageIO.read(new FileInputStream("G:\\Users\\Administrator\\Desktop\\test.png"));
//        int width = result.getWidth();
//        int height = result.getHeight();
//        int count = 0;
//        DBSCAN dbscan = new DBSCAN(100, 30);
//        HashMap<String, Integer> set = new HashMap();
//        for (int i = 0; i < width; i++) {
//            for (int i1 = 0; i1 < height; i1++) {
//                int rgb = result.getRGB(i, i1);
//                double[] color2 = ColorUtil.RGB2HSL(ColorUtil.rgbFromInt(rgb));
//                String key = color2[0] + "," + color2[1];
//                set.put(key, rgb);
//            }
//        }
//        for (Entry<String, Integer> o : set.entrySet()) {
//            double[] color2 = ColorUtil.RGB2HSL(ColorUtil.rgbFromInt(o.getValue()));
//            dbscan.addData((int)color2[0], (int)color2[1]);
//            count++;
//        }
//        System.out.println("points:" + count);
//        java.util.List<Cluster> clusterList = dbscan.dBScan();
//        System.out.println("clusters:" + clusterList.size());
//        for (Cluster cluster : clusterList) {
//            Point center = cluster.getCenter();
//            System.out.println(center.getX() + "," + center.getY());
//        }
//    }
//}
