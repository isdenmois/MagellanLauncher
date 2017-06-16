package isden.mois.magellanlauncher.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import isden.mois.magellanlauncher.utils.formatHumanTime
import java.io.File
import java.io.Serializable

/**
 * Created by isden on 16.06.17.
 */
class BookMetadata (
    val md5: String,
    val author: String,
    val title: String,
    val filename: String,
    val filePath: String,
    val lastAccess: Long,
    val time: BookTime,
    private val thumbnail: String?,
    val progress: String?,
    val firstTime: Long
) : Serializable {
    var currentPage = 0
    var size = 0

    init {
        if (progress != null) {
            val p = progress.split("/")
            if (p.size == 2) {
                currentPage = p[0].toInt()
                size = p[1].toInt()
            }
        }
    }

    fun currentSpentTime(): String {
        return formatHumanTime(time.currentTime)
    }

    fun totalSpentTime(): String {
        return formatHumanTime(time.totalTime)
    }

    fun leftTime(): Long {
        return Math.round(time.speed * (size - currentPage))
    }

    fun totalTime(): String {
        if (currentPage > 0) {
            return formatHumanTime(time.currentTime + leftTime())
        }

        return ""
    }

    fun getThumbnail(): Bitmap? {
        if (thumbnail != null) {
            val thumbFile = File(thumbnail)
            if (thumbFile.exists()) {
                return BitmapFactory.decodeFile(thumbFile.getAbsolutePath())
            }
        }

        return null
    }

    fun formatTimeProgress(): String {
        if (time.totalTime == 0.toLong()) {
            return ""
        }

        if (time.currentTime == time.totalTime) {
            return currentSpentTime() + " / " + totalSpentTime()
        }

        return currentSpentTime() + " (" + totalSpentTime() + ") / " + totalTime()
    }
}
