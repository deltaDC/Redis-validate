package org.manager;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {

        try (Jedis jedis = RedisManager.getJedis()) { // Auto-close using try-with-resources
            System.out.println(jedis.get("testKey"));
        } catch (Exception e) {
            System.err.println("Redis error: " + e.getMessage());
        } finally {
            RedisManager.closePool();
        }
    }
}