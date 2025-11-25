
package com.example.myapp;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.*;

public class SimpleHttpServer extends NanoHTTPD {

    private AudioStreamRecorder recorder;

    public SimpleHttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if ("/".equals(uri)) {
            String html = "<html><body><h2>Audio stream server</h2><p>Open /stream to listen.</p></body></html>";
            return Response.newFixedLengthResponse(Status.OK, "text/html", html);
        }

        if ("/stream".equals(uri)) {
            try {
                if (recorder == null) {
                    recorder = new AudioStreamRecorder();
                    recorder.start();
                }

                PipedInputStream in = recorder.getInputStream();

                // WAV header for PCM 16-bit mono 16kHz
                ByteArrayOutputStream header = new ByteArrayOutputStream();
                writeWavHeader(header, AudioStreamRecorder.SAMPLE_RATE, 1, 16);

                Response res = Response.newChunkedResponse(Status.OK, "audio/wav", new SequenceInputStream(
                        new ByteArrayInputStream(header.toByteArray()), in
                ));
                res.addHeader("Connection","close");
                return res;

            } catch (Exception e) {
                e.printStackTrace();
                return Response.newFixedLengthResponse("Error starting stream");
            }
        }

        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not found");
    }

    public void stopServer() {
        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
        super.stop();
    }

    private void writeWavHeader(OutputStream out, int sampleRate, int channels, int bitsPerSample) throws IOException {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        byte[] header = new byte[44];

        header[0]='R'; header[1]='I'; header[2]='F'; header[3]='F';
        header[4]=0; header[5]=0; header[6]=0; header[7]=0;
        header[8]='W'; header[9]='A'; header[10]='V'; header[11]='E';
        header[12]='f'; header[13]='m'; header[14]='t'; header[15]=' ';
        header[16]=16; header[17]=0; header[18]=0; header[19]=0;
        header[20]=1; header[21]=0;
        header[22]=(byte)channels; header[23]=0;
        header[24]=(byte)(sampleRate&0xff);
        header[25]=(byte)((sampleRate>>8)&0xff);
        header[26]=(byte)((sampleRate>>16)&0xff);
        header[27]=(byte)((sampleRate>>24)&0xff);
        header[28]=(byte)(byteRate&0xff);
        header[29]=(byte)((byteRate>>8)&0xff);
        header[30]=(byte)((byteRate>>16)&0xff);
        header[31]=(byte)((byteRate>>24)&0xff);
        header[32]=(byte)(channels*bitsPerSample/8);
        header[33]=0;
        header[34]=(byte)bitsPerSample; header[35]=0;
        header[36]='d'; header[37]='a'; header[38]='t'; header[39]='a';
        header[40]=0; header[41]=0; header[42]=0; header[43]=0;

        out.write(header);
    }
}
