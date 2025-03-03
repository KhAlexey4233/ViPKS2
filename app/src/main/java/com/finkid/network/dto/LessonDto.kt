package com.finkid.network.dto

data class LessonDto(
    val id: String,
    val theme: String,
    val document: String,
    val homework: List<QuestionDto>,
)
