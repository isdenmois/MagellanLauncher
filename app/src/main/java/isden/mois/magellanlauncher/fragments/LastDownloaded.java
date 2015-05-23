package isden.mois.magellanlauncher.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;
import isden.mois.magellanlauncher.IsdenTools;
import isden.mois.magellanlauncher.R;

/**
 * Created by ray on 03.08.2014.
 */
public class LastDownloaded extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_last_downloaded, container, false);

        return v;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File f = new File((String) view.getTag());
        intent.setDataAndType(Uri.fromFile(f), IsdenTools.get_mime_by_filename(f.getName().toLowerCase()));
        try {
            getActivity().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), /*"Can't start application"*/f.getName().toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }

    static class ViewHolder {
        TextView tv;
        ImageView iv;
    }

    @Override
    public void onResume() {
        super.onResume();

        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.lastReadedLayout);
        layout.removeAllViewsInLayout();
        ViewHolder holder;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        File searchPath = new File(sp.getString("library_path", "/sdcard"));
        if (searchPath.exists()) {
            File[] files = searchPath.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return -Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
            int count = 0;
            String[] types = new String[]{"fb2", "epub"};
            for (int i = 0; i < files.length && count < 5; i++) {
                File item = files[i];
                boolean isBook = false;
                String fileName = item.getName().toLowerCase();
                for (String type : types) {
                    if (fileName.endsWith(type)) {
                        isBook = true;
                        break;
                    }
                }
                if (!isBook) {
                    continue;
                } else {
                    count++;
                }
                Parser parser = new InstantParser();
                EBook eBook = parser.parse(item.getPath(), true);

                View v = getActivity().getLayoutInflater().inflate(R.layout.item_book, null);
                holder = new ViewHolder();
                holder.tv = (TextView) v.findViewById(R.id.book_name);
                holder.iv = (ImageView) v.findViewById(R.id.book_image);

                if (eBook == null) {
                    holder.tv.setText(fileName);
                    holder.iv.setImageResource(R.drawable.book_down);
                } else {
                    holder.tv.setText(eBook.title);
                    if (eBook.cover != null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(eBook.cover, 0, eBook.cover.length);
                        if (bmp != null) {
                            holder.iv.setImageBitmap(bmp);
                        }
                    } else {
                        holder.iv.setImageResource(R.drawable.book_down);
                    }
                }
                v.setTag(item.getPath());
                v.setOnClickListener(this);
                layout.addView(v);
            }
        } else {
            Toast.makeText(getActivity(), R.string.path_not_exist, Toast.LENGTH_SHORT).show();
        }

    }
}
