package com.canopus.delivery.stream.config;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@Component
public class MediaStreamBinaryWebSocketHandler implements WebSocketHandler {

    private final Producer producer;

    public MediaStreamBinaryWebSocketHandler(Producer producer) {
        this.producer = producer;
    }

    @Override
    @Nonnull
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .filter(webSocketMessage -> webSocketMessage.getType().equals(WebSocketMessage.Type.BINARY))
                .map(WebSocketMessage::getPayload)
                .flatMap(dataBuffer -> producer.sendMessage(dataBuffer, System.currentTimeMillis(), session.getId()))
                .log()
                .then();
    }
}