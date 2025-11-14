package com.example.camthprojects

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.camthprojects.models.Document
import java.util.UUID

class DocumentManagementActivity : AppCompatActivity() {

    private lateinit var documentListLayout: LinearLayout
    private lateinit var btnUploadDocument: Button
    private lateinit var projectId: String

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.also { uri ->
                // Persist permission to access the file URI across device reboots
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                saveDocument(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_management)

        projectId = intent.getStringExtra("PROJECT_ID") ?: ""

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Documents"

        documentListLayout = findViewById(R.id.documentList)
        btnUploadDocument = findViewById(R.id.btnUploadDocument)

        btnUploadDocument.setOnClickListener { openFilePicker() }

        loadDocuments()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadDocuments() {
        val documents = InMemoryDataStore.documents.filter { it.projectId == projectId }
        displayDocuments(documents)
    }

    private fun displayDocuments(documents: List<Document>) {
        documentListLayout.removeAllViews()
        val inflater = LayoutInflater.from(this)
        if (documents.isEmpty()) {
            documentListLayout.addView(TextView(this).apply { text = "No documents found for this project." })
        } else {
            documents.forEach { doc ->
                val itemView = inflater.inflate(R.layout.item_document, documentListLayout, false)
                val tvDocumentName = itemView.findViewById<TextView>(R.id.tvDocumentName)
                val btnDownload = itemView.findViewById<Button>(R.id.btnDownload)

                tvDocumentName.text = doc.fileName
                btnDownload.setOnClickListener { 
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(doc.fileUrl)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@DocumentManagementActivity, "Could not open file.", Toast.LENGTH_SHORT).show()
                    }
                }
                documentListLayout.addView(itemView)
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun saveDocument(fileUri: Uri) {
        val fileName = getFileName(fileUri) ?: "unknown_file"

        val newDocument = Document(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            fileName = fileName,
            fileUrl = fileUri.toString() // Save the persistent URI as a string
        )

        InMemoryDataStore.documents.add(newDocument)
        
        Toast.makeText(this, "Document saved locally", Toast.LENGTH_SHORT).show()
        loadDocuments()
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                         result = cursor.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (cut != null) {
                    result = result?.substring(cut + 1)
                }
            }
        }
        return result
    }
}