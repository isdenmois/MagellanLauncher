package isden.mois.magellanlauncher;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import isden.mois.magellanlauncher.adapters.HistoryDetailsAdapter;

/**
 * Created by isden on 16.08.15.
 */
public class HistoryDetailsActivity extends Activity {
    ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);

        Metadata data = (Metadata) this.getIntent().getSerializableExtra("metadata");

        if (data == null) {
            return;
        }

        if (data.title != null) {
            setTitle(data.title);
        }

        TextView author = (TextView) findViewById(R.id.tw_author);
        if (data.author != null && author != null) {
            author.setText(data.author);
        }

        TextView title = (TextView) findViewById(R.id.tw_title);
        if (data.title != null && title != null) {
            title.setText(data.title);
        }

        TextView progress = (TextView) findViewById(R.id.tw_progress);
        if (data.size > 0 && progress != null) {
            progress.setText(data.getProgress());
        }

        TextView time = (TextView) findViewById(R.id.tw_time);
        if (time != null) {
            time.setText(data.getSpentTime());
        }

        TextView start = (TextView) findViewById(R.id.tw_startdate);
        if (data.md5 != null && start != null) {
            long start_time = Onyx.getFirstTime(this, data);
            start.setText(IsdenTools.formatDate(start_time));
        }

        TextView end = (TextView) findViewById(R.id.tw_enddate);
        if (data.lastAccess > 0 && end != null) {
            end.setText(IsdenTools.formatDate(data.lastAccess));
        }

        ImageView icon_view = (ImageView) findViewById(R.id.iw_icon);
        Bitmap icon = Onyx.getThumbnail(data);
        if (icon != null && icon_view != null) {
            icon_view.setImageBitmap(icon);
        }

        list = (ListView) findViewById(R.id.list_details);
        if (data.md5 != null && list != null) {
            HistoryDetailsAdapter adapter = new HistoryDetailsAdapter(this, data);
            list.setAdapter(adapter);
        }
    }

}
