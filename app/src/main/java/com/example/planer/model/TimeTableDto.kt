package com.example.planer.model

data class TimeTableDto(
    var id: Int? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var uploadDate: String? = null,
    var description: String? = null,
    var createUid: String? = null,
    var deleteAble: Boolean? = null
)