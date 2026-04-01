package io.github.luminion.helper.geo;

final class GeoCoordinateConverter {
    private static final double PI = Math.PI;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;
    private static final double X_PI = PI * 3000.0 / 180.0;

    private GeoCoordinateConverter() {}

    static GeoPoint wgs84ToGcj02(double lng, double lat) {
        // 中国境外没有火星坐标偏移，直接返回原始坐标即可。
        if (outOfChina(lng, lat)) {
            return new GeoPoint(lng, lat, CoordinateSystem.GCJ02);
        }
        double[] delta = delta(lng, lat);
        return new GeoPoint(lng + delta[0], lat + delta[1], CoordinateSystem.GCJ02);
    }

    static GeoPoint gcj02ToWgs84(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new GeoPoint(lng, lat, CoordinateSystem.WGS84);
        }
        double[] delta = delta(lng, lat);
        return new GeoPoint(lng - delta[0], lat - delta[1], CoordinateSystem.WGS84);
    }

    static GeoPoint gcj02ToBd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        return new GeoPoint(z * Math.cos(theta) + 0.0065, z * Math.sin(theta) + 0.006, CoordinateSystem.BD09);
    }

    static GeoPoint bd09ToGcj02(double lng, double lat) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        return new GeoPoint(z * Math.cos(theta), z * Math.sin(theta), CoordinateSystem.GCJ02);
    }

    static GeoPoint toWGS84(GeoPoint point) {
        if (point == null || point.getCoordinateSystem() == CoordinateSystem.WGS84) {
            return point;
        }
        if (point.getCoordinateSystem() == CoordinateSystem.GCJ02) {
            return gcj02ToWgs84(point.getLongitude(), point.getLatitude());
        }
        // BD09 需要先退回 GCJ02，再继续转到 WGS84。
        return gcj02ToWgs84(bd09ToGcj02(point.getLongitude(), point.getLatitude()).getLongitude(),
                bd09ToGcj02(point.getLongitude(), point.getLatitude()).getLatitude());
    }

    static GeoPoint toGCJ02(GeoPoint point) {
        if (point == null || point.getCoordinateSystem() == CoordinateSystem.GCJ02) {
            return point;
        }
        if (point.getCoordinateSystem() == CoordinateSystem.WGS84) {
            return wgs84ToGcj02(point.getLongitude(), point.getLatitude());
        }
        return bd09ToGcj02(point.getLongitude(), point.getLatitude());
    }

    static GeoPoint toBD09(GeoPoint point) {
        if (point == null || point.getCoordinateSystem() == CoordinateSystem.BD09) {
            return point;
        }
        GeoPoint gcj02Point = point.getCoordinateSystem() == CoordinateSystem.GCJ02 ? point : toGCJ02(point);
        return gcj02ToBd09(gcj02Point.getLongitude(), gcj02Point.getLatitude());
    }

    static GeoPoint to(GeoPoint point, CoordinateSystem target) {
        if (point == null || target == null || point.getCoordinateSystem() == target) {
            return point;
        }
        switch (target) {
            case WGS84:
                return toWGS84(point);
            case GCJ02:
                return toGCJ02(point);
            case BD09:
                return toBD09(point);
            default:
                throw new IllegalArgumentException("unsupported coordinate system: " + target);
        }
    }

    private static boolean outOfChina(double lng, double lat) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    private static double[] delta(double lng, double lat) {
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        return new double[] {dLng, dLat};
    }

    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
