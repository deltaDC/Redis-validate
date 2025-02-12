package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class SetValidate {

    public static void main(String[] args) {

        // Get Jedis instance
        Jedis jedis = RedisManager.getJedis();

        // 1. Add a user to the active set and check if the user is added successfully
        String userKey = "activeUsers";
        String user1 = "Alice";
        boolean isAdded = addUserToActiveSet(jedis, userKey, user1);
        System.out.println("User " + user1 + " added: " + isAdded);

        // Check if user is active
        boolean isUserActive = isUserActive(jedis, userKey, user1);
        System.out.println("Is user " + user1 + " active? " + isUserActive);

        // 2. Get unique users from two Redis sets
        String setKey1 = "set1";
        String setKey2 = "set2";
        Set<String> uniqueUsers = getUniqueUsersFromSets(jedis, setKey1, setKey2);
        System.out.println("Unique users: " + uniqueUsers);

        // 3. Find users in both Redis sets (intersection)
        Set<String> usersInBothSets = getUsersInBothSets(jedis, setKey1, setKey2);
        System.out.println("Users in both sets: " + usersInBothSets);

        // 4. Remove a user from a Redis set
        String userToRemove = "Bob";
        boolean isRemoved = removeUserFromSet(jedis, setKey1, userToRemove);
        System.out.println("User " + userToRemove + " removed: " + isRemoved);

        // 5. Check if a user is logging in for the first time using Redis Sets
        String loginSetKey = "loginUsers";
        String user2 = "Charlie";
        boolean isFirstLogin = checkUniqueLogin(jedis, loginSetKey, user2);
        System.out.println("Is " + user2 + " logging in for the first time? " + isFirstLogin);


    }

    // 1. Implement a Set to store a list of active users and check if a specific user is in the list
    public static boolean addUserToActiveSet(Jedis jedis, String userKey, String user) {
        long res = jedis.sadd(userKey, user);

        return res == 1;
    }

    public static boolean isUserActive(Jedis jedis, String userKey, String user) {
        return jedis.sismember(userKey, user);
    }

    // 2. Implement a way to get the unique users from two Redis Sets
    public static Set<String> getUniqueUsersFromSets(Jedis jedis, String setKey1, String setKey2) {

        return jedis.sunion(setKey1, setKey2);
    }

    // 3. Implement a command to find users present in both Redis Sets
    public static Set<String> getUsersInBothSets(Jedis jedis, String setKey1, String setKey2) {
        return jedis.sinter(setKey1, setKey2);
    }

    // 4. Implement a way to remove a user from a Redis Set
    public static boolean removeUserFromSet(Jedis jedis, String setKey, String user) {

        long res = jedis.srem(setKey, user);

        return res == 1;
    }

    // 5. Implement a unique login check system using Redis Sets
    public static boolean checkUniqueLogin(Jedis jedis, String loginSetKey, String user) {
        boolean isHaveLogin = jedis.sismember(loginSetKey, user);
        if(isHaveLogin) return false;

        jedis.sadd(loginSetKey, user);

        return true;
    }
}
