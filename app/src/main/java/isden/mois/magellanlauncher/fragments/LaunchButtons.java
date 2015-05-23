package isden.mois.magellanlauncher.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import isden.mois.magellanlauncher.R;

/**
 * Created by ray on 03.08.2014.
 */
public class LaunchButtons extends Fragment {

    private static String TAG = "ButtonsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_launcher_buttons, container, false);

        return v;
    }

}
