package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class HashValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        // Example: Storing user information and updating email
        String userHashKey = "user:1001";
        String userId = "1001";
        String name = "John Doe";
        String email = "john.doe@example.com";

        // Storing user information in the Hash
        boolean isStored = storeUserInfoInHash(jedis, userHashKey, userId, name, email);
        System.out.println("User information stored: " + isStored);

        // Updating the user's email
        String newEmail = "john.doe@newdomain.com";
        boolean isUpdated = updateUserEmail(jedis, userHashKey, newEmail);
        System.out.println("User email updated: " + isUpdated);

        // Example: Getting all fields and values from the Redis Hash
        Map<String, String> userInfo = getAllFieldsAndValuesFromHash(jedis, userHashKey);
        System.out.println("User info: " + userInfo);

        // Example: Checking if a specific field exists in the Hash
        boolean emailExists = checkFieldExistsInHash(jedis, userHashKey, "email");
        System.out.println("Email field exists: " + emailExists);

        // Example: Removing a field from the Redis Hash
        boolean isRemoved = removeFieldFromHash(jedis, userHashKey, "email");
        System.out.println("Email field removed: " + isRemoved);

        // Example: Incrementing a numeric value in the Hash
        String field = "loginCount";
        long incrementedValue = incrementFieldBy5(jedis, userHashKey, field);
        System.out.println("Incremented login count: " + incrementedValue);
    }

    // 1. Implement storing user information into a Redis Hash and updating their email
    public static boolean storeUserInfoInHash(Jedis jedis, String userHashKey, String userId, String name, String email) {
        Map<String, String> mp = new HashMap<>();
        mp.put("userId", userId);
        mp.put("name", name);
        mp.put("email", email);
        mp.put("newKey", "newValue");

        //hset will return number of fields that were added/updated
        long res = jedis.hset(userHashKey, mp);

        return res >= 1;
    }

    public static boolean updateUserEmail(Jedis jedis, String userHashKey, String newEmail) {
        // Check current email value before updating
        String currentEmail = jedis.hget(userHashKey, "email");
        System.out.println("Current Email: " + currentEmail);

        //TODO figure out why hset always return 0
        // Update the "email" field in the user's hash with the new email
        long res = jedis.hset(userHashKey, "email", "newEmail");

        return res > 0;
    }

    // 2. Implement getting all keys and values of a Redis Hash
    public static Map<String, String> getAllFieldsAndValuesFromHash(Jedis jedis, String userHashKey) {

        return jedis.hgetAll(userHashKey);
    }

    // 3. Implement checking if a specific field exists in the Hash
    public static boolean checkFieldExistsInHash(Jedis jedis, String userHashKey, String field) {

        return jedis.hexists(userHashKey, field);
    }

    // 4. Implement removing a field from the Hash and confirming its deletion
    public static boolean removeFieldFromHash(Jedis jedis, String userHashKey, String field) {

        long res = jedis.hdel(userHashKey, field);

        return res == 1;
    }

    // 5. Implement incrementing the numeric value of a field in the Hash by 5
    public static long incrementFieldBy5(Jedis jedis, String userHashKey, String field) {
        return jedis.hincrBy(userHashKey, field, 5);
    }
}
