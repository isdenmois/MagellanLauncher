package isden.mois.magellanlauncher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

import isden.mois.magellanlauncher.holders.BookTime;
import isden.mois.magellanlauncher.utils.DateKt;

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
    public long lastAccess;

    public int readPages;

    BookTime time = new BookTime();

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
        return time.totalTime;
    }

    public String getProgress() {
        return progress + " / " + size;
    }

    public double getPercent() {
        return (double) progress / size * 100;
    }

    public String getSpentTime() {
        return DateKt.formatHumanTime(time.currentTime);
    }

    public String getTotalSpentTime() {
        return DateKt.formatHumanTime(time.totalTime);
    }

    public int leftTime() {
        return (int) Math.round(time.speed * (size - progress));
    }

    public String getTotalTime() {
        if (progress > 0) {
            return DateKt.formatHumanTime(time.currentTime + leftTime());
        }
        return "";
    }

    public Bitmap getThumbnail() {
        if (this.thumbnail == null) {
            return null;
        }

        return BitmapFactory.decodeFile(this.thumbnail);
    }

    public String formatTimeProgress() {
        if (time.totalTime == 0) return null;

        if (time.currentTime == time.totalTime) {
            return getSpentTime() + " / " + getTotalTime();
        }

        return getSpentTime() + " (" + getTotalSpentTime() + ") / " + getTotalTime();
    }

    public String getSpeed() {
        return Math.round(60 * 60 * 1000 / time.speed) + "";
    }
}
