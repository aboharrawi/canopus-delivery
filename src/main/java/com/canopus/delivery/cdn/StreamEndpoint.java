package com.canopus.delivery.cdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class StreamEndpoint {

    private final Logger logger = LogManager.getLogger(StreamEndpoint.class);

    @GetMapping("stream/{videoName}")
    public ResponseEntity<Resource> getVideo(@PathVariable("videoName") String videoName) {
        UrlResource video;
        try {
            logger.info("Resource fetch request with video name : " + videoName);
            video = new UrlResource("file:/var/lib/content/" + videoName);
            if (!video.exists()) {
                logger.error("Video with name: /var/lib/content/" + videoName + " was not found");
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (IOException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .build();
        }

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.noCache())
                .body(video);
    }
}
