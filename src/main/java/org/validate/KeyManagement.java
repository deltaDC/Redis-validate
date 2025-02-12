package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class KeyManagement {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        for(int i = 1; i <= 10; i++) {
//            jedis.set("session:" + i, "value" + i);
        }

        getAllKeysMatches(jedis, "session:*");

        setKeyWithTTL(jedis, "user:1", 10);

        jedis.set("old_key", "123");
        jedis.rename("old_key", "new_key");

        deleteAllKeysByTTL(jedis, "*", 60L);
    }

    private static void deleteAllKeysByTTL(Jedis jedis, String pattern, long timeLeft) {
        Set<String> allKeys = jedis.keys(pattern);

        allKeys.forEach(key -> {
            long ttl = jedis.ttl(key);
            if(ttl == -1) return;
            if (ttl < timeLeft) {
                jedis.del(key);
            }
        });
    }

    private static void setKeyWithTTL(Jedis jedis, String s, int i) {
        jedis.setex(s, i * 60L, "value");
    }

    private static void getAllKeysMatches(Jedis jedis, String s) {
        System.out.println(jedis.keys(s));
    }
}
