package isden.mois.magellanlauncher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.holders.BuiltInIcon;
import isden.mois.magellanlauncher.holders.IIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isden on 28.06.15.
 */
public class DialogIconAdapter extends BaseAdapter {
    LayoutInflater inflater;
    List<IIcon> icons;

    public DialogIconAdapter(LayoutInflater inflater, List<IIcon> icons) {
        this.inflater = inflater;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return icons.size();
    }

    @Override
    public Object getItem(int i) {
        return icons.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_icon, null);
        }

        if (i < icons.size()) {
            IIcon icon = icons.get(i);
            icon.setIcon((ImageView) view.findViewById(R.id.icon_image));
            view.setTag(icon.getString());
        }
        else {
            view.setTag(null);
        }

        return view;
    }
}
