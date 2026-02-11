package io.github.luminion.helper.geographical;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 坐标点工具类
 * <p>
 * 支持 WGS84, GCJ02, BD09 坐标系及其相互转换、距离计算、区域判断
 *
 * @author luminion
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PointHelper {

    /**
     * 坐标系常量：WGS84 (大地坐标系，GPS默认)
     */
    public static final int CS_WGS84 = 0;
    /**
     * 坐标系常量：GCJ02 (火星坐标系，高德、腾讯、谷歌中国)
     */
    public static final int CS_GCJ02 = 1;
    /**
     * 坐标系常量：BD09 (百度坐标系)
     */
    public static final int CS_BD09 = 2;

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

    /**
     * 当前坐标系类型
     */
    private final int coordinateSystem;

    // ================= Factory Methods =================

    /**
     * 构建坐标点 (默认 WGS84)
     */
    public static PointHelper of(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_WGS84);
    }

    /**
     * 构建坐标点 (指定坐标系)
     */
    public static PointHelper of(double longitude, double latitude, int coordinateSystem) {
        return new PointHelper(longitude, latitude, coordinateSystem);
    }

    public static PointHelper of(String longitude, String latitude) {
        return new PointHelper(Double.parseDouble(longitude), Double.parseDouble(latitude), CS_WGS84);
    }

    public static PointHelper of(String longitude, String latitude, int coordinateSystem) {
        return new PointHelper(Double.parseDouble(longitude), Double.parseDouble(latitude), coordinateSystem);
    }

    public static PointHelper of(BigDecimal longitude, BigDecimal latitude) {
        return new PointHelper(longitude.doubleValue(), latitude.doubleValue(), CS_WGS84);
    }

    public static PointHelper of(BigDecimal longitude, BigDecimal latitude, int coordinateSystem) {
        return new PointHelper(longitude.doubleValue(), latitude.doubleValue(), coordinateSystem);
    }

    // ================= Distance Calculations =================

    /**
     * 获取距离米 (默认视为 WGS84 进行计算)
     */
    public static double getDistanceMeters(double longitude1, double latitude1, double longitude2, double latitude2) {
        return of(longitude1, latitude1, CS_WGS84).getDistanceMeters(of(longitude2, latitude2, CS_WGS84));
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

    // ================= Static Transformation Helpers =================
    // 即使保留静态方法，内部也复用实例方法，确保逻辑统一

    public static PointHelper transformBD09ToGCJ02(PointHelper point) {
        // 如果传入的点已经是 GCJ02，直接返回，否则强制视为 BD09 并转换
        if (point.getCoordinateSystem() == CS_GCJ02) return point;
        return point.transformTo(CS_GCJ02);
    }

    public static PointHelper transformBD09ToGCJ02(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_BD09).transformTo(CS_GCJ02);
    }

    public static PointHelper transformGCJ02ToBD09(PointHelper point) {
        if (point.getCoordinateSystem() == CS_BD09) return point;
        return point.transformTo(CS_BD09);
    }

    public static PointHelper transformGCJ02ToBD09(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_GCJ02).transformTo(CS_BD09);
    }

    public static PointHelper transformGCJ02ToWGS84(PointHelper point) {
        if (point.getCoordinateSystem() == CS_WGS84) return point;
        return point.transformTo(CS_WGS84);
    }

    public static PointHelper transformGCJ02ToWGS84(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_GCJ02).transformTo(CS_WGS84);
    }

    public static PointHelper transformWGS84ToGCJ02(PointHelper point) {
        if (point.getCoordinateSystem() == CS_GCJ02) return point;
        return point.transformTo(CS_GCJ02);
    }

    public static PointHelper transformWGS84ToGCJ02(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_WGS84).transformTo(CS_GCJ02);
    }

    public static PointHelper transformBD09ToWGS84(PointHelper point) {
        if (point.getCoordinateSystem() == CS_WGS84) return point;
        return point.transformTo(CS_WGS84);
    }

    public static PointHelper transformBD09ToWGS84(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_BD09).transformTo(CS_WGS84);
    }

    public static PointHelper transformWGS84ToBD09(PointHelper point) {
        if (point.getCoordinateSystem() == CS_BD09) return point;
        return point.transformTo(CS_BD09);
    }

    public static PointHelper transformWGS84ToBD09(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CS_WGS84).transformTo(CS_BD09);
    }

    // ================= Geometry Static Helpers =================

    public static boolean isInCircle(PointHelper point, PointHelper circle, double radius) {
        return point.isInCircle(circle, radius);
    }

    public static boolean isPointInPolygon(PointHelper point, PointHelper... boundaryPoints) {
        return point.isInPolygon(boundaryPoints);
    }

    public static PointHelper getSouthWestPoint(PointHelper[] vertexes) {
        double minLng = vertexes[0].getLongitude();
        double minLat = vertexes[0].getLatitude();
        // 保持输入点的坐标系
        int sys = vertexes[0].getCoordinateSystem();

        for (PointHelper pointHelper : vertexes) {
            double x = pointHelper.getLongitude();
            double y = pointHelper.getLatitude();
            if (x < minLng) minLng = x;
            if (y < minLat) minLat = y;
        }
        return new PointHelper(minLng, minLat, sys);
    }

    public static PointHelper getNorthEastPoint(PointHelper[] vertexes) {
        double maxLng = vertexes[0].getLongitude();
        double maxLat = vertexes[0].getLatitude();
        int sys = vertexes[0].getCoordinateSystem();

        for (PointHelper pointHelper : vertexes) {
            double x = pointHelper.getLongitude();
            double y = pointHelper.getLatitude();
            if (x > maxLng) maxLng = x;
            if (y > maxLat) maxLat = y;
        }
        return new PointHelper(maxLng, maxLat, sys);
    }


    // ================= Internal Logic =================

    /**
     * 判断坐标是否不在国内
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
     * <p>
     * 逻辑修改：为了保证精度，如果两点坐标系不一致或不是WGS84，
     * 会先将两者都转换为 WGS84 坐标系，再使用公式计算。
     *
     * @param point 目标点
     * @return double
     */
    public double getDistanceMeters(PointHelper point) {
        PointHelper p1 = this;
        PointHelper p2 = point;

        // 统一转换为 WGS84 计算，保证 Haversine 公式的准确性
        if (p1.coordinateSystem != CS_WGS84) {
            p1 = p1.transformTo(CS_WGS84);
        }
        if (p2.coordinateSystem != CS_WGS84) {
            p2 = p2.transformTo(CS_WGS84);
        }

        double userLongitude = Math.toRadians(p2.getLongitude());
        double userLatitude = Math.toRadians(p2.getLatitude());
        double longitude = Math.toRadians(p1.getLongitude());
        double latitude = Math.toRadians(p1.getLatitude());

        double a = userLatitude - latitude;
        double b = userLongitude - longitude;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(userLatitude) * Math.cos(latitude) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS_WGS84;
        return s;
    }

    public double getDistanceKilometers(PointHelper point) {
        return getDistanceMeters(point) / 1000.0;
    }

    public boolean isInCircle(PointHelper circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }

    public boolean isInPolygon(List<PointHelper> boundaryPoints) {
        return isInPolygon(boundaryPoints.toArray(new PointHelper[0]));
    }

    /**
     * 判断点是否在区域内
     * 注意：此方法未强制转换坐标系，请确保当前点与边界点坐标系一致，否则结果可能不准确。
     *
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public boolean isInPolygon(PointHelper[] boundaryPoints) {
        boolean result = false;
        if (boundaryPoints == null || boundaryPoints.length < 3) {
            return false;
        }
        if (!isInRectangleBoundary(boundaryPoints)) {
            return false;
        }

        int j = boundaryPoints.length - 1;
        for (int i = 0; i < boundaryPoints.length; i++) {
            PointHelper p1 = boundaryPoints[i];
            PointHelper p2 = boundaryPoints[j];

            if (this.isOnSegment(p1, p2)) {
                return true;
            }

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

    private boolean isOnSegment(PointHelper p1, PointHelper p2) {
        double precision = 1e-7;
        if (this.longitude < Math.min(p1.getLongitude(), p2.getLongitude()) - precision ||
                this.longitude > Math.max(p1.getLongitude(), p2.getLongitude()) + precision ||
                this.latitude < Math.min(p1.getLatitude(), p2.getLatitude()) - precision ||
                this.latitude > Math.max(p1.getLatitude(), p2.getLatitude()) + precision) {
            return false;
        }
        double crossProduct = (this.longitude - p1.getLongitude()) * (p2.getLatitude() - p1.getLatitude())
                - (this.latitude - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude());
        return Math.abs(crossProduct) < precision;
    }

    public boolean isInRectangleArea(PointHelper point1, PointHelper point2) {
        return this.getLongitude() >= Math.min(point1.getLongitude(), point2.getLongitude())
                && this.getLongitude() <= Math.max(point1.getLongitude(), point2.getLongitude())
                && this.getLatitude() >= Math.min(point1.getLatitude(), point2.getLatitude())
                && this.getLatitude() <= Math.max(point1.getLatitude(), point2.getLatitude());
    }

    public boolean isInRectangleBoundary(PointHelper[] boundaryPoints) {
        PointHelper southWestPoint = getSouthWestPoint(boundaryPoints);
        PointHelper northEastPoint = getNorthEastPoint(boundaryPoints);
        return this.getLongitude() >= southWestPoint.getLongitude()
                && this.getLatitude() >= southWestPoint.getLatitude()
                && this.getLongitude() <= northEastPoint.getLongitude()
                && this.getLatitude() <= northEastPoint.getLatitude();
    }

    // ================= Transformation Logic =================

    /**
     * 通用转换方法：将当前点转换为目标坐标系
     *
     * @param targetSystem 目标坐标系 (CS_WGS84, CS_GCJ02, CS_BD09)
     * @return 新的坐标点
     */
    public PointHelper transformTo(int targetSystem) {
        if (this.coordinateSystem == targetSystem) {
            return this;
        }

        // 根据当前坐标系和目标坐标系选择路径
        switch (this.coordinateSystem) {
            case CS_WGS84:
                if (targetSystem == CS_GCJ02) return this.transformWGS84ToGCJ02();
                if (targetSystem == CS_BD09) return this.transformWGS84ToBD09();
                break;
            case CS_GCJ02:
                if (targetSystem == CS_WGS84) return this.transformGCJ02ToWGS84();
                if (targetSystem == CS_BD09) return this.transformGCJ02ToBD09();
                break;
            case CS_BD09:
                if (targetSystem == CS_GCJ02) return this.transformBD09ToGCJ02();
                if (targetSystem == CS_WGS84) return this.transformBD09ToWGS84();
                break;
            default:
                throw new IllegalArgumentException("Unknown source coordinate system: " + this.coordinateSystem);
        }
        throw new IllegalArgumentException("Unknown target coordinate system: " + targetSystem);
    }

    /**
     * 百度坐标（BD09）转 GCJ02
     * <p>注意：即使当前对象标记不是 BD09，调用此方法也会强制按 BD09 算法转换</p>
     */
    public PointHelper transformBD09ToGCJ02() {
        double x = this.longitude - 0.0065;
        double y = this.latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_PI);
        double gcj_lng = z * Math.cos(theta);
        double gcj_lat = z * Math.sin(theta);
        return new PointHelper(gcj_lng, gcj_lat, CS_GCJ02);
    }

    /**
     * GCJ02 转百度坐标
     */
    public PointHelper transformGCJ02ToBD09() {
        double z = Math.sqrt(this.longitude * this.longitude + this.latitude * this.latitude)
                + 0.00002 * Math.sin(this.latitude * x_PI);
        double theta = Math.atan2(this.latitude, this.longitude) + 0.000003 * Math.cos(this.longitude * x_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new PointHelper(bd_lng, bd_lat, CS_BD09);
    }

    /**
     * GCJ02 转 WGS84
     */
    public PointHelper transformGCJ02ToWGS84() {
        if (isOutOfChinaRectangle(this.longitude, this.latitude)) {
            return new PointHelper(this.longitude, this.latitude, CS_WGS84);
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
            return new PointHelper(this.longitude * 2 - mgLng, this.latitude * 2 - mgLat, CS_WGS84);
        }
    }

    /**
     * WGS84 坐标 转 GCJ02
     */
    public PointHelper transformWGS84ToGCJ02() {
        if (isOutOfChinaRectangle(this.longitude, this.latitude)) {
            return new PointHelper(this.longitude, this.latitude, CS_GCJ02);
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
            return new PointHelper(mgLng, mgLat, CS_GCJ02);
        }
    }

    /**
     * 百度坐标BD09 转 WGS84
     */
    public PointHelper transformBD09ToWGS84() {
        PointHelper gcj02Point = transformBD09ToGCJ02();
        return gcj02Point.transformGCJ02ToWGS84();
    }

    /**
     * WGS84 转 百度坐标BD09
     */
    public PointHelper transformWGS84ToBD09() {
        PointHelper gcj02Point = transformWGS84ToGCJ02();
        return gcj02Point.transformGCJ02ToBD09();
    }
}