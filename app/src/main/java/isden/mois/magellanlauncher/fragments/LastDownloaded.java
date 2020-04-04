package isden.mois.magellanlauncher.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import isden.mois.magellanlauncher.IsdenTools;
import isden.mois.magellanlauncher.Metadata;
import isden.mois.magellanlauncher.Onyx;
import isden.mois.magellanlauncher.R;

public class LastDownloaded extends Fragment implements View.OnClickListener {
    private List<Metadata> metadataList = new ArrayList<>();
    private GridView grid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_last_downloaded, container, false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.addedPgDown) {
            onPageDown();
            return;
        }

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

    public void onPageDown() {
        if (grid == null) return;

        grid.setSelection(grid.getLastVisiblePosition());
        grid.invalidate();
    }

    static class ViewHolder {
        TextView tv;
        ImageView iv;
    }

    @Override
    public void onResume() {
        super.onResume();

        metadataList = Onyx.getLastDownloaded(this.getActivity(), 20);
        grid = (GridView) getActivity().findViewById(R.id.addedGrid);

        ImageButton button = (ImageButton) getActivity().findViewById(R.id.addedPgDown);

        button.setOnClickListener(this);
        if (metadataList.size() < 8) {
            button.setVisibility(View.INVISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
        }

        grid.setAdapter(new AddedBooksAdapter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        metadataList = null;
        grid = null;
    }

    private class AddedBooksAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return metadataList.size();
        }

        @Override
        public Metadata getItem(int i) {
            return metadataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View v, ViewGroup parent) {
            if (v == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.item_book, null);
            }

            if (v == null) return null;

            Metadata item = getItem(i);

            if (item == null) return v;

            try {
                ViewHolder holder = new ViewHolder();
                holder.tv = (TextView) v.findViewById(R.id.book_name);
                holder.iv = (ImageView) v.findViewById(R.id.book_image);

                holder.tv.setText(item.title);

                Bitmap thumbnail = item.getThumbnail();
                if (thumbnail != null) {
                    holder.iv.setImageBitmap(thumbnail);
                } else {
                    holder.iv.setImageResource(R.drawable.book_down);
                }

                v.setTag(item.filePath);
                v.setOnClickListener(LastDownloaded.this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return v;
        }
    }
}
