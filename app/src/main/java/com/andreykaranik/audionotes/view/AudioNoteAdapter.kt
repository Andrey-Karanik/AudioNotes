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
import com.andreykaranik.audionotes.model.AudioNote
import com.andreykaranik.audionotes.viewmodel.AudioNoteItem


interface AudioNoteActionListener {
    fun onPlay(audioNote: AudioNote)
    fun onPause(audioNote: AudioNote)
}

class AudioNoteAdapter(private val audioNoteActionListener: AudioNoteActionListener) : RecyclerView.Adapter<AudioNoteAdapter.AudioNoteHolder>(), View.OnClickListener {
    var notes: List<AudioNoteItem> = emptyList()
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

        val audioNoteItem = notes[position]
        val audioNote = audioNoteItem.audioNote

        holder.itemView.tag = audioNoteItem
        holder.playButton.tag = audioNoteItem

        holder.nameTextView.text = audioNote.name
        holder.dateTextView.text = audioNote.date
        holder.timeTextView.text = audioNote.duration.toString()

        if (audioNoteItem.isPlaying) {
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
        val audioNoteItem = v.tag as AudioNoteItem
        when (v.id) {
            R.id.note_play_button -> {
                if (!audioNoteItem.isPlaying) {
                    audioNoteActionListener.onPlay(audioNoteItem.audioNote)
                } else {
                    audioNoteActionListener.onPause(audioNoteItem.audioNote)
                }
            }
            else -> {}
        }
    }
}