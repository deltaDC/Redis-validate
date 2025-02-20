package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        String streamKey = "stream";

//        addEventToStream(jedis, streamKey);

//        readAllStreamEvent(jedis, streamKey);

//        readNewEvent(jedis, streamKey);

        deleteAnEvent(jedis, streamKey);
    }

    private static void deleteAnEvent(Jedis jedis, String streamKey) {

        StreamEntryID streamEntryID = null;

        List<StreamEntry> events = jedis.xrange(streamKey, (StreamEntryID) null, null, 1);
        if (!events.isEmpty()) {
            System.out.println(events.get(0).getID());
            streamEntryID = events.get(0).getID();
        }


        long deleted = jedis.xdel(streamKey, streamEntryID);

        if (deleted > 0) {
            System.out.println("ok");
        } else {
            System.out.println("fail");
        }
    }

    private static void readNewEvent(Jedis jedis, String streamKey) {

        StreamEntryID lastId = StreamEntryID.LAST_ENTRY;

        while (true) {
            List<Map.Entry<String, List<StreamEntry>>> res =
                    jedis.xread(
                            XReadParams.xReadParams().block(5000),
                            Collections.singletonMap(streamKey, lastId)
                    );

            if (res != null) {
                for (Map.Entry<String, List<StreamEntry>> entry : res) {
                    for (StreamEntry streamEntry : entry.getValue()) {
                        System.out.println("Sự kiện mới: " + streamEntry.getID() + " -> " + streamEntry.getFields());

                        lastId = streamEntry.getID();
                    }
                }
            }
        }
    }

    private static void readAllStreamEvent(Jedis jedis, String streamKey) {

        List<StreamEntry> list = jedis.xrange(streamKey, "-", "+");

        System.out.println(list);
    }

    private static void addEventToStream(Jedis jedis, String streamKey) {

        Map<String, String> event = new HashMap<>();
        event.put("title", "test 2");
        event.put("content", "test content 2");

        StreamEntryID eventId = jedis.xadd(streamKey, StreamEntryID.NEW_ENTRY, event);

        //note: when XREAD the eventId of a stream, it is excluded the eventId unix tim
        // eventId = 1740018577558-0
        // so to XREAD we must take eventId - 1 = 1740018577557-0 to read it
        System.out.println(eventId);
    }
}
