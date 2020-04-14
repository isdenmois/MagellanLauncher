package isden.mois.magellanlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import isden.mois.magellanlauncher.models.HistoryDetail;
import isden.mois.magellanlauncher.utils.AppTask;
import isden.mois.magellanlauncher.utils.ListAdapter;
import isden.mois.magellanlauncher.utils.OnyxKt;
import isden.mois.magellanlauncher.utils.ViewHolder;

public class BookActivity extends Activity implements View.OnClickListener {
    Metadata book;
    BookHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        Intent intent = getIntent();

        adapter = new BookHistoryAdapter(this);
        ListView listView = (ListView)findViewById(R.id.bookHistory);

        listView.setAdapter(adapter);

        new BookTask(this, intent.getStringExtra("MD5")).execute();

        findViewById(R.id.bookToRead).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        book = null;
        adapter = null;
    }

    void fillViews() {
        if (book == null) return;

        setText(R.id.bookTitle, book.title);
        setText(R.id.bookAuthor, book.author);
        if (book.progress > 0) {
            setText(R.id.bookProgress, book.getProgress());
            setText(R.id.bookTime, book.getSpentTime());
            setText(R.id.bookSpeed, book.getSpeed());
            setText(R.id.bookPages, book.readPages + "");
        } else {
            findViewById(R.id.bookReadData).setVisibility(View.GONE);
            findViewById(R.id.bookHistoryGrid).setVisibility(View.GONE);
        }
    }

    void setText(int id, String text) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(text);
    }

    @Override
    public void onClick(View view) {
        if (book == null) return;

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File f = new File(book.filePath);
        intent.setDataAndType(Uri.fromFile(f), IsdenTools.get_mime_by_filename(f.getName().toLowerCase()));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, f.getName().toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }

    private class BookTask extends AppTask {
        private String MD5;
        private HistoryDetail[] details = {};

        BookTask(Activity activity, String MD5) {
            super(activity);
            this.MD5 = MD5;
        }

        @Override
        protected void action() {
            book = Onyx.getBook(activity, MD5);

            if (book.progress > 0) {
                details = OnyxKt.getDetailedHistory(activity, MD5);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fillViews();

            if (details.length > 0) {
                adapter.setList(Arrays.asList(details));
            }
        }
    }
}

class BookHistoryAdapter extends ListAdapter<HistoryDetail, BookHistoryViewHolder> {
    BookHistoryAdapter(Context context) {
        super(context, R.layout.item_detail);
    }

    @Override
    protected void fillHolder(HistoryDetail item, BookHistoryViewHolder holder) {
        holder.date.setText(item.getDate());
        holder.progress.setText(item.getProgress() + "");
        holder.pages.setText(item.getPages() + "");
        holder.time.setText(item.spentTime());
        holder.speed.setText(item.formatSpeed());
    }

    @Override
    protected BookHistoryViewHolder getHolder(View v) {
        return new BookHistoryViewHolder(v);
    }
}

class BookHistoryViewHolder extends ViewHolder {
    TextView date;
    TextView progress;
    TextView pages;
    TextView time;
    TextView speed;

    BookHistoryViewHolder(View v) {
        date = (TextView) v.findViewById(R.id.historyDate);
        progress = (TextView) v.findViewById(R.id.historyProgress);
        pages = (TextView) v.findViewById(R.id.historyPages);
        time = (TextView) v.findViewById(R.id.historyTime);
        speed = (TextView) v.findViewById(R.id.historySpeed);
    }
}
