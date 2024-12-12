package com.example.project_kelompok3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private var calendar: Calendar,
    private val onDateSelected: (Date) -> Unit, // Callback untuk tanggal yang dipilih
    private val onMonthChanged: (Calendar) -> Unit, // Callback untuk bulan yang berubah
    private var notesByDate: Map<String, List<String>> = emptyMap() // Notes grouped by date
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = mutableListOf<Date>()
    private var selectedDate: Date? = null // Melacak tanggal yang dipilih

    init {
        generateDays()
    }

    private fun generateDays() {
        days.clear()
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        // Geser ke awal minggu
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1
        tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        // Tambahkan 6 minggu (42 hari)
        for (i in 0 until 42) {
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

        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        // Highlight untuk tanggal hari ini
        val today = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        if (dateFormat.format(date) == dateFormat.format(today)) {
            holder.dayTextView.setBackgroundResource(R.drawable.calendar_day_background_today)
        } else if (notesByDate.containsKey(dateKey)) {
            holder.dayTextView.setBackgroundResource(R.drawable.calendar_day_background_with_notes)
        } else if (selectedDate != null && dateFormat.format(date) == dateFormat.format(selectedDate)) {
            holder.dayTextView.setBackgroundResource(R.drawable.calendar_day_background_selected)
        } else {
            holder.dayTextView.setBackgroundResource(R.drawable.calendar_day_background_default)
        }

        // Klik untuk memilih tanggal
        holder.itemView.setOnClickListener {
            val clickedCalendar = Calendar.getInstance().apply { time = date }
            if (clickedCalendar.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
                // Jika tanggal berada di bulan sebelumnya atau berikutnya
                calendar.time = date
                onMonthChanged(calendar) // Callback untuk perubahan bulan
                generateDays()
                notifyDataSetChanged()
            } else {
                // Pilih tanggal di bulan saat ini
                val previousSelectedDate = selectedDate
                selectedDate = date
                notifyItemChanged(days.indexOf(previousSelectedDate)) // Refresh tanggal sebelumnya
                notifyItemChanged(position) // Refresh tanggal yang dipilih
                onDateSelected(date) // Kirim tanggal yang dipilih ke Fragment
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateCalendar(newCalendar: Calendar) {
        calendar = newCalendar
        generateDays()
        notifyDataSetChanged()
        onMonthChanged(calendar) // Callback untuk memperbarui tampilan bulan di Fragment
    }

    fun updateNotes(newNotesByDate: Map<String, List<String>>) {
        notesByDate = newNotesByDate
        notifyDataSetChanged()
    }

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTextView: TextView = view.findViewById(R.id.tv_day)
    }
}
