package isden.mois.magellanlauncher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import isden.mois.magellanlauncher.R;
import isden.mois.magellanlauncher.models.BookMetadata;
import isden.mois.magellanlauncher.models.HistoryDetail;
import isden.mois.magellanlauncher.utils.OnyxKt;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by isden on 16.08.15.
 */
public class HistoryDetailsAdapter extends BaseAdapter {
    HistoryDetail[] details;
    Context ctx;

    static class ViewHolder {
        TextView date;
        TextView spent;
    }

    public HistoryDetailsAdapter(Context ctx, BookMetadata data) {
        details = OnyxKt.getDetailedHistory(ctx, data);
        Arrays.sort(details, new Comparator<HistoryDetail>() {
            @Override
            public int compare(HistoryDetail h1, HistoryDetail h2) {
                if (h1 == null && h2 == null) {
                    return 0;
                }
                if (h1 == null) {
                    return 1;
                }
                if (h2 == null) {
                    return -1;
                }

                long t1 = h1.getTimestamp();
                long t2 = h2.getTimestamp();
                return (t1 < t2) ? -1 : ((t1 == t2) ? 0 : 1);
            }
        });
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return details.length;
    }

    @Override
    public Object getItem(int i) {
        return details[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        try {
            ViewHolder holder;
            View v = view;
            LayoutInflater inflater = LayoutInflater.from(ctx);
            if (v == null) {
                v = inflater.inflate(R.layout.item_detail, null);
                if (v == null) {
                    return null;
                }
                holder = new ViewHolder();
                holder.date = (TextView) v.findViewById(R.id.tw_date);
                holder.spent = (TextView) v.findViewById(R.id.tw_time);

                v.setTag(holder);
            }

            holder = (ViewHolder) v.getTag();
            HistoryDetail detail = details[i];

            if (holder.date != null) {
                holder.date.setText(detail.getDate());
            }
            if (holder.spent != null) {
                holder.spent.setText(detail.spentTime());
            }

            return v;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
