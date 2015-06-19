package isden.mois.magellanlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static isden.mois.magellanlauncher.IsdenTools.createAppList;

/**
 * Created by isdenmois on 15.05.14.
 */
// TODO: Создать AsyncTask. Переименовать, почистить код, переместить код из адаптера.
public class ApplicationsActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static GridView g;
    private List<Application> packages;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        AppGetTask task = new AppGetTask();
        task.execute();
    }

    @Override
    public void onClick(View v) {
        int first = g.getFirstVisiblePosition();
        int last = g.getLastVisiblePosition();

        int target;

        switch (v.getId()) {
            case R.id.app_prev:
                first -= last - first + 1;
                if (first < 0)
                    first = 0;
                target = first;
                break;
            case R.id.app_next:
                target = last;
                break;

            default:
                target = -100;
        }

        if (target > -100) {
            g.clearFocus();
            g.setSelection(target);
            g.invalidate();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        View v;
        switch (keyCode) {
            case 92:
                v = findViewById(R.id.app_prev);
                onClick(v);
                break;
            case 93:
                v = findViewById(R.id.app_next);
                onClick(v);
                break;
            default:
                return super.onKeyDown(keyCode,event);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Application item = packages.get(i);
        try {
            startActivity(getPackageManager().getLaunchIntentForPackage(item.packageName));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.app_not_started, Toast.LENGTH_LONG).show();
        }
    }

    class AppAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return packages.size();
        }

        @Override
        public long getItemId(int pos)
        {
            return pos;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.item_application, null);
            }
            if (v == null) {
                return null;
            }

            Application item = packages.get(position);
            if (item != null) {
                try {
                    TextView tv = (TextView) v.findViewById(R.id.lib_fname);
                    tv.setText(item.name);

                    ImageView iv = (ImageView) v.findViewById(R.id.lib_fimage);
                    Drawable icon = getPackageManager().getApplicationIcon(item.packageName);
                    iv.setImageDrawable(icon);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return v;
        }

        @Override
        public Application getItem(int pos) {
            return packages.get(pos);
        }
    }

    class AppGetTask extends AsyncTask<Void, Void, Boolean> {
        protected void onPreExecute() {
            setContentView(R.layout.view_pb);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            packages = createAppList(ApplicationsActivity.this);

            return !packages.isEmpty();
        }


        @Override
        protected void onPostExecute(Boolean result) {
            setContentView(R.layout.activity_applications);

            g = (GridView)findViewById(R.id.app_grid);
            g.setAdapter(new AppAdapter());
            g.setOnItemClickListener(ApplicationsActivity.this);

            findViewById(R.id.app_prev).setOnClickListener(ApplicationsActivity.this);
            findViewById(R.id.app_next).setOnClickListener(ApplicationsActivity.this);
        }
    }

}