package com.piggsoft.redis.config.bean;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author piggsoft
 * @version 1.0
 * @create 2016/9/1
 * @since 1.0
 */
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    private boolean isSharding = false;
    private boolean isCluster = false;
    private int maxTotal = 1000;
    private int maxIdle = 100;
    private long maxWaitMillis = 1000L;
    private boolean testOnBorrow = true;
    private int minIdle = 0;


    @XmlElementWrapper(name = "hosts")
    @XmlElementRef
    private List<Host> hosts;

    public boolean isSharding() {
        return isSharding;
    }

    public void setSharding(boolean sharding) {
        isSharding = sharding;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public boolean isCluster() {
        return isCluster;
    }

    public void setCluster(boolean cluster) {
        isCluster = cluster;
    }
}
