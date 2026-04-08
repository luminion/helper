package io.github.luminion.helper.geographical;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 坐标点门面类。
 *
 * <p>GeoHelper 负责表达一个不可变坐标点，并提供统一的工厂方法与常用入口。
 * 坐标转换、距离计算、几何判断等实现已拆分到包内协作类，避免单类职责过重。</p>
 *
 * @author luminion
 */
@Getter
@EqualsAndHashCode
public class GeoHelper {

    /**
     * 坐标系类型
     */
    public enum CoordinateSystem {
        /**
         * WGS84（GPS 原始坐标，国际通用）
         */
        WGS84,
        /**
         * GCJ02（火星坐标）
         */
        GCJ02,
        /**
         * BD09（百度坐标）
         */
        BD09
    }

    /**
     * 经度（单位：度）
     */
    private final double longitude;

    /**
     * 纬度（单位：度）
     */
    private final double latitude;

    /**
     * 坐标系
     */
    private final CoordinateSystem coordinateSystem;

    private GeoHelper(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        GeoArguments.validateCoordinate(longitude, latitude, coordinateSystem);
        this.longitude = longitude;
        this.latitude = latitude;
        this.coordinateSystem = coordinateSystem;
    }

    public static GeoHelper of(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        return new GeoHelper(longitude, latitude, coordinateSystem);
    }

    public static GeoHelper of(String longitude, String latitude, CoordinateSystem coordinateSystem) {
        return of(
                GeoArguments.parseTextCoordinate(longitude, "经度"),
                GeoArguments.parseTextCoordinate(latitude, "纬度"),
                coordinateSystem
        );
    }

    public static GeoHelper of(BigDecimal longitude, BigDecimal latitude, CoordinateSystem coordinateSystem) {
        return of(
                GeoArguments.requireDecimalCoordinate(longitude, "经度"),
                GeoArguments.requireDecimalCoordinate(latitude, "纬度"),
                coordinateSystem
        );
    }

    public static GeoHelper ofWGS84(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoHelper ofWGS84(String longitude, String latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoHelper ofWGS84(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoHelper ofGCJ02(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoHelper ofGCJ02(String longitude, String latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoHelper ofGCJ02(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoHelper ofBD09(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    public static GeoHelper ofBD09(String longitude, String latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    public static GeoHelper ofBD09(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    public static boolean isValid(double longitude, double latitude) {
        return longitude >= -180.0 && longitude <= 180.0 && latitude >= -90.0 && latitude <= 90.0;
    }

    public static double getDistanceMetersWgs84(double longitude1, double latitude1, double longitude2,
                                                double latitude2) {
        return GeoDistanceCalculator.getDistanceMetersWgs84(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersWgs84(String longitude1, String latitude1, String longitude2,
                                                String latitude2) {
        return getDistanceMetersWgs84(
                GeoArguments.parseTextCoordinate(longitude1, "起点经度"),
                GeoArguments.parseTextCoordinate(latitude1, "起点纬度"),
                GeoArguments.parseTextCoordinate(longitude2, "终点经度"),
                GeoArguments.parseTextCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersWgs84(BigDecimal longitude1, BigDecimal latitude1,
                                                BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersWgs84(
                GeoArguments.requireDecimalCoordinate(longitude1, "起点经度"),
                GeoArguments.requireDecimalCoordinate(latitude1, "起点纬度"),
                GeoArguments.requireDecimalCoordinate(longitude2, "终点经度"),
                GeoArguments.requireDecimalCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersGcj02(double longitude1, double latitude1, double longitude2,
                                                double latitude2) {
        return GeoDistanceCalculator.getDistanceMetersGcj02(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersGcj02(String longitude1, String latitude1, String longitude2,
                                                String latitude2) {
        return getDistanceMetersGcj02(
                GeoArguments.parseTextCoordinate(longitude1, "起点经度"),
                GeoArguments.parseTextCoordinate(latitude1, "起点纬度"),
                GeoArguments.parseTextCoordinate(longitude2, "终点经度"),
                GeoArguments.parseTextCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersGcj02(BigDecimal longitude1, BigDecimal latitude1,
                                                BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersGcj02(
                GeoArguments.requireDecimalCoordinate(longitude1, "起点经度"),
                GeoArguments.requireDecimalCoordinate(latitude1, "起点纬度"),
                GeoArguments.requireDecimalCoordinate(longitude2, "终点经度"),
                GeoArguments.requireDecimalCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersBd09(double longitude1, double latitude1, double longitude2,
                                               double latitude2) {
        return GeoDistanceCalculator.getDistanceMetersBd09(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersBd09(String longitude1, String latitude1, String longitude2,
                                               String latitude2) {
        return getDistanceMetersBd09(
                GeoArguments.parseTextCoordinate(longitude1, "起点经度"),
                GeoArguments.parseTextCoordinate(latitude1, "起点纬度"),
                GeoArguments.parseTextCoordinate(longitude2, "终点经度"),
                GeoArguments.parseTextCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersBd09(BigDecimal longitude1, BigDecimal latitude1,
                                               BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersBd09(
                GeoArguments.requireDecimalCoordinate(longitude1, "起点经度"),
                GeoArguments.requireDecimalCoordinate(latitude1, "起点纬度"),
                GeoArguments.requireDecimalCoordinate(longitude2, "终点经度"),
                GeoArguments.requireDecimalCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMeters(GeoHelper point1, GeoHelper point2) {
        return GeoDistanceCalculator.getDistanceMeters(point1, point2);
    }

    public static GeoHelper getSouthWestPoint(GeoHelper[] vertexes) {
        return GeoGeometryHelper.getSouthWestPoint(vertexes);
    }

    public static GeoHelper getSouthWestPoint(Collection<GeoHelper> vertexes) {
        return getSouthWestPoint(GeoArguments.toArray(vertexes));
    }

    public static GeoHelper getNorthEastPoint(GeoHelper[] vertexes) {
        return GeoGeometryHelper.getNorthEastPoint(vertexes);
    }

    public static GeoHelper getNorthEastPoint(Collection<GeoHelper> vertexes) {
        return getNorthEastPoint(GeoArguments.toArray(vertexes));
    }

    public static GeoHelper wgs84ToGcj02(double lng, double lat) {
        return GeoCoordinateConverter.wgs84ToGcj02(lng, lat);
    }

    public static GeoHelper gcj02ToWgs84(double lng, double lat) {
        return GeoCoordinateConverter.gcj02ToWgs84(lng, lat);
    }

    public static GeoHelper gcj02ToBd09(double lng, double lat) {
        return GeoCoordinateConverter.gcj02ToBd09(lng, lat);
    }

    public static GeoHelper bd09ToGcj02(double lng, double lat) {
        return GeoCoordinateConverter.bd09ToGcj02(lng, lat);
    }

    public double getDistanceMeters(GeoHelper point) {
        return GeoDistanceCalculator.getDistanceMeters(this, point);
    }

    public double getDistanceKilometers(GeoHelper point) {
        return getDistanceMeters(point) / 1000.0;
    }

    public boolean isInCircle(GeoHelper circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }

    public boolean isInPolygon(List<GeoHelper> boundaryPoints) {
        return GeoGeometryHelper.isInPolygon(this, boundaryPoints);
    }

    public boolean isInRectangleArea(GeoHelper point1, GeoHelper point2) {
        return GeoGeometryHelper.isInRectangleArea(this, point1, point2);
    }

    public boolean isInRectangleBoundary(GeoHelper[] boundaryPoints) {
        return GeoGeometryHelper.isInRectangleBoundary(this, boundaryPoints);
    }

    public boolean isInRectangleBoundary(Collection<GeoHelper> boundaryPoints) {
        return boundaryPoints != null && !boundaryPoints.isEmpty()
                && isInRectangleBoundary(GeoArguments.toArray(boundaryPoints));
    }

    public GeoHelper toWGS84() {
        return GeoCoordinateConverter.toWGS84(this);
    }

    public GeoHelper toGCJ02() {
        return GeoCoordinateConverter.toGCJ02(this);
    }

    public GeoHelper toBD09() {
        return GeoCoordinateConverter.toBD09(this);
    }

    public GeoHelper to(CoordinateSystem target) {
        if (target == null || target == this.coordinateSystem) {
            return this;
        }
        return GeoCoordinateConverter.to(this, target);
    }

    public double[] toArray() {
        return new double[] {longitude, latitude};
    }

    public String toLngLatString() {
        return longitude + "," + latitude;
    }

    @Override
    public String toString() {
        return toLngLatString() + " [" + coordinateSystem + "]";
    }
}
