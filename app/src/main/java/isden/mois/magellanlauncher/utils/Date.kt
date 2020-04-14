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
    if (time < 60) {
        return "${time % 60}с"
    }

    // Минуты
    time /= 60
    result = "${time % 60}".padStart(2, '0')

    // Часы
    time /= 60
    return "${time}:${result}"
}

/**
 * Format timestamp in next format: 16.09.2017
 */
fun formatDate(timestamp: Long): String {
    return c.format(timestamp)
}
