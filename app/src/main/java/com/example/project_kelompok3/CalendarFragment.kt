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
    private var notesByDate: Map<String, List<String>> = emptyMap() // Notes grouped by date
    private lateinit var tvNotesSection: TextView // TextView for displaying notes below the calendar

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
        tvNotesSection = view.findViewById(R.id.tv_notes_section) // Find the notes section TextView

        // Set today’s date
        tvTodayDate.text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(calendar.time)

        // Set month and year
        tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        // Initialize RecyclerView
        calendarAdapter = CalendarAdapter(calendar, onDateSelected = { date ->
            // Show notes for selected date
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            val notes = notesByDate[selectedDate] ?: listOf("No notes for this date")
            displayNotes(selectedDate, notes)
        }, onMonthChanged = {
            tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(it.time)
        }, notesByDate)

        recyclerView.layoutManager = GridLayoutManager(context, 7) // 7 columns for days
        recyclerView.adapter = calendarAdapter

        // Navigate to previous month
        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendarAdapter.updateCalendar(calendar)
        }

        // Navigate to next month
        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendarAdapter.updateCalendar(calendar)
        }

        return view
    }

    // Function to set notes data from HomeFragment
    fun setNotesData(notes: Map<String, List<Task>>) {
        // Convert tasks to a list of titles grouped by date
        notesByDate = notes.mapValues { entry ->
            entry.value.map { it.title }
        }
        calendarAdapter.updateNotes(notesByDate) // Update calendar adapter with new data
    }

    // Function to display notes below the calendar
    private fun displayNotes(date: String, notes: List<String>) {
        val notesText = "Notes for $date:\n" + notes.joinToString(separator = "\n")
        tvNotesSection.text = notesText
        tvNotesSection.visibility = View.VISIBLE
        }
}