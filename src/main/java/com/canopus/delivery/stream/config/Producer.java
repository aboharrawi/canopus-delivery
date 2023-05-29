package com.canopus.delivery.stream.config;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;


@Service
public class Producer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public Producer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendMessage(DataBuffer message, long timeStamp, String key) {
        return DataBufferUtils.readByteChannel(() -> Channels.newChannel(message.asInputStream()), message.factory(), 1024 * 1024)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>("canopus-live-stream", null, timeStamp, key, bytes);
                    return Mono.fromFuture(this.kafkaTemplate.send(producerRecord));
                })
                .then();
    }
}