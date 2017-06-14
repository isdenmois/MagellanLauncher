package isden.mois.magellanlauncher.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;

import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.httpd.HTTPD;
import isden.mois.magellanlauncher.tasks.CheckForUpdates;

public class HTTPDActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private static final String emptyText = "Адрес будет доступен при подключении к сети";

    private HTTPD server;
    private TextView textIP;
    private TextView textSSID;
    private WifiManager wifiManager;
    private ImageView imageView;
    private String url = emptyText;
    private Bitmap QR;
    private boolean isEmulator = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showIP();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_httpd);

        isEmulator = Build.PRODUCT.startsWith("sdk_");

        textIP = (TextView) findViewById(R.id.textIP);
        textSSID = (TextView) findViewById(R.id.textSSID);
        imageView = (ImageView) findViewById(R.id.imageView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        try {
            this.server = new HTTPD(this);
            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.showIP();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.server != null) {
            this.server.stop();
            this.server = null;
        }
    }

    @Override
    public void onClick(View v) {
        new CheckForUpdates(this).execute();
    }

    private boolean isWifiEnabled () {
        // Set "enabled" for Emulator.
        return isEmulator || wifiManager.isWifiEnabled();

    }

    private boolean isWifiConnected() {
        // Set "connected" for Emulator.
        if (isEmulator) {
            return true;
        }

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return ipAddress != 0;
    }

    private String getIp() {
        // Set static IP for emulator.
        if (isEmulator) {
            return "10.0.0.235";
        }

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format(
                "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)
        );
    }

    private int getPort() {
        // Set static port for emulator.
        if (isEmulator) {
            return 5000;
        }

        return HTTPD.PORT;
    }

    private String getSSID() {
        if (isEmulator) {
            return "BookManagerWIFI";
        }

        return wifiManager.getConnectionInfo().getSSID();
    }

    private void showIP () {
        if (!this.isWifiEnabled()) {
            textSSID.setText("WIFI выключен!");
            textIP.setText(emptyText);
            imageView.setImageResource(0);
        }
        else if (!this.isWifiConnected()) {
            textSSID.setText("Устройство не подкючено к точке доступа!");
            textIP.setText(emptyText);
            imageView.setImageResource(0);
        }
        else {
            String url = "http://" + getIp() + ":" + getPort();
            if (!this.url.equals(url)) {
                try {
                    QR = encodeAsBitmap(url);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }

            this.url = url;
            textSSID.setText("SSID: " + getSSID());
            textIP.setText(url);
            imageView.setImageBitmap(QR);
        }
    }

    private int getClientWidth () {
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        int size = this.getClientWidth() / 2;

        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, size, size, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
