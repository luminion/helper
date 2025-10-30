package io.github.luminion.helper.core.geographical;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 坐标点
 * @author luminion
 */
@Getter
public class GeoPoint {
    /**
     * 经度
     */
    private final double longitude;
    /**
     * 纬度
     */
    private final double latitude;

    public GeoPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public GeoPoint(String longitude, String latitude) {
        this.longitude = Double.parseDouble(longitude);
        this.latitude = Double.parseDouble(latitude);
    }

    public GeoPoint(BigDecimal longitude, BigDecimal latitude) {
        this.longitude = longitude.doubleValue();
        this.latitude = latitude.doubleValue();
    }



  
}