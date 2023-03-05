package com.andreykaranik.audionotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andreykaranik.audionotes.AudioNoteActionListener
import com.andreykaranik.audionotes.R
import com.andreykaranik.audionotes.model.AudioNote
import com.andreykaranik.audionotes.model.AudioNoteListener
import com.andreykaranik.audionotes.model.AudioNoteService


data class AudioNoteItem(
    val audioNote: AudioNote,
    val isInProgress: Boolean
)

class AudioNoteViewModel(
    private val audioNoteService: AudioNoteService
) : BaseViewModel(), AudioNoteActionListener {

    private val _notes = MutableLiveData<List<AudioNote>>()
    val notes: LiveData<List<AudioNote>> = _notes

    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

    var isRecording = false

    var isPlaying = false


    private val listener: AudioNoteListener = {
        _notes.value = it
    }

    init {
        audioNoteService.addListener(listener)
        loadAudioNotes()
    }

    fun loadAudioNotes() {
        audioNoteService.loadAudioNotes().onSuccess {

        }.autoCancel()
    }

    fun startRecording() {
        audioNoteService.startRecording()
            .onSuccess {
                isRecording = true
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
            }
            .autoCancel()
    }

    fun stopRecording() {
        audioNoteService.stopRecording()
            .onSuccess {
                isRecording = false
                audioNoteService.renameRecordFile("my_new_record").onSuccess { audioNoteService.addAudioNote(audioNoteService.getAudioNotesSize(), "my_new_record") }
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
            }
            .autoCancel()
    }

    fun startPlaying(id: Long) {
        audioNoteService.startPlaying(id)
            .onError {
                _actionShowToast.value = Event(R.string.error)
                it.printStackTrace()
            }
            .autoCancel()
    }

    fun stopPlaying() {
        audioNoteService.stopPlaying().autoCancel()
    }

    override fun onCleared() {
        super.onCleared()
        audioNoteService.removeListener(listener)
    }

    override fun onPlay(audioNote: AudioNote) {
        startPlaying(audioNote.id)
    }

    override fun onPause(audioNote: AudioNote) {
        stopPlaying()
    }


}