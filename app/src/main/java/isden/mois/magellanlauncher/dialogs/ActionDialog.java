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
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.adapters.DialogActionAdapter;

/**
 * Created by isden on 27.06.15.
 */
public class ActionDialog implements IDialog, View.OnClickListener, ExpandableListView.OnChildClickListener {
    Context c;
    String key;
    SharedPreferences prefs;
    ExpandableListView expandableListView;
    Dialog dialog;

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
        expandableListView.setAdapter(new DialogActionAdapter(c, value));
        expandableListView.expandGroup(0);
        expandableListView.expandGroup(1);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return true;
            }
        });
        expandableListView.setOnChildClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Choose action");

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
        switch (view.getId()) {
            case R.id.action_down:
            case R.id.action_up:
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
                } else {
                    target = last;
                }

                expandableListView.setSelection(target);
                expandableListView.invalidate();
                break;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        if (view != null) {
            String checked = (String) view.getTag();
            SharedPreferences.Editor e = prefs.edit();
            e.putString(key, checked);
            e.commit();

            dialog.dismiss();
            return true;
        }
        return false;
    }
}
