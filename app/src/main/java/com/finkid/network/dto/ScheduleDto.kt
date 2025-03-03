package com.finkid.network.dto

data class ScheduleDto(
    val creatorEmail: String,
    val id: String,
    val lesson: String?,
    val theme: String?,
    val students: String,
    val subject: String,
    val timeEnd: Long,
    val timeStart: Long,
    var teacherEmail: String?,
    var isExpanded: Boolean = false,
    var homeworkDto: HomeworkDto? = null,
    var homeworksList: List<HomeworkDto>? = null,
)
