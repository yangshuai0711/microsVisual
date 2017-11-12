package calc.dbscan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import calc.dbscan.model.Cluster;
import calc.dbscan.model.Point;
import calc.dbscan.model.Rectange;

/**
 * Created by yuana on 2015/9/21.
 */
public class Dbscan {
    private List<Point> dataSet;
    private double eps;
    private int minPts;

    public Dbscan(int minPts, double eps) {
        this.minPts = minPts;
        this.eps = eps;
        dataSet = new LinkedList<Point>();
    }

    public void loadFramFile(String dataPath) throws IOException {
        if (dataPath == null) { throw new RuntimeException("缺少测试数据文件！"); }
        File file = new File(dataPath);
        if (!file.exists()) {
            throw new RuntimeException("测试数据文件不存在！");
        }
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String row = bufferedReader.readLine();
        while (row != null) {
            String[] rdata = row.split(",");
            dataSet.add(new Point(Double.parseDouble(rdata[1].trim()), Double.parseDouble(rdata[2].trim())));
            row = bufferedReader.readLine();
        }
    }

    public void addData(int x, int y) {
        dataSet.add(new Point(x, y));
    }

    public List<Cluster> dBScan() {
        int i = 0;
        List<Cluster> clusterList = new LinkedList<Cluster>();
        for (Point p : dataSet) {
            if (p.isVisited()) { continue; }
            //设置该点已经被访问
            p.setIsVisited(true);
            List<Point> neighborPts = new LinkedList<Point>();
            boolean isCorePoint = isCorePoint(p, neighborPts);
            if (!isCorePoint)
            //标记为噪音数据
            { p.setIsNoise(true); } else {
                //作为核心点，根据该点创建一个类别
                p.setIsCore(true);
                Cluster cluster = new Cluster("cluset@" + i++, p);
                clusterList.add(cluster);
                expandCluster(p, neighborPts, cluster);
            }
        }
        return clusterList;
    }

    /**
     * 根据核心点扩展
     *
     * @param corePoint   核心点
     * @param neighborPts 密度相连点
     * @param cluster     核心点的簇
     */
    private void expandCluster(Point corePoint, List<Point> neighborPts, Cluster cluster) {
        cluster.addMemberNotExists(corePoint);
        List<Point> nPts;
        Queue<Point> neighborPtQueue = new LinkedList<Point>(neighborPts);
        Point neighborPt = null;
        while (neighborPtQueue.size() > 0) {
            neighborPt = neighborPtQueue.poll();
            cluster.addMemberNotExists(neighborPt);
            //然后针对核心点邻域内的点，如果该点没有被访问
            if (!neighborPt.isVisited()) {
                nPts = new LinkedList<Point>();
                neighborPt.setIsVisited(true);
                boolean isCorePoint = isCorePoint(neighborPt, nPts);
                if (isCorePoint) {
                    neighborPt.setIsCore(true);
                    neighborPtQueue.addAll(nPts);
                }
            }
        }
    }

    /**
     * 查询指定点指定半径内的相邻点
     *
     * @param p
     * @return
     */
    private boolean isCorePoint(Point p, List<Point> neighborPts) {
        Rectange outRectange = new Rectange(p, eps);
        List<Point> rectNeighborPts = new LinkedList<Point>();
        //外接矩形内点
        for (Point point : dataSet) {
            if (outRectange.containPoint(point)) { rectNeighborPts.add(point); }
        }
        if (rectNeighborPts.size() < minPts) { return false; }
        //内接水平矩形内的点
        //d表示内接矩形的长的一半
        double d = Math.sin(45.0) * eps;
        double left = p.getX() - d;
        double top = p.getY() + d;
        double right = p.getX() + d;
        double bottom = p.getY() - d;
        Rectange innerRectance = new Rectange(left, right, top, bottom, p);

        //在内接矩形内的点肯定也在圆内
        for (int i = 0; i < rectNeighborPts.size(); i++) {
            Point point = rectNeighborPts.get(i);
            if (innerRectance.containPoint(point)) {
                neighborPts.add(rectNeighborPts.remove(i));
                i--;
            }
        }
        //对于剩余的点再做距离判断
        for (Point point : rectNeighborPts) {
            double distance = Math.sqrt(Math.pow(point.getX() - p.getX(), 2) + Math.pow(point.getY() - p.getY(), 2));
            if (distance <= eps) { neighborPts.add(point); }
        }
        return neighborPts.size() > minPts;
    }

}
