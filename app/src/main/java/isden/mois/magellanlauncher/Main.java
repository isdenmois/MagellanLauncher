package isden.mois.magellanlauncher;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class Main extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {

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
                startActivity(new Intent(this, Preferences.class));
                break;
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
            case R.id.imgApp:
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
                intent = new Intent(this, History.class);
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

