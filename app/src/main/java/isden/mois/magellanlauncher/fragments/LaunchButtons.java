package isden.mois.magellanlauncher.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import isden.mois.magellanlauncher.R;

public class LaunchButtons extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_launcher_buttons, container, false);
        LinearLayout l = (LinearLayout) view.findViewById(R.id.launcher_buttons_layout);
        if (l != null) {
            for (int i = 0; i < l.getChildCount(); i++) {
                getActivity().registerForContextMenu(l.getChildAt(i));
            }
        }

        return view;
    }
}
