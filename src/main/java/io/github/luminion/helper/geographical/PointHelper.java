package io.github.luminion.helper.geographical;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 坐标点工具类
 *
 * 支持：
 * - WGS84 / GCJ02 / BD09 坐标系标记
 * - 距离计算
 * - 点在圆内、多边形内判断
 * - 坐标系相互转换：toWGS84 / toGCJ02 / toBD09 / to(CoordinateSystem)
 *
 * 注意：
 * - PointHelper 实例内部的经纬度值始终对应其 {@link CoordinateSystem} 字段标记的坐标系
 *
 * @author luminion
 */
@Getter
@EqualsAndHashCode
public class PointHelper {

    /**
     * 坐标系类型
     */
    public enum CoordinateSystem {
        /**
         * WGS84（GPS 原始坐标，国际通用）
         * <p>
         * 常用于 Google Maps（国外版）、GPS 设备、OpenStreetMap
         * </p>
         */
        WGS84,
        /**
         * GCJ02（火星坐标，国家测绘局制定的地理信息系统加密算法）
         * <p>
         * 常用于高德地图、腾讯地图、Google Maps（中国版）
         * </p>
         */
        GCJ02,
        /**
         * BD09（百度坐标，在 GCJ02 基础上二次加密）
         * <p>
         * 仅用于百度地图
         * </p>
         */
        BD09
    }

    // ==================== 常量 ====================

    /**
     * 百度坐标转换专用的 $\pi$ 系数 (Math.PI * 3000.0 / 180.0)
     */
    private static final double BD09_PI = Math.PI * 3000.0 / 180.0;

    /**
     * 克拉索夫斯基椭球体长半轴（单位：米，GCJ 偏移算法使用）
     */
    private static final double KRASOVSKY_A = 6378245.0;
    /**
     * 克拉索夫斯基椭球体偏心率平方
     */
    private static final double KRASOVSKY_EE = 0.00669342162296594323;

    /**
     * 地球平均半径 (米)
     * <p>
     * 修正：Haversine 公式基于正球体模型，使用平均半径 (6371km) 比使用赤道半径 (6378km) 在全球范围内的平均误差更小。
     * </p>
     */
    private static final double EARTH_RADIUS_AVERAGE = 6371008.8;

    /**
     * 线段共线判断的精度（度）
     * 1e-7 度 ≈ 1.1cm
     */
    private static final double SEGMENT_PRECISION = 1e-7;

    // ==================== 字段 ====================

    /**
     * 经度（单位：度），取值范围：[-180.0, 180.0]
     * <p>
     * 具体含义取决于所选的 {@link #coordinateSystem}
     * </p>
     */
    private final double longitude;

    /**
     * 纬度（单位：度），取值范围：[-90.0, 90.0]
     * <p>
     * 具体含义取决于所选的 {@link #coordinateSystem}
     * </p>
     */
    private final double latitude;

    /**
     * 坐标系
     */
    private final CoordinateSystem coordinateSystem;

    /**
     * 坐标点
     *
     * @param longitude        经度
     * @param latitude         纬度
     * @param coordinateSystem 坐标系
     */
    private PointHelper(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("经度范围必须在 [-180, 180] 之间，当前值: " + longitude);
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("纬度范围必须在 [-90, 90] 之间，当前值: " + latitude);
        }
        if (coordinateSystem == null) {
            throw new IllegalArgumentException("坐标系不能为空");
        }
        this.longitude = longitude;
        this.latitude = latitude;
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * 创建 指定坐标系的 坐标点
     *
     * @param longitude        经度
     * @param latitude         纬度
     * @param coordinateSystem 坐标系
     * @return 坐标点工具类
     */
    public static PointHelper of(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        return new PointHelper(longitude, latitude, coordinateSystem);
    }

    /**
     * 创建 WGS84 (GPS) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofWGS84(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CoordinateSystem.WGS84);
    }

    /**
     * 创建 WGS84 (GPS) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofWGS84(String longitude, String latitude) {
        if (longitude == null || latitude == null || longitude.trim().isEmpty() || latitude.trim().isEmpty()) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        return ofWGS84(Double.parseDouble(longitude.trim()), Double.parseDouble(latitude.trim()));
    }

    /**
     * 创建 WGS84 (GPS) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofWGS84(BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        return ofWGS84(longitude.doubleValue(), latitude.doubleValue());
    }

    /**
     * 创建 GCJ02 (火星)
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofGCJ02(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CoordinateSystem.GCJ02);
    }

    /**
     * 创建 GCJ02 (火星) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofGCJ02(String longitude, String latitude) {
        if (longitude == null || latitude == null || longitude.trim().isEmpty() || latitude.trim().isEmpty()) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        return ofGCJ02(Double.parseDouble(longitude.trim()), Double.parseDouble(latitude.trim()));
    }

    /**
     * 创建 GCJ02 (火星) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofGCJ02(BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        return ofGCJ02(longitude.doubleValue(), latitude.doubleValue());
    }

    /**
     * 创建 BD09 (百度)
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofBD09(double longitude, double latitude) {
        return new PointHelper(longitude, latitude, CoordinateSystem.BD09);
    }

    /**
     * 创建 BD09 (百度) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofBD09(String longitude, String latitude) {
        if (longitude == null || latitude == null || longitude.trim().isEmpty() || latitude.trim().isEmpty()) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        return ofBD09(Double.parseDouble(longitude.trim()), Double.parseDouble(latitude.trim()));
    }

    /**
     * 创建 BD09 (百度) 坐标点
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 坐标点工具类
     */
    public static PointHelper ofBD09(BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            throw new IllegalArgumentException("经纬度不能为空");
        }
        return ofBD09(longitude.doubleValue(), latitude.doubleValue());
    }

    /**
     * 校验经纬度是否合法
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return boolean
     */
    public static boolean isValid(double longitude, double latitude) {
        return longitude >= -180.0 && longitude <= 180.0 && latitude >= -90.0 && latitude <= 90.0;
    }

    // ==================== 静态工具方法 (距离计算) ====================

    /**
     * 内部私有方法：计算两点间的 Haversine 距离（纯数学计算，不创建对象）
     *
     * @param lng1 起点经度
     * @param lat1 起点纬度
     * @param lng2 终点经度
     * @param lat2 终点纬度
     * @return 距离 (米)
     */
    private static double calculateHaversineDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);

        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) +
                        Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)
        ));
        return s * EARTH_RADIUS_AVERAGE;
    }

    /**
     * 获取两个 WGS84 坐标点之间的距离
     *
     * @param longitude1 起点经度 (WGS84)
     * @param latitude1  起点纬度 (WGS84)
     * @param longitude2 终点经度 (WGS84)
     * @param latitude2  终点纬度 (WGS84)
     * @return 距离 (米)
     */
    public static double getDistanceMetersWgs84(double longitude1, double latitude1, double longitude2,
                                                double latitude2) {
        return calculateHaversineDistance(longitude1, latitude1, longitude2, latitude2);
    }

    /**
     * 获取两个 WGS84 坐标点之间的距离
     *
     * @param longitude1 起点经度 (WGS84)
     * @param latitude1  起点纬度 (WGS84)
     * @param longitude2 终点经度 (WGS84)
     * @param latitude2  终点纬度 (WGS84)
     * @return 距离 (米)
     */
    public static double getDistanceMetersWgs84(String longitude1, String latitude1, String longitude2,
                                                String latitude2) {
        return getDistanceMetersWgs84(
                Double.parseDouble(longitude1.trim()), Double.parseDouble(latitude1.trim()),
                Double.parseDouble(longitude2.trim()), Double.parseDouble(latitude2.trim())
        );
    }

    /**
     * 获取两个 WGS84 坐标点之间的距离
     *
     * @param longitude1 起点经度 (WGS84)
     * @param latitude1  起点纬度 (WGS84)
     * @param longitude2 终点经度 (WGS84)
     * @param latitude2  终点纬度 (WGS84)
     * @return 距离 (米)
     */
    public static double getDistanceMetersWgs84(BigDecimal longitude1, BigDecimal latitude1,
                                                BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersWgs84(
                longitude1.doubleValue(), latitude1.doubleValue(),
                longitude2.doubleValue(), latitude2.doubleValue()
        );
    }

    /**
     * 获取两个 GCJ02 坐标点之间的距离
     *
     * @param longitude1 起点经度 (GCJ02)
     * @param latitude1  起点纬度 (GCJ02)
     * @param longitude2 终点经度 (GCJ02)
     * @param latitude2  终点纬度 (GCJ02)
     * @return 距离 (米)
     */
    public static double getDistanceMetersGcj02(double longitude1, double latitude1, double longitude2,
                                                double latitude2) {
        PointHelper p1 = gcj02ToWgs84(longitude1, latitude1);
        PointHelper p2 = gcj02ToWgs84(longitude2, latitude2);
        return calculateHaversineDistance(p1.longitude, p1.latitude, p2.longitude, p2.latitude);
    }

    public static double getDistanceMetersGcj02(String longitude1, String latitude1, String longitude2,
                                                String latitude2) {
        return getDistanceMetersGcj02(
                Double.parseDouble(longitude1.trim()), Double.parseDouble(latitude1.trim()),
                Double.parseDouble(longitude2.trim()), Double.parseDouble(latitude2.trim())
        );
    }

    public static double getDistanceMetersGcj02(BigDecimal longitude1, BigDecimal latitude1,
                                                BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersGcj02(
                longitude1.doubleValue(), latitude1.doubleValue(),
                longitude2.doubleValue(), latitude2.doubleValue()
        );
    }

    /**
     * 获取两个 BD09 坐标点之间的距离
     *
     * @param longitude1 起点经度 (BD09)
     * @param latitude1  起点纬度 (BD09)
     * @param longitude2 终点经度 (BD09)
     * @param latitude2  终点纬度 (BD09)
     * @return 距离 (米)
     */
    public static double getDistanceMetersBd09(double longitude1, double latitude1, double longitude2,
                                               double latitude2) {
        // BD09 -> GCJ02 -> WGS84
        PointHelper p1 = bd09ToGcj02(longitude1, latitude1).toWGS84();
        PointHelper p2 = bd09ToGcj02(longitude2, latitude2).toWGS84();
        return calculateHaversineDistance(p1.longitude, p1.latitude, p2.longitude, p2.latitude);
    }

    public static double getDistanceMetersBd09(String longitude1, String latitude1, String longitude2,
                                               String latitude2) {
        return getDistanceMetersBd09(
                Double.parseDouble(longitude1.trim()), Double.parseDouble(latitude1.trim()),
                Double.parseDouble(longitude2.trim()), Double.parseDouble(latitude2.trim())
        );
    }

    public static double getDistanceMetersBd09(BigDecimal longitude1, BigDecimal latitude1,
                                               BigDecimal longitude2, BigDecimal latitude2) {
        return getDistanceMetersBd09(
                longitude1.doubleValue(), latitude1.doubleValue(),
                longitude2.doubleValue(), latitude2.doubleValue()
        );
    }

    /**
     * 获取两个坐标点之间的距离
     *
     * @param point1 坐标点1
     * @param point2 坐标点2
     * @return 距离 (米)
     */
    public static double getDistanceMeters(PointHelper point1, PointHelper point2) {
        return point1.getDistanceMeters(point2);
    }

    /**
     * 获取这组坐标矩形西南角的顶点坐标
     *
     * @param vertexes 顶点列表
     * @return 坐标点工具类
     */
    public static PointHelper getSouthWestPoint(PointHelper[] vertexes) {
        if (vertexes == null || vertexes.length == 0) {
            throw new IllegalArgumentException("vertexes 不能为空");
        }
        double minLng = vertexes[0].getLongitude();
        double minLat = vertexes[0].getLatitude();
        CoordinateSystem cs = vertexes[0].getCoordinateSystem();
        for (PointHelper pointHelper : vertexes) {
            double x = pointHelper.getLongitude();
            double y = pointHelper.getLatitude();
            if (x < minLng) {
                minLng = x;
            }
            if (y < minLat) {
                minLat = y;
            }
        }
        return new PointHelper(minLng, minLat, cs);
    }

    /**
     * 获取这组坐标矩形东北角的顶点坐标
     *
     * @param vertexes 顶点列表
     * @return 坐标点工具类
     */
    public static PointHelper getNorthEastPoint(PointHelper[] vertexes) {
        if (vertexes == null || vertexes.length == 0) {
            throw new IllegalArgumentException("vertexes 不能为空");
        }
        double maxLng = vertexes[0].getLongitude();
        double maxLat = vertexes[0].getLatitude();
        CoordinateSystem cs = vertexes[0].getCoordinateSystem();
        for (PointHelper pointHelper : vertexes) {
            double x = pointHelper.getLongitude();
            double y = pointHelper.getLatitude();
            if (x > maxLng) {
                maxLng = x;
            }
            if (y > maxLat) {
                maxLat = y;
            }
        }
        return new PointHelper(maxLng, maxLat, cs);
    }

    /**
     * WGS84 -> GCJ02
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标点工具类 (GCJ02)
     */
    public static PointHelper wgs84ToGcj02(double lng, double lat) {
        if (isOutOfChinaRectangle(lng, lat)) {
            return ofGCJ02(lng, lat);
        }
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - KRASOVSKY_EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((KRASOVSKY_A * (1 - KRASOVSKY_EE)) / (magic * sqrtMagic) * Math.PI);
        dLng = (dLng * 180.0) / (KRASOVSKY_A / sqrtMagic * Math.cos(radLat) * Math.PI);

        // 转换结果归一化，防止经纬度越界导致异常
        return ofGCJ02(normalizeLongitude(lng + dLng), normalizeLatitude(lat + dLat));
    }

    /**
     * GCJ02 -> WGS84
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标点工具类 (WGS84)
     */
    public static PointHelper gcj02ToWgs84(double lng, double lat) {
        if (isOutOfChinaRectangle(lng, lat)) {
            return ofWGS84(lng, lat);
        }
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - KRASOVSKY_EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((KRASOVSKY_A * (1 - KRASOVSKY_EE)) / (magic * sqrtMagic) * Math.PI);
        dLng = (dLng * 180.0) / (KRASOVSKY_A / sqrtMagic * Math.cos(radLat) * Math.PI);

        // 转换结果归一化，防止经纬度越界导致异常
        return ofWGS84(normalizeLongitude(lng * 2 - (lng + dLng)), normalizeLatitude(lat * 2 - (lat + dLat)));
    }

    /**
     * GCJ02 -> BD09
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标点工具类 (BD09)
     */
    public static PointHelper gcj02ToBd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * BD09_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * BD09_PI);

        // 转换结果归一化，防止经纬度越界导致异常
        return ofBD09(normalizeLongitude(z * Math.cos(theta) + 0.0065), normalizeLatitude(z * Math.sin(theta) + 0.006));
    }

    /**
     * BD09 -> GCJ02
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 坐标点工具类 (GCJ02)
     */
    public static PointHelper bd09ToGcj02(double lng, double lat) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * BD09_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * BD09_PI);

        // 转换结果归一化，防止经纬度越界导致异常
        return ofGCJ02(normalizeLongitude(z * Math.cos(theta)), normalizeLatitude(z * Math.sin(theta)));
    }

    /**
     * 经度环绕处理：将经度约束在 [-180, 180] 之间
     */
    private static double normalizeLongitude(double lng) {
        if (lng >= -180.0 && lng <= 180.0) {
            return lng;
        }
        double x = (lng + 180.0) % 360.0;
        if (x < 0) {
            x += 360.0;
        }
        return x - 180.0;
    }

    /**
     * 纬度截断处理：将纬度约束在 [-90, 90] 之间
     */
    private static double normalizeLatitude(double lat) {
        return Math.max(-90.0, Math.min(90.0, lat));
    }

    /**
     * 判断坐标是否不在国内 (粗略过滤)
     * <p>
     * 采用矩形框快速筛选。由于中国版图在该矩形框之外仍有部分区域，此方法返回 true 表示一定不在国内，返回 false 表示可能在国内。
     * </p>
     */
    private static boolean isOutOfChinaRectangle(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }

    /**
     * 纬度偏移转换算法 (WGS84 -> GCJ02)
     */
    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat
                + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度偏移转换算法 (WGS84 -> GCJ02)
     */
    private static double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    // ==================== 实例方法 ====================

    /**
     * 获取距离
     *
     * @param point 坐标点
     * @return 距离 (米)
     */
    public double getDistanceMeters(PointHelper point) {
        // 先统一转 WGS84
        PointHelper p1 = this.toWGS84();
        PointHelper p2 = point.toWGS84();
        // 调用纯静态方法，避免内部再创建对象
        return calculateHaversineDistance(p1.longitude, p1.latitude, p2.longitude, p2.latitude);
    }

    /**
     * 获取距离
     *
     * @param point 坐标点
     * @return 距离 (千米)
     */
    public double getDistanceKilometers(PointHelper point) {
        return getDistanceMeters(point) / 1000.0;
    }

    /**
     * 是否在圆内
     *
     * @param circle 圆心
     * @param radius 半径 (米)
     * @return boolean
     */
    public boolean isInCircle(PointHelper circle, double radius) {
        return getDistanceMeters(circle) <= radius;
    }

    /**
     * 是否在多边形区域内
     *
     * @param boundaryPoints 边界点列表
     * @return boolean
     */
    public boolean isInPolygon(List<PointHelper> boundaryPoints) {
        if (boundaryPoints == null || boundaryPoints.size() < 3) {
            return false;
        }
        return isInPolygon(boundaryPoints.toArray(new PointHelper[0]));
    }

    /**
     * 判断点是否在多边形区域内
     *
     * @param boundaryPoints 边界点数组
     * @return boolean
     */
    private boolean isInPolygon(PointHelper[] boundaryPoints) {
        if (boundaryPoints == null || boundaryPoints.length < 3) {
            return false;
        }

        // 优化点：合并多重循环。在坐标对齐的同时，同步计算外包矩形 (AABB) 的四个极值
        PointHelper[] alignedPoints = new PointHelper[boundaryPoints.length];
        double minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;

        for (int i = 0; i < boundaryPoints.length; i++) {
            PointHelper p = boundaryPoints[i];
            if (p == null) continue; // 防御性检查

            p = p.to(this.coordinateSystem);
            alignedPoints[i] = p;

            double lng = p.getLongitude();
            double lat = p.getLatitude();
            if (lng < minLng) minLng = lng;
            if (lng > maxLng) maxLng = lng;
            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
        }

        // 外包矩形 (AABB) 快速失败检查
        if (this.longitude < minLng || this.longitude > maxLng || this.latitude < minLat || this.latitude > maxLat) {
            return false;
        }

        // 射线法 (Ray Casting Algorithm)
        boolean result = false;
        int j = alignedPoints.length - 1;
        for (int i = 0; i < alignedPoints.length; i++) {
            PointHelper p1 = alignedPoints[i];
            PointHelper p2 = alignedPoints[j];

            if (p1 == null || p2 == null) {
                j = i;
                continue;
            }

            // 判断是否在线段上
            if (this.isOnSegment(p1, p2)) {
                return true;
            }

            // 射线逻辑
            if ((p1.getLatitude() < this.latitude && p2.getLatitude() >= this.latitude)
                    || (p2.getLatitude() < this.latitude && p1.getLatitude() >= this.latitude)) {

                double intersectX = p1.getLongitude() + (this.latitude - p1.getLatitude())
                        * (p2.getLongitude() - p1.getLongitude()) / (p2.getLatitude() - p1.getLatitude());

                if (this.longitude < intersectX) {
                    result = !result;
                }
            }
            j = i;
        }
        return result;
    }

    /**
     * 判断当前点是否在指定线段上 (允许误差)
     */
    private boolean isOnSegment(PointHelper p1, PointHelper p2) {
        double precision = SEGMENT_PRECISION;
        // 1. 快速排除外包矩形
        if (this.longitude < Math.min(p1.getLongitude(), p2.getLongitude()) - precision ||
                this.longitude > Math.max(p1.getLongitude(), p2.getLongitude()) + precision ||
                this.latitude < Math.min(p1.getLatitude(), p2.getLatitude()) - precision ||
                this.latitude > Math.max(p1.getLatitude(), p2.getLatitude()) + precision) {
            return false;
        }

        // 2. 叉积判断是否共线: (x - x1)*(y2 - y1) - (y - y1)*(x2 - x1)
        double crossProduct = (this.longitude - p1.getLongitude()) * (p2.getLatitude() - p1.getLatitude())
                - (this.latitude - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude());

        return Math.abs(crossProduct) < precision;
    }

    /**
     * 判断点是否在指定的矩形区域内
     *
     * @param point1 线段顶点1
     * @param point2 线段顶点2
     * @return boolean
     */
    public boolean isInRectangleArea(PointHelper point1, PointHelper point2) {
        PointHelper p1 = point1.to(this.coordinateSystem);
        PointHelper p2 = point2.to(this.coordinateSystem);
        return this.longitude >= Math.min(p1.getLongitude(), p2.getLongitude())
                && this.longitude <= Math.max(p1.getLongitude(), p2.getLongitude())
                && this.latitude >= Math.min(p1.getLatitude(), p2.getLatitude())
                && this.latitude <= Math.max(p1.getLatitude(), p2.getLatitude());
    }

    /**
     * 判断点是否在多个点构成的外包矩形内
     *
     * @param boundaryPoints 边界点数组
     * @return boolean
     */
    public boolean isInRectangleBoundary(PointHelper[] boundaryPoints) {
        // 复用 getSouthWest/NorthEast 逻辑，并统一坐标系
        PointHelper southWestPoint = getSouthWestPoint(boundaryPoints).to(this.coordinateSystem);
        PointHelper northEastPoint = getNorthEastPoint(boundaryPoints).to(this.coordinateSystem);
        boolean b1 = this.longitude >= southWestPoint.getLongitude();
        boolean b2 = this.latitude >= southWestPoint.getLatitude();
        boolean b3 = this.longitude <= northEastPoint.getLongitude();
        boolean b4 = this.latitude <= northEastPoint.getLatitude();
        return b1 && b2 && b3 && b4;
    }

    /**
     * 转换为 WGS84 坐标
     *
     * @return 坐标点工具类 (WGS84)
     */
    public PointHelper toWGS84() {
        if (this.coordinateSystem == CoordinateSystem.WGS84) {
            return this;
        }
        if (this.coordinateSystem == CoordinateSystem.GCJ02) {
            return gcj02ToWgs84(this.longitude, this.latitude);
        }
        if (this.coordinateSystem == CoordinateSystem.BD09) {
            PointHelper gcj = bd09ToGcj02(this.longitude, this.latitude);
            return gcj02ToWgs84(gcj.longitude, gcj.latitude);
        }
        return this;
    }

    /**
     * 转换为 GCJ02 坐标
     *
     * @return 坐标点工具类 (GCJ02)
     */
    public PointHelper toGCJ02() {
        if (this.coordinateSystem == CoordinateSystem.GCJ02) {
            return this;
        }
        if (this.coordinateSystem == CoordinateSystem.WGS84) {
            return wgs84ToGcj02(this.longitude, this.latitude);
        }
        if (this.coordinateSystem == CoordinateSystem.BD09) {
            return bd09ToGcj02(this.longitude, this.latitude);
        }
        return this;
    }

    /**
     * 转换为 BD09 坐标
     *
     * @return 坐标点工具类 (BD09)
     */
    public PointHelper toBD09() {
        if (this.coordinateSystem == CoordinateSystem.BD09) {
            return this;
        }
        if (this.coordinateSystem == CoordinateSystem.GCJ02) {
            return gcj02ToBd09(this.longitude, this.latitude);
        }
        if (this.coordinateSystem == CoordinateSystem.WGS84) {
            // WGS84 -> GCJ02 -> BD09
            PointHelper gcj = wgs84ToGcj02(this.longitude, this.latitude);
            return gcj02ToBd09(gcj.longitude, gcj.latitude);
        }
        return this;
    }

    /**
     * 转换为目标坐标系
     *
     * @param target 目标坐标系
     * @return 坐标点工具类
     */
    public PointHelper to(CoordinateSystem target) {
        if (target == null || target == this.coordinateSystem) {
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
                return this;
        }
    }
}