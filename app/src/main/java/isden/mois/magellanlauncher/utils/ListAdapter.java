package isden.mois.magellanlauncher.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class ListAdapter<T, K extends ViewHolder> extends BaseAdapter implements ListTaskAdapter<T> {
    protected List<T> list = new ArrayList<>();
    private int layoutId;
    protected Context context;

    abstract protected void fillHolder(T item, K holder);
    abstract protected K getHolder(View v);

    public ListAdapter(Context context, int layoutId) {
        this.context = context;
        this.layoutId = layoutId;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(this.layoutId, null);
            if (v == null) return null;

            v.setTag(getHolder(v));
        }

        fillHolder(getItem(i), (K)v.getTag());

        return v;
    }

    @Override
    public void setList(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
