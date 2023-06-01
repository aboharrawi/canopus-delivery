package com.canopus.delivery.service;


import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface WebSocketMessageHandle<T> {
    Mono<Void> handleMessage(@Nonnull Message<T> message);
}
