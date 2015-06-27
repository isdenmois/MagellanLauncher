package isden.mois.magellanlauncher.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by isden on 28.06.15.
 */
public class ImageFilter implements FilenameFilter {
    @Override
    public boolean accept(File file, String s) {
        return s.endsWith(".jpg") || s.endsWith(".png");
    }
}
