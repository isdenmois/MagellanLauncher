package isden.mois.magellanlauncher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.adapters.DialogActionAdapter;

/**
 * Created by isden on 27.06.15.
 */
public class ActionDialog implements IDialog, View.OnClickListener {
    Context c;
    String key;
    DialogActionAdapter adapter;
    SharedPreferences prefs;
    ExpandableListView expandableListView;

    public ActionDialog(Context c, String key) {
        this.c = c;
        this.key = key;
        prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    @Override
    public Dialog getDialog() {
        LayoutInflater i = LayoutInflater.from(c);
        View layout = i.inflate(R.layout.dialog_actions, null);
        layout.findViewById(R.id.action_down).setOnClickListener(this);
        layout.findViewById(R.id.action_up).setOnClickListener(this);

        expandableListView = (ExpandableListView) layout.findViewById(R.id.action_list);
        String value = prefs.getString(key, null);
        adapter = new DialogActionAdapter(c, value);
        expandableListView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Choose action");

        builder.setView(layout);
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor e = prefs.edit();
                String checked = adapter.getChecked();
                e.putString(key, checked);
                e.commit();

                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onClick(View view) {
        if (expandableListView == null) {
            return;
        }

        int target;
        int first = expandableListView.getFirstVisiblePosition();
        int last = expandableListView.getLastVisiblePosition();
        if (view.getId() == R.id.action_up) {
            target = first - (last - first);
            if (target < 0) {
                target = 0;
            }
        }
        else {
            target = last;
        }

        expandableListView.setSelection(target);
        expandableListView.invalidate();
    }
}
