package com.example.camthprojects

import android.os.Build
import android.os.Bundle
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.camthprojects.models.Project
import java.text.SimpleDateFormat
import java.util.*

class ProjectScheduleActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var projectDetailsLayout: LinearLayout
    private var projects: ArrayList<Project> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_schedule)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Schedule"

        calendarView = findViewById(R.id.calendarView)
        projectDetailsLayout = findViewById(R.id.projectDetailsLayout)

        projects = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("PROJECTS_LIST", Project::class.java)
        } else {
            intent.getParcelableArrayListExtra("PROJECTS_LIST")
        } ?: arrayListOf()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDateStr = "$dayOfMonth/${month + 1}/$year"
            updateProjectDetails(selectedDateStr)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun updateProjectDetails(selectedDate: String) {
        projectDetailsLayout.removeAllViews()
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val selectedCal = Calendar.getInstance().apply {
            time = sdf.parse(selectedDate) ?: Date()
        }

        val projectsOnDate = projects.filter { project ->
            try {
                val startDate = sdf.parse(project.startDate)
                val endDate = sdf.parse(project.endDate)
                startDate != null && endDate != null &&
                        (selectedCal.time.after(startDate) || selectedCal.time.equals(startDate)) &&
                        (selectedCal.time.before(endDate) || selectedCal.time.equals(endDate))
            } catch (e: Exception) {
                false
            }
        }

        if (projectsOnDate.isEmpty()) {
            projectDetailsLayout.addView(TextView(this).apply { text = "No projects scheduled for this date." })
        } else {
            projectsOnDate.forEach { project ->
                projectDetailsLayout.addView(TextView(this).apply { text = "${project.name} (${project.status})" })
            }
        }
    }
}