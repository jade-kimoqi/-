import com.atguigu.AlbumApplication;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {AlbumApplication.class,RedissonClient.class})
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;
    @Test
    public void test(){

        System.out.println(redissonClient);
    }
}