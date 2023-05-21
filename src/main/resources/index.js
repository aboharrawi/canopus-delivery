(function () {
    document.addEventListener("DOMContentLoaded", function () {
        let preview = document.getElementById("preview");
        let startButton = document.getElementById("startButton");
        let stopButton = document.getElementById("stopButton");
        const conn = new WebSocket('ws://localhost:8080/live-stream');

        conn.onopen = function () {
            console.log("Connected to the signaling server");
        };

        conn.onmessage = function (msg) {
            console.log("Got message", msg.data);
        };

        conn.onerror = function (msg) {
            console.log("Got error", msg.data);
        };

        startButton.addEventListener(
            "click",
            () => {
                navigator.mediaDevices
                    .getDisplayMedia({
                        audio: true,
                        video: {
                            width: 1280,
                            height: 720,
                            frameRate: {
                                ideal: 10,
                                max: 15
                            }
                        },
                    })
                    .then((stream) => {
                        preview.srcObject = stream;
                        return new Promise((resolve) => (preview.onplaying = resolve));
                    })
                    .then(() => startRecording(preview.captureStream()))
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
                audioBitsPerSecond: 128000,
                videoBitsPerSecond: 2500000,
                mimeType: "video/webm;codecs=vp8",
            };

            const recorder = new MediaRecorder(stream, options);

            console.log(recorder)

            recorder.ondataavailable = (event) => {
                console.log("Capturing and sending: " + event.data.size )
                conn.send(event.data);
            };

            recorder.start(10000);
            let stopped = new Promise((resolve, reject) => {
                recorder.onstop = resolve;
                recorder.onerror = (event) => reject(event.name);
            });

            return Promise.resolve(stopped);
        }

        function log(msg) {
            console.log(msg);
        }

        function stop(stream) {
            stream.getTracks().forEach((track) => track.stop());
        }
    })
}());
