package com.example.planer.model

data class PlanDto(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val createUid: String,
    val category: String
)
