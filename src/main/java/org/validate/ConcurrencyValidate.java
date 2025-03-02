package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class ConcurrencyValidate {

    private static final String PRODUCT_KEY = "product:1001";
    private static final String LOCK_KEY = "lock:product:1001";
    private static final String BUYER_KEY = "product:1001:buyer";

    private static final int LOCK_EXPIRE_TIME = 5000; // 5 seconds
    private static final int PROCESS_TIME = 3000; // Simulate processing time

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

    // Initialize product stock in Redis
    public static void initializeProduct(Jedis jedis) {
        jedis.hset(PRODUCT_KEY, "stock", "1"); // Only 1 product available
        jedis.del(BUYER_KEY); // Reset buyer info
    }

    // Acquire Redis lock with expiration time
    public static boolean acquireLock(Jedis jedis, String lockValue) {
        String result = jedis.set(LOCK_KEY, lockValue, SetParams.setParams().nx().px(LOCK_EXPIRE_TIME));
        return "OK".equals(result);
    }

    // Release Redis lock safely using Lua script
    public static void releaseLock(Jedis jedis, String lockValue) {
        Object result = jedis.eval(UNLOCK_SCRIPT, Collections.singletonList(LOCK_KEY),
                Collections.singletonList(lockValue));
    }

    // Extend the lock expiration (keep lock alive while processing)
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

    // Simulate user trying to buy the product
    public static void tryBuyProduct(Jedis jedis, String userId) {
        String lockValue = UUID.randomUUID().toString(); // Unique identifier for each thread

        if (acquireLock(jedis, lockValue)) {
            System.out.println(userId + " acquired the lock");

            try {
                int stock = Integer.parseInt(jedis.hget(PRODUCT_KEY, "stock"));

                if (stock > 0) {
                    jedis.hset(PRODUCT_KEY, "stock", "0"); // Decrement stock
                    jedis.set(BUYER_KEY, userId); // Save the real buyer
                    System.out.println(userId + " bought the product!");
                } else {
                    System.out.println(userId + " failed: Product out of stock!");
                }

                // Simulate processing time and extend lock if needed
                Thread.sleep(PROCESS_TIME);
                extendLock(jedis, lockValue);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                releaseLock(jedis, lockValue);
                System.out.println(userId + " released the lock");
            }
        } else {
            System.out.println(userId + " failed to acquire the lock");
        }
    }

    // Test case: Simulate 1000 users trying to buy the product
    public static void main(String[] args) {
        Jedis jedis = RedisManager.getJedis();
        initializeProduct(jedis);

        for (int i = 0; i < 1000; i++) {
            String userId = "User-" + i;
            new Thread(() -> tryBuyProduct(jedis, userId)).start();
        }

        // Wait for processing and print the real buyer
        try {
            Thread.sleep(7000);
            String buyer = jedis.get(BUYER_KEY);
            System.out.println("Real buyer: " + buyer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
