package com.canopus.delivery.stream.config;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.function.Consumer;

public class StreamUtils {

    public static Mono<DataBuffer> write(Publisher<DataBuffer> source, Path destination, @Nullable Consumer<DataBuffer> consumer) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(destination, "Destination must not be null");

        Set<OpenOption> optionSet = Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        return Mono.create(sink -> {
            try {
                Files.createDirectories(destination.getParent());
                AsynchronousFileChannel channel = AsynchronousFileChannel.open(destination, optionSet, null);
                sink.onDispose(() -> closeChannel(channel));
                DataBufferUtils.write(source, channel)
                        .subscribe(consumer, sink::error, sink::success, Context.of(sink.contextView()));
            } catch (IOException e) {
                sink.error(e);
            }
        });
    }

    static void closeChannel(@Nullable Channel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }
}
