package isden.mois.magellanlauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "main";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Убираем заголовок
        setContentView(R.layout.activity_main);

        LinearLayout l = (LinearLayout) findViewById(R.id.launcher_buttons_layout);
        for (int i = 0; i < l.getChildCount(); i++) {
            l.getChildAt(i).setOnLongClickListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_settings:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            case R.id.main_preferences:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            case R.id.main_about:
                StringBuilder message = new StringBuilder();
                Resources res = getResources();
                String versionName;
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    versionName = packageInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    versionName = "";
                }

                message.append("Magellan Launcher");
                message.append('\n');

                message.append(res.getString(R.string.version));
                message.append(": ");
                message.append(versionName);
                message.append('\n');

                message.append("2015 ");
                message.append('\u00A9');
                message.append(" Denis Moiseev ");
                message.append("<isdenmois@gmail.com>");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.about)
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.current_book:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.neverland.alreader"));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imgFM:
            case R.id.imgLib:
            case R.id.imgSync:
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    String tag = (String) v.getTag();
                    String app_name = prefs.getString("button_" + tag + "_app", "");
                    startActivity(getPackageManager().getLaunchIntentForPackage(app_name));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.launcher_history:
                intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.imgApp:
                intent = new Intent(this, ApplicationsActivity.class);
                startActivity(intent);
                break;

            default:
                Log.i(TAG, "Click by " + v.toString());
        }
    }


    @Override
    public boolean onLongClick(View v) {
        Intent settings = new Intent(this, IconEditActivity.class);
        settings.putExtra("tag", (String) v.getTag());
        startActivity(settings);
        return true;
    }
}

