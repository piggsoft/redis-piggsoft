package com.piggsoft.redis.connection;

import com.piggsoft.redis.config.Config;
import com.piggsoft.redis.config.ConfigFactory;
import com.piggsoft.redis.config.Host;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author piggsoft
 * @version 1.0
 * @create 2016/9/1
 * @since 1.0
 */
public class ClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);

    private static Object config_lock = new Object();

    private static Object pool_lock = new Object();

    private static Object cluster_lock = new Object();

    private static GenericObjectPoolConfig genericObjectPoolConfig;

    private static Pool pool;

    private static JedisCluster jedisCluster;

    private static GenericObjectPoolConfig getConfig() {
        if (genericObjectPoolConfig == null) {
            synchronized (config_lock) {
                if (genericObjectPoolConfig == null) {
                    Config config = ConfigFactory.getConfig();
                    genericObjectPoolConfig = new JedisPoolConfig();
                    genericObjectPoolConfig.setMaxTotal(config.getMaxTotal());
                    genericObjectPoolConfig.setMaxIdle(config.getMaxIdle());
                    genericObjectPoolConfig.setMaxWaitMillis(config.getMaxWaitMillis());
                    // 测试连接是否正常
                    genericObjectPoolConfig.setTestOnBorrow(config.isTestOnBorrow());
                    genericObjectPoolConfig.setMinIdle(config.getMinIdle());
                }
            }
        }
        return genericObjectPoolConfig;
    }



    private static Pool getPool() {
        if (pool == null) {
            synchronized (pool_lock) {
                if (pool == null) {
                    Config config = ConfigFactory.getConfig();
                    if (config.isSharding()) {
                        LOGGER.info("Redis 分片模式启动");
                        List<JedisShardInfo> shards = new ArrayList<>();
                        for (Host host : config.getHosts()) {
                            JedisShardInfo shard = new JedisShardInfo(
                                    host.getUrl(),
                                    host.getPort(),
                                    host.getTimeout() < 0 ? Protocol.DEFAULT_TIMEOUT : host.getTimeout()
                            );
                            shards.add(shard);
                        }
                        pool = new ShardedJedisPool(getConfig(), shards, Hashing.MURMUR_HASH);
                    } else {
                        LOGGER.info("Redis 单机池模式启动");
                        Host host = config.getHosts().get(0);
                        pool = new JedisPool(
                                getConfig(),
                                host.getUrl(),
                                host.getPort(),
                                host.getTimeout() < 0 ? Protocol.DEFAULT_TIMEOUT : host.getTimeout()
                        );
                    }
                }
            }
        }
        return pool;
    }

    public static JedisCommands getCluster() {
        LOGGER.info("Redis 集群模式启动");
        if (jedisCluster == null) {
            synchronized (cluster_lock) {
                if (jedisCluster == null) {
                    Set<HostAndPort> set = new HashSet<>();
                    Config config = ConfigFactory.getConfig();
                    int timeout = -1;
                    for (Host host : config.getHosts()) {
                        HostAndPort hostAndPort = new HostAndPort(host.getUrl(), host.getPort());
                        set.add(hostAndPort);
                        timeout = host.getTimeout() > timeout ? host.getTimeout() : timeout;
                    }
                    jedisCluster = new JedisCluster(
                            set,
                            timeout == -1 ? Protocol.DEFAULT_TIMEOUT : timeout,
                            getConfig()
                    );
                }
            }
        }
        return jedisCluster;
    }

    public static JedisCommands getJedis() {
        JedisCommands jedisCommands = null;
        Config config = ConfigFactory.getConfig();
        if (config.isCluster()) {
            jedisCommands = getCluster();
        } else {
            jedisCommands = (JedisCommands) getPool().getResource();
        }
        return jedisCommands;
    }

    public static void returnResource(JedisCommands jedisCommands) {
        getPool().returnResource(jedisCommands);
    }

}
