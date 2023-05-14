package com.canopus.delivery.cdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class StreamEndpoint {

    private final Logger logger = LogManager.getLogger(StreamEndpoint.class);

    @GetMapping("stream/{videoPath}")
    public ResponseEntity<Resource> getVideo(@PathVariable("videoPath") String videoPath) {
        UrlResource video;
        try {
            logger.info("Resource fetch request with video path : " + videoPath);
            video = new UrlResource("file:" + videoPath);
            if (!video.exists()) {
                logger.error("Video with path: " + videoPath + " was not found");
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (IOException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(video);
    }
}
