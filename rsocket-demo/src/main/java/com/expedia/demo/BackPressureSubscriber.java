package com.expedia.demo;

import io.rsocket.Payload;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BackPressureSubscriber implements Subscriber<Payload> {
  private static final Logger slf4jLogger = LoggerFactory.getLogger(BackPressureSubscriber.class);
  private static final Integer NUMBER_OF_REQUESTED_ITEMS = 5;
  private Subscription subscription;
  int receivedItems;
  CancelService cancelService;
  BlockingDeque<Integer> resultQueue ;

  public BackPressureSubscriber() {
    cancelService = new CancelService();
    resultQueue = new LinkedBlockingDeque<>();
  }
  public void onSubscribe(Subscription subscription) {
    this.subscription = subscription;
    subscription.request(NUMBER_OF_REQUESTED_ITEMS);
  }

  public void onNext(Payload payload) {
    receivedItems++;
    slf4jLogger.info("Data received : {}",payload.getDataUtf8());

    cancelService
        .cancel(Integer.parseInt(payload.getDataUtf8()))
        .subscribe(
            data -> {
              slf4jLogger.info("Data processed : {}",data);
              try {
                resultQueue.put(Integer.parseInt(data));
                if(resultQueue.size() % NUMBER_OF_REQUESTED_ITEMS == 0) {
                  subscription.request(NUMBER_OF_REQUESTED_ITEMS);
                }
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
        );
  }

  public void onError(Throwable throwable) {
    System.out.println("Stream subscription error");
  }

  public void onComplete() {
    System.out.println("Completing subscription");
  }
}
