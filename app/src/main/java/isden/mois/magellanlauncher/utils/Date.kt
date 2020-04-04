package isden.mois.magellanlauncher.utils

import java.text.SimpleDateFormat;

val c = SimpleDateFormat("dd.MM.yyyy")
val time = SimpleDateFormat("HH:mm")

/**
 * Format time in next format: 8ч 45м
 */
fun formatHumanTime(ms: Long): String {
    var time = ms / 1000
    var result = ""

    // Секунды
    if (time < 3600) {
        result = "${time % 60}с"
    }

    // Минуты
    time /= 60
    if (time > 0) {
        result = if (result.length > 0) "${time % 60}м ${result}" else "${time % 60}"
    }

    // Часы
    time /= 60
    if (time > 0) {
        return "${time}:${result}"
    }

    return result
}

/**
 * Format timestamp in next format: 16.09.2017
 */
fun formatDate(timestamp: Long): String {
    return c.format(timestamp)
}
