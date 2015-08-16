package isden.mois.magellanlauncher;

import java.io.Serializable;

/**
 * Created by isden on 10.01.15.
 */
public class Metadata implements Serializable {
    public String md5;
    public String thumbnail;
    public String author;
    public String title;
    public String filename;
    public String filePath;

    public int size;
    public int progress;
    public long totalTime;
    public long lastAccess;

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return filename;
    }

    public long getTime() {
        return totalTime;
    }

    public String getProgress() {
        return progress + "/" + size;
    }

    public double getPercent() {
        return (double) progress / size * 100;
    }

    public String getSpentTime() {
        return IsdenTools.prettyTime(totalTime);
    }

    public String getTotalTime() {
        if (progress > 0) {
            return IsdenTools.prettyTime((int) Math.round((double) size / progress * totalTime));
        }
        return "";
    }

}
