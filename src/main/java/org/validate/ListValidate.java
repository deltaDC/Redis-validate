package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;

public class ListValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

        addTask(jedis, "taskList", "task1");
        addTask(jedis, "taskList", "task2");
        addTask(jedis, "taskList", "task3");

        getFirstTaskAndLastTask(jedis, "taskList");
        System.out.println("-----------------");

        getFirstTaskAndRemove(jedis, "taskList");
        System.out.println("-----------------");

        getListByQueue(jedis, "taskList");
        System.out.println("-----------------");


        addTask(jedis, "taskList", "task1");
        addTask(jedis, "taskList", "task2");
        addTask(jedis, "taskList", "task3");
        addTask(jedis, "taskList", "task3");
        addTask(jedis, "taskList", "task3");
        addTask(jedis, "taskList", "task3");

        getListByStack(jedis, "taskList");

    }

    private static void getListByStack(Jedis jedis, String taskList) {
        long len = jedis.llen(taskList);

        while(len > 0) {
            String task = jedis.rpop(taskList);
            System.out.println(task);
            len--;
        }
    }

    private static void getListByQueue(Jedis jedis, String taskList) {
        long len = jedis.llen(taskList);

        while(len > 0) {
            String task = jedis.lpop(taskList);
            System.out.println(task);
            len--;
        }
    }

    private static void getFirstTaskAndRemove(Jedis jedis, String taskList) {
        String firstTask = jedis.lpop(taskList);

        System.out.println(firstTask);
    }

    private static void getFirstTaskAndLastTask(Jedis jedis, String taskList) {
        String firstTask = jedis.lindex(taskList, 0);
        String lastTask = jedis.lindex(taskList, -1);

        System.out.println(firstTask);

        System.out.println(lastTask);
    }

    private static void addTask(Jedis jedis, String taskList, String task) {
        jedis.rpush(taskList, task);
    }
}
