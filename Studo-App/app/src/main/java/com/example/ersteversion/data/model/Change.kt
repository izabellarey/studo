package com.example.ersteversion.data.model

data class Change(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val open: Boolean
)