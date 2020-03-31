package com.wangjinyin.study.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

public class RedisWithReetrantLock {

	//使用ThreadLocal对redis的锁进行包装使其可重入
	private ThreadLocal<Map<String, Integer>> locks = new ThreadLocal<Map<String,Integer>>();
	
	private  Jedis jedis = null;
	
	public RedisWithReetrantLock(Jedis jedis) {
		this.jedis = jedis;
	}
	
	//上锁
	private boolean _lock(String key) {
		return jedis.set(key, "", "nx", "ex", 5L) != null;
	}
	
	//释放锁
	private void _unlock(String key) {
		jedis.del(key);
	}
	
	private Map<String, Integer> currentLockers() {
		Map<String, Integer> refs = locks.get();
		
		if (refs != null) {
			return refs;
		}
		locks.set(new HashMap<String, Integer>());
		return locks.get();
	}
	
	public boolean lock(String key) {
		Map<String, Integer> refs = currentLockers();
		Integer refCnt = refs.get(key);
		
		if (refCnt != null) {
			refs.put(key, refCnt + 1);
			return true;
		} 
		
		boolean ok = this._lock(key);
		if (!ok) {
			return false;
		}
		
		refs.put(key, 1);
		return true;
	}
	
	public boolean unlock(String key) {
		Map<String, Integer> refs = currentLockers();
		Integer refCnt = refs.get(key);
		
		if (refCnt == null) {
			return false;
		}
		
		refCnt -= 1;
		if (refCnt > 0) {
			refs.put(key, refCnt);
		} else {
			refs.remove(key);
			this._unlock(key);
		}
		
		return true;
	}
}
