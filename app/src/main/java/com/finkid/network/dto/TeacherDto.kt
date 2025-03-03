package com.finkid.network.dto

data class TeacherDto(
    val email: String,
    val name: String?,
    val phone: String?,
    val education: String?,
    val work: String?,
    val bio: String?,
    val services: String?,
    val image: String?,
    val studentIds: List<String>,
)
