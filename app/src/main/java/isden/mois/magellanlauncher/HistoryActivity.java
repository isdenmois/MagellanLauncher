package isden.mois.magellanlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import isden.mois.magellanlauncher.holders.ExternalIcon;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class HistoryActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "HistoryActivity";
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        gridView = (GridView) findViewById(R.id.paged_grid);
        HistoryAdapter adapter = new HistoryAdapter(this);
        gridView.setAdapter(adapter);

        Resources res = getResources();
        String text = String.format(res.getString(R.string.history_format), Onyx.getTotalTime(this));
        setTitle(text);
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
}

class HistoryAdapter extends BaseAdapter {

    private Metadata[] data;
    private Context ctx;

    public HistoryAdapter(Context c) {
        super();
        this.ctx = c;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String limitStr = prefs.getString("history_limit", "0");
        int limit = 0;
        try {
            limit = Integer.parseInt(limitStr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        List<Metadata> d = Onyx.getRecentReading(c, limit);
        data = new Metadata[d.size()];
        data = d.toArray(data);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView title;
        TextView progress;
        TextView spent;
        TextView date;
        ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder holder;
            View v = convertView;
            LayoutInflater inflater = LayoutInflater.from(ctx);
            if (v == null) {
                v = inflater.inflate(R.layout.item_history, null);
                if (v == null) {
                    return null;
                }
                holder = new ViewHolder();
                holder.date = (TextView) v.findViewById(R.id.history_date);
                holder.image = (ImageView) v.findViewById(R.id.history_image);
                holder.title = (TextView) v.findViewById(R.id.history_title);
                holder.progress = (TextView) v.findViewById(R.id.history_progress);
                holder.spent = (TextView) v.findViewById(R.id.history_spent);

                v.setTag(holder);
            }

            holder = (ViewHolder) v.getTag();
            Metadata metadata = data[position];

            Bitmap bmp = Onyx.getThumbnail(metadata);
            if (bmp != null) {
                holder.image.setImageBitmap(bmp);
            }

            if (metadata.title != null && !metadata.title.equals("")) {
                holder.title.setText(metadata.author + " -- " + metadata.title);
            } else {
                holder.title.setText(metadata.getName());
            }


            holder.progress.setText(metadata.getProgress());
            holder.spent.setText(metadata.getSpentTime());

            if (metadata.lastAccess > 0) {
                Calendar c = new GregorianCalendar();
                c.setTimeInMillis(metadata.lastAccess);

                String time = String.format(
                        "%d/%d/%d",
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.YEAR)
                );
                holder.date.setText(time);
            } else {
                holder.date.setText("N/A");
            }

            return v;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
