package com.wangjinyin.study.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.wangjinyin.study.bean.User;
import com.wangjinyin.study.config.RedisUtil;
import com.wangjinyin.study.mapper.UserMapper;

import redis.clients.jedis.Jedis;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private RedisUtil redisUtil;
	
	private final String prefix = "user:";
	
	private final String suffix = ":info";
	
	public User getUser(Long id) {
		
		Jedis jedis = redisUtil.getJedis();
		
		String userJson = jedis.get(prefix + id + suffix);
		
		if (userJson != null && userJson.length() > 0) {
			
			 User user = JSON.parseObject(userJson, User.class);
			 System.out.println("从redis中查询");
			 jedis.close();
			 return user;
		} else {
			
			User user2 = userMapper.selectByPrimaryKey(id);
			jedis.set(prefix + id + suffix, JSON.toJSONString(user2));
			System.out.println("从数据库中查");
			jedis.close();
			return user2;
		}
		
	}
}
