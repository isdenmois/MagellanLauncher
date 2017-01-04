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
import java.util.List;

import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;
import isden.mois.magellanlauncher.IsdenTools;
import isden.mois.magellanlauncher.Metadata;
import isden.mois.magellanlauncher.Onyx;
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

        List<Metadata> metadataList = Onyx.getLastDownloaded(this.getActivity(), 5);

        for (Metadata metadata : metadataList) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.item_book, null);
            holder = new ViewHolder();
            holder.tv = (TextView) v.findViewById(R.id.book_name);
            holder.iv = (ImageView) v.findViewById(R.id.book_image);

            holder.tv.setText(metadata.title);

            Bitmap thumbnail = metadata.getThumbnail();
            if (thumbnail != null) {
                holder.iv.setImageBitmap(thumbnail);
            }
            else {
                holder.iv.setImageResource(R.drawable.book_down);
            }

            v.setTag(metadata.filePath);
            v.setOnClickListener(this);
            layout.addView(v);
        }

        Toast.makeText(getActivity(), R.string.path_not_exist, Toast.LENGTH_SHORT).show();
    }
}
