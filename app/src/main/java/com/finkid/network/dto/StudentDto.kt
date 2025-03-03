package com.finkid.network.dto

data class StudentDto(
    val email: String,
    val name: String,
    val phone: String?,
    val birthDate: String?,
    val image: String?,
    var rating: Int = 0
)
