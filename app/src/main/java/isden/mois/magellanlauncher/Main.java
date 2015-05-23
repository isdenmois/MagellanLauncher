package isden.mois.magellanlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class Main extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "main";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Убираем заголовок
        setContentView(R.layout.activity_main);
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
            case R.id.launcher_buttons:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.neverland.alreader"));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imgFM:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.mhoffs.filemanager"));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imgLib:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.harasoft.relaunch"));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.launcher_history:
                intent = new Intent(this, History.class);
                startActivity(intent);
                break;
            case R.id.imgApp:
                Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                break;
            case R.id.imgSync:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("lysesoft.andsmb"));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.i(TAG, "Click by " + v.toString());
        }
    }


}

