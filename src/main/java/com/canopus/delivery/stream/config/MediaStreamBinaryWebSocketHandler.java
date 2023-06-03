package com.canopus.delivery.stream.config;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@Component
public class MediaStreamBinaryWebSocketHandler implements WebSocketHandler {

    private final StreamingMessageHandler messageHandler;

    public MediaStreamBinaryWebSocketHandler(StreamingMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    @Nonnull
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(Mono.just(session.textMessage(session.getId())))
                .and(session.receive()
                        .filter(webSocketMessage -> webSocketMessage.getType().equals(WebSocketMessage.Type.BINARY))
                        .map(WebSocketMessage::getPayload)
                        .flatMap(dataBuffer -> messageHandler.handleMessage(constructMessage(dataBuffer, session)))
                        .next());
    }

    private Message<DataBuffer> constructMessage(DataBuffer dataBuffer, WebSocketSession session) {
        return MessageBuilder.withPayload(dataBuffer)
                .setHeader("sessionId", session.getId())
                .setHeader(MessageHeaders.CONTENT_TYPE, session.getHandshakeInfo().getHeaders().getContentType())
                .build();
    }
}