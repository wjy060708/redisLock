package com.wangjinyin.study.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis 的 工具类
 */
public class RedisUtil {
    // 创建一个连接池
    private JedisPool jedisPool = null;
    // 初始化方法
    public void init(String host,int prot,int database){
        // 创建连接池的参数配置类
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 总数
        jedisPoolConfig.setMaxTotal(200);
        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 在获取连接时，检查是否有效
        jedisPoolConfig.setTestOnBorrow(true);
        // 创建连接池
        jedisPool = new JedisPool(jedisPoolConfig,host,prot,20*1000);
    }

    // 获取连接池中的Jedis
    public Jedis getJedis(){
        return  jedisPool.getResource();
    }
}
