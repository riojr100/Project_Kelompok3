package com.example.project_kelompok3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val btnPrevMonth = view.findViewById<ImageView>(R.id.btn_prev_month)
        val btnNextMonth = view.findViewById<ImageView>(R.id.btn_next_month)
        val recyclerView = view.findViewById<RecyclerView>(R.id.calendar_recycler_view)

        // Set the current month and year
        tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        // Initialize RecyclerView
        calendarAdapter = CalendarAdapter(calendar)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 7 columns for days of the week
        recyclerView.adapter = calendarAdapter

        // Navigate to previous month
        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar(tvMonthYear)
        }

        // Navigate to next month
        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar(tvMonthYear)
        }

        return view
    }

    private fun updateCalendar(tvMonthYear: TextView) {
        tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        calendarAdapter.updateCalendar(calendar)
    }
}
