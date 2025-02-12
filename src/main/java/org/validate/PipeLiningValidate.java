package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PipeLiningValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        setKeysInPipeline(jedis);

        jedis.set("counter", "0");

        int n = 1000;

        long startTimeNoPipeline = System.nanoTime();
        increaseCounterWithoutPipeline(jedis, "counter", n);
        long endTimeNoPipeline = System.nanoTime();
        System.out.println("Time without pipeline: " + (endTimeNoPipeline - startTimeNoPipeline) / 1_000_000.0 + " ms");

        jedis.set("counter", "0");

        long startTimeWithPipeline = System.nanoTime();
        increaseCounterWithPipeline(jedis, "counter", n);
        long endTimeWithPipeline = System.nanoTime();
        System.out.println("Time with pipeline: " + (endTimeWithPipeline - startTimeWithPipeline) / 1_000_000.0 + " ms");


        getUserValuesWithPipeline(jedis, "user");

        createKeysAndDeleteWithPipeline(jedis);

        jedis.set("key1", "value1");
        jedis.set("key2", "value2");
        jedis.set("key3", "value3");

        checkAndUpdateKeys(jedis);

    }

    private static void checkAndUpdateKeys(Jedis jedis) {
        Pipeline pipeline = jedis.pipelined();

        pipeline.exists("key1");
        pipeline.set("key1", "newValue1");

        pipeline.exists("key2");
        pipeline.set("key2", "newValue2");

        pipeline.exists("key3");
        pipeline.set("key3", "newValue3");

        pipeline.sync();
    }

    private static void createKeysAndDeleteWithPipeline(Jedis jedis) {
        Pipeline pipeline = new Pipeline(jedis);

        int n = 1000;

        for(int i = 1; i <= n; i++) {
            pipeline.set("user:" + i, "value" + i);
        }

        for(int i = 1; i <= n; i++) {
            pipeline.del("user:" + i);
        }

        pipeline.sync();
    }

    private static void getUserValuesWithPipeline(Jedis jedis, String user) {
        Pipeline pipeline = new Pipeline(jedis);

        for(int i = 1; i <= 5; i++) {
            pipeline.get(user + ":" + i);
        }
        pipeline.sync();
    }

    private static void increaseCounterWithoutPipeline(Jedis jedis, String key, int n) {
        for (int i = 0; i < n; i++) {
            jedis.incr(key);
        }
    }

    private static void increaseCounterWithPipeline(Jedis jedis, String key, int n) {
        Pipeline pipeline = new Pipeline(jedis);
        for(int i = 0; i < n; i++) {
            pipeline.incr(key);
        }
        pipeline.sync();
    }

    private static void setKeysInPipeline(Jedis jedis) {
        Pipeline pipeline = jedis.pipelined();
        pipeline.set("a", "valueA");
        pipeline.set("b", "valueB");
        pipeline.set("c", "valueC");
        pipeline.sync();
    }
}
