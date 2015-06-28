package isden.mois.magellanlauncher.holders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import isden.mois.magellanlauncher.R;

import java.io.File;

/**
 * Created by isden on 28.06.15.
 */
public class ExternalIcon implements IIcon {
    File image;

    public ExternalIcon(File file) {
        image = file;
    }

    public ExternalIcon(String filename) {
        image = new File(filename);
    }

    @Override
    public void setIcon(ImageView iw) {
        if (iw != null) {
            if (image.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                if (bitmap == null) {
                    iw.setImageResource(R.drawable.book_img);
                } else {
                    iw.setImageBitmap(bitmap);
                }
            } else {
                iw.setImageResource(R.drawable.book_img);
            }
        }
    }

    @Override
    public String getString() {
        return image.getAbsolutePath();
    }
}
