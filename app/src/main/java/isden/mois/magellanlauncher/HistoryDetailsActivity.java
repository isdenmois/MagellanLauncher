package isden.mois.magellanlauncher;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import isden.mois.magellanlauncher.adapters.HistoryDetailsAdapter;
import isden.mois.magellanlauncher.models.BookMetadata;
import isden.mois.magellanlauncher.utils.DateKt;

/**
 * Created by isden on 16.08.15.
 */
public class HistoryDetailsActivity extends Activity {
    ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);

        BookMetadata data = (BookMetadata) this.getIntent().getSerializableExtra("metadata");

        if (data == null) {
            return;
        }

        setTitle(data.getTitle());

        TextView author = (TextView) findViewById(R.id.tw_author);
        if (author != null) {
            author.setText(data.getAuthor());
        }

        TextView title = (TextView) findViewById(R.id.tw_title);
        if (title != null) {
            title.setText(data.getTitle());
        }

        TextView progress = (TextView) findViewById(R.id.tw_progress);
        if (data.getSize() > 0 && progress != null) {
            progress.setText(data.getProgress());
        }

        TextView time = (TextView) findViewById(R.id.tw_time);
        if (time != null) {
            time.setText(data.currentSpentTime());
        }

        TextView start = (TextView) findViewById(R.id.tw_startdate);
        if (start != null) {
            long start_time = data.getFirstTime();
            start.setText(DateKt.formatDate(start_time));
        }

        TextView end = (TextView) findViewById(R.id.tw_enddate);
        if (data.getLastAccess() > 0 && end != null) {
            end.setText(DateKt.formatDate(data.getLastAccess()));
        }

        ImageView icon_view = (ImageView) findViewById(R.id.iw_icon);
        Bitmap icon = data.getThumbnail();
        if (icon != null && icon_view != null) {
            icon_view.setImageBitmap(icon);
        }

        list = (ListView) findViewById(R.id.list_details);
        if (list != null) {
            HistoryDetailsAdapter adapter = new HistoryDetailsAdapter(this, data);
            list.setAdapter(adapter);
        }
    }

}
