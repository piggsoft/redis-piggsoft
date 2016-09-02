package com.piggsoft.redis.proxy;

import com.piggsoft.redis.connection.ClientFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.Method;

/**
 * @author piggsoft
 * @version 1.0
 * @create 2016/9/2
 * @since 1.0
 */
public class RedisProxy implements MethodInterceptor {

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
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[]{JedisCommands.class});
        enhancer.setCallback(new RedisProxy());
        return (JedisCommands) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        long time = System.currentTimeMillis();
        JedisCommands jedisCommands = ClientFactory.getJedis();
        Object result = ReflectionUtils.invokeMethod(method, jedisCommands, objects);
        ClientFactory.returnResource(jedisCommands);
        LOGGER.debug("本次耗时: {}(ms)", System.currentTimeMillis() - time);
        return result;
    }
}
