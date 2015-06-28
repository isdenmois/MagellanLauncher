package isden.mois.magellanlauncher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import static isden.mois.magellanlauncher.IsdenTools.createAppList;

import isden.mois.magellanlauncher.*;

/**
 * Created by isdenmois on 27.06.2015.
 */
public class DialogActionAdapter extends BaseExpandableListAdapter {
    LayoutInflater inflater;

    List<Application> activities;
    List<Application> apps;
    RadioButton checkedRB;
    String checkedPkg;

    public DialogActionAdapter(Context c, String key) {
        inflater = LayoutInflater.from(c);

        apps = createAppList(c);

        activities = new ArrayList<Application>();
        activities.add(new Application(PreferencesActivity.class, "Настройки"));
        activities.add(new Application(HistoryActivity.class, "История"));
        activities.add(new Application(ApplicationsActivity.class, "Приложения"));

        checkedPkg = key;
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public int getChildrenCount(int i) {
        switch (i) {
            case 0:
                return activities.size();
            case 1:
                return apps.size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int i) {
        switch (i) {
            case 0:
                return activities;
            case 1:
                return apps;
        }
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        switch (i) {
            case 0:
                if (i1 < activities.size()) {
                    return activities.get(i1);
                }
                break;

            case 1:
                if (i1 < apps.size()) {
                    return apps.get(i1);
                }
        }
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_group, null);
        }

        TextView textGroup = (TextView) view.findViewById(R.id.textGroup);
        if (i > 0) {
            textGroup.setText("Приложения");
        }
        else {
            textGroup.setText("Активности");
        }

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_radio, null);
        }

        RadioButton r = (RadioButton) view.findViewById(R.id.radio_rb);
        List<Application> pkgList;
        if (i <= 0) {
            pkgList = activities;
        }
        else {
            pkgList = apps;
        }

        if (pkgList.size() > i1 && r != null) {
            TextView textView = (TextView) view.findViewById(R.id.radio_title);
            Application app = pkgList.get(i1);
            textView.setText(app.name);
            view.setTag(app.packageName);

            r.setFocusable(false);
            r.setClickable(false);
            if (checkedPkg != null && app.packageName != null && checkedPkg.equals(app.packageName)) {
                r.setChecked(true);
            }
            else {
                r.setChecked(false);
            }
        }
        view.setClickable(false);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}

