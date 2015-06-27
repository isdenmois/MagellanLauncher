package isden.mois.magellanlauncher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.*;
import android.widget.ExpandableListView;
import isden.mois.magellanlauncher.adapters.DialogActionAdapter;
import isden.mois.magellanlauncher.dialogs.ActionDialog;
import isden.mois.magellanlauncher.dialogs.IDialog;

import java.util.List;
import java.util.ListIterator;

import static isden.mois.magellanlauncher.IsdenTools.createAppList;

public class IconEditActivity extends PreferenceActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        final String tag = intent.getStringExtra("tag");
        // создаем экран
        final PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen(this);
        // говорим Activity, что rootScreen - корневой
        setPreferenceScreen(rootScreen);

        final Preference action = new Preference(this);
        action.setKey("button_" + tag + "_app");
        action.setTitle("Действие");
        action.setSummary("Выберите действие для запуска");
        action.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                IDialog builder = new ActionDialog(IconEditActivity.this, action.getKey());
                Dialog dialog = builder.getDialog();
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
