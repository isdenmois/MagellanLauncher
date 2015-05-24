package isden.mois.magellanlauncher.adapters;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;

import isden.mois.magellanlauncher.R;

public class IconsAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;

    private File[] mThumbs;

    public IconsAdapter(Context c) {
        mInflater = LayoutInflater.from(c);
        mContext = c;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String path = prefs.getString("external_icons_path", "/mnt/storage");

        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            mThumbs = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith("jpeg");
                }
            });
        }
    }

    public int getCount() {
        return mThumbs.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {  // if it's not recycled,
            convertView = mInflater.inflate(R.layout.item_icon, null);
            convertView.setLayoutParams(new GridView.LayoutParams(90, 90));
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.icon.setAdjustViewBounds(true);
        holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.icon.setPadding(8, 8, 8, 8);
        File iconFile = mThumbs[position];
        if (iconFile.exists()) {
            Bitmap iconBitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
            holder.icon.setImageBitmap(iconBitmap);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView icon;
    }
}