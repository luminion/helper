package io.github.luminion.helper.core.geographical;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 坐标点
 *
 * @author luminion
 */
@Getter
@EqualsAndHashCode
public class GeoPoint {
    private static final double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;
    private static final double earth = 6378.137;

    /**
     * 经度
     */
    private final double longitude;
    /**
     * 纬度
     */
    private final double latitude;

    public GeoPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public GeoPoint(String longitude, String latitude) {
        this.longitude = Double.parseDouble(longitude);
        this.latitude = Double.parseDouble(latitude);
    }

    public GeoPoint(BigDecimal longitude, BigDecimal latitude) {
        this.longitude = longitude.doubleValue();
        this.latitude = latitude.doubleValue();
    }


    /**
     * 根据这组坐标，画一个矩形，然后得到这个矩形西南角的顶点坐标
     *
     * @param vertexes 顶点
     * @return {@link GeoPoint }
     */
    public static GeoPoint getSouthWestPoint(GeoPoint[] vertexes) {
        double minLng = vertexes[0].getLongitude();
        double minLat = vertexes[0].getLatitude();
        for (GeoPoint geoPoint : vertexes) {
            double x = geoPoint.getLongitude();
            double y = geoPoint.getLatitude();
            if (x < minLng) {
                minLng = x;
            }
            if (y < minLat) {
                minLat = y;
            }
        }
        return new GeoPoint(minLng, minLat);
    }

    /**
     * 根据这组坐标，画一个矩形，然后得到这个矩形东北角的顶点坐标
     *
     * @param vertexes 顶点
     * @return {@link GeoPoint }
     */
    public static GeoPoint getNorthEastPoint(GeoPoint[] vertexes) {
        double maxLng = vertexes[0].getLongitude();
        double maxLat = vertexes[0].getLatitude();
        for (GeoPoint geoPoint : vertexes) {
            double x = geoPoint.getLongitude();
            double y = geoPoint.getLatitude();
            if (x > maxLng) {
                maxLng = x;
            }
            if (y > maxLat) {
                maxLat = y;
            }
        }
        return new GeoPoint(maxLng, maxLat);
    }

    /**
     * 获取距离 (米)
     *
     * @param point 点
     * @return double
     */
    public double getDistanceMeters(GeoPoint point) {
        //用户纬度
        double userLongitude = Math.toRadians(point.getLongitude());
        double userLatitude = Math.toRadians(point.getLatitude());

        double longitude = Math.toRadians(this.longitude);
        double latitude = Math.toRadians(this.latitude);
        // 纬度之差
        double a = userLatitude - latitude;
        // 经度之差
        double b = userLongitude - longitude;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(userLatitude) * Math.cos(latitude) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘赤道半径, 返回单位: 米, 地球半径,单位 米
        s = s * 6378137;
        return s;
    }

    /**
     * 获取距离 (千米)
     *
     * @param point 点
     * @return double
     */
    public double getDistanceKilometers(GeoPoint point) {
        return getDistanceMeters(point) / 1000;
    }

    /**
     * 是否在圆圈中
     *
     * @param circle 圆心
     * @param radius 半径(米)
     * @return boolean
     */
    public boolean isInCircle(GeoPoint circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }


    /**
     * 是否在指定区域内(基本思路是用交点法)
     *
     * @param boundaryPoints 边界点
     * @return boolean
     * @link <a href="https://blog.csdn.net/zheng12tian/article/details/40617445">原文链接</a>
     */
    public boolean isInPolygon(List<GeoPoint> boundaryPoints) {
        return isInPolygon(boundaryPoints.toArray(new GeoPoint[0]));
    }

    /**
     * 判断点是否在区域内
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInPolygon(GeoPoint[] boundaryPoints) {
        // 防止第一个点与最后一个点相同
        if (boundaryPoints != null && boundaryPoints.length > 0 && boundaryPoints[boundaryPoints.length - 1].equals(boundaryPoints[0])) {
            boundaryPoints = Arrays.copyOf(boundaryPoints, boundaryPoints.length - 1);
        }
        int pointCount = boundaryPoints.length;

        // 首先判断点是否在多边形的外包矩形内，如果在，则进一步判断，否则返回false
        if (!isInRectangleBoundary(boundaryPoints)) {
            return false;
        }

        // 如果点与多边形的其中一个顶点重合，那么直接返回true
        for (int i = 0; i < pointCount; i++) {
            if (this.equals(boundaryPoints[i])) {
                return true;
            }
        }
        
        // 基本思想是利用X轴射线法，计算射线与多边形各边的交点，如果是偶数，则点在多边形外，否则在多边形内。还会考虑一些特殊情况，如点在多边形顶点上， 点在多边形边上等特殊情况。
       
        // X轴射线与多边形的交点数
        int intersectPointCount = 0;
        // X轴射线与多边形的交点权值
        float intersectPointWeights = 0;
        // 浮点类型计算时候与0比较时候的容差0.0000000002
        double precision = 2e-10;
        // 边P1P2的两个端点
        GeoPoint point1 = boundaryPoints[0], point2;
        // 循环判断所有的边
        for (int i = 1; i <= pointCount; i++) {
            point2 = boundaryPoints[i % pointCount];

            // 如果点的y坐标在边P1P2的y坐标开区间范围之外，那么不相交。
            if (this.getLatitude() < Math.min(point1.getLatitude(), point2.getLatitude())
                    || this.getLatitude() > Math.max(point1.getLatitude(), point2.getLatitude())) {
                point1 = point2;
                continue;
            }

            // 此处判断射线与边相交
            // 如果点的y坐标在边P1P2的y坐标开区间内
            if (this.getLatitude() > Math.min(point1.getLatitude(), point2.getLatitude())
                    && this.getLatitude() < Math.max(point1.getLatitude(), point2.getLatitude())) {
                // 若边P1P2是垂直的
                if (point1.getLongitude() == point2.getLongitude()) {
                    // 若点在垂直的边P1P2上，则点在多边形内
                    if (this.getLongitude() == point1.getLongitude()) {
                        return true;
                    }
                    // 若点在在垂直的边P1P2左边，则点与该边必然有交点
                    else if (this.getLongitude() < point1.getLongitude()) {
                        ++intersectPointCount;
                    }
                }
                // 若边P1P2是斜线
                else {
                    // 点point的x坐标在点P1和P2的左侧
                    if (this.getLongitude() <= Math.min(point1.getLongitude(), point2.getLongitude())) {
                        ++intersectPointCount;
                    }
                    // 点point的x坐标在点P1和P2的x坐标中间
                    else if (this.getLongitude() > Math.min(point1.getLongitude(), point2.getLongitude())
                            && this.getLongitude() < Math.max(point1.getLongitude(), point2.getLongitude())) {
                        double slopeDiff = getSlopeDiff(point1, point2);
                        if (slopeDiff > 0) {
                            // 由于double精度在计算时会有损失，故匹配一定的容差。经试验，坐标经度可以达到0.0001
                            if (slopeDiff < precision) {
                                // 点在斜线P1P2上
                                return true;
                            } else {
                                // 点与斜线P1P2有交点
                                intersectPointCount++;
                            }
                        }
                    }
                }
            }
            // 点的y坐标不在边P1P2的y坐标开区间内
            else {
                // 边P1P2水平
                if (point1.getLatitude() == point2.getLatitude()) {
                    if (isInRectangleEdge(point1, point2)) {
                        return true;
                    }
                }
                // 判断点通过多边形顶点
                if (((this.getLatitude() == point1.getLatitude() && this.getLongitude() < point1.getLongitude())) || (this.getLatitude() == point2.getLatitude() && this.getLongitude() < point2.getLongitude())) {
                    if (point2.getLatitude() < point1.getLatitude()) {
                        intersectPointWeights += -0.5;
                    } else if (point2.getLatitude() > point1.getLatitude()) {
                        intersectPointWeights += 0.5;
                    }
                }
            }
            point1 = point2;
        }
        // 偶数在多边形外
        if ((intersectPointCount + Math.abs(intersectPointWeights)) % 2 == 0) {
            return false;
        }
        // 奇数在多边形内
        else { 
            return true;
        }
    }


    /**
     * 获取与指定直线的斜率差
     * <p>
     * 当斜率差值接近0（小于某个精度值）时，说明当前点在由 point1 和 point2 构成的线段上
     *
     * @param point1 point1 线段顶点1
     * @param point2 point2 线段顶点2
     * @return double
     */
    private double getSlopeDiff(GeoPoint point1, GeoPoint point2) {
        double slopeDiff = 0.0d;
        if (point1.getLatitude() > point2.getLatitude()) {
            slopeDiff = (this.getLatitude() - point2.getLatitude()) / (this.getLongitude() - point2.getLongitude()) - (point1.getLatitude() - point2.getLatitude()) / (point1.getLongitude() - point2.getLongitude());
        } else {
            slopeDiff = (this.getLatitude() - point1.getLatitude()) / (this.getLongitude() - point1.getLongitude()) - (point2.getLatitude() - point1.getLatitude()) / (point2.getLongitude() - point1.getLongitude());
        }
        return slopeDiff;
    }

    /**
     * 判断是否在多边形边P上
     *
     * @param point1 point1 线段顶点1
     * @param point2 point2 线段顶点2
     * @return boolean
     */
    public boolean isInRectangleEdge(GeoPoint point1, GeoPoint point2) {
        if (this.getLongitude() <= Math.max(point1.getLongitude(), point2.getLongitude()) && this.getLongitude() >= Math.min(point1.getLongitude(), point2.getLongitude())) {
            // 若点在水平的边P1P2上，则点在多边形内
            return true;
        }
        return false;
    }

    /**
     * 判断是否在指定坐标外包矩形边界上，也可以用于计算在矩形内(根据这些点，构造一个外包矩形)
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInRectangleBoundary(GeoPoint[] boundaryPoints) {
        // 西南角点
        GeoPoint southWestPoint = getSouthWestPoint(boundaryPoints);
        // 东北角点
        GeoPoint northEastPoint = getNorthEastPoint(boundaryPoints);
        boolean b = this.getLongitude() >= southWestPoint.getLongitude();
        boolean b3 = this.getLatitude() >= southWestPoint.getLatitude();
        boolean b2 = this.getLongitude() <= northEastPoint.getLongitude();
        boolean b4 = this.getLatitude() <= northEastPoint.getLatitude();
        return b && b3 && b2 && b4;
    }


}