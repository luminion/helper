import io.github.bootystar.helper.base.http.IpHelper;
import org.junit.jupiter.api.Test;

/**
 * @author bootystar
 */
public class IpTest {
    
    @Test
    public void test() {
        String ip = IpHelper.getLocalIp();
        System.out.println(ip);
    }
}
