package org.validate;

import org.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

public class TransactionValidate {

    public static void main(String[] args) {

        Jedis jedis = RedisManager.getJedis();

//        multipleSet(jedis);

//        stimulateFailureTransaction(jedis);

//        transactionWithWatch(jedis);

        simultaneouslyTransaction(jedis);

    }

    private static void simultaneouslyTransaction(Jedis jedis) {

        jedis.setbit("chairNumber", 1, false);

        jedis.watch("chairNumber");

        boolean isBooked = Boolean.parseBoolean(jedis.get("chairNumber"));

        if (!isBooked) {
            Transaction transaction = jedis.multi();

            transaction.setbit("chairNumber", 1, true);

            System.out.println("some other client modified here");

            List<Object> results = transaction.exec();

            if (results == null) {
                System.out.println("Transaction fail");
            } else {
                System.out.println("Transaction completed");
            }
        } else {
            System.out.println("...");
        }

    }

    private static void transactionWithWatch(Jedis jedis) {
        jedis.set("account_balance", "500");

        jedis.watch("account_balance");

        double currentBalance = Double.parseDouble(jedis.get("account_balance"));

        double amountToDeduct = 100;

        if (currentBalance >= amountToDeduct) {
            Transaction transaction = jedis.multi();

            transaction.decrBy("account_balance", (long) amountToDeduct);

            List<Object> results = transaction.exec();

            if (results == null) {
                System.out.println("Transaction aborted");
            } else {
                System.out.println("Transaction completed");
            }
        } else {
            System.out.println("Not enough balance");
        }
    }

    private static void stimulateFailureTransaction(Jedis jedis) {

        jedis.set("stringKey", "stringValue");

        Transaction transaction = jedis.multi();
        transaction.set("key1", "value1");
        transaction.set("key2", "value2");

        //fail, other command still execute
        transaction.hset("stringKey", "hashValue", "hashValue");

        transaction.set("key3", "value3");
        transaction.set("key4", "value4");

        transaction.exec();

    }

    private static void multipleSet(Jedis jedis) {

        Transaction transaction = jedis.multi();
        transaction.set("key1", "1");
        transaction.set("key2", "2");
        transaction.set("key3", "3");
        transaction.set("key4", "4");
        transaction.exec();

    }
}
