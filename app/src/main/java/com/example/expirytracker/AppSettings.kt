package com.example.expirytracker
enum class TimeUnitOption {
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS
}

enum class DateFormatOption {
    DD_MM_YYYY,
    MM_DD_YYYY,
    YYYY_MM_DD
}

enum class SortOrderOption {
    EXPIRY_ASC,
    NAME_ASC,
    RECENTLY_ADDED
}

data class AppSettings(
    val enableExpiryAlerts: Boolean = true,
    val defaultAlertHour: Int = 9,
    val defaultAlertMinute: Int = 0,
    val defaultReminderLeadTimeValue: Int = 2,
    val defaultReminderLeadTimeUnit: TimeUnitOption = TimeUnitOption.DAYS,
    val repeatIntervalValue: Int = 0,
    val repeatIntervalUnit: TimeUnitOption = TimeUnitOption.DAYS,
    val expiringSoonThresholdValue: Int = 3,
    val expiringSoonThresholdUnit: TimeUnitOption = TimeUnitOption.DAYS,
    val autoDeleteExpiredValue: Int = 7,
    val autoDeleteExpiredUnit: TimeUnitOption = TimeUnitOption.DAYS,
    val dateFormat: DateFormatOption = DateFormatOption.DD_MM_YYYY,
    val sortOrder: SortOrderOption = SortOrderOption.EXPIRY_ASC
)
