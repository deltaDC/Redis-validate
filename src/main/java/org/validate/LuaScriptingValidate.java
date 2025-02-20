package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class LuaScriptingValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

//        sumOfTwoIntLua(jedis);
//
//        checkKeyAndUpdateLua(jedis);
//
//        batchInsertLua(jedis);
//
//        getKeysLargerThanThresholdLua(jedis);

        compareSpeedOfNormalCommandVsPipelineVsLua(jedis);

    }

    private static void compareSpeedOfNormalCommandVsPipelineVsLua(Jedis jedis) {
        String key = "benchmark_key";
        int iterations = 1000;

        jedis.set(key, "0");

        long startNormal = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            jedis.incr(key);
        }
        long endNormal = System.nanoTime();
        System.out.println("Normal Commands Time: " + (endNormal - startNormal) / 1e6 + " ms");

        jedis.set(key, "0");

        long startPipeline = System.nanoTime();
        Pipeline pipeline = jedis.pipelined();
        for (int i = 0; i < iterations; i++) {
            pipeline.incr(key);
        }
        pipeline.sync();
        long endPipeline = System.nanoTime();
        System.out.println("Pipeline Time: " + (endPipeline - startPipeline) / 1e6 + " ms");

        jedis.set(key, "0");

        String luaScript = "for i=1," + iterations + " do redis.call('INCR', KEYS[1]) end";
        long startLua = System.nanoTime();
        jedis.eval(luaScript, 1, key);
        long endLua = System.nanoTime();
        System.out.println("Lua Script Time: " + (endLua - startLua) / 1e6 + " ms");
    }

    private static void getKeysLargerThanThresholdLua(Jedis jedis) {

        jedis.set("key1", "100");
        jedis.set("key2", "50");
        jedis.set("key3", "200");

        String luaScript = """
                    local threshold = tonumber(ARGV[1])
                    local cursor = '0'
                    local result = {}
                
                    repeat
                        local scan_result = redis.call('SCAN', cursor, 'MATCH', '*')
                        cursor = scan_result[1]
                        local keys = scan_result[2]
                
                        for _, key in ipairs(keys) do
                            local value = tonumber(redis.call('GET', key))
                            if value and value > threshold then
                                table.insert(result, key)
                            end
                        end
                    until cursor == '0'
               
                    return result
                """;

        Object result = jedis.eval(luaScript, 0, "75");
        System.out.println(result);
    }

    private static void batchInsertLua(Jedis jedis) {
        String luaScript = """
                    for i = 1, #KEYS do
                        redis.call('SET', KEYS[i], ARGV[i])
                    end return 'Batch insert completed'
                """;

        Object result = jedis.eval(luaScript, 3, "key1", "key2", "key3", "value1", "value2", "value3");
        System.out.println(result);
    }

    private static void checkKeyAndUpdateLua(Jedis jedis) {
        jedis.set("myKey", "oldValue");

        String luaScript = """
                    if redis.call('EXIST', KEYS[1]) == 1 then
                        redis.call('SET', KEYS[1], ARGV[1])
                        return 'Updated'
                    else
                        return 'Key is not exist'
                    end
                """;
        Object result = jedis.eval(luaScript, 1, "myKey", "newValue");
        System.out.println(result);
    }

    private static void sumOfTwoIntLua(Jedis jedis) {

        jedis.set("num1", "10");
        jedis.set("num2", "20");

        String luaScript = """
                    local a = tonumber(redis.call('GET', KEYS[1])) or 0
                    local b = tonumber(redis.call('GET', KEYS[2])) or 0
                    return a + b
                """;

        Object result = jedis.eval(luaScript, 2, "num1", "num2");
        System.out.println(result);

    }
}
