package isden.mois.magellanlauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.ExpandableListView;
import isden.mois.magellanlauncher.adapters.DialogActionAdapter;

import java.util.List;
import java.util.ListIterator;

import static isden.mois.magellanlauncher.IsdenTools.createAppList;

public class IconEditActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String tag = intent.getStringExtra("tag");
        // создаем экран
        final PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen(this);
        // говорим Activity, что rootScreen - корневой
        setPreferenceScreen(rootScreen);

        ListPreference list = new ListPreference(this);
        list.setKey("button_" + tag + "_app");
        list.setTitle("Приложение");
        list.setSummary("Выберите приложение для запуска");

        List<Application> apps = createAppList(this);
        ListIterator<Application> app_it = apps.listIterator();

        CharSequence[] entries = new CharSequence[apps.size()];
        CharSequence[] entries_values = new CharSequence[apps.size()];

        for (int i = 0; app_it.hasNext(); i++) {
            Application app = app_it.next();
            entries[i] = app.name;
            entries_values[i] = app.packageName;
        }

        list.setEntries(entries);
        list.setEntryValues(entries_values);

        rootScreen.addPreference(list);

        final Preference action = new Preference(this);
        action.setKey("button_" + tag + "_app");
        action.setTitle("Действие");
        action.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(IconEditActivity.this);
                builder.setTitle("Choose action");

                ExpandableListView myList = new ExpandableListView(IconEditActivity.this);
                DialogActionAdapter myAdapter = new DialogActionAdapter(IconEditActivity.this);
                myList.setAdapter(myAdapter);

                builder.setView(myList);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
        rootScreen.addPreference(action);


        CheckBoxPreference chb = new CheckBoxPreference(this);
        chb.setKey("button_" + tag + "_external_icon");
        chb.setTitle("Использовать внешние иконки");

        rootScreen.addPreference(chb);

        final EditTextPreference libpath = new EditTextPreference(this);
        libpath.setKey("external_icons_path");
        libpath.setTitle("Путь к иконкам");
        libpath.setEnabled(chb.isChecked());
        libpath.setDefaultValue("/mnt/storage/.icons");

        rootScreen.addPreference(libpath);

        final Preference icon = new Preference(this);
        icon.setTitle("Иконка");
        if (chb.isChecked()) {
            icon.setKey("button_" + tag + "external");
        } else {
            icon.setKey("button_" + tag + "internal");
        }

        rootScreen.addPreference(icon);


        icon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                return true;
            }
        });

        chb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                libpath.setEnabled((Boolean) o);

                if ((Boolean) o) {
                    icon.setKey("button_" + tag + "external");
                } else {
                    icon.setKey("button_" + tag + "internal");
                }
                return true;
            }
        });

    }
}
