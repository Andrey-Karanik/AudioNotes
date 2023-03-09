package com.andreykaranik.audionotes.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.andreykaranik.audionotes.R

interface AudioNoteDialogListener {
    fun onCancel()
    fun onSave(name: String)
}

class AudioNoteDialog(private val audioNoteDialogListener: AudioNoteDialogListener) : AppCompatDialogFragment() {

    private lateinit var noteNameEditText : EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.audio_note_dialog, null)

        builder.setView(view)
            .setTitle(R.string.enter_a_name)
            .setNegativeButton(R.string.cancel) {
                    p0, p1 ->  audioNoteDialogListener.onCancel()}
            .setPositiveButton(R.string.save) {
                    p0, p1 ->  audioNoteDialogListener.onSave(noteNameEditText.text.toString())}

        if (view != null) {
            noteNameEditText = view.findViewById(R.id.note_name_edit_text)
        }

        return builder.create()
    }
}