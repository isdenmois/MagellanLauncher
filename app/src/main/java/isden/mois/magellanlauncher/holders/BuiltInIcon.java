package isden.mois.magellanlauncher.holders;

import android.widget.ImageView;

/**
 * Created by isden on 28.06.15.
 */
public class BuiltInIcon implements IIcon {
    int image;

    public BuiltInIcon(int image) {
        this.image = image;
    }

    @Override
    public void setIcon(ImageView iw) {
        if (iw != null) {
            iw.setImageResource(image);
        }
    }

    @Override
    public String getString() {
        return String.valueOf(image);
    }
}
