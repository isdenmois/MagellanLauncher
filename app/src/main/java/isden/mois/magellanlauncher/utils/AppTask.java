package isden.mois.magellanlauncher.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import isden.mois.magellanlauncher.R;

abstract public class AppTask extends AsyncTask<Object, Void, Void> {
    protected int indicatorId = R.id.activityIndicator;
    protected int containerId = R.id.container;
    protected Activity activity;

    public AppTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        activity.findViewById(this.containerId).setVisibility(View.GONE);
        activity.findViewById(this.indicatorId).setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Object... voids) {
        this.action();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        activity.findViewById(this.indicatorId).setVisibility(View.GONE);
        activity.findViewById(this.containerId).setVisibility(View.VISIBLE);
    }

    abstract protected void action();
}
