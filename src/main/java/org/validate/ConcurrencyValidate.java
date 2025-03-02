package org.validate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class ConcurrencyValidate {

    private static final String PRODUCT_KEY = "product:1001";
    private static final String LOCK_KEY = "lock:product:1001";
    private static final String BUYER_KEY = "product:1001:buyer";

    private static final int LOCK_EXPIRE_TIME = 5000;
    private static final int PROCESS_TIME = 3000;

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    private static final String EXTEND_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('pexpire', KEYS[1], ARGV[2]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    public static void initializeProduct(Jedis jedis) {
        jedis.hset(PRODUCT_KEY, "stock", "1");
        jedis.del(BUYER_KEY);
    }

    public static boolean acquireLock(Jedis jedis, String lockValue) {
        String result = jedis.set(LOCK_KEY, lockValue, SetParams.setParams().nx().px(LOCK_EXPIRE_TIME));
        return "OK".equals(result);
    }

    public static void releaseLock(Jedis jedis, String lockValue) {
        Object result = jedis.eval(UNLOCK_SCRIPT, Collections.singletonList(LOCK_KEY),
                Collections.singletonList(lockValue));
    }

    public static void extendLock(Jedis jedis, String lockValue) {
        Object result = jedis.eval(EXTEND_LOCK_SCRIPT,
                Collections.singletonList(LOCK_KEY),
                Arrays.asList(lockValue, String.valueOf(LOCK_EXPIRE_TIME)));

        if (result.equals(1L)) {
            System.out.println("Lock extended successfully");
        } else {
            System.out.println("Failed to extend lock (maybe expired or stolen)");
        }
    }

    public static void tryBuyProduct(Jedis jedis, String userId) {
        String lockValue = UUID.randomUUID().toString();

        if (acquireLock(jedis, lockValue)) {
            System.out.println(userId + " acquired the lock");

            try {
                int stock = Integer.parseInt(jedis.hget(PRODUCT_KEY, "stock"));

                if (stock > 0) {
                    jedis.hset(PRODUCT_KEY, "stock", "0");
                    jedis.set(BUYER_KEY, userId);
                    System.out.println(userId + " bought the product!");
                } else {
                    System.out.println(userId + " failed: Product out of stock!");
                }

                Thread.sleep(PROCESS_TIME);
                extendLock(jedis, lockValue);

            } catch (InterruptedException e) {
            } finally {
                releaseLock(jedis, lockValue);
                System.out.println(userId + " released the lock");
            }
        } else {
            System.out.println(userId + " failed to acquire the lock");
        }
    }

    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool("localhost", 6379);

        try (Jedis jedis = jedisPool.getResource()) {
            initializeProduct(jedis);
        }

        for (int i = 0; i < 100; i++) {
            String userId = "User-" + i;
            new Thread(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    tryBuyProduct(jedis, userId);
                }
            }).start();
        }

        try {
            Thread.sleep(7000);
            try (Jedis jedis = jedisPool.getResource()) {
                String buyer = jedis.get(BUYER_KEY);
                System.out.println("Real buyer: " + buyer);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jedisPool.close();
    }
}
