package com.example.expirytracker
import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("expiry_tracker_settings", Context.MODE_PRIVATE)

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean("enableExpiryAlerts", settings.enableExpiryAlerts)
            putInt("defaultAlertHour", settings.defaultAlertHour)
            putInt("defaultAlertMinute", settings.defaultAlertMinute)
            putInt("defaultReminderLeadTimeValue", settings.defaultReminderLeadTimeValue)
            putString("defaultReminderLeadTimeUnit", settings.defaultReminderLeadTimeUnit.name)
            putInt("repeatIntervalValue", settings.repeatIntervalValue)
            putString("repeatIntervalUnit", settings.repeatIntervalUnit.name)
            putInt("expiringSoonThresholdValue", settings.expiringSoonThresholdValue)
            putString("expiringSoonThresholdUnit", settings.expiringSoonThresholdUnit.name)
            putInt("autoDeleteExpiredValue", settings.autoDeleteExpiredValue)
            putString("autoDeleteExpiredUnit", settings.autoDeleteExpiredUnit.name)
            putString("dateFormat", settings.dateFormat.name)
            putString("sortOrder", settings.sortOrder.name)
            apply()
        }
    }

    fun loadSettings(): AppSettings {
        return AppSettings(
            enableExpiryAlerts = prefs.getBoolean("enableExpiryAlerts", true),
            defaultAlertHour = prefs.getInt("defaultAlertHour", 9),
            defaultAlertMinute = prefs.getInt("defaultAlertMinute", 0),
            defaultReminderLeadTimeValue = prefs.getInt("defaultReminderLeadTimeValue", 2),
            defaultReminderLeadTimeUnit = TimeUnitOption.valueOf(
                prefs.getString("defaultReminderLeadTimeUnit", TimeUnitOption.DAYS.name)!!
            ),
            repeatIntervalValue = prefs.getInt("repeatIntervalValue", 0),
            repeatIntervalUnit = TimeUnitOption.valueOf(
                prefs.getString("repeatIntervalUnit", TimeUnitOption.DAYS.name)!!
            ),
            expiringSoonThresholdValue = prefs.getInt("expiringSoonThresholdValue", 3),
            expiringSoonThresholdUnit = TimeUnitOption.valueOf(
                prefs.getString("expiringSoonThresholdUnit", TimeUnitOption.DAYS.name)!!
            ),
            autoDeleteExpiredValue = prefs.getInt("autoDeleteExpiredValue", 7),
            autoDeleteExpiredUnit = TimeUnitOption.valueOf(
                prefs.getString("autoDeleteExpiredUnit", TimeUnitOption.DAYS.name)!!
            ),
            dateFormat = DateFormatOption.valueOf(
                prefs.getString("dateFormat", DateFormatOption.DD_MM_YYYY.name)!!
            ),
            sortOrder = SortOrderOption.valueOf(
                prefs.getString("sortOrder", SortOrderOption.EXPIRY_ASC.name)!!
            )
        )
    }
}
