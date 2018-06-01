package com.linkedin.mitm.services;

import io.netty.util.concurrent.Promise;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;


/**
 * This class runs long running tasks in a separate thread pool. When a task completes, its associated callback is invoked.
 */
public class LongRunningTaskService {
  private static final Logger LOG = Logger.getLogger(LongRunningTaskService.class);

  public interface LongRunningTaskCallback<R> {
    R fullfillPromise() throws Exception;

    Promise<R> getPromise();
  }

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  private static final List<LongRunningTaskCallback> _callbackList = new ArrayList<>();

  private static final ReentrantLock LOCK = new ReentrantLock();
  private static final Condition NOT_EMPTY = LOCK.newCondition();

  static {
    EXECUTOR_SERVICE.submit(() -> {
          while (true) {
            LOCK.lock();
            try {
              while (_callbackList.isEmpty()) {
                NOT_EMPTY.await();
              }
              for (Iterator<LongRunningTaskCallback> it = _callbackList.iterator(); it.hasNext(); ) {
                LongRunningTaskCallback callback = it.next();
                Promise promise = callback.getPromise();
                try  {
                  Object result = callback.fullfillPromise();
                  promise.setSuccess(result);
                } catch (Exception e) {
                  LOG.error("Failed to complete callback", e);
                  promise.setFailure(e);
                } finally {
                  _callbackList.remove(callback);
                }
              }
            } catch (InterruptedException e) {
              LOG.debug("shutting down thread pool in LongRunningTaskService");
              return;
            } finally {
              LOCK.unlock();
            }
          }
        }
    );
  }

  public static void submitTaskCallback(LongRunningTaskCallback callback) {
    LOCK.lock();
    try {
      _callbackList.add(callback);
      NOT_EMPTY.signal();
    } finally {
      LOCK.unlock();
    }
  }

  public static void shutdownAndAwaitTermination() {
    EXECUTOR_SERVICE.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!EXECUTOR_SERVICE.awaitTermination(60, TimeUnit.SECONDS)) {
        EXECUTOR_SERVICE.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!EXECUTOR_SERVICE.awaitTermination(60, TimeUnit.SECONDS))
          LOG.error("LongRunningTaskService thread pool did not terminate");
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      EXECUTOR_SERVICE.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
