package isden.mois.magellanlauncher.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.alibaba.fastjson.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import isden.mois.magellanlauncher.Constants;
import isden.mois.magellanlauncher.MainActivity;

/**
 * Created by isden on 05.01.17.
 */

public class CheckForUpdates extends AsyncTask<Void, Void, String> implements ParentTask {
    private Context context;
    private String currentVersion;
    private SharedPreferences settings;
    private String newVersion;

    public CheckForUpdates(Context context) {
        this.context = context;
        Toast.makeText(context, "Проверка обновлений", Toast.LENGTH_SHORT).show();

        this.settings = context.getSharedPreferences(Constants.UPDATES_PREFERENCES, Context.MODE_PRIVATE);
        this.currentVersion = settings.getString("current-version", "1.0");
    }

    @Override
    protected String doInBackground(Void... voids) {
        String resultJson = "";

        try {
            URL url = new URL(Constants.VERSION_CHECK_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultJson;
    }

    @Override
    protected void onPostExecute(String strJson) {
        super.onPostExecute(strJson);

        if (strJson == null || strJson.length() == 0) {
            Toast.makeText(context, "Не удалось получить информацию об версии", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject dataJsonObj = JSON.parseObject(strJson);
            String tag = dataJsonObj.getString("tag_name");


            if (tag != null && !currentVersion.equals(tag)) {
                this.newVersion = tag;
                downloadFiles(dataJsonObj.getJSONArray("assets"));
            }
            else {
                Toast.makeText(context, "Версия актуальна", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadFiles(JSONArray assets) {
        int length = assets.size();
        String[] data = new String[length];

        for (int i = 0; i < length; i++) {
            JSONObject a = assets.getJSONObject(i);
            data[i] = a.getString("browser_download_url");
        }

        Toast.makeText(context, "Началось скачивание файлов", Toast.LENGTH_LONG).show();
        new GetFiles(context, this).execute(data);
    }

    @Override
    public void done() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("current-version", this.newVersion);
        editor.apply();
        Toast.makeText(context, "Загружена версия " + this.newVersion, Toast.LENGTH_LONG).show();
    }

    @Override
    public void fail() {
        Toast.makeText(context, "Возникла ошибка в процессе обновления", Toast.LENGTH_LONG).show();
    }
}
