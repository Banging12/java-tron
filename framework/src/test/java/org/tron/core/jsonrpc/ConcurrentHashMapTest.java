package org.tron.core.jsonrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.tron.common.logsfilter.capsule.BlockFilterCapsule;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.core.services.jsonrpc.TronJsonRpcImpl;
import org.tron.core.services.jsonrpc.filters.BlockFilterAndResult;

@Slf4j
public class ConcurrentHashMapTest {

  private static int randomInt(int minInt, int maxInt) {
    return (int) Math.round(Math.random() * (maxInt - minInt) + minInt);
  }

  @Test
  public void testHandleBlockHash() {
    int times = 100;
    int eachCount = 200;

    Map<String, BlockFilterAndResult> conMap = TronJsonRpcImpl.getBlockFilter2ResultFull();
    Map<String, List<String>> resultMap1 = new ConcurrentHashMap<>(); // used to check result
    Map<String, List<String>> resultMap2 = new ConcurrentHashMap<>(); // used to check result
    Map<String, List<String>> resultMap3 = new ConcurrentHashMap<>(); // used to check result

    for (int i = 0; i < 5; i++) {
      BlockFilterAndResult filterAndResult = new BlockFilterAndResult();
      String filterID = String.valueOf(i);

      conMap.put(filterID, filterAndResult);
      resultMap1.put(filterID, new ArrayList<>());
      resultMap2.put(filterID, new ArrayList<>());
      resultMap3.put(filterID, new ArrayList<>());
    }

    try {
      Thread.sleep(randomInt(100, 200));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Thread putThread = new Thread(new Runnable() {
      public void run() {

        for (int i = 1; i <= times; i++) {
          System.out.println(
              "put time " + i + ", from " + (1 + (i - 1) * eachCount) + " to " + i * eachCount);

          for (int j = 1 + (i - 1) * eachCount; j <= i * eachCount; j++) {
            BlockFilterCapsule blockFilterCapsule =
                new BlockFilterCapsule(String.valueOf(j), false);
            TronJsonRpcImpl.handleBLockFilter(blockFilterCapsule);
          }
          try {
            Thread.sleep(randomInt(100, 200));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

    });

    Thread getThread1 = new Thread(new Runnable() {
      public void run() {
        for (int t = 1; t <= times * 2; t++) {

          try {
            Thread.sleep(randomInt(100, 200));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          System.out.println("get time " + t);

          for (int k = 0; k < 5; k++) {
            // System.out.println("get key " + k);

            try {
              Object[] blockHashList = TronJsonRpcImpl.getFilterResult(String.valueOf(k), conMap,
                  TronJsonRpcImpl.getEventFilter2ResultFull());

              for (Object str : blockHashList) {
                resultMap1.get(String.valueOf(k)).add(str.toString());
              }

            } catch (ItemNotFoundException e) {
              // Assert.fail(e.getMessage());
            }
          }
        }
      }
    });

    Thread getThread2 = new Thread(new Runnable() {
      public void run() {
        for (int t = 1; t <= times * 2; t++) {

          try {
            Thread.sleep(randomInt(100, 200));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          System.out.println("Thread2 get time " + t);

          for (int k = 0; k < 5; k++) {
            System.out.println("get key " + k);

            try {
              Object[] blockHashList = TronJsonRpcImpl.getFilterResult(String.valueOf(k), conMap,
                  TronJsonRpcImpl.getEventFilter2ResultFull());

              // if (blockHashList.length == 0) {
              //   continue;
              // }

              for (Object str : blockHashList) {
                try {
                  resultMap2.get(String.valueOf(k)).add(str.toString());
                } catch (Exception e) {

                }
              }

            } catch (ItemNotFoundException e) {
              // Assert.fail(e.getMessage());
            }
          }
        }
      }
    });

    Thread getThread3 = new Thread(new Runnable() {
      public void run() {
        for (int t = 1; t <= times * 2; t++) {

          try {
            Thread.sleep(randomInt(100, 200));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          System.out.println("Thread3 get time " + t);

          for (int k = 0; k < 5; k++) {
            // System.out.println("get key " + k);

            try {
              Object[] blockHashList = TronJsonRpcImpl.getFilterResult(String.valueOf(k), conMap,
                  TronJsonRpcImpl.getEventFilter2ResultFull());

              // if (blockHashList.length == 0) {
              //   continue;
              // }

              for (Object str : blockHashList) {
                try {
                  resultMap3.get(String.valueOf(k)).add(str.toString());
                } catch (Exception e) {
                  logger.error("--------");
                  e.printStackTrace();
                }
              }

            } catch (ItemNotFoundException e) {
              // Assert.fail(e.getMessage());
            }
          }
        }
      }
    });

    putThread.start();
    getThread1.start();
    getThread2.start();
    getThread3.start();

    try {
      putThread.join();
      getThread1.join();
      getThread2.join();
      getThread3.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("-----------------------------------------------------------------------");

    for (int i = 0; i < 5; i++) {
      List<String> pResult = resultMap1.get(String.valueOf(i));
      pResult.addAll(resultMap2.get(String.valueOf(i)));
      pResult.addAll(resultMap3.get(String.valueOf(i)));
      // System.out.println("check  get key " + i + " value is " + pResult);

      for (int j = 1; j <= times * eachCount; j++) {
        if (!pResult.contains(ByteArray.toJsonHex(String.valueOf(j)))) {
          System.out.println(String.format("key %s not contains %s", i, j));
        }
        // Assert.assertTrue(pResult.contains(String.valueOf(j)));
      }
    }
  }

}