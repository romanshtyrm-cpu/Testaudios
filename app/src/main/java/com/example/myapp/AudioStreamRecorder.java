
package com.example.myapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class AudioStreamRecorder {
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder;
    private boolean running = false;

    private final PipedInputStream input;
    private final PipedOutputStream output;

    public AudioStreamRecorder() throws IOException {
        output = new PipedOutputStream();
        input = new PipedInputStream(output, 4096);
    }

    public void start() {
        int bufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufSize);
        recorder.startRecording();
        running = true;

        new Thread(() -> {
            byte[] buf = new byte[1024];
            while (running) {
                int r = recorder.read(buf, 0, buf.length);
                if (r > 0) {
                    try {
                        output.write(buf, 0, r);
                        output.flush();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }
        try { output.close(); } catch (Exception ignored) {}
    }

    public PipedInputStream getInputStream() {
        return input;
    }
}
