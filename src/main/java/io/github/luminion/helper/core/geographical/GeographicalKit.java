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



}
