package com.andreykaranik.audionotes.model

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.andreykaranik.audionotes.task.SimpleTask
import com.andreykaranik.audionotes.task.Task
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.Callable

typealias AudioNoteListener = (notes: List<AudioNote>) -> Unit

class AudioNoteService(private val context : Context) {
    private var notes = mutableListOf<AudioNote>()
    private var isLoaded = false
    private var listeners = mutableSetOf<AudioNoteListener>()
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    fun loadAudioNotes(): Task<Unit> = SimpleTask<Unit>(Callable {
        isLoaded = true
        notifyChanges()
    })

    fun startRecording(): Task<Unit> = SimpleTask<Unit>(Callable {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("${context.externalCacheDir?.absolutePath}/record.3gp")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
    })

    fun stopRecording(): Task<Unit> = SimpleTask<Unit>(Callable {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    })

    fun startPlaying(id: Long): Task<Unit> = SimpleTask<Unit>(Callable {
        player = MediaPlayer().apply {
            setDataSource("${context.externalCacheDir?.absolutePath}/${notes.find {it.id == id}?.name}.3gp")
            prepare()
            start()
        }
    })

    fun stopPlaying(): Task<Unit> = SimpleTask<Unit>(Callable {
        player?.release()
        player = null
    })

    fun renameRecordFile(fileName: String): Task<Unit> = SimpleTask<Unit>(Callable {
        val p: File? = context.externalCacheDir
        val from = File(p, "record.3gp")
        val to = File(p, "${fileName}.3gp")
        from.renameTo(to)

        return@Callable
    })

    fun addAudioNote(id: Long, name: String, date: String, duration: Long) {
        notes.add(AudioNote(id, name, date, duration))
        notifyChanges()
    }

    fun getAudioNotesSize() : Long = notes.size.toLong()


    fun addListener(listener: AudioNoteListener) {
        listeners.add(listener)
        if (isLoaded) {
            listener(notes)
        }
    }

    fun removeListener(listener: AudioNoteListener) {
        listeners.remove(listener)
        if (isLoaded) {
            listener(notes)
        }
    }

    private fun notifyChanges() {
        if (!isLoaded) {
            return
        }
        listeners.forEach {
            it(notes)
        }
    }
}