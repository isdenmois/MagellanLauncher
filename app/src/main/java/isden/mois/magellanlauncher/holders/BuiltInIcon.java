package isden.mois.magellanlauncher.holders;

import android.widget.ImageView;
import isden.mois.magellanlauncher.MainActivity;

/**
 * Created by isden on 28.06.15.
 */
public class BuiltInIcon implements IIcon {
    private int index;

    public BuiltInIcon(int image) {
        if (image >= 0 && image < MainActivity.builtInImages.length()) {
            this.index = image;
        }
        else {
            this.index = 0;
        }
    }

    @Override
    public void setIcon(ImageView iw) {
        if (iw != null && index >= 0 && index < MainActivity.builtInImages.length()) {
            int image = MainActivity.builtInImages.getResourceId(index, -1);
            if (index >= 0) {
                iw.setImageResource(image);
            }
        }
    }

    @Override
    public String getString() {
        return String.valueOf(index);
    }
}
