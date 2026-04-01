package io.github.luminion.helper.geo;

import java.util.Collection;
import java.util.List;

/**
 * 地理坐标值对象。
 *
 * @author luminion
 */
public class GeoPoint {
    private final double longitude;
    private final double latitude;
    private final CoordinateSystem coordinateSystem;

    GeoPoint(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        GeoArguments.validateCoordinate(longitude, latitude, coordinateSystem);
        this.longitude = longitude;
        this.latitude = latitude;
        this.coordinateSystem = coordinateSystem;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public double getDistanceMeters(GeoPoint point) {
        return GeoDistanceCalculator.getDistanceMeters(this, point);
    }

    public double getDistanceKilometers(GeoPoint point) {
        return getDistanceMeters(point) / 1000.0;
    }

    public boolean isInCircle(GeoPoint circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }

    public boolean isInPolygon(List<GeoPoint> boundaryPoints) {
        return GeoGeometryHelper.isInPolygon(this, boundaryPoints);
    }

    public boolean isInRectangleArea(GeoPoint point1, GeoPoint point2) {
        return GeoGeometryHelper.isInRectangleArea(this, point1, point2);
    }

    public boolean isInRectangleBoundary(GeoPoint[] boundaryPoints) {
        return GeoGeometryHelper.isInRectangleBoundary(this, boundaryPoints);
    }

    public boolean isInRectangleBoundary(Collection<GeoPoint> boundaryPoints) {
        return boundaryPoints != null && !boundaryPoints.isEmpty()
                && isInRectangleBoundary(GeoArguments.toArray(boundaryPoints));
    }

    public GeoPoint toWGS84() {
        return GeoCoordinateConverter.toWGS84(this);
    }

    public GeoPoint toGCJ02() {
        return GeoCoordinateConverter.toGCJ02(this);
    }

    public GeoPoint toBD09() {
        return GeoCoordinateConverter.toBD09(this);
    }

    public GeoPoint to(CoordinateSystem target) {
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GeoPoint)) {
            return false;
        }
        GeoPoint that = (GeoPoint) other;
        return Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && coordinateSystem == that.coordinateSystem;
    }

    @Override
    public int hashCode() {
        int result = coordinateSystem != null ? coordinateSystem.hashCode() : 0;
        long temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
