package com.andreykaranik.audionotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andreykaranik.audionotes.R
import com.andreykaranik.audionotes.model.AudioNote
import com.andreykaranik.audionotes.model.AudioNoteListener
import com.andreykaranik.audionotes.model.AudioNoteService
import com.andreykaranik.audionotes.view.AudioNoteActionListener
import com.andreykaranik.audionotes.view.AudioNoteDialogListener


data class AudioNoteItem(
    val audioNote: AudioNote,
    val isInProgress: Boolean
)

class AudioNoteViewModel(
    private val audioNoteService: AudioNoteService
) : BaseViewModel(), AudioNoteActionListener, AudioNoteDialogListener {

    private val _notes = MutableLiveData<List<AudioNote>>()
    val notes: LiveData<List<AudioNote>> = _notes

    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

    private val _actionShowDialog = MutableLiveData<Event<Unit>>()
    val actionShowDialog: LiveData<Event<Unit>> = _actionShowDialog

    private val _actionStartRecordingSuccess = MutableLiveData<Event<Boolean>>()
    val actionStartRecordingSuccess: LiveData<Event<Boolean>> = _actionStartRecordingSuccess

    private val _actionStopRecordingSuccess = MutableLiveData<Event<Boolean>>()
    val actionStopRecordingSuccess: LiveData<Event<Boolean>> = _actionStopRecordingSuccess

    private var _isPlaying = false

    var isRecording = false


    private val listener: AudioNoteListener = {
        _notes.value = it
    }

    init {
        audioNoteService.addListener(listener)
        loadAudioNotes()
    }

    fun loadAudioNotes() {
        audioNoteService.loadAudioNotes().onError{}.autoCancel()
    }

    fun startRecording() {
        audioNoteService.startRecording()
            .onSuccess {
                isRecording = true
                _actionStartRecordingSuccess.value = Event(true)
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
                _actionStartRecordingSuccess.value = Event(false)
            }
            .autoCancel()
    }

    fun stopRecording() {
        audioNoteService.stopRecording()
            .onSuccess {
                isRecording = false
                _actionShowDialog.value = Event(Unit)
                _actionStopRecordingSuccess.value = Event(true)
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
                _actionStopRecordingSuccess.value = Event(false)
            }
            .autoCancel()
    }

    fun startPlaying(id: Long) {
        audioNoteService.startPlaying(id)
            .onError {
                _actionShowToast.value = Event(R.string.error)
            }
            .autoCancel()
        _isPlaying = true
    }

    fun stopPlaying() {
        audioNoteService.stopPlaying().autoCancel()
        _isPlaying = false
    }

    fun isEmpty(): Boolean {
        return audioNoteService.getAudioNotesSize() == 0L
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

    override fun isPlaying(): Boolean {
        return _isPlaying
    }

    override fun onCancel() {}

    override fun onSave(name: String, date: String, duration: Long) {
        audioNoteService.renameRecordFile(name)
            .onSuccess {
                audioNoteService.addAudioNote(audioNoteService.getAudioNotesSize(), name, date, duration)
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
            }
            .autoCancel()
    }


}