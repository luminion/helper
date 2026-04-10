package io.github.luminion.helper.geographical;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoHelperTest {

    @Test
    void shouldCalculateDistanceAndGeometry() {
        GeoHelper point = GeoHelper.ofWGS84(116.404, 39.915);
        GeoHelper near = GeoHelper.ofWGS84(116.405, 39.9155);

        assertTrue(point.getDistanceMeters(near) > 0);
        assertTrue(point.isInCircle(near, 200));
        assertFalse(point.isInCircle(near, 10));
        assertTrue(point.isInRectangleArea(
                GeoHelper.ofWGS84(116.3, 39.8),
                GeoHelper.ofWGS84(116.5, 40.0)
        ));
        assertTrue(point.isInPolygon(Arrays.asList(
                GeoHelper.ofWGS84(116.3, 39.8),
                GeoHelper.ofWGS84(116.5, 39.8),
                GeoHelper.ofWGS84(116.5, 40.0),
                GeoHelper.ofWGS84(116.3, 40.0)
        )));
    }

    @Test
    void shouldConvertCoordinateSystemsApproximately() {
        GeoHelper wgs84 = GeoHelper.ofWGS84(116.404, 39.915);
        GeoHelper gcj02 = wgs84.toGCJ02();
        GeoHelper convertedBack = gcj02.toWGS84();

        assertEquals(GeoHelper.CoordinateSystem.GCJ02, gcj02.getCoordinateSystem());
        assertEquals(wgs84.getLongitude(), convertedBack.getLongitude(), 0.0005);
        assertEquals(wgs84.getLatitude(), convertedBack.getLatitude(), 0.0005);
    }
}
