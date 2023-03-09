package com.andreykaranik.audionotes.model

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.andreykaranik.audionotes.task.SimpleTask
import com.andreykaranik.audionotes.task.Task
import java.io.File
import java.util.concurrent.Callable


typealias AudioNoteListener = (notes: List<AudioNote>) -> Unit
typealias IdIsPlayingListener = (idIsPlaying: Long) -> Unit

class AudioNoteService(private val context : Context) {
    private var notes = mutableListOf<AudioNote>()
    private var currentIdIsPlaying : Long = -1
    private var isLoaded = false
    private var audioNoteListeners = mutableSetOf<AudioNoteListener>()
    private var idIsPlayingListeners = mutableSetOf<IdIsPlayingListener>()
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var duration = 0

    fun loadAudioNotes(): Task<Unit> = SimpleTask<Unit>(Callable {
        isLoaded = true
        notifyAudioNoteChanges()
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
        if (currentIdIsPlaying != -1L) {
            stopPlaying(currentIdIsPlaying)
        }
        player = MediaPlayer().apply {
            setDataSource("${context.externalCacheDir?.absolutePath}/${notes.find {it.id == id}?.name}.3gp")
            setOnCompletionListener {
                currentIdIsPlaying = -1
                notifyIdIsPlayingNoteChanges()
            }
            prepare()
            start()
            currentIdIsPlaying = id
            notifyIdIsPlayingNoteChanges()
        }
    })

    fun stopPlaying(id: Long): Task<Unit> = SimpleTask<Unit>(Callable {
        player?.release()
        player = null
        currentIdIsPlaying = -1
        notifyIdIsPlayingNoteChanges()
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
        notifyAudioNoteChanges()
    }

    fun getAudioNotesSize() : Long = notes.size.toLong()


    fun addAudioNoteListener(listener: AudioNoteListener) {
        audioNoteListeners.add(listener)
        if (isLoaded) {
            listener(notes)
        }
    }

    fun removeAudioNoteListener(listener: AudioNoteListener) {
        audioNoteListeners.remove(listener)
        if (isLoaded) {
            listener(notes)
        }
    }

    private fun notifyAudioNoteChanges() {
        if (!isLoaded) {
            return
        }
        audioNoteListeners.forEach {
            it(notes)
        }
    }

    fun addIdIsPlayingListener(listener: IdIsPlayingListener) {
        idIsPlayingListeners.add(listener)
    }

    fun removeIdIsPlayingListener(listener: IdIsPlayingListener) {
        idIsPlayingListeners.remove(listener)
    }

    private fun notifyIdIsPlayingNoteChanges() {
        idIsPlayingListeners.forEach {
            it(currentIdIsPlaying)
        }
    }

    fun getAudioNoteDuration(fileName: String): Long {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource("${context.externalCacheDir?.absolutePath}/${fileName}.3gp")
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr!!.toLong()
    }
}