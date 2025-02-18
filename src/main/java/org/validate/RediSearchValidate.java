package org.validate;

import io.redisearch.Schema;
import io.redisearch.client.Client;
import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

public class RediSearchValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        Client client = new Client("myIndex", jedis);

        Schema schema = new Schema().addTextField("title", 5.0).addTextField("content", 1.0);

        client.createIndex(schema, null);

        jedis.close();
    }
}
