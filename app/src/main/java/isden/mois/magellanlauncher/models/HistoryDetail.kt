package isden.mois.magellanlauncher.models

import isden.mois.magellanlauncher.utils.formatHumanTime

class HistoryDetail(
        var date: String,
        var timestamp: Long,
        var spent: Long,
        var progress: Int,
        var pages: Int,
        var speed: Int
) {
    fun spentTime(): String {
        return formatHumanTime(spent)
    }

    fun formatSpeed(): String = (60 * 60 * 1000 / speed).toString()
}