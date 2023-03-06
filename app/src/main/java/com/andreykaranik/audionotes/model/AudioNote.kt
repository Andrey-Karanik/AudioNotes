package com.andreykaranik.audionotes.model

data class AudioNote(
    val id: Long,
    val name: String,
    val date: String,
    val duration: Long
)