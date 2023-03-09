package com.andreykaranik.audionotes.view

import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andreykaranik.audionotes.R
import com.andreykaranik.audionotes.formatDuration
import com.andreykaranik.audionotes.model.AudioNote


interface AudioNoteActionListener {
    fun onPlay(audioNote: AudioNote)
    fun onPause(audioNote: AudioNote)

}

class AudioNoteAdapter(private val audioNoteActionListener: AudioNoteActionListener) : RecyclerView.Adapter<AudioNoteAdapter.AudioNoteHolder>(), View.OnClickListener {
    var notes: List<AudioNote> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }
    var currentIdIsPlaying: Long = -1
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioNoteHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.audio_note_item, parent, false)
        itemView.findViewById<ImageButton>(R.id.note_play_button).setOnClickListener(this)
        return AudioNoteHolder(itemView)
    }

    override fun onBindViewHolder(holder: AudioNoteHolder, position: Int) {

        val audioNote = notes[position]

        holder.itemView.tag = audioNote
        holder.playButton.tag = audioNote

        holder.nameTextView.text = audioNote.name
        holder.dateTextView.text = audioNote.date
        holder.timeTextView.text = formatDuration(audioNote.duration)

        if (currentIdIsPlaying == audioNote.id) {
            holder.playButton.setBackgroundResource(R.drawable.dark_round_button)
            holder.playButton.setImageResource(R.drawable.ic_round_pause)
        } else {
            holder.playButton.setBackgroundResource(R.drawable.round_button)
            holder.playButton.setImageResource(R.drawable.ic_round_play_arrow)
        }
    }

    override fun getItemCount(): Int = notes.size

    class AudioNoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView : TextView = itemView.findViewById(R.id.note_name_text_view)
        val dateTextView : TextView = itemView.findViewById(R.id.note_date_text_view)
        val timeTextView : TextView = itemView.findViewById(R.id.note_time_text_view)
        val playButton : ImageButton = itemView.findViewById(R.id.note_play_button)
    }

    override fun onClick(v: View) {
        val audioNote = v.tag as AudioNote
        when (v.id) {
            R.id.note_play_button -> {
                if (currentIdIsPlaying != audioNote.id) {
                    audioNoteActionListener.onPlay(audioNote)
                } else {
                    audioNoteActionListener.onPause(audioNote)
                }
            }
            else -> {}
        }
    }
}