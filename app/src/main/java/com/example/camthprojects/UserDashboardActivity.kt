package com.example.camthprojects

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.camthprojects.models.Project
import com.example.camthprojects.models.ServiceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var projectListLayout: LinearLayout
    private lateinit var requestListLayout: LinearLayout
    private lateinit var btnRequestService: Button
    private lateinit var db: AppDatabase
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "User Dashboard"

        projectListLayout = findViewById(R.id.projectList)
        requestListLayout = findViewById(R.id.requestList)
        btnRequestService = findViewById(R.id.btnRequestService)

        db = AppDatabase.getDatabase(this)
        currentUserId = intent.getStringExtra("USER_ID") ?: ""

        btnRequestService.setOnClickListener { showRequestServiceDialog() }
        createNotificationChannel()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadDashboard() {
        loadProjects()
        loadRequests()
    }

    private fun loadProjects() = lifecycleScope.launch(Dispatchers.IO) {
        val projects = db.projectDao().getAll().filter { it.assignedUserId == currentUserId }
        withContext(Dispatchers.Main) {
            projectListLayout.removeAllViews()
            if (projects.isEmpty()) {
                projectListLayout.addView(TextView(this@UserDashboardActivity).apply { text = "No projects assigned."; textSize = 16f })
            } else {
                projects.forEach { project ->
                    val card = CardView(this@UserDashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 0, 0, 16) }
                        radius = 16f
                        setContentPadding(32, 32, 32, 32)
                        setOnClickListener { showProjectDetailsDialog(project) }

                        val layout = LinearLayout(this@UserDashboardActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            addView(TextView(this@UserDashboardActivity).apply { text = project.name; textSize = 20f; setTypeface(null, Typeface.BOLD) })
                            addView(TextView(this@UserDashboardActivity).apply { text = "Status: ${project.status}"; textSize = 16f; setTypeface(null, Typeface.ITALIC); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                        }
                        addView(layout)
                    }
                    projectListLayout.addView(card)
                }
            }
        }
    }

    private fun showProjectDetailsDialog(project: Project) {
        val detailsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)

            addView(TextView(this@UserDashboardActivity).apply { text = "Project Name: ${project.name}"; textSize = 18f; setTypeface(null, Typeface.BOLD) })
            addView(TextView(this@UserDashboardActivity).apply { text = "Description: ${project.description}"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
            addView(TextView(this@UserDashboardActivity).apply { text = "Status: ${project.status}"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
            addView(TextView(this@UserDashboardActivity).apply { text = "Start Date: ${project.startDate}"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
            addView(TextView(this@UserDashboardActivity).apply { text = "End Date: ${project.endDate}"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
        }

        AlertDialog.Builder(this)
            .setTitle("Project Details")
            .setIcon(R.drawable.logo_camth)
            .setView(detailsLayout)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun loadRequests() = lifecycleScope.launch(Dispatchers.IO) {
        val requests = db.serviceRequestDao().getAll().filter { it.userId == currentUserId }
        withContext(Dispatchers.Main) {
            requestListLayout.removeAllViews()
            if (requests.isEmpty()) {
                requestListLayout.addView(TextView(this@UserDashboardActivity).apply { text = "No service requests submitted."; textSize = 16f })
            } else {
                requests.forEach { request ->
                    val card = CardView(this@UserDashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                        radius = 16f
                        setContentPadding(32, 32, 32, 32)

                        val layout = LinearLayout(this@UserDashboardActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            addView(TextView(this@UserDashboardActivity).apply { text = request.title; textSize = 20f; setTypeface(null, Typeface.BOLD) })
                            addView(TextView(this@UserDashboardActivity).apply { text = "Approval: ${request.approvalStatus}"; textSize = 16f; setTypeface(null, Typeface.ITALIC); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                        }
                        addView(layout)
                    }
                    requestListLayout.addView(card)
                }
            }
        }
    }

    private fun showRequestServiceDialog() {
        val services = arrayOf(
            "Select a Service",
            "Electrical Cables Upgrades",
            "Code Violation Repair / Home Sale Inspection Repair",
            "Modernization of Electrical Installation in Transformers",
            "Installation & Maintenance of Street Lights",
            "Laying of MV & LV Cables",
            "Installation & Maintenance of Mini Substations",
            "Installation & Maintenance of Meters in Pillar Boxes",
            "Stringing Overhead Contractors"
        )

        val spServices = Spinner(this).apply {
            adapter = ArrayAdapter(this@UserDashboardActivity, android.R.layout.simple_spinner_dropdown_item, services)
        }
        val etDesc = EditText(this).apply { hint = "Brief description of the service needed"; textSize = 18f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 } }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            addView(spServices)
            addView(etDesc)
        }

        AlertDialog.Builder(this)
            .setTitle("New Service Request")
            .setIcon(R.drawable.logo_camth)
            .setView(layout)
            .setPositiveButton("Submit") { _, _ ->
                val selectedService = spServices.selectedItem.toString()
                val description = etDesc.text.toString()
                if (selectedService != services[0] && description.isNotBlank()) {
                    val request = ServiceRequest(id = UUID.randomUUID().toString(), userId = currentUserId, title = selectedService, description = description)
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.serviceRequestDao().insert(request)
                        withContext(Dispatchers.Main) {
                            sendNotificationToAdmins("New Service Request", "A new service request has been submitted: '$selectedService'")
                            loadRequests()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SERVICE_REQUESTS", "Service Requests", NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun sendNotificationToAdmins(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "SERVICE_REQUESTS").setSmallIcon(R.drawable.logo_camth).setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_DEFAULT)
        getSystemService(NotificationManager::class.java).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}