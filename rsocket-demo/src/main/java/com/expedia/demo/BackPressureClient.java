package com.expedia.demo;

import io.rsocket.*;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.util.LinkedList;
import java.util.Queue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;


public class BackPressureClient {

  private static final Logger slf4jLogger = LoggerFactory.getLogger(BackPressureClient.class);

  static final String HOST = "localhost";
  static final int PORT = 7000;


  public static void main(String[] args) throws InterruptedException {

    RSocket client =  RSocketFactory
        .connect()
        .transport(TcpClientTransport.create(HOST, PORT))
        .start()
        .block();

    slf4jLogger.info("Client started");

    client.requestStream(DefaultPayload.create("Start"))
        .subscribe(new BackPressureSubscriber());


    Thread.currentThread().join();
  }
}
