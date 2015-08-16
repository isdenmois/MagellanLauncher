package isden.mois.magellanlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import isden.mois.magellanlauncher.adapters.HistoryAdapter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class HistoryActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = "HistoryActivity";
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HistoryLoadTask task = new HistoryLoadTask();
        task.execute();
    }

    @Override
    public void onClick(View v) {
        int first = gridView.getFirstVisiblePosition();
        int last = gridView.getLastVisiblePosition();

        int target;
        switch (v.getId()) {
            case R.id.prev_button:
                target = first - 5;
                break;
            case R.id.next_button:
                target = last;
                break;
            default:
                target = -100;
                Log.d(TAG, v.toString());
        }
        if (target != -100) {
            gridView.setSelection(target);
            gridView.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clean_dirty_history:
                Onyx.cleanDirtyHistory(this);
                Toast.makeText(this, R.string.clean_dirty_history_success, Toast.LENGTH_LONG).show();
            break;
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        View v;
        switch (keyCode) {
            case 92:
                v = findViewById(R.id.prev_button);
                if (v != null) {
                    onClick(v);
                }
                break;
            case 93:
                v = findViewById(R.id.next_button);
                if (v != null) {
                    onClick(v);
                }
                break;
            default:
                return super.onKeyDown(keyCode,event);
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Metadata item = (Metadata) adapterView.getItemAtPosition(i);
        Intent intent = new Intent(HistoryActivity.this, HistoryDetailsActivity.class);
        intent.putExtra("metadata", item);
        this.startActivity(intent);
    }

    class HistoryLoadTask extends AsyncTask<Void, Void, Void> {
        HistoryAdapter adapter;

        protected void onPreExecute() {
            setContentView(R.layout.view_pb);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            adapter = new HistoryAdapter(HistoryActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setContentView(R.layout.activity_history);
            gridView = (GridView) findViewById(R.id.paged_grid);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(HistoryActivity.this);

            Resources res = getResources();
            String text = String.format(res.getString(R.string.history_format), Onyx.getTotalTime(HistoryActivity.this));
            setTitle(text);
        }
    }
}


