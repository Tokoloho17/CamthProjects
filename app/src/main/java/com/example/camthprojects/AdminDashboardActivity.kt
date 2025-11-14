package com.example.camthprojects

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.example.camthprojects.models.Project
import com.example.camthprojects.models.ServiceRequest
import com.example.camthprojects.models.User
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var userListLayout: LinearLayout
    private lateinit var projectListLayout: LinearLayout
    private lateinit var requestListLayout: LinearLayout
    private lateinit var btnAddProject: Button
    private lateinit var btnProjectSchedule: Button
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvOngoingProjects: TextView
    private lateinit var tvPendingRequests: TextView
    private lateinit var etProjectSearch: EditText
    private lateinit var etRequestSearch: EditText

    private var allUsers: List<User> = emptyList()
    private var allProjects: List<Project> = emptyList()
    private var allRequests: List<ServiceRequest> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Dashboard"

        userListLayout = findViewById(R.id.userList)
        projectListLayout = findViewById(R.id.projectList)
        requestListLayout = findViewById(R.id.requestList)
        btnAddProject = findViewById(R.id.btnAddProject)
        btnProjectSchedule = findViewById(R.id.btnProjectSchedule)
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvOngoingProjects = findViewById(R.id.tvOngoingProjects)
        tvPendingRequests = findViewById(R.id.tvPendingRequests)
        etProjectSearch = findViewById(R.id.etProjectSearch)
        etRequestSearch = findViewById(R.id.etRequestSearch)

        loadDashboard()

        btnAddProject.setOnClickListener { showProjectDialog() }
        btnProjectSchedule.setOnClickListener { 
            val intent = Intent(this, ProjectScheduleActivity::class.java)
            // Pass the projects list to the schedule activity
            intent.putParcelableArrayListExtra("PROJECTS_LIST", ArrayList(allProjects))
            startActivity(intent)
        }
        createNotificationChannel()

        etProjectSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { loadProjectsUI(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etRequestSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { loadRequestsUI(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    override fun onResume() {
        super.onResume()
        // Refresh the dashboard every time the screen is shown
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
        InMemoryDataStore.clearAll()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadDashboard() {
        allUsers = InMemoryDataStore.users
        allProjects = InMemoryDataStore.projects
        allRequests = InMemoryDataStore.serviceRequests

        loadUsersUI()
        loadProjectsUI()
        loadRequestsUI()
        updateAnalyticsUI()
        checkProjectDeadlines()
    }

    private fun checkProjectDeadlines() {
        val today = Calendar.getInstance()
        val upcoming = mutableListOf<String>()
        val overdue = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

        allProjects.forEach { project ->
            try {
                val endDate = dateFormat.parse(project.endDate)
                if (endDate != null) {
                    val diff = (endDate.time - today.time.time) / (1000 * 60 * 60 * 24)
                    if (diff in 0..7) {
                        upcoming.add(project.name)
                    } else if (diff < 0) {
                        overdue.add(project.name)
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboardActivity", "Error parsing date for project: ${project.name}", e)
            }
        }

        if (upcoming.isNotEmpty() || overdue.isNotEmpty()) {
            val message = "Upcoming: ${upcoming.joinToString()}\nOverdue: ${overdue.joinToString()}"
            sendNotification("Project Deadline Alert", message)
        }
    }

    private fun updateAnalyticsUI() {
        tvTotalUsers.text = "Users: ${allUsers.size}"
        tvOngoingProjects.text = "Ongoing: ${allProjects.count { it.status == "In Progress" }}"
        tvPendingRequests.text = "Pending: ${allRequests.count { it.approvalStatus == "Pending" }}"
    }

    private fun loadUsersUI() {
        userListLayout.removeAllViews()
        allUsers.forEach { user ->
            val card = CardView(this@AdminDashboardActivity).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                radius = 16f
                setContentPadding(32, 32, 32, 32)
                setOnClickListener { showUserOptions(user) }

                val layout = LinearLayout(this@AdminDashboardActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(this@AdminDashboardActivity).apply { text = user.fullName; textSize = 20f; setTypeface(null, Typeface.BOLD) })
                    addView(TextView(this@AdminDashboardActivity).apply { text = user.email; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                    addView(TextView(this@AdminDashboardActivity).apply { text = "Role: ${user.role}"; textSize = 16f; setTypeface(null, Typeface.ITALIC); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                }
                addView(layout)
            }
            userListLayout.addView(card)
        }
    }

    private fun showUserOptions(user: User) {
        val options = arrayOf("Update Role", "Delete User")
        AlertDialog.Builder(this).setTitle("Manage User").setIcon(R.drawable.logo_camth).setItems(options) { _, which ->
            when (which) {
                0 -> showUpdateRoleDialog(user)
                1 -> deleteUser(user)
            }
        }.show()
    }

    private fun showUpdateRoleDialog(user: User) {
        val roles = arrayOf("user", "admin")
        AlertDialog.Builder(this).setTitle("Update Role").setIcon(R.drawable.logo_camth).setSingleChoiceItems(roles, roles.indexOf(user.role)) { dialog, which ->
            val updatedUser = user.copy(role = roles[which])
            val index = InMemoryDataStore.users.indexOfFirst { it.uid == user.uid }
            if (index != -1) {
                InMemoryDataStore.users[index] = updatedUser
            }
            loadDashboard()
            dialog.dismiss()
        }.show()
    }

    private fun deleteUser(user: User) {
        InMemoryDataStore.users.removeIf { it.uid == user.uid }
        loadDashboard()
    }

    private fun loadProjectsUI(query: String = "") {
        projectListLayout.removeAllViews()
        val projects = allProjects.filter { it.name.contains(query, ignoreCase = true) || it.status.contains(query, ignoreCase = true) }
        projects.forEach { project ->
            val assignedUser = allUsers.find { it.uid == project.assignedUserId }?.fullName ?: "Unassigned"
            val card = CardView(this@AdminDashboardActivity).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                radius = 16f
                setContentPadding(32, 32, 32, 32)
                setOnClickListener { showProjectOptions(project) }

                val layout = LinearLayout(this@AdminDashboardActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(this@AdminDashboardActivity).apply { text = project.name; textSize = 20f; setTypeface(null, Typeface.BOLD) })
                    addView(TextView(this@AdminDashboardActivity).apply { text = "Assigned to: $assignedUser"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                    addView(TextView(this@AdminDashboardActivity).apply { text = "Status: ${project.status}"; textSize = 16f; setTypeface(null, Typeface.ITALIC); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                }
                addView(layout)
            }
            projectListLayout.addView(card)
        }
    }

    private fun showProjectOptions(project: Project) {
        val options = arrayOf("Edit Project", "Update Status", "Delete Project", "Manage Documents")
        AlertDialog.Builder(this).setTitle("Manage Project").setIcon(R.drawable.logo_camth).setItems(options) { _, which ->
            when (which) {
                0 -> showProjectDialog(project)
                1 -> showUpdateProjectStatusDialog(project)
                2 -> deleteProject(project)
                3 -> {
                    val intent = Intent(this, DocumentManagementActivity::class.java)
                    intent.putExtra("PROJECT_ID", project.id)
                    startActivity(intent)
                }
            }
        }.show()
    }

    private fun showProjectDialog(project: Project? = null) {
        val isEditing = project != null
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val etName = EditText(this).apply { hint = "Project Name"; textSize = 18f }
        val etDesc = EditText(this).apply { hint = "Project Description"; textSize = 18f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 } }
        val btnStartDate = Button(this).apply { text = "Start Date"; textSize = 18f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 } }
        val btnEndDate = Button(this).apply { text = "End Date"; textSize = 18f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 } }
        val spUsers = Spinner(this).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 16 } }

        dialogLayout.addView(etName)
        dialogLayout.addView(etDesc)
        dialogLayout.addView(btnStartDate)
        dialogLayout.addView(btnEndDate)
        dialogLayout.addView(spUsers)

        spUsers.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allUsers.map { it.fullName })

        if (isEditing) {
            project?.let {
                etName.setText(it.name)
                etDesc.setText(it.description)
                btnStartDate.text = it.startDate
                btnEndDate.text = it.endDate
                spUsers.setSelection(allUsers.indexOfFirst { user -> user.uid == it.assignedUserId })
            }
        }

        btnStartDate.setOnClickListener { showDatePickerDialog { date -> btnStartDate.text = date } }
        btnEndDate.setOnClickListener { showDatePickerDialog { date -> btnEndDate.text = date } }

        AlertDialog.Builder(this)
            .setTitle(if (isEditing) "Edit Project" else "Add Project")
            .setIcon(R.drawable.logo_camth)
            .setView(dialogLayout)
            .setPositiveButton(if (isEditing) "Save" else "Add") { _, _ ->
                val name = etName.text.toString()
                val description = etDesc.text.toString()
                val startDate = btnStartDate.text.toString()
                val endDate = btnEndDate.text.toString()
                val selectedUser = allUsers[spUsers.selectedItemPosition]

                if (name.isNotBlank()) {
                    if (isEditing) {
                        project?.let {
                            val updatedProject = it.copy(name = name, description = description, startDate = startDate, endDate = endDate, assignedUserId = selectedUser.uid)
                            val index = InMemoryDataStore.projects.indexOfFirst { p -> p.id == it.id }
                            if (index != -1) {
                                InMemoryDataStore.projects[index] = updatedProject
                            }
                            sendNotification("Project Update", "Project '${updatedProject.name}' has been updated.")
                            loadDashboard()
                        }
                    } else {
                        val newProject = Project(id = UUID.randomUUID().toString(), name = name, description = description, startDate = startDate, endDate = endDate, assignedUserId = selectedUser.uid, status = "Not Started")
                        InMemoryDataStore.projects.add(newProject)
                        sendNotification("Project Update", "Project '${newProject.name}' has been assigned to ${selectedUser.fullName}.")
                        loadDashboard()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePickerDialog(onDateSet: (String) -> Unit) {
        val c = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day -> onDateSet("$day/${month + 1}/$year") },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showUpdateProjectStatusDialog(project: Project) {
        val statuses = arrayOf("Not Started", "In Progress", "Completed")
        AlertDialog.Builder(this).setTitle("Update Status").setIcon(R.drawable.logo_camth).setSingleChoiceItems(statuses, statuses.indexOf(project.status)) { dialog, which ->
            val updatedProject = project.copy(status = statuses[which])
            val index = InMemoryDataStore.projects.indexOfFirst { p -> p.id == project.id }
            if (index != -1) {
                InMemoryDataStore.projects[index] = updatedProject
            }
            val user = allUsers.find { it.uid == project.assignedUserId }
            user?.let { sendNotification("Project Status Updated", "Status of '${project.name}' is now ${statuses[which]}.") }
            loadDashboard()
            dialog.dismiss()
        }.show()
    }

    private fun deleteProject(project: Project) {
        InMemoryDataStore.projects.removeIf { it.id == project.id }
        loadDashboard()
    }

    private fun loadRequestsUI(query: String = "") {
        requestListLayout.removeAllViews()
        val requests = allRequests.filter { it.title.contains(query, ignoreCase = true) || it.approvalStatus.contains(query, ignoreCase = true) }
        requests.forEach { request ->
            val user = allUsers.find { it.uid == request.userId }?.fullName ?: "Unknown"
            val card = CardView(this@AdminDashboardActivity).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                radius = 16f
                setContentPadding(32, 32, 32, 32)
                setOnClickListener { showRequestOptions(request) }

                val layout = LinearLayout(this@AdminDashboardActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(this@AdminDashboardActivity).apply { text = request.title; textSize = 20f; setTypeface(null, Typeface.BOLD) })
                    addView(TextView(this@AdminDashboardActivity).apply { text = "From: $user"; textSize = 16f; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                    addView(TextView(this@AdminDashboardActivity).apply { text = "Approval: ${request.approvalStatus}"; textSize = 16f; setTypeface(null, Typeface.ITALIC); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 } })
                }
                addView(layout)
            }
            requestListLayout.addView(card)
        }
    }

    private fun showRequestOptions(request: ServiceRequest) {
        val options = arrayOf("Approve Request", "Deny Request", "Update Status", "Delete Request")
        AlertDialog.Builder(this).setTitle("Manage Service Request").setIcon(R.drawable.logo_camth).setItems(options) { _, which ->
            when (which) {
                0 -> updateRequestApproval(request, "Approved")
                1 -> updateRequestApproval(request, "Denied")
                2 -> showUpdateRequestStatusDialog(request)
                3 -> deleteRequest(request)
            }
        }.show()
    }

    private fun updateRequestApproval(request: ServiceRequest, newApprovalStatus: String) {
        val updatedRequest = request.copy(approvalStatus = newApprovalStatus)
        val index = InMemoryDataStore.serviceRequests.indexOfFirst { it.id == request.id }
        if (index != -1) {
            InMemoryDataStore.serviceRequests[index] = updatedRequest
        }
        val user = allUsers.find { it.uid == request.userId }
        user?.let { sendNotification("Service Request Update", "Your request '${request.title}' has been $newApprovalStatus.") }
        loadDashboard()
    }

    private fun showUpdateRequestStatusDialog(request: ServiceRequest) {
        val statuses = arrayOf("Pending", "In Progress", "Completed", "Rejected")
        AlertDialog.Builder(this).setTitle("Update Request Status").setIcon(R.drawable.logo_camth).setSingleChoiceItems(statuses, statuses.indexOf(request.status)) { dialog, which ->
            val updatedRequest = request.copy(status = statuses[which])
            val index = InMemoryDataStore.serviceRequests.indexOfFirst { it.id == request.id }
            if (index != -1) {
                InMemoryDataStore.serviceRequests[index] = updatedRequest
            }
            loadDashboard()
            dialog.dismiss()
        }.show()
    }

    private fun deleteRequest(request: ServiceRequest) {
        InMemoryDataStore.serviceRequests.removeIf { it.id == request.id }
        loadDashboard()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("PROJECT_UPDATES", "Project Updates", NotificationManager.IMPORTANCE_DEFAULT)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, "PROJECT_UPDATES").setSmallIcon(R.drawable.logo_camth).setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_DEFAULT)
        getSystemService(NotificationManager::class.java).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}