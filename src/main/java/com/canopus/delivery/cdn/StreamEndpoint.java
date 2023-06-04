package com.canopus.delivery.cdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@Controller
public class StreamEndpoint {

    private final Logger logger = LogManager.getLogger(StreamEndpoint.class);

    @GetMapping("stream/{sessionId}/{videoName}")
    @CrossOrigin(methods = {RequestMethod.HEAD, RequestMethod.GET})
    public ResponseEntity<Resource> getVideo(@PathVariable("sessionId") String sessionId, @PathVariable("videoName") String videoName) {
        UrlResource video;
        try {
            logger.info("Resource fetch request with video name : " + videoName);
            video = new UrlResource("file:/var/lib/content/" + sessionId + "/" + videoName);
            if (!video.exists()) {
                logger.error("Video with name: file:/var/lib/content/" + sessionId + "/" + videoName + " was not found");
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (IOException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.noCache())
                .body(video);
    }

    @GetMapping("stream-fragment/{sessionId}/{uuid}")
    @CrossOrigin(methods = {RequestMethod.HEAD, RequestMethod.GET})
    public ResponseEntity<Resource> getVideoPart(@PathVariable("sessionId") String sessionId, @PathVariable("uuid") String uuid) {
        UrlResource video;
        try {
            logger.info("Resource fetch request with video id : " + uuid);
            video = new UrlResource("file:/var/lib/content/" + sessionId + "/" + uuid);
            if (!video.exists()) {
                logger.error("Video with id: file:/var/lib/content/" + sessionId + "/" + uuid + " was not found");
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (IOException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.noCache())
                .body(video);
    }
}
