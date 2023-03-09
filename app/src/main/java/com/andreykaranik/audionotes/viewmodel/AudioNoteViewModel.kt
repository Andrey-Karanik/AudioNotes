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
    var isPlaying: Boolean
)

class AudioNoteViewModel(
    private val audioNoteService: AudioNoteService
) : BaseViewModel(), AudioNoteActionListener, AudioNoteDialogListener {

    private val _noteItems = MutableLiveData<List<AudioNoteItem>>()
    val noteItems: LiveData<List<AudioNoteItem>> = _noteItems

    private val _actionShowToast = MutableLiveData<Event<Int>>()
    val actionShowToast: LiveData<Event<Int>> = _actionShowToast

    private val _actionShowDialog = MutableLiveData<Event<Unit>>()
    val actionShowDialog: LiveData<Event<Unit>> = _actionShowDialog

    private val _actionStartRecordingSuccess = MutableLiveData<Event<Boolean>>()
    val actionStartRecordingSuccess: LiveData<Event<Boolean>> = _actionStartRecordingSuccess

    private val _actionStopRecordingSuccess = MutableLiveData<Event<Boolean>>()
    val actionStopRecordingSuccess: LiveData<Event<Boolean>> = _actionStopRecordingSuccess

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private val noteIdsIsPlaying = mutableSetOf<Long>()
    private var notes: List<AudioNote> = emptyList()
        set(value) {
            field = value
            notifyUpdates()
        }


    private val listener: AudioNoteListener = {
        notes = it
    }

    init {
        audioNoteService.addListener(listener)
        _isRecording.value = false
        loadAudioNotes()
    }

    fun loadAudioNotes() {
        audioNoteService.loadAudioNotes().onError{}.autoCancel()
    }

    fun startRecording() {
        audioNoteService.startRecording()
            .onSuccess {
                _isRecording.value = true
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
                _isRecording.value = false
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
            .onSuccess {
                noteIdsIsPlaying.add(id)
                notifyUpdates()
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
            }
            .autoCancel()
    }

    fun stopPlaying(id: Long) {
        audioNoteService.stopPlaying()
            .onSuccess {
                noteIdsIsPlaying.remove(id)
                notifyUpdates()
            }
            .onError {
                _actionShowToast.value = Event(R.string.error)
            }
            .autoCancel()
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
        stopPlaying(audioNote.id)

    }

    private fun isPlaying(audioNote: AudioNote): Boolean {
        return noteIdsIsPlaying.contains(audioNote.id)
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

    private fun notifyUpdates() {
        _noteItems.postValue(notes.map {note -> AudioNoteItem(note, isPlaying(note)) })
    }


}