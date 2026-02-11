package io.github.luminion.helper.geographical;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 坐标点工具类
 *
 * 支持：
 * - WGS84 / GCJ02 / BD09 坐标系标记
 * - 距离计算（内部自动转为 WGS84 进行计算）
 * - 点在圆内、多边形内判断
 * - 坐标系相互转换：toWGS84 / toGCJ02 / toBD09 / to(CoordinateSystem)
 *
 * 注意：
 * - PointHelper 实例内部的经纬度值始终对应其 {@link CoordinateSystem} 字段标记的坐标系
 * - 距离计算会将双方转换为 WGS84 后再计算
 * - 多边形判断默认要求「当前点」和「多边形所有顶点」使用同一坐标系
 *
 * @author lumin…
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PointHelper {

    /**
     * 坐标系
     */
    public enum CoordinateSystem {
        /**
         * WGS84（GPS 原始坐标，国际通用）
         */
        WGS84,
        /**
         * GCJ02（火星坐标，高德/腾讯等常用）
         */
        GCJ02,
        /**
         * BD09（百度坐标）
         */
        BD09
    }

    // ==================== 常量 ====================

    private static final double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double PI = 3.1415926535897932384626;

    /**
     * 克拉索夫斯基椭球体长半轴（GCJ 偏移算法使用）
     */
    private static final double KRASOVSKY_A = 6378245.0;
    /**
     * 克拉索夫斯基椭球体偏心率平方
     */
    private static final double KRASOVSKY_EE = 0.00669342162296594323;

    /**
     * WGS84 椭球体近似半径 (米) —— 用于 Haversine 距离
     */
    private static final double EARTH_RADIUS_WGS84 = 6378137.0;

    /**
     * 线段共线判断的精度（度）
     * 1e-7 度 ≈ 1.1cm
     */
    private static final double SEGMENT_PRECISION = 1e-7;

    // ==================== 字段 ====================

    /**
     * 经度（单位：度），含义取决于 {@link #coordinateSystem}
     */
    private final double longitude;

    /**
     * 纬度（单位：度），含义取决于 {@link #coordinateSystem}
     */
    private final double latitude;

    /**
     * 坐标系
     */
    private final CoordinateSystem coordinateSystem;

    // ==================== 工厂方法 ====================

    /**
     * 默认按 WGS84 创建
     */
    public static PointHelper of(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static PointHelper of(String longitude, String latitude) {
        return of(Double.parseDouble(longitude), Double.parseDouble(latitude));
    }

    public static PointHelper of(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude.doubleValue(), latitude.doubleValue());
    }

    /**
     * 指定坐标系创建
     */
    public static PointHelper of(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        return new PointHelper(longitude, latitude, coordinateSystem);
    }

    public static PointHelper of(String longitude, String latitude, CoordinateSystem coordinateSystem) {
        return of(Double.parseDouble(longitude), Double.parseDouble(latitude), coordinateSystem);
    }

    public static PointHelper of(BigDecimal longitude, BigDecimal latitude, CoordinateSystem coordinateSystem) {
        return of(longitude.doubleValue(), latitude.doubleValue(), coordinateSystem);
    }

    // ==================== 距离计算（静态） ====================

    /**
     * 获取距离米
     */
    public static double getDistanceMeters(double longitude1, double latitude1,
                                           double longitude2, double latitude2) {
        return of(longitude1, latitude1).getDistanceMeters(of(longitude2, latitude2));
    }

    public static double getDistanceMeters(String longitude1, String latitude1,
                                           String longitude2, String latitude2) {
        return of(longitude1, latitude1).getDistanceMeters(of(longitude2, latitude2));
    }

    public static double getDistanceMeters(BigDecimal longitude1, BigDecimal latitude1,
                                           BigDecimal longitude2, BigDecimal latitude2) {
        return of(longitude1, latitude1).getDistanceMeters(of(longitude2, latitude2));
    }

    public static double getDistanceMeters(PointHelper point1, PointHelper point2) {
        return point1.getDistanceMeters(point2);
    }

    /**
     * 获取距离千米（假定入参为 WGS84）
     */
    public static double getDistanceKilometer(double longitude1, double latitude1,
                                              double longitude2, double latitude2) {
        return of(longitude1, latitude1).getDistanceKilometers(of(longitude2, latitude2));
    }

    public static double getDistanceKilometer(String longitude1, String latitude1,
                                              String longitude2, String latitude2) {
        return of(longitude1, latitude1).getDistanceKilometers(of(longitude2, latitude2));
    }

    public static double getDistanceKilometer(BigDecimal longitude1, BigDecimal latitude1,
                                              BigDecimal longitude2, BigDecimal latitude2) {
        return of(longitude1, latitude1).getDistanceKilometers(of(longitude2, latitude2));
    }

    public static double getDistanceKilometer(PointHelper point1, PointHelper point2) {
        return point1.getDistanceKilometers(point2);
    }

    // ==================== 坐标系转换（静态入口） ====================

    public static PointHelper toWGS84(PointHelper point) {
        return point.toWGS84();
    }

    public static PointHelper toGCJ02(PointHelper point) {
        return point.toGCJ02();
    }

    public static PointHelper toBD09(PointHelper point) {
        return point.toBD09();
    }

    public static PointHelper toWGS84(double longitude, double latitude, CoordinateSystem cs) {
        return of(longitude, latitude, cs).toWGS84();
    }

    public static PointHelper toGCJ02(double longitude, double latitude, CoordinateSystem cs) {
        return of(longitude, latitude, cs).toGCJ02();
    }

    public static PointHelper toBD09(double longitude, double latitude, CoordinateSystem cs) {
        return of(longitude, latitude, cs).toBD09();
    }

    // ==================== 实例方法：距离 ====================

    /**
     * 获取距离 (米)
     * <p>内部会将双方统一转换为 WGS84 后，再用 Haversine 公式计算</p>
     *
     * @param point 点
     * @return 距离（米）
     */
    public double getDistanceMeters(PointHelper point) {
        PointHelper p1 = this.toWGS84();
        PointHelper p2 = point.toWGS84();

        double lng1 = Math.toRadians(p1.getLongitude());
        double lat1 = Math.toRadians(p1.getLatitude());
        double lng2 = Math.toRadians(p2.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());

        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLng / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return c * EARTH_RADIUS_WGS84;
    }

    /**
     * 获取距离 (千米)
     *
     * @param point 点
     * @return 距离（千米）
     */
    public double getDistanceKilometers(PointHelper point) {
        return getDistanceMeters(point) / 1000.0;
    }

    /**
     * 是否在圆圈中（内部使用 WGS84 计算距离）
     *
     * @param circle 圆心
     * @param radius 半径(米)
     * @return boolean
     */
    public boolean isInCircle(PointHelper circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }

    // ==================== 多边形 / 矩形 ====================

    /**
     * 是否在指定区域内(基本思路是用交点法)
     * <p>注意：当前点与 boundaryPoints 必须使用同一坐标系，否则结果不可靠</p>
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInPolygon(List<PointHelper> boundaryPoints) {
        if (boundaryPoints == null || boundaryPoints.size() < 3) {
            return false;
        }
        return isInPolygon(boundaryPoints.toArray(new PointHelper[0]));
    }

    /**
     * 判断点是否在区域内
     * <p>注意：当前点与 boundaryPoints 必须使用同一坐标系，否则结果不可靠</p>
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInPolygon(PointHelper[] boundaryPoints) {
        boolean result = false;
        if (boundaryPoints == null || boundaryPoints.length < 3) {
            return false;
        }

        // 外包矩形快速失败
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
            if ((p1.getLatitude() < this.latitude && p2.getLatitude() >= this.latitude)
                    || (p2.getLatitude() < this.latitude && p1.getLatitude() >= this.latitude)) {

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
        double precision = SEGMENT_PRECISION;
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
     * 判断是否在多边形边所在的矩形区域内
     *
     * @param point1 线段顶点1
     * @param point2 线段顶点2
     * @return boolean
     */
    public boolean isInRectangleArea(PointHelper point1, PointHelper point2) {
        return this.longitude >= Math.min(point1.getLongitude(), point2.getLongitude())
                && this.longitude <= Math.max(point1.getLongitude(), point2.getLongitude())
                && this.latitude >= Math.min(point1.getLatitude(), point2.getLatitude())
                && this.latitude <= Math.max(point1.getLatitude(), point2.getLatitude());
    }

    /**
     * 判断是否在指定坐标外包矩形内(根据这些点，构造一个外包矩形)
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInRectangleBoundary(PointHelper[] boundaryPoints) {
        PointHelper southWestPoint = getSouthWestPoint(boundaryPoints);
        PointHelper northEastPoint = getNorthEastPoint(boundaryPoints);
        boolean b1 = this.longitude >= southWestPoint.getLongitude();
        boolean b2 = this.latitude >= southWestPoint.getLatitude();
        boolean b3 = this.longitude <= northEastPoint.getLongitude();
        boolean b4 = this.latitude <= northEastPoint.getLatitude();
        return b1 && b2 && b3 && b4;
    }

    /**
     * 根据这组坐标，画一个矩形，然后得到这个矩形西南角的顶点坐标
     *
     * @param vertexes 顶点（至少一个）
     * @return 西南角顶点
     */
    public static PointHelper getSouthWestPoint(PointHelper[] vertexes) {
        if (vertexes == null || vertexes.length == 0) {
            throw new IllegalArgumentException("vertexes 不能为空");
        }
        double minLng = vertexes[0].getLongitude();
        double minLat = vertexes[0].getLatitude();
        CoordinateSystem cs = vertexes[0].getCoordinateSystem();
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
        return new PointHelper(minLng, minLat, cs);
    }

    /**
     * 根据这组坐标，画一个矩形，然后得到这个矩形东北角的顶点坐标
     *
     * @param vertexes 顶点（至少一个）
     * @return 东北角顶点
     */
    public static PointHelper getNorthEastPoint(PointHelper[] vertexes) {
        if (vertexes == null || vertexes.length == 0) {
            throw new IllegalArgumentException("vertexes 不能为空");
        }
        double maxLng = vertexes[0].getLongitude();
        double maxLat = vertexes[0].getLatitude();
        CoordinateSystem cs = vertexes[0].getCoordinateSystem();
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
        return new PointHelper(maxLng, maxLat, cs);
    }

    // ==================== 坐标系转换（实例方法） ====================

    /**
     * 转换为 WGS84 坐标
     */
    public PointHelper toWGS84() {
        switch (this.coordinateSystem) {
            case WGS84:
                return this;
            case GCJ02:
                return gcj02ToWGS84Internal();
            case BD09:
                return bd09ToGCJ02Internal().gcj02ToWGS84Internal();
            default:
                return this;
        }
    }

    /**
     * 转换为 GCJ02 坐标
     */
    public PointHelper toGCJ02() {
        switch (this.coordinateSystem) {
            case GCJ02:
                return this;
            case WGS84:
                return wgs84ToGCJ02Internal();
            case BD09:
                return bd09ToGCJ02Internal();
            default:
                return this;
        }
    }

    /**
     * 转换为 BD09 坐标
     */
    public PointHelper toBD09() {
        switch (this.coordinateSystem) {
            case BD09:
                return this;
            case GCJ02:
                return gcj02ToBD09Internal();
            case WGS84:
                // WGS84 -> GCJ02 -> BD09
                return wgs84ToGCJ02Internal().gcj02ToBD09Internal();
            default:
                return this;
        }
    }

    /**
     * 转换为目标坐标系
     */
    public PointHelper to(CoordinateSystem target) {
        if (target == null || target == this.coordinateSystem) {
            return this;
        }
        switch (target) {
            case WGS84:
                return toWGS84();
            case GCJ02:
                return toGCJ02();
            case BD09:
                return toBD09();
            default:
                return this;
        }
    }

    // ====== 各种具体转换内部实现（不做坐标系判断，只假定当前坐标系正确） ======

    /**
     * 当前为 BD09：转 GCJ02
     */
    private PointHelper bd09ToGCJ02Internal() {
        double x = this.longitude - 0.0065;
        double y = this.latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_PI);
        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        return new PointHelper(gcjLng, gcjLat, CoordinateSystem.GCJ02);
    }

    /**
     * 当前为 GCJ02：转 BD09
     */
    private PointHelper gcj02ToBD09Internal() {
        double z = Math.sqrt(this.longitude * this.longitude + this.latitude * this.latitude)
                + 0.00002 * Math.sin(this.latitude * x_PI);
        double theta = Math.atan2(this.latitude, this.longitude) + 0.000003 * Math.cos(this.longitude * x_PI);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new PointHelper(bdLng, bdLat, CoordinateSystem.BD09);
    }

    /**
     * 当前为 GCJ02：转 WGS84（近似逆解）
     */
    private PointHelper gcj02ToWGS84Internal() {
        if (isOutOfChinaRectangle(this.longitude, this.latitude)) {
            return new PointHelper(this.longitude, this.latitude, CoordinateSystem.WGS84);
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
            double wgsLat = this.latitude * 2 - mgLat;
            double wgsLng = this.longitude * 2 - mgLng;
            return new PointHelper(wgsLng, wgsLat, CoordinateSystem.WGS84);
        }
    }

    /**
     * 当前为 WGS84：转 GCJ02
     */
    private PointHelper wgs84ToGCJ02Internal() {
        if (isOutOfChinaRectangle(this.longitude, this.latitude)) {
            return new PointHelper(this.longitude, this.latitude, CoordinateSystem.GCJ02);
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
            return new PointHelper(mgLng, mgLat, CoordinateSystem.GCJ02);
        }
    }

    // ==================== 私有工具方法 ====================

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
}