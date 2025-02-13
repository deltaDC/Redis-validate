package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

import java.util.List;

public class GeoValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        saveStoreLocation(jedis);

        getAllStoresBy10KMRadius(jedis);

        getDistianceBetweenStores(jedis);
    }

    private static void getDistianceBetweenStores(Jedis jedis) {
        String store1 = "store1";
        String store2 = "store2";

        double distance = jedis.geodist("store:location", store1, store2, GeoUnit.KM);
        System.out.println(distance);
    }

    private static void getAllStoresBy10KMRadius(Jedis jedis) {
        double longitude = 77.5946;
        double latitude = 12.9716;

        List<GeoRadiusResponse> nearbyStores = jedis.georadius(
                "stores:location", longitude, latitude, 10,
                GeoUnit.KM,
                GeoRadiusParam.geoRadiusParam().withDist().sortAscending()
        );

        for (GeoRadiusResponse store : nearbyStores) {
            System.out.println(store.getMemberByString() + " - " + store.getDistance() + " km");
        }
    }

    private static void saveStoreLocation(Jedis jedis) {
        String storeKey = "store:location";
        String storeName = "store1";
        double longitude = 77.5946;
        double latitude = 12.9716;

        jedis.geoadd(storeKey, longitude, latitude, storeName);
        jedis.geoadd(storeKey, 80.5946, latitude, "store2");

        System.out.println(jedis.geopos(storeKey, storeName));
    }
}
