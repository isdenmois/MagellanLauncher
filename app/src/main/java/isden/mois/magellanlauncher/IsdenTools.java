package isden.mois.magellanlauncher;

import android.webkit.MimeTypeMap;

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
}
