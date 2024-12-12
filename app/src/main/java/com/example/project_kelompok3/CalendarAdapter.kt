package com.example.project_kelompok3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val calendar: Calendar) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = mutableListOf<Date>()

    init {
        generateDays()
    }

    private fun generateDays() {
        days.clear()
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1
        tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        for (i in 0 until 42) { // 6 weeks view
            days.add(tempCalendar.time)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        val day = SimpleDateFormat("d", Locale.getDefault()).format(date)
        holder.dayTextView.text = day

        // Highlight today's date
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        if (dateFormat.format(date) == dateFormat.format(today)) {
            holder.dayTextView.setBackgroundResource(R.drawable.calendar_day_background)
        } else {
            holder.dayTextView.setBackgroundResource(0)
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateCalendar(newCalendar: Calendar) {
        calendar.time = newCalendar.time
        generateDays()
        notifyDataSetChanged()
    }

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTextView: TextView = view.findViewById(R.id.tv_day)
    }
}
