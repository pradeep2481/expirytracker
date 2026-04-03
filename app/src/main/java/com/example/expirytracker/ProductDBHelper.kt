package com.example.expirytracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

data class Product(
    val id: Long,
    val name: String,
    val expiryDate: Long,
    val alarmDate: Long
)

class ProductDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ProductExpiry.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "products"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EXPIRY_DATE = "expiry_date"
        private const val COLUMN_ALARM_DATE = "alarm_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EXPIRY_DATE INTEGER NOT NULL,
                $COLUMN_ALARM_DATE INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addProduct(name: String, expiryDate: Long, alarmDate: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EXPIRY_DATE, expiryDate)
            put(COLUMN_ALARM_DATE, alarmDate)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getProductsByName(name: String): List<Product> {
        val db = readableDatabase
        val products = mutableListOf<Product>()

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EXPIRY_DATE, COLUMN_ALARM_DATE),
            "$COLUMN_NAME = ?",
            arrayOf(name),
            null,
            null,
            "$COLUMN_EXPIRY_DATE ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    Product(
                        id = it.getLong(0),
                        name = it.getString(1),
                        expiryDate = it.getLong(2),
                        alarmDate = it.getLong(3)
                    )
                )
            }
        }

        return products
    }

    fun getProductsByExpiryDate(date: Long): List<Product> {
        val db = readableDatabase
        val products = mutableListOf<Product>()

        val calStart = Calendar.getInstance()
        calStart.timeInMillis = date
        calStart.set(Calendar.HOUR_OF_DAY, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)
        calStart.set(Calendar.MILLISECOND, 0)
        val startOfDay = calStart.timeInMillis

        val calEnd = Calendar.getInstance()
        calEnd.timeInMillis = date
        calEnd.set(Calendar.HOUR_OF_DAY, 23)
        calEnd.set(Calendar.MINUTE, 59)
        calEnd.set(Calendar.SECOND, 59)
        calEnd.set(Calendar.MILLISECOND, 999)
        val endOfDay = calEnd.timeInMillis

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EXPIRY_DATE, COLUMN_ALARM_DATE),
            "$COLUMN_EXPIRY_DATE BETWEEN ? AND ?",
            arrayOf(startOfDay.toString(), endOfDay.toString()),
            null,
            null,
            "$COLUMN_EXPIRY_DATE ASC, $COLUMN_NAME ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    Product(
                        id = it.getLong(0),
                        name = it.getString(1),
                        expiryDate = it.getLong(2),
                        alarmDate = it.getLong(3)
                    )
                )
            }
        }

        return products
    }

    fun getProductsByExpiryDateRange(startDate: Long, endDate: Long): List<Product> {
        val db = readableDatabase
        val products = mutableListOf<Product>()

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EXPIRY_DATE, COLUMN_ALARM_DATE),
            "$COLUMN_EXPIRY_DATE BETWEEN ? AND ?",
            arrayOf(startDate.toString(), endDate.toString()),
            null,
            null,
            "$COLUMN_EXPIRY_DATE ASC, $COLUMN_NAME ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    Product(
                        id = it.getLong(0),
                        name = it.getString(1),
                        expiryDate = it.getLong(2),
                        alarmDate = it.getLong(3)
                    )
                )
            }
        }

        return products
    }

    fun getAllProducts(): List<Product> {
        val db = readableDatabase
        val products = mutableListOf<Product>()

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EXPIRY_DATE, COLUMN_ALARM_DATE),
            null,
            null,
            null,
            null,
            "$COLUMN_EXPIRY_DATE ASC, $COLUMN_NAME ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(
                    Product(
                        id = it.getLong(0),
                        name = it.getString(1),
                        expiryDate = it.getLong(2),
                        alarmDate = it.getLong(3)
                    )
                )
            }
        }

        return products
    }

    fun delProductsByExpiryDate(date: Long): Int {
        val db = writableDatabase

        val calStart = Calendar.getInstance()
        calStart.timeInMillis = date
        calStart.set(Calendar.HOUR_OF_DAY, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)
        calStart.set(Calendar.MILLISECOND, 0)
        val startOfDay = calStart.timeInMillis

        val calEnd = Calendar.getInstance()
        calEnd.timeInMillis = date
        calEnd.set(Calendar.HOUR_OF_DAY, 23)
        calEnd.set(Calendar.MINUTE, 59)
        calEnd.set(Calendar.SECOND, 59)
        calEnd.set(Calendar.MILLISECOND, 999)
        val endOfDay = calEnd.timeInMillis

        return db.delete(
            TABLE_NAME,
            "$COLUMN_EXPIRY_DATE BETWEEN ? AND ?",
            arrayOf(startOfDay.toString(), endOfDay.toString())
        )
    }

    fun deleteExpiredProductsOlderThan(cutoffMillis: Long): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_NAME,
            "$COLUMN_EXPIRY_DATE < ?",
            arrayOf(cutoffMillis.toString())
        )
    }
}