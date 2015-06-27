package isden.mois.magellanlauncher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.adapters.DialogActionAdapter;
import isden.mois.magellanlauncher.adapters.DialogIconAdapter;
import isden.mois.magellanlauncher.filters.ImageFilter;
import isden.mois.magellanlauncher.holders.BuiltInIcon;
import isden.mois.magellanlauncher.holders.ExternalIcon;
import isden.mois.magellanlauncher.holders.IIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by isden on 28.06.15.
 */
public class IconDialog implements IDialog, View.OnClickListener, AdapterView.OnItemClickListener {
    LayoutInflater inflater;
    SharedPreferences prefs;
    Context c;

    List<IIcon> icons;
    int[] iconArray = new int[]{
        R.drawable.applications,
        R.drawable.history,
        R.drawable.library,
        R.drawable.settings,
        R.drawable.sync
    };
    GridView gw;
    String key;
    Dialog dialog;

    public IconDialog(Context c, String key) {
        this.key = key;
        this.c = c;
        inflater = LayoutInflater.from(c);
        prefs = PreferenceManager.getDefaultSharedPreferences(c);

        icons = new ArrayList<IIcon>();
        for (int icon : iconArray) {
            icons.add(new BuiltInIcon(icon));
        }

        String filename = prefs.getString("icons_path", null);
        if (filename != null) {
            File icon_dir = new File(filename);
            if (icon_dir.exists() && icon_dir.isDirectory()) {
                File[] files = icon_dir.listFiles(new ImageFilter());
                for (File file : files) {
                    icons.add(new ExternalIcon(file));
                }
            }
        }
    }

    @Override
    public Dialog getDialog() {
        View layout = inflater.inflate(R.layout.dialog_icons, null);
        layout.findViewById(R.id.icons_down).setOnClickListener(this);
        layout.findViewById(R.id.icons_up).setOnClickListener(this);

        gw = (GridView) layout.findViewById(R.id.gw_icons);
        gw.setOnItemClickListener(this);
        gw.setAdapter(new DialogIconAdapter(inflater, icons));

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Choose the icon");

        builder.setView(layout);
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        dialog = builder.create();
        return dialog;
    }

    @Override
    public void onClick(View view) {
        if (gw == null) {
            return;
        }

        int target;
        int first = gw.getFirstVisiblePosition();
        int last = gw.getLastVisiblePosition();
        if (view.getId() == R.id.action_up) {
            target = first - (last - first);
            if (target < 0) {
                target = 0;
            }
        }
        else {
            target = last;
        }

        gw.setSelection(target);
        gw.invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(key, (String) view.getTag());
        e.commit();

        dialog.dismiss();
    }
}
