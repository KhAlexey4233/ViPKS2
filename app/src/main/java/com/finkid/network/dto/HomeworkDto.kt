package com.finkid.network.dto

data class HomeworkDto(
  val userEmail: String,
  val id: String,
  val answersList: List<AnswerDto>,
  val document: String,
)
