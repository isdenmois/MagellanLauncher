package isden.mois.magellanlauncher.models

import isden.mois.magellanlauncher.utils.formatHumanTime

/**
 * Created by isden on 16.06.17.
 */
class HistoryDetail(
    var date: String,
    var timestamp: Long,
    var spent: Long
) {
    var speed: Double = 0.0

    fun spentTime(): String {
        return formatHumanTime(spent)
    }
}