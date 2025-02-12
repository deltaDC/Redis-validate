package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchOperationValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        jedis.set("key1", "value1");
        jedis.set("key2", "value2");

        getMultipleKeysBatchOperation(jedis);
        getMultipleKeysPipeline(jedis);

        setMultipleLeaderBoardMark(jedis);

    }

    private static void setMultipleLeaderBoardMark(Jedis jedis) {
        Map<String, String> leaderboardData = new HashMap<>();
        leaderboardData.put("leaderboard:1", "1000");
        leaderboardData.put("leaderboard:2", "1500");
        leaderboardData.put("leaderboard:3", "2000");
        leaderboardData.put("leaderboard:4", "1800");

        jedis.mset("abc", "123");

        System.out.println("Leaderboard points:");
        for (String key : leaderboardData.keySet()) {
            String value = jedis.get(key);
            System.out.println(key + ": " + value);
        }
    }

    private static void getMultipleKeysPipeline(Jedis jedis) {
        Pipeline pipeline = new Pipeline(jedis);

        List<Response<String>> responses = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            responses.add(pipeline.get("key" + i));
        }

        System.out.println("before sync");
        for (int i = 0; i < responses.size(); i++) {
            String value = responses.get(i).get();
            System.out.println("key" + (i + 1) + ": " + value);
        }

        pipeline.sync();

        System.out.println("after sync");

        for (int i = 0; i < responses.size(); i++) {
            String value = responses.get(i).get();
            System.out.println("key" + (i + 1) + ": " + value);
        }
    }

    private static void getMultipleKeysBatchOperation(Jedis jedis) {
        List<String> command = new ArrayList<>();

        for(int i = 1; i <= 100; i++) {
            command.add("key" + i);
        }

        System.out.println("before mget");
        List<String> value = jedis.mget(command.toArray(new String[0]));
        System.out.println("after mget");

        System.out.println(value);
    }
}
