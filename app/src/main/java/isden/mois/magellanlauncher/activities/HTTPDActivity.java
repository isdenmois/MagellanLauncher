package isden.mois.magellanlauncher.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.httpd.HTTPD;
import isden.mois.magellanlauncher.tasks.CheckForUpdates;

public class HTTPDActivity extends AppCompatActivity {
    private HTTPD server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_httpd);

        new CheckForUpdates(this).execute();

        try {
            this.server = new HTTPD(this);
            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.server != null) {
            this.server.stop();
            this.server = null;
        }
    }
}
