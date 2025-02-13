package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class BitMapValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();
        
        markUserLogin(jedis, "user1");

        checkUserLogin(jedis, "user1");

        countUserLogin(jedis, "user1");

        saveUserStatus(jedis, 1, true);
        saveUserStatus(jedis, 2, false);
        saveUserStatus(jedis, 3, true);

        List<Integer> onlineUsers = getOnlineUsers(jedis, 10);
        System.out.println(onlineUsers);
    }

    private static List<Integer> getOnlineUsers(Jedis jedis, int maxUsers) {
        List<Integer> onlineUsers = new ArrayList<>();

        for (int userId = 0; userId < maxUsers; userId++) {
            if (jedis.getbit("user:online", userId)) {
                onlineUsers.add(userId);
            }
        }
        return onlineUsers;
    }

    private static void saveUserStatus(Jedis jedis, int userId, boolean b) {
        String key = "user:online";
        jedis.setbit(key, userId, b);
    }

    private static void countUserLogin(Jedis jedis, String user1) {
        long count = jedis.bitcount("user#1:login:Feb");
        System.out.println(count);
    }

    private static void checkUserLogin(Jedis jedis, String user1) {
        boolean isLogin = jedis.getbit("user#1:login:Feb", 15);
        System.out.println(isLogin);
    }

    private static void markUserLogin(Jedis jedis, String user1) {
        String key = "user#1:login:Feb";

        jedis.setbit(key, 1, true);
        jedis.setbit(key, 2, true);
        jedis.setbit(key, 3, true);

    }
}
