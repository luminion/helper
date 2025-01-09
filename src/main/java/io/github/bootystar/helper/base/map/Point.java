package io.github.bootystar.helper.base.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bootystar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    /**
     * 经度
     */
    private double longitude;
    /**
     * 纬度
     */
    private double latitude;
}