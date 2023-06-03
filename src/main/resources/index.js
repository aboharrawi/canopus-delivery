(function () {
    document.addEventListener("DOMContentLoaded", function () {
        let preview = document.getElementById("preview");
        let startButton = document.getElementById("startButton");
        let stopButton = document.getElementById("stopButton");
        const conn = new WebSocket('ws://localhost:8084/live-stream');
        let sessionId;

        conn.onopen = function () {
            console.log("Connected to the signaling server");
        };

        conn.onmessage = function (msg) {
            console.log("Got message", msg.data);
            sessionId = msg.data;
        };

        conn.onerror = function (msg) {
            console.log("Got error", msg.data);
        };

        startButton.addEventListener(
            "click",
            () => {
                navigator.mediaDevices
                    .getDisplayMedia({
                        audio: {
                            sampleSize: 16,
                            channelCount: 2,
                            echoCancellation: true,
                            suppressLocalAudioPlayback: true,
                            noiseSuppression: true
                        },
                        video: {
                            aspectRatio: 1.7777777778,
                            width: 1920,
                            height: 1080,
                            frameRate: 60,
                            noiseSuppression: true
                        },
                    })
                    .then((stream) => startRecording(stream))
                    .catch((error) => {
                        log(error);
                    });
            },
            false
        );

        stopButton.addEventListener(
            "click",
            () => {
                stop(preview.srcObject);
            },
            false
        );

        function startRecording(stream) {

            const options = {
                mimeType: "video/webm;codecs=vp9,opus",
                videoBitsPerSecond: 2500000
            };

            const recorder = new MediaRecorder(stream, options);

            console.log(recorder)

            recorder.ondataavailable = (event) => {
                console.log("Capturing and sending: " + event.data.size)
                conn.send(event.data);
            };

            recorder.start(1000); //5sec
            let stopped = new Promise((resolve, reject) => {
                recorder.onstop = resolve;
                recorder.onerror = (event) => reject(event.name);
            });

            setTimeout(function () {
                handleWebStream();
            }, 2000);

            return Promise.resolve(stopped);
        }

        function log(msg) {
            console.log(msg);
        }

        function stop(stream) {
            stream.getTracks().forEach((track) => track.stop());
        }

        function handleWebStream() {
            var video = document.querySelector('#live-stream');
            var assetURL = 'http://localhost:8084/stream/' + getSessionId() + '/merge.webm';
            var mimeCodec = 'video/webm;codecs=vp9,opus';
            var bytesFetched = 0;
            var currentSegment = 0;
            var fileSize = 0;

            var mediaSource = null;
            if ('MediaSource' in window && MediaSource.isTypeSupported(mimeCodec)) {
                mediaSource = new MediaSource();
                video.src = URL.createObjectURL(mediaSource);
                mediaSource.addEventListener('sourceopen', sourceOpen);
            } else {
                console.error('Unsupported MIME type or codec: ', mimeCodec);
            }

            var sourceBuffer = null;

            function sourceOpen(_) {
                sourceBuffer = mediaSource.addSourceBuffer(mimeCodec);
                getFileLength(assetURL, function (fileLength) {
                    fileSize = fileLength;
                    fetchRange(assetURL, fileLength - (1024 * 512), fileLength - 1, appendSegment);
                    setInterval(checkBuffer, 2000)
                    video.addEventListener('canplay', function () {
                        video.play();
                    });
                });
            }

            function getFileLength(url, cb) {
                var xhr = new XMLHttpRequest;
                xhr.open('head', url);
                xhr.onload = function () {
                    cb(xhr.getResponseHeader('content-length'));
                };
                xhr.send();
            }

            function fetchRange(url, start, end, cb) {
                var xhr = new XMLHttpRequest;
                xhr.open('get', url);
                xhr.responseType = 'arraybuffer';
                xhr.setRequestHeader('Range', 'bytes=' + start + '-' + end);
                xhr.onload = function () {
                    bytesFetched += parseInt(xhr.getResponseHeader('content-length'));
                    currentSegment++;
                    cb(xhr.response);
                };
                xhr.send();
            }

            function appendSegment(chunk) {
                sourceBuffer.appendBuffer(chunk);
            }

            function checkBuffer(_) {
                var currentSegment = getCurrentSegment();
                if (shouldFetchNextSegment(currentSegment)) {
                    fetchNewSegment();
                }
            }

            function fetchNewSegment() {
                getFileLength(assetURL, function (contentLength) {
                    fileSize = contentLength;
                    fetchRange(assetURL, bytesFetched, fileSize - 1, appendSegment)
                })
            }

            function getCurrentSegment() {
                return currentSegment;
            }

            function shouldFetchNextSegment(currentSegment) {
                return true;
            }
        }

        function getSessionId() {
            return sessionId;
        }
    })
}());
