package io.github.luminion.helper.geo;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * 地理坐标静态入口。
 * <p>
 * 负责创建 {@link GeoPoint}，以及提供坐标系转换、距离计算和几何判断的快捷调用。
 *
 * @author luminion
 */
public abstract class GeoHelper {
    private GeoHelper() {}

    /**
     * 以数值形式创建坐标点。
     */
    public static GeoPoint of(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        return new GeoPoint(longitude, latitude, coordinateSystem);
    }

    /**
     * 以字符串形式创建坐标点。
     */
    public static GeoPoint of(String longitude, String latitude, CoordinateSystem coordinateSystem) {
        return of(
                GeoArguments.parseTextCoordinate(longitude, "经度"),
                GeoArguments.parseTextCoordinate(latitude, "纬度"),
                coordinateSystem
        );
    }

    /**
     * 以 {@link BigDecimal} 形式创建坐标点。
     */
    public static GeoPoint of(BigDecimal longitude, BigDecimal latitude, CoordinateSystem coordinateSystem) {
        return of(
                GeoArguments.requireDecimalCoordinate(longitude, "经度"),
                GeoArguments.requireDecimalCoordinate(latitude, "纬度"),
                coordinateSystem
        );
    }

    public static GeoPoint ofWGS84(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoPoint ofWGS84(String longitude, String latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoPoint ofWGS84(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoPoint ofGCJ02(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoPoint ofGCJ02(String longitude, String latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoPoint ofGCJ02(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoPoint ofBD09(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    public static GeoPoint ofBD09(String longitude, String latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    public static GeoPoint ofBD09(BigDecimal longitude, BigDecimal latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    /**
     * 校验经纬度范围是否合法。
     */
    public static boolean isValid(double longitude, double latitude) {
        return longitude >= -180.0 && longitude <= 180.0 && latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * 计算两个 WGS84 坐标之间的球面距离，单位米。
     */
    public static double getDistanceMetersWgs84(double longitude1, double latitude1, double longitude2, double latitude2) {
        return GeoDistanceCalculator.getDistanceMetersWgs84(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersWgs84(String longitude1, String latitude1, String longitude2, String latitude2) {
        return getDistanceMetersWgs84(
                GeoArguments.parseTextCoordinate(longitude1, "起点经度"),
                GeoArguments.parseTextCoordinate(latitude1, "起点纬度"),
                GeoArguments.parseTextCoordinate(longitude2, "终点经度"),
                GeoArguments.parseTextCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersWgs84(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersWgs84(
                GeoArguments.requireDecimalCoordinate(longitude1, "起点经度"),
                GeoArguments.requireDecimalCoordinate(latitude1, "起点纬度"),
                GeoArguments.requireDecimalCoordinate(longitude2, "终点经度"),
                GeoArguments.requireDecimalCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersGcj02(double longitude1, double latitude1, double longitude2, double latitude2) {
        return GeoDistanceCalculator.getDistanceMetersGcj02(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersGcj02(String longitude1, String latitude1, String longitude2, String latitude2) {
        return getDistanceMetersGcj02(
                GeoArguments.parseTextCoordinate(longitude1, "起点经度"),
                GeoArguments.parseTextCoordinate(latitude1, "起点纬度"),
                GeoArguments.parseTextCoordinate(longitude2, "终点经度"),
                GeoArguments.parseTextCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersGcj02(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersGcj02(
                GeoArguments.requireDecimalCoordinate(longitude1, "起点经度"),
                GeoArguments.requireDecimalCoordinate(latitude1, "起点纬度"),
                GeoArguments.requireDecimalCoordinate(longitude2, "终点经度"),
                GeoArguments.requireDecimalCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersBd09(double longitude1, double latitude1, double longitude2, double latitude2) {
        return GeoDistanceCalculator.getDistanceMetersBd09(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersBd09(String longitude1, String latitude1, String longitude2, String latitude2) {
        return getDistanceMetersBd09(
                GeoArguments.parseTextCoordinate(longitude1, "起点经度"),
                GeoArguments.parseTextCoordinate(latitude1, "起点纬度"),
                GeoArguments.parseTextCoordinate(longitude2, "终点经度"),
                GeoArguments.parseTextCoordinate(latitude2, "终点纬度")
        );
    }

    public static double getDistanceMetersBd09(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersBd09(
                GeoArguments.requireDecimalCoordinate(longitude1, "起点经度"),
                GeoArguments.requireDecimalCoordinate(latitude1, "起点纬度"),
                GeoArguments.requireDecimalCoordinate(longitude2, "终点经度"),
                GeoArguments.requireDecimalCoordinate(latitude2, "终点纬度")
        );
    }

    /**
     * 按坐标对象统一计算距离，内部会先转换到 WGS84 再计算。
     */
    public static double getDistanceMeters(GeoPoint point1, GeoPoint point2) {
        return GeoDistanceCalculator.getDistanceMeters(point1, point2);
    }

    public static GeoPoint getSouthWestPoint(GeoPoint[] vertexes) {
        return GeoGeometryHelper.getSouthWestPoint(vertexes);
    }

    public static GeoPoint getSouthWestPoint(Collection<GeoPoint> vertexes) {
        return getSouthWestPoint(GeoArguments.toArray(vertexes));
    }

    public static GeoPoint getNorthEastPoint(GeoPoint[] vertexes) {
        return GeoGeometryHelper.getNorthEastPoint(vertexes);
    }

    public static GeoPoint getNorthEastPoint(Collection<GeoPoint> vertexes) {
        return getNorthEastPoint(GeoArguments.toArray(vertexes));
    }

    public static GeoPoint wgs84ToGcj02(double lng, double lat) {
        return GeoCoordinateConverter.wgs84ToGcj02(lng, lat);
    }

    public static GeoPoint gcj02ToWgs84(double lng, double lat) {
        return GeoCoordinateConverter.gcj02ToWgs84(lng, lat);
    }

    public static GeoPoint gcj02ToBd09(double lng, double lat) {
        return GeoCoordinateConverter.gcj02ToBd09(lng, lat);
    }

    public static GeoPoint bd09ToGcj02(double lng, double lat) {
        return GeoCoordinateConverter.bd09ToGcj02(lng, lat);
    }
}
