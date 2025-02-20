package org.validate;

import io.redisearch.Schema;
import io.redisearch.client.Client;
import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.commands.ProtocolCommand;

public class RediSearchValidate {

    public enum RediSearchCommand implements ProtocolCommand {
        FT_CREATE("FT.CREATE"),
        FT_SEARCH("FT.SEARCH");

        private final byte[] raw;

        RediSearchCommand(String command) {
            this.raw = command.getBytes();
        }

        @Override
        public byte[] getRaw() {
            return raw;
        }
    }

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        addData(jedis);

        createIndex(jedis);
    }

    public static void createIndex(Jedis jedis) {

        jedis.getClient().sendCommand(RediSearchCommand.FT_CREATE,
                "myIndex", "ON", "HASH", "PREFIX", "1", "doc:",
                "SCHEMA", "title", "TEXT", "SORTABLE", "price", "NUMERIC", "SORTABLE");

    }

    public static void addData(Jedis jedis) {

        jedis.hset("doc:1", "title", "Redis Search Tutorial");
        jedis.hset("doc:1", "price", "15");
        jedis.hset("doc:2", "title", "Learn Redis with Examples");
        jedis.hset("doc:2", "price", "20");

        jedis.hset("doc:3", "title", "Advanced Redis Guide");
        jedis.hset("doc:3", "price", "25");

        System.out.println("ok");

    }

    public static void searchByKeyword(Jedis jedis, String keyword) {

        Object response = jedis.sendCommand(Protocol.Command.valueOf("FT.SEARCH"), "myIndex", keyword);
        System.out.println("Kết quả tìm kiếm: " + response);

    }

    public static void searchWithFilter(Jedis jedis, String keyword, int minPrice, int maxPrice) {

        Object response = jedis.sendCommand(Protocol.Command.valueOf("FT.SEARCH"),
                "myIndex", keyword, "FILTER", "price", String.valueOf(minPrice), String.valueOf(maxPrice));
        System.out.println(response);

    }

    public static void searchWithPagination(Jedis jedis, String keyword, int offset, int limit) {

        Object response = jedis.sendCommand(Protocol.Command.valueOf("FT.SEARCH"),
                "myIndex", keyword, "LIMIT", String.valueOf(offset), String.valueOf(limit));
        System.out.println(response);

    }

    public static void listIndexes(Jedis jedis) {

        Object response = jedis.sendCommand(Protocol.Command.valueOf("FT._LIST"));
        System.out.println(response);

    }
}
