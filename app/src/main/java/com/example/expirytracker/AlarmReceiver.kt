package com.example.expirytracker

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val productName = intent.getStringExtra("productName") ?: "Product"
        val expiryDateMillis = intent.getLongExtra("expiryDate", 0L)
        val notificationId = intent.getIntExtra("notificationId", 0)
        Log.i("AlarmReceiver", "Alarm triggered for $productName")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return
        }

        val formattedDate = if (expiryDateMillis > 0L) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expiryDateMillis))
        } else {
            "unknown date"
        }

        val notification = NotificationCompat.Builder(context, "PRODUCT_EXPIRY_CHANNEL")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Product Expiry Alert")
            .setContentText("$productName is expiring on $formattedDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(notificationId, notification)
    }
}