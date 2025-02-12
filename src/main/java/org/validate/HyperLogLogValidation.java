package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

public class HyperLogLogValidation {

    public static void main(String[] args) {


        Jedis jedis = RedisManager.getJedis();

        hyperLogLogApproximate(jedis);

        countDistinctUser(jedis);

        mergeTwoHyperLogLog(jedis);

    }

    private static void mergeTwoHyperLogLog(Jedis jedis) {
        String hyperLogLogKey1 = "user:visits:today";
        String hyperLogLogKey2 = "user:visits:yesterday";
        String hyperLogLogKey3 = "user:visits:week";

        String[] userIds1 = {"user1", "user2", "user3", "user1", "user4", "user2", "user5"};
        String[] userIds2 = {"user1", "user2", "user3", "user1", "user4", "user2", "user5", "user6", "user7", "user8"};

        for (String userId : userIds1) {
            jedis.pfadd(hyperLogLogKey1, userId);
        }

        for (String userId : userIds2) {
            jedis.pfadd(hyperLogLogKey2, userId);
        }

        jedis.pfmerge(hyperLogLogKey3, hyperLogLogKey1, hyperLogLogKey2);

        long uniqueUserCount = jedis.pfcount(hyperLogLogKey3);

        System.out.println(uniqueUserCount);
    }

    private static void countDistinctUser(Jedis jedis) {
        String hyperLogLogKey = "user:visits:today";

        String[] userIds = {"user1", "user2", "user3", "user1", "user4", "user2", "user5"};

        for (String userId : userIds) {
            jedis.pfadd(hyperLogLogKey, userId);
        }

        long uniqueUserCount = jedis.pfcount(hyperLogLogKey);

        System.out.println(uniqueUserCount);
    }

    private static void hyperLogLogApproximate(Jedis jedis) {

        jedis.del("hll");

        for(int i = 1; i <= 1000; i++) {
            jedis.pfadd("hll", "element" + i);
        }

        long count = jedis.pfcount("hll");
        System.out.println(count == 1000); //1012
    }
}
