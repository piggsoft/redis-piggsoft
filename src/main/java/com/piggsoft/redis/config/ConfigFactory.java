package com.piggsoft.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.xml.bind.JAXB;
import java.net.URL;

/**
 * <br>Created by fire pigg on 2016/5/24.
 *
 * @author piggsoft
 */
public class ConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);

    private static Config config;

    private static Object lock = new Object();

    public static Config getConfig() {
        if (null == config) {
            synchronized (lock) {
                if (null == config) {
                    try {
                        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                        Resource[] resources = resolver.getResources("classpath*:redis.xml");
                        if (resources != null && resources.length > 0) {
                            Resource resource = resources[0];
                            URL url = resource.getURL();
                            config = JAXB.unmarshal(url, Config.class);
                        }
                    } catch (Exception e) {
                        LOGGER.error("读取配置文件出错", e);
                    }
                }
            }
        }
        return config;
    }
}
