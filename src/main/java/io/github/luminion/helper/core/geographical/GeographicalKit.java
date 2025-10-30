package io.github.luminion.helper.core.geographical;

import java.math.BigDecimal;

/**
 * 地理地图工具
 *
 * @author luminion
 */
@SuppressWarnings("unused")
public abstract class GeographicalKit {

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
     * 判断点是否在区域内
     *
     * @param point          点
     * @param boundaryPoints 边界点
     * @return boolean
     */
    public static boolean isPointInPolygon(GeoPoint point, GeoPoint... boundaryPoints) {
        return point.isInsidePolygon(boundaryPoints);
    }


}
