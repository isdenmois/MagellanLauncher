package isden.mois.magellanlauncher.utils

import java.text.SimpleDateFormat;

/**
 * Created by isden on 16.06.17.
 */
val c = SimpleDateFormat("MM.dd.yyyy")

fun formatHumanTime(timestamp: Long): String {
    var time = timestamp / 1000
    var result = ""

    // Секунды
    if (time < 3600) {
        result = "${time % 60}с"
    }

    // Минуты
    time /= 60
    if (time > 0) {
        result = "${time % 60}м ${result}"
    }

    // Часы
    time /= 60
    if (time > 0) {
        return "${time}ч ${result}"
    }

    return result
}

fun formatDate(timestamp: Long): String {
    return c.format(timestamp)
}
