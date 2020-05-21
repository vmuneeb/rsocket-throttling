package com.expedia.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class CancelService {
  static final String HOST = "localhost";
  static final int PORT = 3000;

  private static final Logger log = LoggerFactory.getLogger(CancelService.class);

  WebClient webClient;

  public CancelService() {
    webClient = WebClient.create("http://"+HOST+":"+PORT+"");
  }

  public Mono<String> cancel(int id) {
    log.info(String.format("Calling getUser(%d)", id));

    return webClient.get()
        .uri("/{id}", id)
        .retrieve()
        .bodyToMono(String.class);
  }
}
