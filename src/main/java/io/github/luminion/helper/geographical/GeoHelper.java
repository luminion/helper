package io.github.luminion.helper.geographical;

import io.github.luminion.helper.geo.GeoPoint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.geo.GeoHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.geo.GeoHelper}
 */
@Deprecated
public class GeoHelper {
    public enum CoordinateSystem {
        WGS84,
        GCJ02,
        BD09
    }

    private final GeoPoint delegate;

    private GeoHelper(GeoPoint delegate) {
        this.delegate = delegate;
    }

    public double getLongitude() {
        return delegate.getLongitude();
    }

    public double getLatitude() {
        return delegate.getLatitude();
    }

    public CoordinateSystem getCoordinateSystem() {
        return fromNew(delegate.getCoordinateSystem());
    }

    public static GeoHelper of(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.of(longitude, latitude, toNew(coordinateSystem)));
    }

    public static GeoHelper of(String longitude, String latitude, CoordinateSystem coordinateSystem) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.of(longitude, latitude, toNew(coordinateSystem)));
    }

    public static GeoHelper of(BigDecimal longitude, BigDecimal latitude, CoordinateSystem coordinateSystem) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.of(longitude, latitude, toNew(coordinateSystem)));
    }

    public static GeoHelper ofWGS84(double longitude, double latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofWGS84(longitude, latitude));
    }

    public static GeoHelper ofWGS84(String longitude, String latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofWGS84(longitude, latitude));
    }

    public static GeoHelper ofWGS84(BigDecimal longitude, BigDecimal latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofWGS84(longitude, latitude));
    }

    public static GeoHelper ofGCJ02(double longitude, double latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofGCJ02(longitude, latitude));
    }

    public static GeoHelper ofGCJ02(String longitude, String latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofGCJ02(longitude, latitude));
    }

    public static GeoHelper ofGCJ02(BigDecimal longitude, BigDecimal latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofGCJ02(longitude, latitude));
    }

    public static GeoHelper ofBD09(double longitude, double latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofBD09(longitude, latitude));
    }

    public static GeoHelper ofBD09(String longitude, String latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofBD09(longitude, latitude));
    }

    public static GeoHelper ofBD09(BigDecimal longitude, BigDecimal latitude) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.ofBD09(longitude, latitude));
    }

    public static boolean isValid(double longitude, double latitude) {
        return io.github.luminion.helper.geo.GeoHelper.isValid(longitude, latitude);
    }

    public static double getDistanceMetersWgs84(double longitude1, double latitude1, double longitude2, double latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersWgs84(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersWgs84(String longitude1, String latitude1, String longitude2, String latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersWgs84(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersWgs84(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersWgs84(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersGcj02(double longitude1, double latitude1, double longitude2, double latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersGcj02(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersGcj02(String longitude1, String latitude1, String longitude2, String latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersGcj02(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersGcj02(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersGcj02(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersBd09(double longitude1, double latitude1, double longitude2, double latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersBd09(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersBd09(String longitude1, String latitude1, String longitude2, String latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersBd09(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMetersBd09(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMetersBd09(longitude1, latitude1, longitude2, latitude2);
    }

    public static double getDistanceMeters(GeoHelper point1, GeoHelper point2) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMeters(point1.delegate, point2.delegate);
    }

    public static GeoHelper getSouthWestPoint(GeoHelper[] vertexes) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.getSouthWestPoint(toNewArray(vertexes)));
    }

    public static GeoHelper getSouthWestPoint(Collection<GeoHelper> vertexes) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.getSouthWestPoint(toNewCollection(vertexes)));
    }

    public static GeoHelper getNorthEastPoint(GeoHelper[] vertexes) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.getNorthEastPoint(toNewArray(vertexes)));
    }

    public static GeoHelper getNorthEastPoint(Collection<GeoHelper> vertexes) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.getNorthEastPoint(toNewCollection(vertexes)));
    }

    public static GeoHelper wgs84ToGcj02(double lng, double lat) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.wgs84ToGcj02(lng, lat));
    }

    public static GeoHelper gcj02ToWgs84(double lng, double lat) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.gcj02ToWgs84(lng, lat));
    }

    public static GeoHelper gcj02ToBd09(double lng, double lat) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.gcj02ToBd09(lng, lat));
    }

    public static GeoHelper bd09ToGcj02(double lng, double lat) {
        return fromNew(io.github.luminion.helper.geo.GeoHelper.bd09ToGcj02(lng, lat));
    }

    public double getDistanceMeters(GeoHelper point) {
        return delegate.getDistanceMeters(point.delegate);
    }

    public double getDistanceKilometers(GeoHelper point) {
        return delegate.getDistanceKilometers(point.delegate);
    }

    public boolean isInCircle(GeoHelper circle, double radius) {
        return delegate.isInCircle(circle.delegate, radius);
    }

    public boolean isInPolygon(List<GeoHelper> boundaryPoints) {
        return io.github.luminion.helper.geo.GeoHelper.getDistanceMeters(delegate, delegate) >= 0
                && delegate.isInPolygon(toNewList(boundaryPoints));
    }

    public boolean isInRectangleArea(GeoHelper point1, GeoHelper point2) {
        return delegate.isInRectangleArea(point1.delegate, point2.delegate);
    }

    public boolean isInRectangleBoundary(GeoHelper[] boundaryPoints) {
        return delegate.isInRectangleBoundary(toNewArray(boundaryPoints));
    }

    public boolean isInRectangleBoundary(Collection<GeoHelper> boundaryPoints) {
        return delegate.isInRectangleBoundary(toNewCollection(boundaryPoints));
    }

    public GeoHelper toWGS84() {
        return fromNew(delegate.toWGS84());
    }

    public GeoHelper toGCJ02() {
        return fromNew(delegate.toGCJ02());
    }

    public GeoHelper toBD09() {
        return fromNew(delegate.toBD09());
    }

    public GeoHelper to(CoordinateSystem target) {
        return fromNew(delegate.to(toNew(target)));
    }

    public double[] toArray() {
        return delegate.toArray();
    }

    public String toLngLatString() {
        return delegate.toLngLatString();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoHelper)) {
            return false;
        }
        return delegate.equals(((GeoHelper) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    private static io.github.luminion.helper.geo.CoordinateSystem toNew(CoordinateSystem coordinateSystem) {
        return io.github.luminion.helper.geo.CoordinateSystem.valueOf(coordinateSystem.name());
    }

    private static CoordinateSystem fromNew(io.github.luminion.helper.geo.CoordinateSystem coordinateSystem) {
        return CoordinateSystem.valueOf(coordinateSystem.name());
    }

    private static GeoHelper fromNew(GeoPoint point) {
        return point == null ? null : new GeoHelper(point);
    }

    private static GeoPoint[] toNewArray(GeoHelper[] points) {
        if (points == null) {
            return new GeoPoint[0];
        }
        GeoPoint[] result = new GeoPoint[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = points[i] == null ? null : points[i].delegate;
        }
        return result;
    }

    private static Collection<GeoPoint> toNewCollection(Collection<GeoHelper> points) {
        List<GeoPoint> result = new ArrayList<GeoPoint>();
        if (points == null) {
            return result;
        }
        for (GeoHelper point : points) {
            if (point != null) {
                result.add(point.delegate);
            }
        }
        return result;
    }

    private static List<GeoPoint> toNewList(List<GeoHelper> points) {
        List<GeoPoint> result = new ArrayList<GeoPoint>();
        if (points == null) {
            return result;
        }
        for (GeoHelper point : points) {
            if (point != null) {
                result.add(point.delegate);
            }
        }
        return result;
    }
}
