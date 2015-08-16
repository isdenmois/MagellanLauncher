package isden.mois.magellanlauncher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;
import isden.mois.magellanlauncher.dialogs.ActionDialog;
import isden.mois.magellanlauncher.dialogs.IDialog;
import isden.mois.magellanlauncher.dialogs.IconDialog;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "main";

    public static TypedArray builtInImages;

    Intent startIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startIntent = getIntent();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Убираем заголовок
        setContentView(R.layout.activity_main);

        Resources r = getResources();
        builtInImages = r.obtainTypedArray(R.array.images_list);

        LinearLayout l = (LinearLayout) findViewById(R.id.launcher_buttons_layout);
        if (l != null) {
            for (int i = 0; i < l.getChildCount(); i++) {
                registerForContextMenu(l.getChildAt(i));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(v.getId(), R.string.icon_action, 0, R.string.icon_action);
        menu.add(v.getId(), R.string.icon_icon, 0, R.string.icon_icon);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        View view = findViewById(item.getGroupId());
        String tag = (String) view.getTag();
        String key;
        IDialog builder;
        switch (item.getItemId()) {
            case R.string.icon_action:
                key = "button_" + tag + "_app";
                builder = new ActionDialog(this, key);
                break;
            case R.string.icon_icon:
                key = "button_" + tag + "_icon";
                builder = new IconDialog(this, key);
                break;
            default:
                return false;
        }

        Dialog dialog = builder.getDialog();
        dialog.show();

        return super.onContextItemSelected(item);
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
            case R.id.launcher_history:
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
            case R.id.imgApp:
                intent = new Intent(this, ApplicationsActivity.class);
                startActivity(intent);
                break;

            default:
                Log.i(TAG, "Click by " + v.toString());
        }
    }

    public void reCreate() {
        finish();
        startActivity(startIntent);
    }
}

