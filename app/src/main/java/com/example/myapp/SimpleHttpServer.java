package com.example.myapp;

import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.ByteArrayInputStream;
import java.io.SequenceInputStream;
import java.util.Map;

public class SimpleHttpServer extends NanoHTTPD {

    private static final String TAG = "SimpleHttpServer";

    public SimpleHttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = session.getUri();
            Log.d(TAG, "Request: " + uri);

            // Главная страница: /index
            if (uri.equals("/") || uri.equals("/index")) {
                String html = "<html><body><h1>Audio Stream Server</h1>" +
                              "<p>Используйте <a href=\"/stream\">/stream</a> для получения WAV аудио.</p>" +
                              "</body></html>";

                return Response.newFixedLengthResponse(Status.OK, "text/html", html);
            }

            // Реальный поток аудио WAV
            if (uri.equals("/stream")) {

                byte[] wavHeader = createWavHeader(44100, 16, 1);
                ByteArrayInputStream headerStream = new ByteArrayInputStream(wavHeader);

                AudioStreamInputStream audioStream = new AudioStreamInputStream();

                SequenceInputStream fullStream = new SequenceInputStream(headerStream, audioStream);

                return Response.newChunkedResponse(Status.OK, "audio/wav", fullStream);
            }

            return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not found");

        } catch (Exception e) {
            Log.e(TAG, "Error in serve()", e);
            return Response.newFixedLengthResponse("Server error: " + e.getMessage());
        }
    }

    private byte[] createWavHeader(int sampleRate, int bitsPerSample, int channels) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;

        byte[] header = new byte[44];

        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = header[5] = header[6] = header[7] = (byte) 0xFF;
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
        header[20] = 1; header[21] = 0;
        header[22] = (byte) channels; header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) ((channels * bitsPerSample) / 8);
        header[33] = 0;
        header[34] = (byte) bitsPerSample;
        header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = header[41] = header[42] = header[43] = (byte) 0xFF;

        return header;
    }

    @Override
    public void stop() {
        Log.d(TAG, "Server stopped");
        super.stop();
    }
}