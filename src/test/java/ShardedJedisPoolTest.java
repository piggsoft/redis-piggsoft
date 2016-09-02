import com.piggsoft.redis.connection.ClientFactory;
import com.piggsoft.redis.proxy.RedisProxy;
import org.junit.Test;
import redis.clients.jedis.JedisCommands;

import java.util.UUID;

/**
 * @author piggsoft
 * @version 1.0
 * @create 2016/9/1
 * @since 1.0
 */
public class ShardedJedisPoolTest {

    @Test
    public void test() {
        JedisCommands jedisCommands = ClientFactory.getJedis();
        ClientFactory.returnResource(jedisCommands);
    }

    @Test
    public void test01() {
        JedisCommands jedisCommands = RedisProxy.getProxyInstance();
        System.out.println(jedisCommands.get("1111"));
        jedisCommands.set("1111", "hahaha", "NX", "PX", 1000L);
        System.out.println(jedisCommands.get("1111"));
        System.out.println(jedisCommands.ttl("1111"));
        jedisCommands.expire("1111", 10);
        System.out.println(jedisCommands.ttl("1111"));
    }

    /**
     * 分片测试
     */
    @Test
    public void test02() {
        for (int i=0; i<10; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j=0; j<10000; j++) {
                        JedisCommands jedisCommands = RedisProxy.getProxyInstance();
                        jedisCommands.set("test" + UUID.randomUUID().toString(), "hahaha", "NX", "PX", 10000L);
                    }
                }
            }).start();
        }
        try {
            Thread.sleep(10000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
