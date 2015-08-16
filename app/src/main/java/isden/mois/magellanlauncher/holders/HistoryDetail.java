package isden.mois.magellanlauncher.holders;

import isden.mois.magellanlauncher.IsdenTools;

/**
 * Created by isden on 16.08.15.
 */
public class HistoryDetail {
    public String date;
    public long spent;

    public HistoryDetail(String date, long spent) {
        this.date = date;
        this.spent = spent;
    }

    public String getDate() {
        if (date == null) {
            return "";
        }
        return this.date;
    }

    public String getSpent() {
        return IsdenTools.prettyTime(this.spent);
    }
}
