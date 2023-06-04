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

            recorder.start(2000);
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
