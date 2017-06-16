package isden.mois.magellanlauncher.models

import java.io.Serializable

/**
 * Created by isden on 16.06.17.
 */

class BookTime : Serializable {
    var currentTime: Long = 0
    var totalTime: Long = 0
    var speed: Double = 0.0
}
