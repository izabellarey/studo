package com.example.ersteversion.data.model

data class Lesson(
    val id: Int = -1,
    val subject: String,
    val teacher: String,
    val schoolClass: String,
    val room: Room,
    val day: Day,
    val rasterIndex: Int,
    val changed: Boolean = false,
    val changeReason: String? = null
)