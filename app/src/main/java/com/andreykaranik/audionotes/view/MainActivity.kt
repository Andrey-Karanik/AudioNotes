package com.andreykaranik.audionotes.view

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andreykaranik.audionotes.App
import com.andreykaranik.audionotes.R
import com.andreykaranik.audionotes.model.AudioNote
import com.andreykaranik.audionotes.viewmodel.AudioNoteViewModel
import com.andreykaranik.audionotes.viewmodel.ViewModelFactory
import java.io.IOException

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity() {


    private lateinit var adapter : AudioNoteAdapter
    private lateinit var recordButton: ImageButton
    private lateinit var emptyListTextView: TextView

    private val viewModel : AudioNoteViewModel by viewModels { ViewModelFactory(applicationContext as App) }

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onStart() {
        super.onStart()

        adapter = AudioNoteAdapter(viewModel)

        emptyListTextView = findViewById(R.id.empty_list_text_view)

        viewModel.noteItems.observe(this, Observer {
            adapter.notes = it
            if (viewModel.isEmpty()) {
                emptyListTextView.visibility = View.VISIBLE
            } else {
                emptyListTextView.visibility = View.GONE
            }
        })

        viewModel.actionShowToast.observe(this, Observer {
            it.getValue()?.let { messageRes -> toast(messageRes) }
        })

        viewModel.actionShowDialog.observe(this, Observer {
            showDialog()
        })

        viewModel.isRecording.observe(this, Observer {
            if (it) {
                recordButton.setBackgroundResource(R.drawable.dark_round_button)
                recordButton.setImageResource(R.drawable.ic_round_stop)
            } else {
                recordButton.setBackgroundResource(R.drawable.round_button)
                recordButton.setImageResource(R.drawable.ic_round_mic)
            }
        })

        viewModel.actionStartRecordingSuccess.observe(this, Observer {
            recordButton.isEnabled = true
        })

        viewModel.actionStopRecordingSuccess.observe(this, Observer {
            recordButton.isEnabled = true
        })

        val layoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.divider)?.let { itemDecorator.setDrawable(it) }
        recyclerView.addItemDecoration(itemDecorator)
        recordButton = findViewById<ImageButton>(R.id.record_button)
        recordButton.setOnClickListener {
            if (viewModel.isRecording.value == false) {
                viewModel.startRecording()
            } else {
                viewModel.stopRecording()
            }
            recordButton.isEnabled = false
        }

    }

    fun showDialog() {
        val dialog = AudioNoteDialog(viewModel)
        dialog.show(supportFragmentManager, "audio note dialog")
    }

    fun toast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }
}