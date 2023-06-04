package com.canopus.delivery.stream.config;

import com.canopus.delivery.service.WebSocketMessageHandle;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;


@Service
public class StreamingMessageHandler implements WebSocketMessageHandle<DataBuffer> {

    private final KafkaTemplate<String, Message<Map<String, String>>> kafkaTemplate;

    public StreamingMessageHandler(KafkaTemplate<String, Message<Map<String, String>>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> handleMessage(@Nonnull Message<DataBuffer> message) {
        String sessionId = String.valueOf(message.getHeaders().get("sessionId", String.class));
        String uuid = String.valueOf(message.getHeaders().getId());
        long timeStamp = Optional.ofNullable(message.getHeaders().getTimestamp()).orElse(System.currentTimeMillis());
        Path location = Path.of("/var/lib/content", sessionId, uuid);
        String indexFile = "/var/lib/content/" + sessionId + "/playlist.m3u8";
        return StreamUtils.write(Flux.just(message.getPayload()), location, dataBuffer ->
                        sendMessage(constructMessage(location.toString(), indexFile, uuid, null, timeStamp, sessionId)).subscribe())
                .then();
    }

    public Mono<SendResult<String, Message<Map<String, String>>>> sendMessage(Message<Map<String, String>> message) {
        return Mono.fromFuture(this.kafkaTemplate.send(message));
    }

    private Message<Map<String, String>> constructMessage(String location, String indexFile, String key, Integer partition, Long timestamp, String sessionId) {
        return MessageBuilder.withPayload(Map.of("location", location))
                .setHeader("index-file", indexFile)
                .setHeader("indexed-item-streaming-url", "http://localhost:8084/stream-fragment/" + sessionId + "/" + key)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.TIMESTAMP, timestamp)
                .setHeader(KafkaHeaders.PARTITION, partition)
                .setHeader(KafkaHeaders.TOPIC, "canopus-live-stream")
                .setHeader("sessionId", sessionId)
                .build();
    }
}