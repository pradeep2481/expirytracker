package com.example.expirytracker

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.app.TimePickerDialog
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: ProductDBHelper

    private lateinit var selectedExpiryDateText: TextView
    private lateinit var searchExpiryDateText: TextView
    private lateinit var deleteExpiryDateText: TextView

    private lateinit var btnOk: Button
    private lateinit var searchOk: Button
    private lateinit var deleteOk: Button
    private lateinit var searchProductButton: Button
    private lateinit var searchDateButton: Button
    private lateinit var deleteDateButton: Button
    private lateinit var searchRangeButton: Button
    private lateinit var searchProductOk: Button
    private lateinit var searchRangeOk: Button
    private lateinit var btnScanBarcode: ImageButton
    private lateinit var btnScanExpiry: ImageButton

    private lateinit var expiryDatePicker: DatePicker
    private lateinit var searchDatePicker: DatePicker
    private lateinit var deleteDatePicker: DatePicker
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker

    private lateinit var productNameInput: EditText
    private lateinit var alarmDaysInput: EditText
    private lateinit var searchProductInput: EditText

    private lateinit var recyclerView: RecyclerView
    private lateinit var closeResultsButton: Button

    private lateinit var homeSection: View
    private lateinit var addSection: View
    private lateinit var searchSection: View
    private lateinit var searchContent: View
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var homeQuickAddButton: Button
    private lateinit var homeQuickSearchButton: Button
    private lateinit var homeStatsText: TextView
    private lateinit var settingsSection: View

    private lateinit var switchEnableAlerts: Switch
    private lateinit var btnDefaultAlertTime: Button
    private lateinit var editDefaultReminderValue: EditText
    private lateinit var spinnerDefaultReminderUnit: Spinner
    private lateinit var editRepeatValue: EditText
    private lateinit var spinnerRepeatUnit: Spinner
    private lateinit var editExpiringSoonValue: EditText
    private lateinit var spinnerExpiringSoonUnit: Spinner
    private lateinit var editAutoDeleteValue: EditText
    private lateinit var spinnerAutoDeleteUnit: Spinner
    private lateinit var spinnerDateFormat: Spinner
    private lateinit var spinnerSortOrder: Spinner
    private lateinit var btnSaveSettings: Button

    private lateinit var settingsManager: SettingsManager
    private var currentAlertHour: Int = 9
    private var currentAlertMinute: Int = 0

    private val httpClient = OkHttpClient()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val scanResult = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
            val scanMode = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_MODE)

            if (scanResult != null) {
                if (scanMode == ScannerActivity.SCAN_MODE_BARCODE) {
                    fetchProductNameFromBarcode(scanResult)
                } else if (scanMode == ScannerActivity.SCAN_MODE_EXPIRY) {
                    parseAndSetScannedDate(scanResult)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ProductDBHelper(this)

        btnOk = findViewById(R.id.btnOk)
        searchOk = findViewById(R.id.searchOk)
        deleteOk = findViewById(R.id.deleteOk)

        searchProductButton = findViewById(R.id.searchProductButton)
        searchDateButton = findViewById(R.id.searchDateButton)
        deleteDateButton = findViewById(R.id.deleteDateButton)
        searchRangeButton = findViewById(R.id.searchRangeButton)
        searchProductOk = findViewById(R.id.searchProductOk)
        searchRangeOk = findViewById(R.id.searchRangeOk)
        btnScanBarcode = findViewById(R.id.btnScanBarcode)
        btnScanExpiry = findViewById(R.id.btnScanExpiry)

        selectedExpiryDateText = findViewById(R.id.selectedExpiryDateText)
        searchExpiryDateText = findViewById(R.id.searchExpiryDateText)
        deleteExpiryDateText = findViewById(R.id.deleteExpiryDateText)

        expiryDatePicker = findViewById(R.id.expiryDatePicker)
        searchDatePicker = findViewById(R.id.searchDatePicker)
        deleteDatePicker = findViewById(R.id.deleteDatePicker)
        startDatePicker = findViewById(R.id.startDatePicker)
        endDatePicker = findViewById(R.id.endDatePicker)

        productNameInput = findViewById(R.id.productNameInput)
        alarmDaysInput = findViewById(R.id.alarmDaysInput)
        searchProductInput = findViewById(R.id.searchProductInput)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        closeResultsButton = findViewById(R.id.closeResultsButton)

        homeSection = findViewById(R.id.homeSection)
        addSection = findViewById(R.id.addSection)
        searchSection = findViewById(R.id.searchSection)
        searchContent = findViewById(R.id.searchContent)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        homeQuickAddButton = findViewById(R.id.homeQuickAddButton)
        homeQuickSearchButton = findViewById(R.id.homeQuickSearchButton)
        homeStatsText = findViewById(R.id.homeStatsText)
        settingsManager = SettingsManager(this)

        settingsSection = findViewById(R.id.settingsSection)

        switchEnableAlerts = findViewById(R.id.switchEnableAlerts)
        btnDefaultAlertTime = findViewById(R.id.btnDefaultAlertTime)
        editDefaultReminderValue = findViewById(R.id.editDefaultReminderValue)
        spinnerDefaultReminderUnit = findViewById(R.id.spinnerDefaultReminderUnit)
        editRepeatValue = findViewById(R.id.editRepeatValue)
        spinnerRepeatUnit = findViewById(R.id.spinnerRepeatUnit)
        editExpiringSoonValue = findViewById(R.id.editExpiringSoonValue)
        spinnerExpiringSoonUnit = findViewById(R.id.spinnerExpiringSoonUnit)
        editAutoDeleteValue = findViewById(R.id.editAutoDeleteValue)
        spinnerAutoDeleteUnit = findViewById(R.id.spinnerAutoDeleteUnit)
        spinnerDateFormat = findViewById(R.id.spinnerDateFormat)
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

        createNotificationChannel()
        requestNotificationPermissionIfNeeded()
        dbHelper.writableDatabase
        cleanupOldExpiredProducts()

        btnOk.setOnClickListener {
            addProduct()
        }

        searchProductButton.setOnClickListener {
            hideResults()
            setSearchByNameInputVisible()
            setSearchProductInputHidden()
            setSearchRangeInputHidden()
        }

        searchProductOk.setOnClickListener {
            searchProduct()
        }

        searchDateButton.setOnClickListener {
            hideResults()
            setSearchProductInputVisible()
            setSearchByNameInputHidden()
            setSearchRangeInputHidden()
        }

        searchOk.setOnClickListener {
            searchByDate()
        }

        searchRangeButton.setOnClickListener {
            hideResults()
            setSearchRangeInputVisible()
            setSearchByNameInputHidden()
            setSearchProductInputHidden()
        }

        searchRangeOk.setOnClickListener {
            searchByDateRange()
        }

        deleteDateButton.setOnClickListener {
            hideResults()
            setDeleteProductInputVisible()
        }

        deleteOk.setOnClickListener {
            deleteByDate()
        }

        selectedExpiryDateText.setOnClickListener {
            showExpiryDatePickerDialog()
        }

        searchExpiryDateText.setOnClickListener {
            showExpirySearchDatePickerDialog()
        }

        deleteExpiryDateText.setOnClickListener {
            showDeleteDatePickerDialog()
        }

        closeResultsButton.setOnClickListener {
            hideResults()
        }

        homeQuickAddButton.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_add
        }

        homeQuickSearchButton.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_search
        }

        btnScanBarcode.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java).apply {
                putExtra("SCAN_MODE", ScannerActivity.SCAN_MODE_BARCODE)
            }
            scannerLauncher.launch(intent)
        }

        btnScanExpiry.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java).apply {
                putExtra("SCAN_MODE", ScannerActivity.SCAN_MODE_EXPIRY)
            }
            scannerLauncher.launch(intent)
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            hideResults()
            hideAllInputSections()

            when (item.itemId) {
                R.id.nav_home -> {
                    updateHomeStats()
                    showSection(homeSection)
                    true
                }
                R.id.nav_add -> {
                    loadDefaultsIntoAddProduct()
                    showSection(addSection)
                    true
                }
                R.id.nav_search -> {
                    showSection(searchSection)
                    true
                }
                R.id.nav_settings -> {
                    loadSettingsIntoUi()
                    showSection(settingsSection)
                    true
                }
                else -> false
            }
        }

        hideAllInputSections()
        hideResults()
        updateHomeStats()
        bottomNavigation.selectedItemId = R.id.nav_home
        setupSettingsSpinners()
        btnSaveSettings.setOnClickListener {
            saveSettingsFromUi()
        }
        btnDefaultAlertTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    currentAlertHour = hourOfDay
                    currentAlertMinute = minute
                    btnDefaultAlertTime.text = String.format(Locale.getDefault(), "%02d:%02d", currentAlertHour, currentAlertMinute)
                },
                currentAlertHour,
                currentAlertMinute,
                true
            ).show()
        }

        // Handle system back button navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (recyclerView.visibility == View.VISIBLE) {
                    // If results are shown, hide them first
                    hideResults()
                } else if (bottomNavigation.selectedItemId != R.id.nav_home) {
                    // If not on Home, go to Home
                    bottomNavigation.selectedItemId = R.id.nav_home
                } else {
                    // Already on Home, exit app
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun fetchProductNameFromBarcode(barcode: String) {
        val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
        val request = Request.Builder().url(url).build()

        productNameInput.setText("Searching...")

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    productNameInput.setText(barcode)
                    toast("API error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread { productNameInput.setText(barcode) }
                        return
                    }

                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val json = JSONObject(responseData)
                        val status = json.optInt("status")
                        if (status == 1) {
                            val product = json.optJSONObject("product")
                            val productName = product?.optString("product_name") ?: barcode
                            runOnUiThread {
                                productNameInput.setText(productName)
                                toast("Product found: $productName")
                            }
                        } else {
                            runOnUiThread {
                                productNameInput.setText(barcode)
                                toast("Product not found in database")
                            }
                        }
                    }
                }
            }
        })
    }

    private fun parseAndSetScannedDate(dateStr: String) {
        try {
            val parts = dateStr.split("/", "-", ".")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val year = parts[2].toInt()

                expiryDatePicker.updateDate(year, month, day)
                selectedExpiryDateText.text = formatDateText(year, month, day)
                toast("Expiry date scanned: $dateStr")
            }
        } catch (e: Exception) {
            toast("Could not parse date: $dateStr")
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showSection(section: View) {
        homeSection.visibility = View.GONE
        addSection.visibility = View.GONE
        searchSection.visibility = View.GONE
        settingsSection.visibility = View.GONE
        section.visibility = View.VISIBLE
    }

    private fun hideAllInputSections() {
        setSearchByNameInputHidden()
        setSearchProductInputHidden()
        setSearchRangeInputHidden()
        setDeleteProductInputHidden()
    }

    private fun setSearchProductInputHidden() {
        searchExpiryDateText.visibility = View.GONE
        searchOk.visibility = View.GONE
    }

    private fun setSearchProductInputVisible() {
        searchExpiryDateText.visibility = View.VISIBLE
        searchOk.visibility = View.VISIBLE
    }

    private fun setDeleteProductInputHidden() {
        deleteExpiryDateText.visibility = View.GONE
        deleteOk.visibility = View.GONE
    }

    private fun setDeleteProductInputVisible() {
        deleteExpiryDateText.visibility = View.VISIBLE
        deleteOk.visibility = View.VISIBLE
    }

    private fun setSearchByNameInputVisible() {
        searchProductInput.visibility = View.VISIBLE
        searchProductOk.visibility = View.VISIBLE
    }

    private fun setSearchByNameInputHidden() {
        searchProductInput.visibility = View.GONE
        searchProductOk.visibility = View.GONE
    }

    private fun setSearchRangeInputVisible() {
        startDatePicker.visibility = View.VISIBLE
        endDatePicker.visibility = View.VISIBLE
        searchRangeOk.visibility = View.VISIBLE
    }

    private fun setSearchRangeInputHidden() {
        startDatePicker.visibility = View.GONE
        endDatePicker.visibility = View.GONE
        searchRangeOk.visibility = View.GONE
    }

    private fun showExpiryDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedExpiryDateText.text =
                    formatDateText(selectedYear, selectedMonth, selectedDay)
                expiryDatePicker.updateDate(selectedYear, selectedMonth, selectedDay)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun showExpirySearchDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                searchExpiryDateText.text =
                    formatDateText(selectedYear, selectedMonth, selectedDay)
                searchDatePicker.updateDate(selectedYear, selectedMonth, selectedDay)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun showDeleteDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                deleteExpiryDateText.text =
                    formatDateText(selectedYear, selectedMonth, selectedDay)
                deleteDatePicker.updateDate(selectedYear, selectedMonth, selectedDay)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun formatDateText(year: Int, zeroBasedMonth: Int, day: Int): String {
        return String.format(
            Locale.getDefault(),
            "%02d/%02d/%04d",
            day,
            zeroBasedMonth + 1,
            year
        )
    }
    private fun setupSettingsSpinners() {
        val timeUnits = TimeUnitOption.entries.map { it.name }
        val dateFormats = DateFormatOption.entries.map { it.name }
        val sortOrders = SortOrderOption.entries.map { it.name }

        val timeUnitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeUnits)
        timeUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerDefaultReminderUnit.adapter = timeUnitAdapter
        spinnerRepeatUnit.adapter = timeUnitAdapter
        spinnerExpiringSoonUnit.adapter = timeUnitAdapter
        spinnerAutoDeleteUnit.adapter = timeUnitAdapter

        val dateFormatAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateFormats)
        dateFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDateFormat.adapter = dateFormatAdapter

        val sortOrderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOrders)
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSortOrder.adapter = sortOrderAdapter
    }
    private fun loadSettingsIntoUi() {
        val settings = settingsManager.loadSettings()

        switchEnableAlerts.isChecked = settings.enableExpiryAlerts

        currentAlertHour = settings.defaultAlertHour
        currentAlertMinute = settings.defaultAlertMinute
        btnDefaultAlertTime.text = String.format(Locale.getDefault(), "%02d:%02d", currentAlertHour, currentAlertMinute)

        editDefaultReminderValue.setText(settings.defaultReminderLeadTimeValue.toString())
        spinnerDefaultReminderUnit.setSelection(TimeUnitOption.entries.indexOf(settings.defaultReminderLeadTimeUnit))

        editRepeatValue.setText(settings.repeatIntervalValue.toString())
        spinnerRepeatUnit.setSelection(TimeUnitOption.entries.indexOf(settings.repeatIntervalUnit))

        editExpiringSoonValue.setText(settings.expiringSoonThresholdValue.toString())
        spinnerExpiringSoonUnit.setSelection(TimeUnitOption.entries.indexOf(settings.expiringSoonThresholdUnit))

        editAutoDeleteValue.setText(settings.autoDeleteExpiredValue.toString())
        spinnerAutoDeleteUnit.setSelection(TimeUnitOption.entries.indexOf(settings.autoDeleteExpiredUnit))

        spinnerDateFormat.setSelection(DateFormatOption.entries.indexOf(settings.dateFormat))
        spinnerSortOrder.setSelection(SortOrderOption.entries.indexOf(settings.sortOrder))
    }
    private fun saveSettingsFromUi() {
        val settings = AppSettings(
            enableExpiryAlerts = switchEnableAlerts.isChecked,
            defaultAlertHour = currentAlertHour,
            defaultAlertMinute = currentAlertMinute,
            defaultReminderLeadTimeValue = editDefaultReminderValue.text.toString().toIntOrNull() ?: 2,
            defaultReminderLeadTimeUnit = TimeUnitOption.entries[spinnerDefaultReminderUnit.selectedItemPosition],
            repeatIntervalValue = editRepeatValue.text.toString().toIntOrNull() ?: 0,
            repeatIntervalUnit = TimeUnitOption.entries[spinnerRepeatUnit.selectedItemPosition],
            expiringSoonThresholdValue = editExpiringSoonValue.text.toString().toIntOrNull() ?: 3,
            expiringSoonThresholdUnit = TimeUnitOption.entries[spinnerExpiringSoonUnit.selectedItemPosition],
            autoDeleteExpiredValue = editAutoDeleteValue.text.toString().toIntOrNull() ?: 7,
            autoDeleteExpiredUnit = TimeUnitOption.entries[spinnerAutoDeleteUnit.selectedItemPosition],
            dateFormat = DateFormatOption.entries[spinnerDateFormat.selectedItemPosition],
            sortOrder = SortOrderOption.entries[spinnerSortOrder.selectedItemPosition]
        )

        settingsManager.saveSettings(settings)
        toast("Settings saved")
        updateHomeStats()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMillisForMidnight(year: Int, zeroBasedMonth: Int, day: Int): Long {
        val localDate = LocalDate.of(year, zeroBasedMonth + 1, day)
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addProduct() {
        val name = productNameInput.text.toString().trim()
        val alarmDaysText = alarmDaysInput.text.toString().trim()

        if (name.isBlank()) {
            toast("Please enter product name")
            return
        }

        if (alarmDaysText.isBlank()) {
            toast("Please enter alarm days")
            return
        }

        val alarmDays = alarmDaysText.toLongOrNull()
        if (alarmDays == null || alarmDays < 0) {
            toast("Please enter a valid number of alarm days")
            return
        }

        val expiryDate = getMillisForMidnight(
            expiryDatePicker.year,
            expiryDatePicker.month,
            expiryDatePicker.dayOfMonth
        )

        val today = getMillisForMidnight(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        if (expiryDate < today) {
            toast("Expiry date cannot be in the past")
            return
        }

        val alarmDate = expiryDate - (alarmDays * 24L * 60L * 60L * 1000L)

        val insertedId = dbHelper.addProduct(name, expiryDate, alarmDate)

        if (insertedId == -1L) {
            toast("Failed to save product")
            return
        }

        setAlarm(
            productId = insertedId,
            productName = name,
            expiryDate = expiryDate,
            alarmDate = alarmDate
        )

        toast("Product added")

        productNameInput.text.clear()
        alarmDaysInput.text.clear()
        selectedExpiryDateText.text = "Select expiry date"

        updateHomeStats()
    }

    private fun searchProduct() {
        val name = searchProductInput.text.toString().trim()

        if (name.isBlank()) {
            toast("Please enter product name")
            return
        }

        val products = dbHelper.getProductsByName(name)

        if (products.isEmpty()) {
            toast("No product found")
            return
        }

        showResults(sortProducts(products))
        searchProductInput.text.clear()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun searchByDate() {
        val searchDate = getMillisForMidnight(
            searchDatePicker.year,
            searchDatePicker.month,
            searchDatePicker.dayOfMonth
        )

        val products = dbHelper.getProductsByExpiryDate(searchDate)

        if (products.isEmpty()) {
            toast("No products found for the selected expiry date.")
            return
        }

        showResults(sortProducts(products))
    }

    private fun searchByDateRange() {
        val startDate = Calendar.getInstance().apply {
            set(startDatePicker.year, startDatePicker.month, startDatePicker.dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endDate = Calendar.getInstance().apply {
            set(endDatePicker.year, endDatePicker.month, endDatePicker.dayOfMonth, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        if (startDate > endDate) {
            toast("Start date cannot be after end date")
            return
        }

        val products = dbHelper.getProductsByExpiryDateRange(startDate, endDate)

        if (products.isEmpty()) {
            toast("No products found in the selected range.")
            return
        }

        showResults(sortProducts(products))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteByDate() {
        val deleteDate = getMillisForMidnight(
            deleteDatePicker.year,
            deleteDatePicker.month,
            deleteDatePicker.dayOfMonth
        )

        val deletedRows = dbHelper.delProductsByExpiryDate(deleteDate)

        toast("$deletedRows product(s) deleted")
        updateHomeStats()
    }

    private fun setAlarm(productId: Long, productName: String, expiryDate: Long, alarmDate: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = productId.toInt()
        val notificationId = productId.toInt()

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("productName", productName)
            putExtra("expiryDate", expiryDate)
            putExtra("notificationId", notificationId)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            flags
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmDate,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmDate,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmDate,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Alarm could not be scheduled: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "PRODUCT_EXPIRY_CHANNEL",
                "ProductExpiryChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Product Expiry Notifications"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getExpiryStatus(expiryMillis: Long): String {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diff = expiryMillis - today
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            days < 0 -> "❌ Expired"
            days == 0L -> "⚠️ Expires today"
            days <= 3 -> "⚠️ $days day(s) left"
            else -> "✅ $days day(s) left"
        }
    }

    private fun getDaysLeft(expiryMillis: Long): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return (expiryMillis - today) / (24L * 60L * 60L * 1000L)
    }

    private fun updateHomeStats() {
        val settings = settingsManager.loadSettings()
        val thresholdMillis = convertToMillis(
            settings.expiringSoonThresholdValue,
            settings.expiringSoonThresholdUnit
        )

        val allProducts = dbHelper.getAllProducts()
        val total = allProducts.size
        val expired = allProducts.count { it.expiryDate < System.currentTimeMillis() }
        val expiringSoon = allProducts.count {
            val diff = it.expiryDate - System.currentTimeMillis()
            diff in 0..thresholdMillis
        }

        homeStatsText.text = "Total products: $total\nExpired: $expired\nExpiring soon: $expiringSoon"
    }
    private fun loadDefaultsIntoAddProduct() {
        val settings = settingsManager.loadSettings()
        alarmDaysInput.setText(
            if (settings.defaultReminderLeadTimeUnit == TimeUnitOption.DAYS)
                settings.defaultReminderLeadTimeValue.toString()
            else
                ""
        )
    }
    private fun cleanupOldExpiredProducts() {
        val settings = settingsManager.loadSettings()
        val retentionMillis = convertToMillis(
            settings.autoDeleteExpiredValue,
            settings.autoDeleteExpiredUnit
        )

        val cutoffMillis = System.currentTimeMillis() - retentionMillis
        val deletedRows = dbHelper.deleteExpiredProductsOlderThan(cutoffMillis)

        if (deletedRows > 0) {
            toast("Auto-cleaned $deletedRows old expired product(s)")
        }
    }
    private fun sortProducts(products: List<Product>): List<Product> {
        return when (settingsManager.loadSettings().sortOrder) {
            SortOrderOption.EXPIRY_ASC -> products.sortedBy { it.expiryDate }
            SortOrderOption.NAME_ASC -> products.sortedBy { it.name.lowercase(Locale.getDefault()) }
            SortOrderOption.RECENTLY_ADDED -> products.sortedByDescending { it.id }
        }
    }
    private fun showResults(products: List<Product>) {
        searchContent.visibility = View.GONE
        recyclerView.adapter = ProductResultAdapter(products)
        recyclerView.visibility = View.VISIBLE
        closeResultsButton.visibility = View.VISIBLE
    }

    private fun hideResults() {
        searchContent.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        closeResultsButton.visibility = View.GONE
        recyclerView.adapter = null
    }

    private fun formatMillis(timeMillis: Long): String {
        val settings = settingsManager.loadSettings()
        val pattern = when (settings.dateFormat) {
            DateFormatOption.DD_MM_YYYY -> "dd/MM/yyyy"
            DateFormatOption.MM_DD_YYYY -> "MM/dd/yyyy"
            DateFormatOption.YYYY_MM_DD -> "yyyy-MM-dd"
        }

        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeMillis))
    }
    private fun convertToMillis(value: Int, unit: TimeUnitOption): Long {
        return when (unit) {
            TimeUnitOption.MINUTES -> value * 60_000L
            TimeUnitOption.HOURS -> value * 3_600_000L
            TimeUnitOption.DAYS -> value * 86_400_000L
            TimeUnitOption.WEEKS -> value * 7L * 86_400_000L
            TimeUnitOption.MONTHS -> value * 30L * 86_400_000L
            TimeUnitOption.YEARS -> value * 365L * 86_400_000L
        }
    }
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}