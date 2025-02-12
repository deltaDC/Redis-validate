package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SortedSetValidate {

    public static void main(String[] args) {
        Jedis jedis = RedisManager.getJedis();

        String leaderboardKey = "game_leaderboard";

        addUserToLeaderboard(jedis, leaderboardKey, "alice", getRandomScore());
        addUserToLeaderboard(jedis, leaderboardKey, "bob", getRandomScore());
        addUserToLeaderboard(jedis, leaderboardKey, "charlie", getRandomScore());

        String topUser = getTopUser(jedis, leaderboardKey);
        System.out.println("Top user: " + topUser);

        increaseUserScore(jedis, leaderboardKey, "alice", 10);

        Long rank = getUserRank(jedis, leaderboardKey, "alice");
        System.out.println("Alice's rank: " + rank);

        Set<String> topUsers = getTopUsers(jedis, leaderboardKey, 3);
        System.out.println("Top 3 users: " + topUsers);
    }

    public static boolean addUserToLeaderboard(Jedis jedis, String leaderboardKey, String user, double score) {
        long r = jedis.zadd(leaderboardKey, score, user);

        return r > 0;
    }

    public static String getTopUser(Jedis jedis, String leaderboardKey) {
        return jedis.zrevrange(leaderboardKey, 0, 0).get(0);
    }

    public static void increaseUserScore(Jedis jedis, String leaderboardKey, String user, double increment) {
        jedis.zincrby(leaderboardKey, increment, user);
    }

    public static Long getUserRank(Jedis jedis, String leaderboardKey, String user) {

        Long rank = jedis.zrank(leaderboardKey, user);
        return (rank == null) ? null : rank + 1;
    }

    public static Set<String> getTopUsers(Jedis jedis, String leaderboardKey, int topN) {

        List<String> ls =  jedis.zrange(leaderboardKey, 0, topN - 1);

        return new HashSet<>(ls);
    }

    public static double getRandomScore() {
        return Math.random() * 100;
    }
}
