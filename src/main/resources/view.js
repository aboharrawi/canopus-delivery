(function () {
    document.addEventListener("DOMContentLoaded", function () {
        handleWebStream();
        console.log(getSessionId())
    });

    function handleWebStream() {
        var video = document.querySelector('#live-stream');
        var mimeCodec = 'video/webm;codecs=vp9,opus';

        var mediaSource = null;
        if ('MediaSource' in window && MediaSource.isTypeSupported(mimeCodec)) {
            mediaSource = new MediaSource();
            video.src = URL.createObjectURL(mediaSource);
            mediaSource.addEventListener('sourceopen', sourceOpen);
        } else {
            console.error('Unsupported MIME type or codec: ', mimeCodec);
        }

        let sourceBuffer = null;
        let position = 0;

        function sourceOpen(_) {
            sourceBuffer = mediaSource.addSourceBuffer("video/webm;codecs=vp9,opus");
            requestPlaylistManifest();
        }

        function handleManifest(manifest) {
            let intervalId = setInterval(function () {
                if (manifest.segments[position]) {
                    console.log(manifest.segments.length + " " + position)
                    if (position === manifest.segments.length - 1) {
                        requestPlaylistManifest();
                    }
                    requestSegment(manifest, position++);
                } else {
                    clearInterval(intervalId);
                }
            }, manifest.segments[0].duration * 1000);
        }

        function requestPlaylistManifest() {
            let parser = new m3u8Parser.Parser();
            fetch("http://localhost:8084/playlist/" + getSessionId(), {method: "GET", cache: "no-cache"})
                .then(value => value.text())
                .then(value => {
                    parser.push(value);
                    parser.end();
                    return parser.manifest;
                }).then(manifest => handleManifest(manifest));
        }

        function requestSegment(manifest, position) {
            fetch(manifest.segments[position].uri, {method: "GET", cache: "no-cache"})
                .then(value => value.arrayBuffer())
                .then(value => appendSegment(value));
        }

        function appendSegment(chunk) {
            sourceBuffer.appendBuffer(chunk);
        }
    }

    function getSessionId() {
        return new URLSearchParams(window.location.search).get('sessionId');
    }
}());