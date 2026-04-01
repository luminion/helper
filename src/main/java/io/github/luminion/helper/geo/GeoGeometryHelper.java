package io.github.luminion.helper.geo;

import java.util.List;

final class GeoGeometryHelper {
    private GeoGeometryHelper() {}

    static GeoPoint getSouthWestPoint(GeoPoint[] vertexes) {
        if (vertexes == null || vertexes.length == 0) {
            return null;
        }
        double minLongitude = vertexes[0].getLongitude();
        double minLatitude = vertexes[0].getLatitude();
        CoordinateSystem coordinateSystem = vertexes[0].getCoordinateSystem();
        for (GeoPoint vertex : vertexes) {
            if (vertex == null) {
                continue;
            }
            minLongitude = Math.min(minLongitude, vertex.getLongitude());
            minLatitude = Math.min(minLatitude, vertex.getLatitude());
        }
        return new GeoPoint(minLongitude, minLatitude, coordinateSystem);
    }

    static GeoPoint getNorthEastPoint(GeoPoint[] vertexes) {
        if (vertexes == null || vertexes.length == 0) {
            return null;
        }
        double maxLongitude = vertexes[0].getLongitude();
        double maxLatitude = vertexes[0].getLatitude();
        CoordinateSystem coordinateSystem = vertexes[0].getCoordinateSystem();
        for (GeoPoint vertex : vertexes) {
            if (vertex == null) {
                continue;
            }
            maxLongitude = Math.max(maxLongitude, vertex.getLongitude());
            maxLatitude = Math.max(maxLatitude, vertex.getLatitude());
        }
        return new GeoPoint(maxLongitude, maxLatitude, coordinateSystem);
    }

    static boolean isInPolygon(GeoPoint point, List<GeoPoint> boundaryPoints) {
        if (point == null || boundaryPoints == null || boundaryPoints.size() < 3) {
            return false;
        }
        GeoPoint currentPoint = GeoCoordinateConverter.toWGS84(point);
        boolean inside = false;
        int size = boundaryPoints.size();
        for (int i = 0, j = size - 1; i < size; j = i++) {
            GeoPoint pi = GeoCoordinateConverter.toWGS84(boundaryPoints.get(i));
            GeoPoint pj = GeoCoordinateConverter.toWGS84(boundaryPoints.get(j));
            boolean intersect = ((pi.getLatitude() > currentPoint.getLatitude()) != (pj.getLatitude() > currentPoint.getLatitude()))
                    && (currentPoint.getLongitude() < (pj.getLongitude() - pi.getLongitude())
                    * (currentPoint.getLatitude() - pi.getLatitude()) / (pj.getLatitude() - pi.getLatitude())
                    + pi.getLongitude());
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    static boolean isInRectangleArea(GeoPoint target, GeoPoint point1, GeoPoint point2) {
        if (target == null || point1 == null || point2 == null) {
            return false;
        }
        GeoPoint sw = getSouthWestPoint(new GeoPoint[] {point1, point2});
        GeoPoint ne = getNorthEastPoint(new GeoPoint[] {point1, point2});
        GeoPoint current = GeoCoordinateConverter.toWGS84(target);
        GeoPoint wgs84SouthWest = GeoCoordinateConverter.toWGS84(sw);
        GeoPoint wgs84NorthEast = GeoCoordinateConverter.toWGS84(ne);
        return current.getLongitude() >= wgs84SouthWest.getLongitude()
                && current.getLongitude() <= wgs84NorthEast.getLongitude()
                && current.getLatitude() >= wgs84SouthWest.getLatitude()
                && current.getLatitude() <= wgs84NorthEast.getLatitude();
    }

    static boolean isInRectangleBoundary(GeoPoint target, GeoPoint[] boundaryPoints) {
        if (target == null || boundaryPoints == null || boundaryPoints.length < 2) {
            return false;
        }
        GeoPoint southWest = getSouthWestPoint(boundaryPoints);
        GeoPoint northEast = getNorthEastPoint(boundaryPoints);
        return isInRectangleArea(target, southWest, northEast);
    }
}
