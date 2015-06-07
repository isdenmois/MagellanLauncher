package isden.mois.magellanlauncher;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by isden on 11.01.15.
 */
public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
