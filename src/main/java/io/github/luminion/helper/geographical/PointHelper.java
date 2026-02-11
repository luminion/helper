package io.github.luminion.helper.geographical;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 坐标点
 *
 * @author luminion
 * @link <a href=
 *       "https://blog.csdn.net/zheng12tian/article/details/40617445">交点法判断点是否在多边形内</a>
 * @link <a href=
 *       "https://blog.csdn.net/a704397849/article/details/121133616">坐标系转化</a>
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PointHelper {
    private static final double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;
    /**
     * 克拉索夫斯基椭球体长半轴
     */
    private static final double KRASOVSKY_A = 6378245.0;
    /**
     * 克拉索夫斯基椭球体偏心率平方i
     */
    private static final double KRASOVSKY_EE = 0.00669342162296594323;

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
        return new PointHelper(Double.parseDouble(longitude), Double.parseDouble(latitude));
    }

    public static PointHelper of(BigDecimal longitude, BigDecimal latitude) {
        return new PointHelper(longitude.doubleValue(), latitude.doubleValue());
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
        return of(longitude1, latitude1).getDistanceMeters(of(longitude2, latitude2));
    }

    public static double getDistanceMeters(String longitude1, String latitude1, String longitude2, String latitude2) {
        return of(longitude1, latitude1).getDistanceMeters(of(longitude2, latitude2));
    }

    public static double getDistanceMeters(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2,
                                           BigDecimal latitude2) {
        return of(longitude1, latitude1).getDistanceMeters(of(longitude2, latitude2));
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
    public static double getDistanceKilometer(double longitude1, double latitude1, double longitude2,
                                              double latitude2) {
        return of(longitude1, latitude1).getDistanceKilometers(of(longitude2, latitude2));
    }

    public static double getDistanceKilometer(String longitude1, String latitude1, String longitude2,
                                              String latitude2) {
        return of(longitude1, latitude1).getDistanceKilometers(of(longitude2, latitude2));
    }

    public static double getDistanceKilometer(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2,
                                              BigDecimal latitude2) {
        return of(longitude1, latitude1).getDistanceKilometers(of(longitude2, latitude2));
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
     * 这是一个矩形框。中国版图并非矩形。这会导致如果在国外但在该矩形框延伸区域内（如部分邻国区域）
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标一定不在国内时返回true, 返回false代表不确定
     */
    private static boolean isOutOfChinaRectangle(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }

    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat
                + 0.2 * Math.sqrt(Math.abs(lng));
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
     * WGS84 地球半径 (米)
     */
    private static final double EARTH_RADIUS_WGS84 = 6378137.0;

    /**
     * 获取距离 (米)
     *
     * @param point 点
     * @return double
     */
    public double getDistanceMeters(PointHelper point) {
        // 用户纬度
        double userLongitude = Math.toRadians(point.getLongitude());
        double userLatitude = Math.toRadians(point.getLatitude());

        double longitude = Math.toRadians(this.longitude);
        double latitude = Math.toRadians(this.latitude);
        // 纬度之差
        double a = userLatitude - latitude;
        // 经度之差
        double b = userLongitude - longitude;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(userLatitude) * Math.cos(latitude) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘赤道半径, 返回单位: 米
        s = s * EARTH_RADIUS_WGS84;
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
        boolean result = false;
        // 防止数据异常
        if (boundaryPoints == null || boundaryPoints.length < 3) {
            return false;
        }

        // 首先判断外包矩形，快速失败
        if (!isInRectangleBoundary(boundaryPoints)) {
            return false;
        }

        int j = boundaryPoints.length - 1;
        for (int i = 0; i < boundaryPoints.length; i++) {
            PointHelper p1 = boundaryPoints[i];
            PointHelper p2 = boundaryPoints[j];

            // 1. 判断是否在线段上 (包含端点)
            if (this.isOnSegment(p1, p2)) {
                return true;
            }

            // 2. 射线法逻辑 (X轴向右射线)
            // 判断边的两端点是否在射线的两侧 (p1.lat < lat <= p2.lat 或者 p2.lat < lat <= p1.lat)
            // 仅仅当射线的一条边穿越射线时才做计算 (左闭右开 或者 左开右闭，防止顶点重复计算)
            if ((p1.getLatitude() < this.latitude && p2.getLatitude() >= this.latitude)
                    || (p2.getLatitude() < this.latitude && p1.getLatitude() >= this.latitude)) {

                // 计算交点的 X 坐标
                // x = x1 + (y - y1) * (x2 - x1) / (y2 - y1)
                // 为了避免除法精度问题，使用乘法形式判断:
                // 如果 point.x < intersection.x，则射线穿过该边。
                // 等价于: point.x - x1 < (y - y1) * (x2 - x1) / (y2 - y1)
                // 再变形(注意 y2-y1 正负号): (point.x - x1) * (y2 - y1) < (y - y1) * (x2 - x1)
                // (这也是叉积的一种形式)

                // 叉积判断: Cross(Vector(P1, P), Vector(P1, P2))
                // 如果结果与其纬度差方向不一致，说明点在交点左侧
                // 这里采用更直观的交点公式，但在除法前转为乘法比较并注意符号
                // 只有当点在交点左侧时，result 取反

                // 简单点：计算交点 intersectX
                double intersectX = p1.getLongitude() + (this.latitude - p1.getLatitude())
                        * (p2.getLongitude() - p1.getLongitude()) / (p2.getLatitude() - p1.getLatitude());

                if (this.longitude < intersectX) {
                    result = !result;
                }
            }
            j = i;
        }

        return result;
    }

    /**
     * 判断当前点是否在指定线段上 (允许误差)
     */
    private boolean isOnSegment(PointHelper p1, PointHelper p2) {
        //double precision = 2e-10; // 毫米精度
        double precision = 1e-7;// 厘米精度
        // 1. 快速排除外包矩形
        if (this.longitude < Math.min(p1.getLongitude(), p2.getLongitude()) - precision ||
                this.longitude > Math.max(p1.getLongitude(), p2.getLongitude()) + precision ||
                this.latitude < Math.min(p1.getLatitude(), p2.getLatitude()) - precision ||
                this.latitude > Math.max(p1.getLatitude(), p2.getLatitude()) + precision) {
            return false;
        }

        // 2. 叉积判断是否共线: (x - x1)*(y2 - y1) - (y - y1)*(x2 - x1)
        double crossProduct = (this.longitude - p1.getLongitude()) * (p2.getLatitude() - p1.getLatitude())
                - (this.latitude - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude());

        return Math.abs(crossProduct) < precision;
    }

    /**
     * 判断是否在多边形边P上 (公共方法需严谨)
     * <p>
     * 实际上是判断是否在以 p1, p2 为对角线的矩形范围内。
     * 原逻辑只判断了 Longitude，这是不安全的。
     * 现修正为完整的矩形范围判断。如果需要严格判断是否在线段上，建议使用新的 isOnSegment 方法。
     * 保持方法名兼容性，但修正逻辑。
     *
     * @param point1 point1 线段顶点1
     * @param point2 point2 线段顶点2
     * @return boolean
     */
    public boolean isInRectangleArea(PointHelper point1, PointHelper point2) {
        return this.getLongitude() >= Math.min(point1.getLongitude(), point2.getLongitude())
                && this.getLongitude() <= Math.max(point1.getLongitude(), point2.getLongitude())
                && this.getLatitude() >= Math.min(point1.getLatitude(), point2.getLatitude())
                && this.getLatitude() <= Math.max(point1.getLatitude(), point2.getLatitude());
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
        double z = Math.sqrt(this.longitude * this.longitude + this.latitude * this.latitude)
                + 0.00002 * Math.sin(this.latitude * x_PI);
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
            magic = 1 - KRASOVSKY_EE * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((KRASOVSKY_A * (1 - KRASOVSKY_EE)) / (magic * sqrtMagic) * PI);
            dLng = (dLng * 180.0) / (KRASOVSKY_A / sqrtMagic * Math.cos(radLat) * PI);
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
            magic = 1 - KRASOVSKY_EE * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((KRASOVSKY_A * (1 - KRASOVSKY_EE)) / (magic * sqrtMagic) * PI);
            dLng = (dLng * 180.0) / (KRASOVSKY_A / sqrtMagic * Math.cos(redLat) * PI);
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