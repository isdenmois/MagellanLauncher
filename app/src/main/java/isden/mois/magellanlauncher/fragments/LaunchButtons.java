package isden.mois.magellanlauncher.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.holders.BuiltInIcon;
import isden.mois.magellanlauncher.holders.ExternalIcon;
import isden.mois.magellanlauncher.holders.IIcon;

/**
 * Created by ray on 03.08.2014.
 */
public class LaunchButtons extends Fragment {

    private static String TAG = "ButtonsFragment";
    SharedPreferences prefs;

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.launcher_buttons_layout);
        if (l != null) {
            for (int i = 0; i < l.getChildCount(); i++) {
                ImageView iw = (ImageView) l.getChildAt(i);
                setImage(iw);
            }
            l.invalidate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return inflater.inflate(R.layout.fragment_launcher_buttons, container, false);
    }

    private void setImage(ImageView iw) {
        if (iw == null) {
            return;
        }
        final String tag = (String) iw.getTag();
        final String key = "button_" + tag + "_icon";

        String img_coded = prefs.getString(key, null);
        if (img_coded == null) {
            return;
        }
        int result;
        IIcon icon;
        try {
            result = Integer.parseInt(img_coded);
        }
        catch (NumberFormatException e) {
            result = 0;
        }
        if (result >= 0) {
            icon = new BuiltInIcon(result);
        }
        else {
            icon = new ExternalIcon(img_coded);
        }

        icon.setIcon(iw);
    }

}
