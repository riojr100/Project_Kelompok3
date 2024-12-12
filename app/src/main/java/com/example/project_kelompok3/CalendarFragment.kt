package com.example.project_kelompok3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val tvMonthYear = view.findViewById<TextView>(R.id.tv_month_year)
        val tvTodayDate = view.findViewById<TextView>(R.id.tv_today_date)
        val btnPrevMonth = view.findViewById<View>(R.id.btn_prev_month)
        val btnNextMonth = view.findViewById<View>(R.id.btn_next_month)
        val recyclerView = view.findViewById<RecyclerView>(R.id.calendar_recycler_view)

        // Set hari ini
        tvTodayDate.text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(calendar.time)

        // Set bulan dan tahun
        tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        // Inisialisasi RecyclerView
        calendarAdapter = CalendarAdapter(calendar, onDateSelected = {
            // Tidak melakukan apa-apa karena bagian bawah sudah dihapus
        }, onMonthChanged = {
            tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(it.time)
        })

        recyclerView.layoutManager = GridLayoutManager(context, 7) // 7 kolom untuk hari
        recyclerView.adapter = calendarAdapter

        // Navigasi Bulan Sebelumnya
        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendarAdapter.updateCalendar(calendar)
        }

        // Navigasi Bulan Berikutnya
        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendarAdapter.updateCalendar(calendar)
        }

        return view
    }
}
