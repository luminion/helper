package io.github.luminion.helper.core.geographical;

import java.math.BigDecimal;

/**
 * 地理地图工具
 *
 * @author luminion
 */
@SuppressWarnings("unused")
public abstract class GeographicalKit {

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
        return new GeoPoint(longitude1, latitude1).getDistanceMeters(new GeoPoint(longitude2, latitude2));
    }

    public static double getDistanceMeters(String longitude1, String latitude1, String longitude2, String latitude2) {
        return new GeoPoint(longitude1, latitude1).getDistanceMeters(new GeoPoint(longitude2, latitude2));
    }

    public static double getDistanceMeters(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return new GeoPoint(longitude1, latitude1).getDistanceMeters(new GeoPoint(longitude2, latitude2));
    }

    public static double getDistanceMeters(GeoPoint point1, GeoPoint point2) {
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
        return new GeoPoint(longitude1, latitude1).getDistanceKilometers(new GeoPoint(longitude2, latitude2));
    }

    public static double getDistanceKilometer(String longitude1, String latitude1, String longitude2, String latitude2) {
        return new GeoPoint(longitude1, latitude1).getDistanceKilometers(new GeoPoint(longitude2, latitude2));
    }

    public static double getDistanceKilometer(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return new GeoPoint(longitude1, latitude1).getDistanceKilometers(new GeoPoint(longitude2, latitude2));
    }

    public static double getDistanceKilometer(GeoPoint point1, GeoPoint point2) {
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
    public static boolean isInCircle(GeoPoint point, GeoPoint circle, double radius) {
        return point.isInCircle(circle, radius);
    }

    /**
     * 判断点是否在区域内
     *
     * @param point          点
     * @param boundaryPoints 区域边界顶点
     * @return boolean
     */
    public static boolean isPointInPolygon(GeoPoint point, GeoPoint... boundaryPoints) {
        return point.isInPolygon(boundaryPoints);
    }

    /**
     * 百度坐标（BD09）转 GCJ02
     *
     * @param point BD09坐标点
     * @return GCJ02 坐标点
     */
    public static GeoPoint transformBD09ToGCJ02(GeoPoint point) {
        return point.transformBD09ToGCJ02();
    }

    public static GeoPoint transformBD09ToGCJ02(double longitude, double latitude) {
        return new GeoPoint(longitude, latitude).transformBD09ToGCJ02();
    }

    /**
     * GCJ02 转百度坐标
     *
     * @param point GCJ02坐标点
     * @return 百度坐标点
     */
    public static GeoPoint transformGCJ02ToBD09(GeoPoint point) {
        return point.transformGCJ02ToBD09();
    }

    public static GeoPoint transformGCJ02ToBD09(double longitude, double latitude) {
        return new GeoPoint(longitude, latitude).transformGCJ02ToBD09();
    }

    /**
     * GCJ02 转 WGS84
     *
     * @param point GCJ02坐标点
     * @return WGS84坐标点
     */
    public static GeoPoint transformGCJ02ToWGS84(GeoPoint point) {
        return point.transformGCJ02ToWGS84();
    }

    public static GeoPoint transformGCJ02ToWGS84(double longitude, double latitude) {
        return new GeoPoint(longitude, latitude).transformGCJ02ToWGS84();
    }

    /**
     * WGS84 坐标 转 GCJ02
     *
     * @param point WGS84坐标点
     * @return GCJ02 坐标点
     */
    public static GeoPoint transformWGS84ToGCJ02(GeoPoint point) {
        return point.transformWGS84ToGCJ02();
    }

    public static GeoPoint transformWGS84ToGCJ02(double longitude, double latitude) {
        return new GeoPoint(longitude, latitude).transformWGS84ToGCJ02();
    }

    /**
     * 百度坐标BD09 转 WGS84
     *
     * @param point BD09坐标点
     * @return WGS84 坐标点
     */
    public static GeoPoint transformBD09ToWGS84(GeoPoint point) {
        return point.transformBD09ToWGS84();
    }

    public static GeoPoint transformBD09ToWGS84(double longitude, double latitude) {
        return new GeoPoint(longitude, latitude).transformBD09ToWGS84();
    }

    /**
     * WGS84 转 百度坐标BD09
     *
     * @param point WGS84坐标点
     * @return BD09 坐标点
     */
    public static GeoPoint transformWGS84ToBD09(GeoPoint point) {
        return point.transformWGS84ToBD09();
    }

    public static GeoPoint transformWGS84ToBD09(double longitude, double latitude) {
        return new GeoPoint(longitude, latitude).transformWGS84ToBD09();
    }

}
