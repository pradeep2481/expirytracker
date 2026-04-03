package com.example.expirytracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProductResultAdapter(
    private val items: List<Product>
) : RecyclerView.Adapter<ProductResultAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemContainer: LinearLayout = view.findViewById(R.id.itemContainer)
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvExpiry: TextView = view.findViewById(R.id.tvExpiry)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_result, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = items[position]
        val status = getExpiryStatus(item.expiryDate)

        holder.tvId.text = "ID: ${item.id}"
        holder.tvName.text = "Name: ${item.name}"
        holder.tvExpiry.text = "Expiry: ${formatMillis(item.expiryDate)}"
        holder.tvStatus.text = "Status: $status"

        holder.itemContainer.setBackgroundColor(
            when {
                status.startsWith("Expired") -> Color.parseColor("#FFCDD2")   // light red
                status.startsWith("Expires today") || status.contains("day(s) left") && getDaysLeft(item.expiryDate) <= 3 ->
                    Color.parseColor("#FFE0B2") // light orange
                else -> Color.parseColor("#C8E6C9") // light green
            }
        )
    }

    override fun getItemCount(): Int = items.size

    private fun formatMillis(timeMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timeMillis))
    }

    private fun getDaysLeft(expiryMillis: Long): Long {
        val calToday = Calendar.getInstance()
        calToday.set(Calendar.HOUR_OF_DAY, 0)
        calToday.set(Calendar.MINUTE, 0)
        calToday.set(Calendar.SECOND, 0)
        calToday.set(Calendar.MILLISECOND, 0)
        val today = calToday.timeInMillis

        return (expiryMillis - today) / (24L * 60L * 60L * 1000L)
    }

    private fun getExpiryStatus(expiryMillis: Long): String {
        val days = getDaysLeft(expiryMillis)
        return when {
            days < 0 -> "Expired"
            days == 0L -> "Expires today"
            else -> "$days day(s) left"
        }
    }
}
