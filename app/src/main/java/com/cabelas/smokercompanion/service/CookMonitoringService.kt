package com.cabelas.smokercompanion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cabelas.smokercompanion.MainActivity
import com.cabelas.smokercompanion.R
import com.cabelas.smokercompanion.SmokerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CookMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var observerJob: Job? = null
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var hasAlarmedProbes = mutableSetOf<Int>()

    companion object {
        const val CHANNEL_MONITORING_ID = "cabelas_cook_monitoring"
        const val CHANNEL_ALARM_ID = "cabelas_probe_alarm"
        const val NOTIFICATION_ID = 1001
        const val ALARM_NOTIFICATION_ID = 1002

        const val ACTION_START_MONITORING = "com.cabelas.smokercompanion.START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.cabelas.smokercompanion.STOP_MONITORING"

        fun startService(context: Context) {
            val intent = Intent(context, CookMonitoringService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CookMonitoringService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_MONITORING) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val initialNotification = buildMonitoringNotification("Initializing Smoker Monitor...", "Connecting...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        startObservingSmokerState()

        return START_STICKY
    }

    private fun startObservingSmokerState() {
        observerJob?.cancel()
        val repo = (application as? SmokerApp)?.repository ?: return

        observerJob = serviceScope.launch {
            repo.smokerState.collect { state ->
                val title = "Grill: ${state.temperatureUnit.format(state.currentChamberTempF)} → ${state.temperatureUnit.format(state.targetChamberTempF)} | Smoke: ${state.smokeLevel}"
                val activeProbes = state.activeProbes
                val probeSummary = if (activeProbes.isNotEmpty()) {
                    activeProbes.joinToString(" | ") { probe ->
                        "${probe.name}: ${state.temperatureUnit.format(probe.currentTempF)}${if (probe.targetTempF > 0) "/${state.temperatureUnit.format(probe.targetTempF)}" else ""}"
                    }
                } else {
                    "No meat probes connected | Pellet Hopper: ${state.hopper.levelPercentage}%"
                }

                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildMonitoringNotification(title, probeSummary)
                )

                // Check for Probe Target Alarms
                activeProbes.forEach { probe ->
                    if (probe.isAlarmEnabled && probe.isTargetReached && !hasAlarmedProbes.contains(probe.id)) {
                        hasAlarmedProbes.add(probe.id)
                        triggerTargetAlarm(probe.name, state.temperatureUnit.format(probe.currentTempF))
                    } else if (!probe.isTargetReached) {
                        hasAlarmedProbes.remove(probe.id)
                    }
                }
            }
        }
    }

    private fun buildMonitoringNotification(title: String, text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_MONITORING_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun triggerTargetAlarm(probeName: String, currentTempFormatted: String) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val alarmNotification = NotificationCompat.Builder(this, CHANNEL_ALARM_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔥 Target Reached: $probeName")
            .setContentText("$probeName has reached $currentTempFormatted! Ready to pull or rest.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(ALARM_NOTIFICATION_ID + probeName.hashCode(), alarmNotification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val monitorChannel = NotificationChannel(
                CHANNEL_MONITORING_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setShowBadge(false)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                getString(R.string.notif_alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_alarm_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
            }

            notificationManager.createNotificationChannel(monitorChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        observerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
