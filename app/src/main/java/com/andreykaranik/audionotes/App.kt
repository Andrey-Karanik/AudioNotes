package com.andreykaranik.audionotes

import android.app.Application
import com.andreykaranik.audionotes.model.AudioNoteService

class App : Application() {
    lateinit var audioNoteService : AudioNoteService
    override fun onCreate() {
        super.onCreate()
        audioNoteService = AudioNoteService(this)
    }
}