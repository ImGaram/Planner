package com.example.planer.model

data class PlanDto(
    var id: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var date: String? = null,
    var createUid: String? = null,
    var category: String? = null
)
