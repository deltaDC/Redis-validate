package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

public class StringValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        jedis.set("helloKey", "Hello");
        String res1 = appendKeyString(jedis, "helloKey", "world !!!");
        System.out.println(res1);


        String jsonValue = "{\"id\":1,\"name\":\"Alice\",\"email\":\"alice@example.com\"}";
        String res2 = jsonSaveAndGet(jedis, "jsonKey", jsonValue);
        System.out.println(res2);


        setKeyIfNotExist(jedis, "someNewKey", "some Value");
        setKeyIfNotExist(jedis, "helloKey", "some old key-value");


        jedis.set("numberKey", "0");
        String res3 = increaseValueByKey(jedis, "numberKey", 10L);
        System.out.println(res3);

        jedis.set("oldKey", "pending");
        changeKeyIfValueIsPending(jedis);
    }

    public static String appendKeyString(Jedis jedis, String key, String appendString) {
        jedis.append(key, " " + appendString);

        return jedis.get(key);

    }

    public static String jsonSaveAndGet(Jedis jedis, String key, String json) {
        jedis.set(key, json);

        return jedis.get(key);
    }

    public static void setKeyIfNotExist(Jedis jedis, String key, String value) {
        long isNotExist = jedis.setnx(key, value);

        if (isNotExist == 1) {
            System.out.println("Key was set: " + jedis.get(key));
        } else {
            System.out.println("Key already exists, value: " + jedis.get(key));
        }
    }

    public static String increaseValueByKey(Jedis jedis, String key, Long increment) {
        long newValue = jedis.incrBy(key, increment);

        return String.valueOf(newValue);
    }

    private static void changeKeyIfValueIsPending(Jedis jedis) {
        String res = jedis.get("oldKey");

        if("pending".equalsIgnoreCase(res)) {
            jedis.set("oldKey", "processed");
        }
    }

}
