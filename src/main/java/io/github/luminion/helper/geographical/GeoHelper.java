package io.github.luminion.helper.geographical;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 地理坐标工具类。
 *
 * <p>保留三类核心能力：距离计算、图形内判断、坐标系转换。</p>
 *
 * @author luminion
 */
@Getter
@EqualsAndHashCode
public class GeoHelper {

    public enum CoordinateSystem {
        WGS84,
        GCJ02,
        BD09
    }

    private static final double EARTH_RADIUS_METERS = 6371008.8;
    private static final double GCJ_AXIS = 6378245.0;
    private static final double GCJ_EE = 0.00669342162296594323;
    private static final double BD09_PI = Math.PI * 3000.0 / 180.0;
    private static final double SEGMENT_PRECISION = 1e-7;

    private final double longitude;
    private final double latitude;
    private final CoordinateSystem coordinateSystem;

    private GeoHelper(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        validateCoordinate(longitude, latitude);
        if (coordinateSystem == null) {
            throw new IllegalArgumentException("coordinateSystem is null");
        }
        this.longitude = longitude;
        this.latitude = latitude;
        this.coordinateSystem = coordinateSystem;
    }

    public static GeoHelper of(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        return new GeoHelper(longitude, latitude, coordinateSystem);
    }

    public static GeoHelper ofWGS84(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.WGS84);
    }

    public static GeoHelper ofGCJ02(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.GCJ02);
    }

    public static GeoHelper ofBD09(double longitude, double latitude) {
        return of(longitude, latitude, CoordinateSystem.BD09);
    }

    public static boolean isValid(double longitude, double latitude) {
        return longitude >= -180.0 && longitude <= 180.0 && latitude >= -90.0 && latitude <= 90.0;
    }

    public static double getDistanceMeters(GeoHelper point1, GeoHelper point2) {
        requirePoint(point1, "point1");
        requirePoint(point2, "point2");
        GeoHelper wgs84Point1 = point1.toWGS84();
        GeoHelper wgs84Point2 = point2.toWGS84();
        return haversineMeters(
                wgs84Point1.longitude,
                wgs84Point1.latitude,
                wgs84Point2.longitude,
                wgs84Point2.latitude
        );
    }

    public static GeoHelper wgs84ToGcj02(double longitude, double latitude) {
        validateCoordinate(longitude, latitude);
        if (isOutOfChina(longitude, latitude)) {
            return ofGCJ02(longitude, latitude);
        }
        double deltaLatitude = transformLatitude(longitude - 105.0, latitude - 35.0);
        double deltaLongitude = transformLongitude(longitude - 105.0, latitude - 35.0);
        double radLatitude = Math.toRadians(latitude);
        double magic = Math.sin(radLatitude);
        magic = 1 - GCJ_EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        deltaLatitude = (deltaLatitude * 180.0)
                / ((GCJ_AXIS * (1 - GCJ_EE)) / (magic * sqrtMagic) * Math.PI);
        deltaLongitude = (deltaLongitude * 180.0)
                / (GCJ_AXIS / sqrtMagic * Math.cos(radLatitude) * Math.PI);
        return ofGCJ02(
                normalizeLongitude(longitude + deltaLongitude),
                normalizeLatitude(latitude + deltaLatitude)
        );
    }

    public static GeoHelper gcj02ToWgs84(double longitude, double latitude) {
        validateCoordinate(longitude, latitude);
        if (isOutOfChina(longitude, latitude)) {
            return ofWGS84(longitude, latitude);
        }
        double deltaLatitude = transformLatitude(longitude - 105.0, latitude - 35.0);
        double deltaLongitude = transformLongitude(longitude - 105.0, latitude - 35.0);
        double radLatitude = Math.toRadians(latitude);
        double magic = Math.sin(radLatitude);
        magic = 1 - GCJ_EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        deltaLatitude = (deltaLatitude * 180.0)
                / ((GCJ_AXIS * (1 - GCJ_EE)) / (magic * sqrtMagic) * Math.PI);
        deltaLongitude = (deltaLongitude * 180.0)
                / (GCJ_AXIS / sqrtMagic * Math.cos(radLatitude) * Math.PI);
        return ofWGS84(
                normalizeLongitude(longitude * 2 - (longitude + deltaLongitude)),
                normalizeLatitude(latitude * 2 - (latitude + deltaLatitude))
        );
    }

    public static GeoHelper gcj02ToBd09(double longitude, double latitude) {
        validateCoordinate(longitude, latitude);
        double z = Math.sqrt(longitude * longitude + latitude * latitude)
                + 0.00002 * Math.sin(latitude * BD09_PI);
        double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * BD09_PI);
        return ofBD09(
                normalizeLongitude(z * Math.cos(theta) + 0.0065),
                normalizeLatitude(z * Math.sin(theta) + 0.006)
        );
    }

    public static GeoHelper bd09ToGcj02(double longitude, double latitude) {
        validateCoordinate(longitude, latitude);
        double x = longitude - 0.0065;
        double y = latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * BD09_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * BD09_PI);
        return ofGCJ02(
                normalizeLongitude(z * Math.cos(theta)),
                normalizeLatitude(z * Math.sin(theta))
        );
    }

    public double getDistanceMeters(GeoHelper point) {
        return getDistanceMeters(this, point);
    }

    public double getDistanceKilometers(GeoHelper point) {
        return getDistanceMeters(point) / 1000.0;
    }

    public boolean isInCircle(GeoHelper center, double radiusMeters) {
        requirePoint(center, "center");
        if (radiusMeters < 0) {
            throw new IllegalArgumentException("radiusMeters must be >= 0");
        }
        return getDistanceMeters(center) <= radiusMeters;
    }

    public boolean isInPolygon(List<GeoHelper> boundaryPoints) {
        if (boundaryPoints == null || boundaryPoints.size() < 3) {
            return false;
        }

        GeoHelper current = toWGS84();
        List<GeoHelper> polygon = new ArrayList<>(boundaryPoints.size());
        double minLongitude = Double.POSITIVE_INFINITY;
        double maxLongitude = Double.NEGATIVE_INFINITY;
        double minLatitude = Double.POSITIVE_INFINITY;
        double maxLatitude = Double.NEGATIVE_INFINITY;

        for (GeoHelper boundaryPoint : boundaryPoints) {
            if (boundaryPoint == null) {
                continue;
            }
            GeoHelper normalized = boundaryPoint.toWGS84();
            polygon.add(normalized);
            minLongitude = Math.min(minLongitude, normalized.longitude);
            maxLongitude = Math.max(maxLongitude, normalized.longitude);
            minLatitude = Math.min(minLatitude, normalized.latitude);
            maxLatitude = Math.max(maxLatitude, normalized.latitude);
        }

        if (polygon.size() < 3) {
            return false;
        }
        if (current.longitude < minLongitude || current.longitude > maxLongitude
                || current.latitude < minLatitude || current.latitude > maxLatitude) {
            return false;
        }

        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            GeoHelper point1 = polygon.get(i);
            GeoHelper point2 = polygon.get(j);

            if (isOnSegment(current, point1, point2)) {
                return true;
            }

            boolean intersect = ((point1.latitude > current.latitude) != (point2.latitude > current.latitude))
                    && (current.longitude < (point2.longitude - point1.longitude)
                    * (current.latitude - point1.latitude) / (point2.latitude - point1.latitude)
                    + point1.longitude);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    public boolean isInRectangleArea(GeoHelper point1, GeoHelper point2) {
        requirePoint(point1, "point1");
        requirePoint(point2, "point2");
        GeoHelper current = toWGS84();
        GeoHelper wgs84Point1 = point1.toWGS84();
        GeoHelper wgs84Point2 = point2.toWGS84();
        return current.longitude >= Math.min(wgs84Point1.longitude, wgs84Point2.longitude)
                && current.longitude <= Math.max(wgs84Point1.longitude, wgs84Point2.longitude)
                && current.latitude >= Math.min(wgs84Point1.latitude, wgs84Point2.latitude)
                && current.latitude <= Math.max(wgs84Point1.latitude, wgs84Point2.latitude);
    }

    public GeoHelper toWGS84() {
        if (coordinateSystem == CoordinateSystem.WGS84) {
            return this;
        }
        if (coordinateSystem == CoordinateSystem.GCJ02) {
            return gcj02ToWgs84(longitude, latitude);
        }
        GeoHelper gcj02Point = bd09ToGcj02(longitude, latitude);
        return gcj02ToWgs84(gcj02Point.longitude, gcj02Point.latitude);
    }

    public GeoHelper toGCJ02() {
        if (coordinateSystem == CoordinateSystem.GCJ02) {
            return this;
        }
        if (coordinateSystem == CoordinateSystem.WGS84) {
            return wgs84ToGcj02(longitude, latitude);
        }
        return bd09ToGcj02(longitude, latitude);
    }

    public GeoHelper toBD09() {
        if (coordinateSystem == CoordinateSystem.BD09) {
            return this;
        }
        GeoHelper gcj02Point = coordinateSystem == CoordinateSystem.GCJ02 ? this : toGCJ02();
        return gcj02ToBd09(gcj02Point.longitude, gcj02Point.latitude);
    }

    public GeoHelper to(CoordinateSystem target) {
        if (target == null || target == coordinateSystem) {
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
                throw new IllegalArgumentException("unsupported coordinateSystem: " + target);
        }
    }

    @Override
    public String toString() {
        return longitude + "," + latitude + " [" + coordinateSystem + "]";
    }

    private static double haversineMeters(double longitude1, double latitude1, double longitude2, double latitude2) {
        double radLatitude1 = Math.toRadians(latitude1);
        double radLatitude2 = Math.toRadians(latitude2);
        double deltaLatitude = radLatitude1 - radLatitude2;
        double deltaLongitude = Math.toRadians(longitude1 - longitude2);
        double sinLatitude = Math.sin(deltaLatitude / 2.0);
        double sinLongitude = Math.sin(deltaLongitude / 2.0);
        double a = sinLatitude * sinLatitude
                + Math.cos(radLatitude1) * Math.cos(radLatitude2) * sinLongitude * sinLongitude;
        return 2.0 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }

    private static boolean isOutOfChina(double longitude, double latitude) {
        return longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271;
    }

    private static double transformLatitude(double longitude, double latitude) {
        double result = -100.0 + 2.0 * longitude + 3.0 * latitude
                + 0.2 * latitude * latitude + 0.1 * longitude * latitude
                + 0.2 * Math.sqrt(Math.abs(longitude));
        result += (20.0 * Math.sin(6.0 * longitude * Math.PI)
                + 20.0 * Math.sin(2.0 * longitude * Math.PI)) * 2.0 / 3.0;
        result += (20.0 * Math.sin(latitude * Math.PI)
                + 40.0 * Math.sin(latitude / 3.0 * Math.PI)) * 2.0 / 3.0;
        result += (160.0 * Math.sin(latitude / 12.0 * Math.PI)
                + 320.0 * Math.sin(latitude * Math.PI / 30.0)) * 2.0 / 3.0;
        return result;
    }

    private static double transformLongitude(double longitude, double latitude) {
        double result = 300.0 + longitude + 2.0 * latitude
                + 0.1 * longitude * longitude + 0.1 * longitude * latitude
                + 0.1 * Math.sqrt(Math.abs(longitude));
        result += (20.0 * Math.sin(6.0 * longitude * Math.PI)
                + 20.0 * Math.sin(2.0 * longitude * Math.PI)) * 2.0 / 3.0;
        result += (20.0 * Math.sin(longitude * Math.PI)
                + 40.0 * Math.sin(longitude / 3.0 * Math.PI)) * 2.0 / 3.0;
        result += (150.0 * Math.sin(longitude / 12.0 * Math.PI)
                + 300.0 * Math.sin(longitude / 30.0 * Math.PI)) * 2.0 / 3.0;
        return result;
    }

    private static double normalizeLongitude(double longitude) {
        if (longitude >= -180.0 && longitude <= 180.0) {
            return longitude;
        }
        double value = (longitude + 180.0) % 360.0;
        if (value < 0) {
            value += 360.0;
        }
        return value - 180.0;
    }

    private static double normalizeLatitude(double latitude) {
        return Math.max(-90.0, Math.min(90.0, latitude));
    }

    private static boolean isOnSegment(GeoHelper target, GeoHelper point1, GeoHelper point2) {
        if (target.longitude < Math.min(point1.longitude, point2.longitude) - SEGMENT_PRECISION
                || target.longitude > Math.max(point1.longitude, point2.longitude) + SEGMENT_PRECISION
                || target.latitude < Math.min(point1.latitude, point2.latitude) - SEGMENT_PRECISION
                || target.latitude > Math.max(point1.latitude, point2.latitude) + SEGMENT_PRECISION) {
            return false;
        }
        double crossProduct = (target.longitude - point1.longitude) * (point2.latitude - point1.latitude)
                - (target.latitude - point1.latitude) * (point2.longitude - point1.longitude);
        return Math.abs(crossProduct) < SEGMENT_PRECISION;
    }

    private static void validateCoordinate(double longitude, double latitude) {
        if (!isValid(longitude, latitude)) {
            throw new IllegalArgumentException("invalid coordinate: longitude=" + longitude + ", latitude=" + latitude);
        }
    }

    private static void requirePoint(GeoHelper point, String name) {
        if (point == null) {
            throw new IllegalArgumentException(name + " is null");
        }
    }
}
