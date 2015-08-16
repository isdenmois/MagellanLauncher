package isden.mois.magellanlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.webkit.MimeTypeMap;

import java.util.*;

public class IsdenTools {
    public static String get_mime_by_filename(String filename) {
        String ext;
        String type;

        int lastdot = filename.lastIndexOf(".");
        if (lastdot > 0) {
            ext = filename.substring(lastdot + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(ext);
            if (type != null) {
                return type;
            }
            if (ext.equals("fb2"))
                return "application/x-fictionbook";
        }
        return "*/*";
    }

    public static String prettyTime(long time) {
        time /= 1000; // миллисекунды.

        String result = "";
        if (time < 60 * 60) { // секунды
            result = " " + (time % 60) + "с";
        }

        time /= 60;
        if (time > 0) // минуты
        {
            result = (time % 60) + "м" + result;
        }
        time /= 60;
        if (time > 0) {
            result = time + "ч " + result;
        }
        return result;
    }

    /**
     * Creates sorted list with applications.
     */
    public static List<Application> createAppList(Context ctx) {
        String selfName = ctx.getPackageName();
        PackageManager pm = ctx.getPackageManager();
        List<Application> packages = new ArrayList<Application>();

        Intent componentSearchIntent = new Intent();
        componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        componentSearchIntent.setAction(Intent.ACTION_MAIN);
        List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent, 0);

        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                Application app = new Application();
                app.packageName = ri.activityInfo.packageName;
                try {
                    if (ri.activityInfo.labelRes != 0) {
                        app.name = (String) ri.activityInfo.loadLabel(pm);
                    } else {
                        app.name = (String) ri.loadLabel(pm);
                    }
                } catch (Exception e) { }
                if (app.packageName != null && !app.packageName.equals(selfName)) {
                    packages.add(app);
                }
            }
        }
        Collections.sort(packages, new AppComparator());

        return packages;
    }

    public static String formatDate(long ms) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(ms);

        return String.format(
            "%02d/%02d/%d",
            c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.YEAR)
        );
    }
}
