package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entities.PriceAlertEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PriceAlertNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "agl_price_alerts_channel"
        const val CHANNEL_NAME = "AGL Price Alerts"
        const val CHANNEL_DESCRIPTION = "Real-time alerts triggered by external Base price oracle thresholds"
        private const val BASE_NOTIFICATION_ID = 4000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendPriceAlertNotification(
        alert: PriceAlertEntity,
        currentPrice: Double,
        oracleProvider: String = "Chainlink Base Oracle"
    ): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "PRICE_ALERTS")
            putExtra("ALERT_ID", alert.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val conditionSymbol = if (alert.condition == "ABOVE") "▲ Rises Above" else "▼ Drops Below"
        val priceDiff = currentPrice - alert.targetPriceUsd
        val pctDiff = (priceDiff / alert.targetPriceUsd) * 100.0
        val diffFormatted = if (pctDiff >= 0) "+%.2f%%".format(pctDiff) else "%.2f%%".format(pctDiff)

        val title = "🪙 AGL Price Alert: Target Reached!"
        val shortContent = "AGL is now $${"%.3f".format(currentPrice)} (Threshold: $${"%.3f".format(alert.targetPriceUsd)})"

        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val bigText = buildString {
            append("• Target Threshold: $${"%.3f".format(alert.targetPriceUsd)} ($conditionSymbol)\n")
            append("• Oracle Price: $${"%.3f".format(currentPrice)} ($diffFormatted)\n")
            append("• Feed Source: $oracleProvider (Base L2)\n")
            if (alert.note.isNotBlank()) {
                append("• Strategy Note: ${alert.note}\n")
            }
            append("• Triggered at: $timeStr")
        }

        val notificationId = BASE_NOTIFICATION_ID + (alert.id % 500).toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(title)
                    .setSummaryText("Price Alert Hit")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(Color.parseColor("#00D4FF")) // BaseCyan
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
