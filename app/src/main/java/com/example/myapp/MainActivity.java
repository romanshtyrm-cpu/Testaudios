
package com.example.myapp;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvUrl;
    private Button btnStart;
    private Button btnStop;

    private SimpleHttpServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = findViewById(R.id.tvStatus);
        tvUrl = findViewById(R.id.tvUrl);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> {
            if (server == null) {
                try {
                    server = new SimpleHttpServer(8080);
                    server.start();
                    tvStatus.setText("Server: ON");
                    String ip = getDeviceIp();
                    tvUrl.setText("http://" + ip + ":8080/");
                } catch (IOException e) {
                    e.printStackTrace();
                    tvStatus.setText("Server: failed to start");
                }
            }
        });

        btnStop.setOnClickListener(v -> {
            if (server != null) {
                server.stop();
                server = null;
                tvStatus.setText("Server: OFF");
                tvUrl.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private String getDeviceIp() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}
