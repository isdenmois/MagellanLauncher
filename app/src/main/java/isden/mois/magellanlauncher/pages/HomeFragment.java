package isden.mois.magellanlauncher.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import isden.mois.magellanlauncher.IsdenTools;
import isden.mois.magellanlauncher.Metadata;
import isden.mois.magellanlauncher.Onyx;
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.utils.ListAdapter;
import isden.mois.magellanlauncher.utils.ListTask;
import isden.mois.magellanlauncher.utils.ListTaskAdapter;
import isden.mois.magellanlauncher.utils.ViewHolder;

public class HomeFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private GridView grid;
    private AddedBooksAdapter adapter = new AddedBooksAdapter(null);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        grid = (GridView) getActivity().findViewById(R.id.addedGrid);

        adapter.setContext(getContext());
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);

        ImageButton button = (ImageButton) getActivity().findViewById(R.id.addedPgDown);

        button.setOnClickListener(this);

        new HomeBooksTask(getActivity(), adapter).execute();
    }

    @Override
    public void onClick(View view) {
        onPageDown();
    }

    public void onPageDown() {
        if (grid == null) return;

        grid.setSelection(grid.getLastVisiblePosition());
        grid.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        grid = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File f = new File(((BookViewHolder) view.getTag()).path);
        intent.setDataAndType(Uri.fromFile(f), IsdenTools.get_mime_by_filename(f.getName().toLowerCase()));
        try {
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), /*"Can't start application"*/f.getName().toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }
}

class HomeBooksTask extends ListTask<Metadata> {
    private Metadata lastRead;

    HomeBooksTask(Activity activity, ListTaskAdapter<Metadata> adapter) {
        super(activity, adapter);
    }

    @Override
    protected void action() {
        lastRead = Onyx.getCurrentBook(activity);
        list = Onyx.getLastDownloaded(activity, 20);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        setLastReading();

        View button = activity.findViewById(R.id.addedPgDown);
        if (list.size() < 8) {
            button.setVisibility(View.INVISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
        }
    }

    private void setLastReading() {
        if (lastRead == null) return;

        TextView twTitle = (TextView) activity.findViewById(R.id.txtTitle);
        TextView twAuthor = (TextView) activity.findViewById(R.id.txtAuthor);

        if (lastRead.getTitle() != null) {
            twTitle.setText(lastRead.getTitle());
            twAuthor.setText(lastRead.getAuthor());
        } else {
            twTitle.setText(lastRead.getName());
            twAuthor.setHeight(0);
        }

        ImageView imgBook = (ImageView) activity.findViewById(R.id.imgBook);
        Bitmap image = lastRead.getThumbnail();

        if (image == null) {
            imgBook.setImageResource(R.drawable.book_img);
        } else {
            imgBook.setImageBitmap(image);
        }

        TextView progressText = (TextView) activity.findViewById(R.id.twProgress);
        progressText.setText(lastRead.getProgress());

        TextView TimeReadText = (TextView) activity.findViewById(R.id.twReadTime);
        TimeReadText.setText(lastRead.formatTimeProgress());

        TextView speedTW = (TextView) activity.findViewById(R.id.twSpeed);
        speedTW.setText(lastRead.getSpeed());
    }
}

class AddedBooksAdapter extends ListAdapter<Metadata, BookViewHolder> {
    AddedBooksAdapter(Context context) {
        super(context, R.layout.item_book);
    }

    @Override
    protected void fillHolder(Metadata item, BookViewHolder holder) {
        holder.tv.setText(item.title);
        holder.path = item.filePath;

        Bitmap thumbnail = item.getThumbnail();

        if (thumbnail != null) {
            holder.iv.setImageBitmap(thumbnail);
        } else {
            holder.iv.setImageResource(R.drawable.book_down);
        }
    }

    @Override
    protected BookViewHolder getHolder(View v) {
        return new BookViewHolder(v);
    }
}

class BookViewHolder extends ViewHolder {
    TextView tv;
    ImageView iv;
    String path;

    BookViewHolder(View v) {
        tv = (TextView) v.findViewById(R.id.book_name);
        iv = (ImageView) v.findViewById(R.id.book_image);
    }
}
