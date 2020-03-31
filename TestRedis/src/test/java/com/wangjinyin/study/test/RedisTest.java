package com.wangjinyin.study.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.wangjinyin.study.config.RedisUtil;
import com.wangjinyin.study.config.RedisWithReetrantLock;

import redis.clients.jedis.Jedis;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
	
  @Autowired
  private RedisUtil redisUtil;

  @Test
  public void test() {
	  Jedis jedis = redisUtil.getJedis();
	  
	  RedisWithReetrantLock redis = new RedisWithReetrantLock(jedis);
	  
	  System.out.println(redis.lock("yui"));
	  System.out.println(redis.lock("yui"));
	  System.out.println(redis.unlock("yui"));
	  System.out.println(redis.unlock("yui"));
	  
  }
}
