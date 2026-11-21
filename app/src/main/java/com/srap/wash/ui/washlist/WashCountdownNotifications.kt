package com.srap.wash.ui.washlist

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.srap.wash.MainActivity
import com.srap.wash.R

internal class WashCountdownNotifications(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun clearLegacyNotification() {
        manager.cancel(1)
    }

    fun update(countdowns: Map<Int, WashCountdown>, removeMissing: Boolean) {
        val posted = manager.activeNotifications.filter { it.tag == CHANNEL_ID }.associateBy { it.id }
        if (removeMissing) {
            (posted.keys - countdowns.keys).forEach { manager.cancel(CHANNEL_ID, it) }
        }
        if (countdowns.isEmpty() || !manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "洗衣倒计时", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        for ((goodsId, countdown) in countdowns) {
            val millisLeft = countdown.deadlineMillis - System.currentTimeMillis()
            if (millisLeft <= 0) {
                manager.cancel(CHANNEL_ID, goodsId)
                continue
            }
            if (posted[goodsId]?.notification?.extras?.getLong(EXTRA_DEADLINE) ==
                countdown.deadlineMillis
            ) continue

            val content = RemoteViews(context.packageName, R.layout.wash_countdown_notification).apply {
                setTextViewText(R.id.wash_notification_machine, countdown.machineName)
                setChronometer(
                    R.id.wash_notification_timer,
                    SystemClock.elapsedRealtime() + millisLeft,
                    "预计剩余 %s",
                    true
                )
                setChronometerCountDown(R.id.wash_notification_timer, true)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(countdown.machineName)
                .setContentText("预计洗完倒计时")
                .setContentIntent(openApp)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(content)
                .setCustomBigContentView(content)
                .setShowWhen(false)
                .addExtras(Bundle().apply { putLong(EXTRA_DEADLINE, countdown.deadlineMillis) })
                .setTimeoutAfter(millisLeft)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build()
            manager.notify(CHANNEL_ID, goodsId, notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "countdown_channel"
        const val EXTRA_DEADLINE = "com.srap.wash.countdown.deadline"
    }
}
