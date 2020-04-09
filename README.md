# redis实现分布式锁
## 1.定义
  一个操作要修改用户的状态，修改状态需要先读出用户的状态，在内存里进行修改，改完了再存回去。如果这样的操作同时进行，就     会出现并发问题，因为“读取”和“存储”这个操作不是原子操作（原子操作是指不会被线程调度机制打断的操作，这种操作一旦开     始，就会一直运行到结束，中间不会有任何线程的切换）这个时候就需要分布式锁来保证同一时间修改数据的进程数。
## 2.分布式锁的条件
  * 可以保证在分布式部署的应用集群中，同一个方法在同一时间只能被一台机器-上的一个线程执行。
  * 避免死锁（可重入锁）
  * 高可用的释放或获取锁
  * 性能保障
## 3.redis实现分布式锁的原理
  * redis指令setnx(set if not exists 即：当key不存在时才能进行设置，反之则放弃此次操作)允许一个客户端进行操作。
  * 获取锁：setnx(key, value) 释放锁del(key)
  * 且先到先得，可保证锁的公平性
  
### 3.1 存在一个问题（死锁）？
   当一个用户加锁之后出现异常，没有调用del(key)释放锁，造成死锁。
   * 解决办法：
   > 给key设置过期时间，即执行expire key 5，这样在出现死锁后5秒后自动释放锁
### 3.2 expire自动释放锁可能出现新的问题
   1. setnx(key, value) //加锁
   2. expire key 5      //设置5秒后自动释放锁
   3. del key           //释放锁
   当上面三条指令并非原子操作，如果在加锁之后服务器宕机 （expire key 5）指令得不到执行，同样或造成死锁。
   * 思考：能否使用redis的事务进行同步？
   * 解决办法
   > 针对于上述问题：redis2.8之后作者合并了setnx与expire指令，即两条指令同时执行
   >> **SET key value [EX seconds] [PX milliseconds] [NX|XX]** 参考： [redis命令大全](http://doc.redisfans.com/string/set.html)
   >> **set lock true ex 5 nx**
### 3.3 超时问题
  如某个线程执行任务的时长不确定，可能会出现设置的过期时间过短，任务没有执行完，就已经释放锁了，则其他线程会介入则会导致锁失效。
  > 上述问题是redis作为分布式锁的一大弊端，为了避免该问题，redis的分布式锁不要用于较长时间的任务，如果真的偶尔出现了问题，造成的数据小错乱可能需要人工介入解决。
### 3.4 可重入性
  可重入性是指线程在持有锁的情况下再次请求加锁，如果一个锁支持同一个线程的多次加锁，那么这个锁就是可重入的，类似于java语言中的ReentrantLock就是可重入锁，redis分布式锁实现可重入性需要对set指令进行包装 使用ThreadLocal变量来存储当前持有锁的计数。
    package com.wangjinyin.study.config;

    import java.util.HashMap;
    import java.util.Map;

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
