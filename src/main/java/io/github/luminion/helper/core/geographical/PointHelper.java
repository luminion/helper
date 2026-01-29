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
 * @link <a href="https://blog.csdn.net/zheng12tian/article/details/40617445">交点法判断点是否在多边形内</a>
 * @link <a href="https://blog.csdn.net/a704397849/article/details/121133616">坐标系转化</a>
 */
@Getter
@EqualsAndHashCode
public class PointHelper {
    private static final double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;

    /**
     * 经度
     */
    private final double longitude;
    /**
     * 纬度
     */
    private final double latitude;

    public static PointHelper of(double longitude, double latitude) {
        return new PointHelper(longitude, latitude);
    }

    public static PointHelper of(String longitude, String latitude) {
        return new PointHelper(longitude, latitude);
    }

    public static PointHelper of(BigDecimal longitude, BigDecimal latitude) {
        return new PointHelper(longitude, latitude);
    }

    public PointHelper(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public PointHelper(String longitude, String latitude) {
        this.longitude = Double.parseDouble(longitude);
        this.latitude = Double.parseDouble(latitude);
    }

    public PointHelper(BigDecimal longitude, BigDecimal latitude) {
        this.longitude = longitude.doubleValue();
        this.latitude = latitude.doubleValue();
    }

    /**
     * 获取距离米
     *
     * @param longitude1 经度1
     * @param latitude1  纬度1
     * @param longitude2 经度2
     * @param latitude2  纬度2
     * @return double
     */
    public static double getDistanceMeters(double longitude1, double latitude1, double longitude2, double latitude2) {
        return new PointHelper(longitude1, latitude1).getDistanceMeters(new PointHelper(longitude2, latitude2));
    }

    public static double getDistanceMeters(String longitude1, String latitude1, String longitude2, String latitude2) {
        return new PointHelper(longitude1, latitude1).getDistanceMeters(new PointHelper(longitude2, latitude2));
    }

    public static double getDistanceMeters(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return new PointHelper(longitude1, latitude1).getDistanceMeters(new PointHelper(longitude2, latitude2));
    }

    public static double getDistanceMeters(PointHelper point1, PointHelper point2) {
        return point1.getDistanceMeters(point2);
    }

    /**
     * 获取距离千米
     *
     * @param longitude1 经度1
     * @param latitude1  纬度1
     * @param longitude2 经度2
     * @param latitude2  纬度2
     * @return double
     */
    public static double getDistanceKilometer(double longitude1, double latitude1, double longitude2, double latitude2) {
        return new PointHelper(longitude1, latitude1).getDistanceKilometers(new PointHelper(longitude2, latitude2));
    }

    public static double getDistanceKilometer(String longitude1, String latitude1, String longitude2, String latitude2) {
        return new PointHelper(longitude1, latitude1).getDistanceKilometers(new PointHelper(longitude2, latitude2));
    }

    public static double getDistanceKilometer(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return new PointHelper(longitude1, latitude1).getDistanceKilometers(new PointHelper(longitude2, latitude2));
    }

    public static double getDistanceKilometer(PointHelper point1, PointHelper point2) {
        return point1.getDistanceKilometers(point2);
    }

    /**
     * 是否在圆圈中
     *
     * @param point  点
     * @param circle 圆点
     * @param radius 半径
     * @return boolean
     */
    public static boolean isInCircle(PointHelper point, PointHelper circle, double radius) {
        return point.isInCircle(circle, radius);
    }

    /**
     * 判断点是否在区域内
     *
     * @param point          点
     * @param boundaryPoints 区域边界顶点
     * @return boolean
     */
    public static boolean isPointInPolygon(PointHelper point, PointHelper... boundaryPoints) {
        return point.isInPolygon(boundaryPoints);
    }

    /**
     * 百度坐标（BD09）转 GCJ02
     *
     * @param point BD09坐标点
     * @return GCJ02 坐标点
     */
    public static PointHelper transformBD09ToGCJ02(PointHelper point) {
        return point.transformBD09ToGCJ02();
    }

    public static PointHelper transformBD09ToGCJ02(double longitude, double latitude) {
        return new PointHelper(longitude, latitude).transformBD09ToGCJ02();
    }

    /**
     * GCJ02 转百度坐标
     *
     * @param point GCJ02坐标点
     * @return 百度坐标点
     */
    public static PointHelper transformGCJ02ToBD09(PointHelper point) {
        return point.transformGCJ02ToBD09();
    }

    public static PointHelper transformGCJ02ToBD09(double longitude, double latitude) {
        return new PointHelper(longitude, latitude).transformGCJ02ToBD09();
    }

    /**
     * GCJ02 转 WGS84
     *
     * @param point GCJ02坐标点
     * @return WGS84坐标点
     */
    public static PointHelper transformGCJ02ToWGS84(PointHelper point) {
        return point.transformGCJ02ToWGS84();
    }

    public static PointHelper transformGCJ02ToWGS84(double longitude, double latitude) {
        return new PointHelper(longitude, latitude).transformGCJ02ToWGS84();
    }

    /**
     * WGS84 坐标 转 GCJ02
     *
     * @param point WGS84坐标点
     * @return GCJ02 坐标点
     */
    public static PointHelper transformWGS84ToGCJ02(PointHelper point) {
        return point.transformWGS84ToGCJ02();
    }

    public static PointHelper transformWGS84ToGCJ02(double longitude, double latitude) {
        return new PointHelper(longitude, latitude).transformWGS84ToGCJ02();
    }

    /**
     * 百度坐标BD09 转 WGS84
     *
     * @param point BD09坐标点
     * @return WGS84 坐标点
     */
    public static PointHelper transformBD09ToWGS84(PointHelper point) {
        return point.transformBD09ToWGS84();
    }

    public static PointHelper transformBD09ToWGS84(double longitude, double latitude) {
        return new PointHelper(longitude, latitude).transformBD09ToWGS84();
    }

    /**
     * WGS84 转 百度坐标BD09
     *
     * @param point WGS84坐标点
     * @return BD09 坐标点
     */
    public static PointHelper transformWGS84ToBD09(PointHelper point) {
        return point.transformWGS84ToBD09();
    }

    public static PointHelper transformWGS84ToBD09(double longitude, double latitude) {
        return new PointHelper(longitude, latitude).transformWGS84ToBD09();
    }


    /**
     * 根据这组坐标，画一个矩形，然后得到这个矩形西南角的顶点坐标
     *
     * @param vertexes 顶点
     * @return {@link PointHelper }
     */
    public static PointHelper getSouthWestPoint(PointHelper[] vertexes) {
        double minLng = vertexes[0].getLongitude();
        double minLat = vertexes[0].getLatitude();
        for (PointHelper pointHelper : vertexes) {
            double x = pointHelper.getLongitude();
            double y = pointHelper.getLatitude();
            if (x < minLng) {
                minLng = x;
            }
            if (y < minLat) {
                minLat = y;
            }
        }
        return new PointHelper(minLng, minLat);
    }

    /**
     * 根据这组坐标，画一个矩形，然后得到这个矩形东北角的顶点坐标
     *
     * @param vertexes 顶点
     * @return {@link PointHelper }
     */
    public static PointHelper getNorthEastPoint(PointHelper[] vertexes) {
        double maxLng = vertexes[0].getLongitude();
        double maxLat = vertexes[0].getLatitude();
        for (PointHelper pointHelper : vertexes) {
            double x = pointHelper.getLongitude();
            double y = pointHelper.getLatitude();
            if (x > maxLng) {
                maxLng = x;
            }
            if (y > maxLat) {
                maxLat = y;
            }
        }
        return new PointHelper(maxLng, maxLat);
    }

    /**
     * 判断坐标是否不在国内
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标一定不在国内时返回true, 返回false代表不确定
     */
    private static boolean isOutOfChinaRectangle(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }

    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }


    /**
     * 获取距离 (米)
     *
     * @param point 点
     * @return double
     */
    public double getDistanceMeters(PointHelper point) {
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
    public double getDistanceKilometers(PointHelper point) {
        return getDistanceMeters(point) / 1000;
    }

    /**
     * 是否在圆圈中
     *
     * @param circle 圆心
     * @param radius 半径(米)
     * @return boolean
     */
    public boolean isInCircle(PointHelper circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }


    /**
     * 是否在指定区域内(基本思路是用交点法)
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInPolygon(List<PointHelper> boundaryPoints) {
        return isInPolygon(boundaryPoints.toArray(new PointHelper[0]));
    }

    /**
     * 判断点是否在区域内
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInPolygon(PointHelper[] boundaryPoints) {
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
        PointHelper point1 = boundaryPoints[0], point2;
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
    private double getSlopeDiff(PointHelper point1, PointHelper point2) {
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
    public boolean isInRectangleEdge(PointHelper point1, PointHelper point2) {
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
    public boolean isInRectangleBoundary(PointHelper[] boundaryPoints) {
        // 西南角点
        PointHelper southWestPoint = getSouthWestPoint(boundaryPoints);
        // 东北角点
        PointHelper northEastPoint = getNorthEastPoint(boundaryPoints);
        boolean b = this.getLongitude() >= southWestPoint.getLongitude();
        boolean b3 = this.getLatitude() >= southWestPoint.getLatitude();
        boolean b2 = this.getLongitude() <= northEastPoint.getLongitude();
        boolean b4 = this.getLatitude() <= northEastPoint.getLatitude();
        return b && b3 && b2 && b4;
    }


    /**
     * 百度坐标（BD09）转 GCJ02
     *
     * @return GCJ02 坐标点
     */
    public PointHelper transformBD09ToGCJ02() {
        double x = this.longitude - 0.0065;
        double y = this.latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_PI);
        double gcj_lng = z * Math.cos(theta);
        double gcj_lat = z * Math.sin(theta);
        return new PointHelper(gcj_lng, gcj_lat);
    }

    /**
     * GCJ02 转百度坐标
     *
     * @return 百度坐标：[经度，纬度]
     */
    public PointHelper transformGCJ02ToBD09() {
        double z = Math.sqrt(this.longitude * this.longitude + this.latitude * this.latitude) + 0.00002 * Math.sin(this.latitude * x_PI);
        double theta = Math.atan2(this.latitude, this.longitude) + 0.000003 * Math.cos(this.longitude * x_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new PointHelper(bd_lng, bd_lat);
    }

    /**
     * GCJ02 转 WGS84
     *
     * @return WGS84坐标：[经度，纬度]
     */
    public PointHelper transformGCJ02ToWGS84() {
        if (isOutOfChinaRectangle(this.longitude, this.latitude)) {
            return new PointHelper(this.longitude, this.latitude);
        } else {
            double dLat = transformLat(this.longitude - 105.0, this.latitude - 35.0);
            double dLng = transformLng(this.longitude - 105.0, this.latitude - 35.0);
            double radLat = this.latitude / 180.0 * PI;
            double magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
            dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
            double mgLat = this.latitude + dLat;
            double mgLng = this.longitude + dLng;
            return new PointHelper(this.longitude * 2 - mgLng, this.latitude * 2 - mgLat);
        }
    }

    /**
     * WGS84 坐标 转 GCJ02
     *
     * @return GCJ02 坐标：[经度，纬度]
     */
    public PointHelper transformWGS84ToGCJ02() {
        if (isOutOfChinaRectangle(this.longitude, this.latitude)) {
            return new PointHelper(this.longitude, this.latitude);
        } else {
            double dLat = transformLat(this.longitude - 105.0, this.latitude - 35.0);
            double dLng = transformLng(this.longitude - 105.0, this.latitude - 35.0);
            double redLat = this.latitude / 180.0 * PI;
            double magic = Math.sin(redLat);
            magic = 1 - ee * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
            dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(redLat) * PI);
            double mgLat = this.latitude + dLat;
            double mgLng = this.longitude + dLng;
            return new PointHelper(mgLng, mgLat);
        }
    }

    /**
     * 百度坐标BD09 转 WGS84
     *
     * @return WGS84 坐标：[经度，纬度]
     */
    public PointHelper transformBD09ToWGS84() {
        PointHelper gcj02Point = transformBD09ToGCJ02();
        return gcj02Point.transformGCJ02ToWGS84();
    }

    /**
     * WGS84 转 百度坐标BD09
     *
     * @return BD09 坐标：[经度，纬度]
     */
    public PointHelper transformWGS84ToBD09() {
        PointHelper gcj02Point = transformWGS84ToGCJ02();
        return gcj02Point.transformGCJ02ToBD09();
    }


}