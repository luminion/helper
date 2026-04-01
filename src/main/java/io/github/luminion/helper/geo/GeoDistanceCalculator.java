package io.github.luminion.helper.geo;

final class GeoDistanceCalculator {
    private static final double EARTH_RADIUS = 6378137.0;

    private GeoDistanceCalculator() {}

    static double getDistanceMetersWgs84(double longitude1, double latitude1, double longitude2, double latitude2) {
        return haversine(longitude1, latitude1, longitude2, latitude2);
    }

    static double getDistanceMetersGcj02(double longitude1, double latitude1, double longitude2, double latitude2) {
        GeoPoint point1 = GeoCoordinateConverter.gcj02ToWgs84(longitude1, latitude1);
        GeoPoint point2 = GeoCoordinateConverter.gcj02ToWgs84(longitude2, latitude2);
        return getDistanceMeters(point1, point2);
    }

    static double getDistanceMetersBd09(double longitude1, double latitude1, double longitude2, double latitude2) {
        GeoPoint point1 = GeoCoordinateConverter.toWGS84(new GeoPoint(longitude1, latitude1, CoordinateSystem.BD09));
        GeoPoint point2 = GeoCoordinateConverter.toWGS84(new GeoPoint(longitude2, latitude2, CoordinateSystem.BD09));
        return getDistanceMeters(point1, point2);
    }

    static double getDistanceMeters(GeoPoint point1, GeoPoint point2) {
        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("points must not be null");
        }
        GeoPoint wgs84Point1 = GeoCoordinateConverter.toWGS84(point1);
        GeoPoint wgs84Point2 = GeoCoordinateConverter.toWGS84(point2);
        return haversine(wgs84Point1.getLongitude(), wgs84Point1.getLatitude(), wgs84Point2.getLongitude(), wgs84Point2.getLatitude());
    }

    private static double haversine(double longitude1, double latitude1, double longitude2, double latitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double deltaLat = lat1 - lat2;
        double deltaLng = Math.toRadians(longitude1 - longitude2);
        double sinLat = Math.sin(deltaLat / 2.0);
        double sinLng = Math.sin(deltaLng / 2.0);
        double a = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLng * sinLng;
        double c = 2.0 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }
}
