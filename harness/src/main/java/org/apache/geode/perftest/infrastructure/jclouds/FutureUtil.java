package org.apache.geode.perftest.infrastructure.jclouds;

import java.util.concurrent.CompletableFuture;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class FutureUtil {

  public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> googleFuture) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Futures.addCallback(googleFuture, new FutureCallback<T>() {
      @Override
      public void onSuccess(T result) {
        completableFuture.complete(result);
      }

      @Override
      public void onFailure(Throwable t) {
        completableFuture.completeExceptionally(t);
      }
    });

    return completableFuture;
  }
}
