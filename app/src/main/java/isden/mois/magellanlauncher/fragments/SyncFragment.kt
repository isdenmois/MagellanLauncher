package isden.mois.magellanlauncher.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

import isden.mois.magellanlauncher.R
import isden.mois.magellanlauncher.httpd.HTTPD
import kotlinx.android.synthetic.main.activity_httpd.*
import java.io.IOException

class SyncFragment : Fragment() {
    private var server: HTTPD? = null
    private var wifiManager: WifiManager? = null
    private var url: String = emptyText
    private var QR: Bitmap? = null
    private var isEmulator: Boolean = false

    // Set static port for emulator.
    private val port = HTTPD.PORT

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showIP()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.activity_httpd, container, false)

        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        v.findViewById(R.id.toggleButton).setOnClickListener {
            wifiManager?.setWifiEnabled(!isWifiEnabled)
        }

        try {
            server?.stop()
            server = HTTPD(context)
            server?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()

        server?.stop()
        server = null
        wifiManager = null
        QR = null
    }

    override fun onResume() {
        super.onResume()

        this.showIP()

        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        activity.registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()
        activity.unregisterReceiver(receiver)
    }

    private // Set "enabled" for Emulator.
    val isWifiEnabled: Boolean
        get() = wifiManager?.isWifiEnabled ?: false

    private // Set "connected" for Emulator.
    val isWifiConnected: Boolean
        get() {
            val ipAddress = wifiManager!!.connectionInfo.ipAddress
            return ipAddress != 0
        }

    private // Set static IP for emulator.
    val ip: String
        get() {
            if (isEmulator) {
                return "10.0.0.46"
            }

            val ipAddress = wifiManager!!.connectionInfo.ipAddress
            return "%d.%d.%d.%d".format(
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
            )
        }

    private val ssid: String
        get() {
            if (isEmulator) {
                return "BookManagerWIFI"
            }

            return wifiManager!!.connectionInfo.ssid
        }

    private fun showIP() {
        if (!this.isWifiEnabled) {
            textSSID?.text = "WIFI выключен!"
            textIP?.text = emptyText
            imageView?.setImageResource(0)
            toggleButton?.isChecked = false
        } else if (!this.isWifiConnected) {
            textSSID?.text = "Устройство не подкючено к точке доступа!"
            textIP?.text = emptyText
            imageView?.setImageResource(0)
            toggleButton?.isChecked = true
        } else {
            val url = "http://$ip:$port"
            if (this.url != url) {
                try {
                    QR = encodeAsBitmap(url)
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
            }

            this.url = url
            textSSID?.text = "SSID: $ssid"
            textIP?.text = url
            imageView?.setImageBitmap(QR)
            toggleButton?.isChecked = true
        }
    }

    private val clientWidth: Int
        get() {
            val metrics = this.resources.displayMetrics
            return metrics.widthPixels
        }

    @Throws(WriterException::class)
    internal fun encodeAsBitmap(str: String): Bitmap? {
        val result: BitMatrix
        val size = this.clientWidth / 2

        try {
            result = MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, size, size, null)
        } catch (iae: IllegalArgumentException) {
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0..h - 1) {
            val offset = y * w
            for (x in 0..w - 1) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val BLACK = 0xFF000000.toInt()
        private val emptyText = "Адрес будет доступен при подключении к сети"
    }
}
