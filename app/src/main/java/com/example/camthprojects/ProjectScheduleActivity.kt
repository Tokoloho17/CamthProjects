package com.example.camthprojects

import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ProjectScheduleActivity : AppCompatActivity() {

    private lateinit var scheduleListLayout: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_schedule)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Schedule"

        scheduleListLayout = findViewById(R.id.scheduleList)
        db = AppDatabase.getDatabase(this)

        loadSchedule()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadSchedule() = lifecycleScope.launch(Dispatchers.IO) {
        val projects = db.projectDao().getAll()
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val sortedProjects = projects.sortedBy { project ->
            try {
                dateFormat.parse(project.endDate)
            } catch (e: Exception) {
                null
            }
        }

        withContext(Dispatchers.Main) {
            scheduleListLayout.removeAllViews()
            sortedProjects.forEach { project ->
                val card = CardView(this@ProjectScheduleActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                    radius = 16f
                    setContentPadding(32, 32, 32, 32)

                    val layout = LinearLayout(this@ProjectScheduleActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        addView(TextView(this@ProjectScheduleActivity).apply { text = project.name; textSize = 20f; setTypeface(null, Typeface.BOLD) })
                        addView(TextView(this@ProjectScheduleActivity).apply { text = "Due: ${project.endDate}"; textSize = 16f; setTypeface(null, Typeface.ITALIC); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                    }
                    addView(layout)
                }
                scheduleListLayout.addView(card)
            }
        }
    }
}