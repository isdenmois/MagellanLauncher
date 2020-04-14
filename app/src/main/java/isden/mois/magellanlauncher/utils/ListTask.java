package isden.mois.magellanlauncher.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

abstract public class ListTask<T> extends AppTask {
    protected List<T> list = new ArrayList<>();
    private ListTaskAdapter<T> adapter;

    public ListTask(Activity activity, ListTaskAdapter<T> adapter) {
        super(activity);
        this.adapter = adapter;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        adapter.setList(list);
        super.onPostExecute(aVoid);
    }
}
