package io.github.luminion.helper.geo;

import java.math.BigDecimal;
import java.util.Collection;

final class GeoArguments {
    private GeoArguments() {}

    static void validateCoordinate(double longitude, double latitude, CoordinateSystem coordinateSystem) {
        if (coordinateSystem == null) {
            throw new IllegalArgumentException("coordinateSystem must not be null");
        }
        if (!GeoHelper.isValid(longitude, latitude)) {
            throw new IllegalArgumentException("invalid coordinate: longitude=" + longitude + ", latitude=" + latitude);
        }
    }

    static double parseTextCoordinate(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is blank");
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " is invalid: " + text, e);
        }
    }

    static double requireDecimalCoordinate(BigDecimal decimal, String fieldName) {
        if (decimal == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return decimal.doubleValue();
    }

    static GeoPoint[] toArray(Collection<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            return new GeoPoint[0];
        }
        return points.toArray(new GeoPoint[0]);
    }
}
