package isden.mois.magellanlauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import isden.mois.magellanlauncher.models.KeyDownFragment;
import isden.mois.magellanlauncher.models.KeyDownListener;
import isden.mois.magellanlauncher.pages.HomeFragment;
import isden.mois.magellanlauncher.pages.LibraryFragment;
import isden.mois.magellanlauncher.pages.SyncFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "main";

    Intent startIntent;
    KeyDownListener keyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources r = getResources();

        startIntent = getIntent();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Убираем заголовок
        setContentView(R.layout.activity_main);

        openHome();
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

                message.append("2020 ");
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openHome();
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) keyCode = KeyEvent.KEYCODE_PAGE_DOWN;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) keyCode = KeyEvent.KEYCODE_PAGE_UP;

        if (keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            if (keyListener != null) {
                keyListener.onKeyDown(keyCode);
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.nowRead:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.neverland.alreader"));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.imgSettings:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String tag = (String) v.getTag();
                String app_name = prefs.getString("button_" + tag + "_app", "");

                try {
                    Class action = Class.forName(app_name);
                    intent = new Intent(this, action);
                } catch (ClassNotFoundException e) {
                    intent = getPackageManager().getLaunchIntentForPackage(app_name);
                }

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.imgHome:
                this.openHome();
                break;

            case R.id.imgLibrary:
                this.openLibrary();
                break;

            case R.id.imgApps:
                intent = new Intent(this, ApplicationsActivity.class);
                startActivity(intent);
                break;

            case R.id.imgSync:
                this.openSync();
                break;

            default:
                Log.i(TAG, "Click by " + v.toString());
        }
    }

    private void openHome() {
        this.changeFragment(new HomeFragment(), R.id.imgHome);
    }

    private void openLibrary() {
        this.changeFragment(new LibraryFragment(), R.id.imgLibrary);
    }

    private void openSync() {
        this.changeFragment(new SyncFragment(), R.id.imgSync);
    }

    private void changeFragment(KeyDownFragment fragment, int id) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        findViewById(R.id.activityIndicator).setVisibility(View.GONE);
        findViewById(R.id.container).setVisibility(View.VISIBLE);

        transaction.replace(R.id.container, fragment);

        transaction.commit();

        keyListener = fragment;

        setActive(id);
    }

    private void setActive(int id) {
        LinearLayout layout = (LinearLayout)findViewById(R.id.launcher_buttons_layout);
        if (layout == null) return;

        int count = layout.getChildCount();
        for (int i = 0; i < count; i++) {
            LinearLayout child = (LinearLayout)layout.getChildAt(i);
            TextView text = (TextView)child.getChildAt(1);

            if (child.getId() == id) {
                text.setTypeface(null, Typeface.BOLD);
            } else {
                text.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    public void reCreate() {
        finish();
        startActivity(startIntent);
    }
}

