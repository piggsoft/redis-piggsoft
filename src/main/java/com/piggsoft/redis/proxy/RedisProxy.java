package com.piggsoft.redis.proxy;

import com.piggsoft.redis.connection.ClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author piggsoft
 * @version 1.0
 * @create 2016/9/2
 * @since 1.0
 */
public class RedisProxy implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisProxy.class);

    private RedisProxy() {

    }

    private static Object lock = new Object();
    private static JedisCommands proxy;

    public static JedisCommands getProxyInstance() {
        if (proxy == null) {
            synchronized (lock) {
                if (proxy == null) {
                    proxy = newProxyInstance();
                }
            }
        }
        return proxy;
    }

    private static JedisCommands newProxyInstance() {
        Object proxy = Proxy.newProxyInstance(
                JedisCommands.class.getClassLoader(),
                new Class<?>[] {JedisCommands.class},
                new RedisProxy()
        );
        return JedisCommands.class.cast(proxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long time = System.currentTimeMillis();
        JedisCommands jedisCommands = ClientFactory.getJedis();
        Object result = ReflectionUtils.invokeMethod(method, jedisCommands, args);
        ClientFactory.returnResource(jedisCommands);
        LOGGER.debug("本次耗时: {}(ms)", System.currentTimeMillis() - time);
        return result;
    }
}
