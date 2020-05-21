import io.rsocket.*;
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


public class BackPressureServer {

  private static final Logger slf4jLogger = LoggerFactory.getLogger(BackPressureServer.class);

  static final String HOST = "localhost";
  static final int PORT = 7000;


  public static void main(String[] args) throws InterruptedException {

    RSocketFactory.receive()
        .acceptor(new HelloWorldSocketAcceptor())
        .transport(TcpServerTransport.create(HOST, PORT))
        .start()
        .subscribe();
    slf4jLogger.info("Server running");

    Thread.currentThread().join();
  }


  static class HelloWorldSocketAcceptor implements SocketAcceptor {
    private static final Logger slf4jLogger = LoggerFactory.getLogger(HelloWorldSocketAcceptor.class);
    Queue<Integer> requestQueue;

    public HelloWorldSocketAcceptor() {
      requestQueue = new LinkedList<Integer>();
      for (int i = 1; i < 1000; i++) {
        requestQueue.add(i);
      }
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
      slf4jLogger.info("Received connection with setup payload: [{}] and meta-data: [{}]", setup.getDataUtf8(), setup.getMetadataUtf8());
      return Mono.just(
          new AbstractRSocket() {
            @Override
            public Mono<Void> fireAndForget(Payload payload) {
              slf4jLogger.info(
                  "Received 'fire-and-forget' request with payload: [{}]", payload.getDataUtf8());
              return Mono.empty();
            }

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
              slf4jLogger.info(
                  "Received 'request response' request with payload: [{}] ", payload.getDataUtf8());
              return Mono.just(DefaultPayload.create("Hello " + payload.getDataUtf8()));
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
              slf4jLogger.info(
                  "Received 'request stream' request with payload: [{}] ", payload.getDataUtf8());
              return Flux.fromIterable(requestQueue)
                  .map(data -> DefaultPayload.create(data.toString()));
            }

            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
              return Flux.from(payloads)
                  .doOnNext(
                      payload -> {
                        slf4jLogger.info("Received payload: [{}]", payload.getDataUtf8());
                      })
                  .map(
                      payload ->
                          DefaultPayload.create(
                              "Hello " + payload.getDataUtf8() + " @ " + Instant.now()))
                  .subscribeOn(Schedulers.parallel());
            }

            @Override
            public Mono<Void> metadataPush(Payload payload) {
              slf4jLogger.info(
                  "Received 'metadata push' request with metadata: [{}]",
                  payload.getMetadataUtf8());
              return Mono.empty();
            }
          });
    }
  }

}
