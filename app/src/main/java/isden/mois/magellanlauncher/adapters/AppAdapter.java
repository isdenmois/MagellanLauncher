package com.fray.launcher.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.fray.launcher.R;
import com.fray.launcher.ViewFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fray on 13.05.14.
 */
//TODO: переместить все это в Activity. Создать AsyncTask. Данные о приложении передавать в конструкторе.
public class AppAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private static List<String> packages;
    private Context context;
    private static String selfName;

    public static void updateAppList(Context con)
    {
        selfName = con.getPackageName();
        packages = createAppList(con.getPackageManager());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String item = packages.get(i);
        String items[] = item.split("\\%");
        try {
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(items[0]));
        }
        catch (Exception e) {
            Toast.makeText(context, R.string.appnotstarted,Toast.LENGTH_SHORT).show();
        }

    }

    static public List<String> createAppList(PackageManager pm) {
        List<String> rc = new ArrayList<String>();
        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);
        String pname;
        String aname;
        String hname = "";
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                pname = ri.activityInfo.packageName;
                aname = ri.activityInfo.name;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        hname = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        hname = (String) ri.loadLabel(pm);
                    }
                } catch (Exception e) {
                    // emply
                }
                if (pname != null && !pname.equals(selfName))
                    rc.add(pname + "%" + aname + "%" + hname);
            }
        }
        Collections.sort(rc, new AppComparator());
        return rc;
    }


    public AppAdapter(Context con)
    {
        super();
        this.context = con;
    }


    @Override
    public int getCount()
    {
        return packages.size();
    }

    @Override
    public long getItemId(int pos)
    {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup  parent)
    {
        String item = packages.get(position);
        return ViewFactory.Instace().getItem(convertView,item);
    }

    @Override
    public String getItem(int pos)
    {
        return packages.get(pos);
    }
}

class AppComparator implements java.util.Comparator<String> {
    @Override
    public int compare(String a, String b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null && b != null) {
            return 1;
        }
        if (a != null && b == null) {
            return -1;
        }
        String[] ap = a.split("\\%");
        String[] bp = b.split("\\%");
        return ap[2].compareToIgnoreCase(bp[2]);
    }
} 
