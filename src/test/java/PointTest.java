import io.github.bootystar.helper.base.map.MapHelper;
import org.junit.jupiter.api.Test;

/**
 * @author bootystar
 */
public class PointTest {
    
    @Test
    public void test(){
        String s1 = "103.92377,30.57447";
        String s2 = "103.01006640539948,29.04151625412239";
        MapHelper.Point p1 = new MapHelper.Point();
        p1.setLongitude(Double.parseDouble(s1.split(",")[0]));
        p1.setLatitude(Double.parseDouble(s1.split(",")[1]));
        MapHelper.Point p2 = new MapHelper.Point();
        p2.setLongitude(Double.parseDouble(s2.split(",")[0]));
        p2.setLatitude(Double.parseDouble(s2.split(",")[1]));

        System.out.println(MapHelper.getDistanceKilometer(p1, p2));
        System.out.println(MapHelper.getDistanceMeters(p1, p2));
        


    }
}
