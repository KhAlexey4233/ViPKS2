package com.finkid.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.finkid.network.dto.LessonDto
import com.finkid.network.dto.QuestionDto
import com.finkid.network.dto.ScheduleDto
import com.finkid.network.dto.StudentDto
import com.finkid.network.dto.TeacherDto

class TeacherViewModel : ViewModel() {
    private val _teacher = MutableLiveData<TeacherDto?>(null)
    val teacher: LiveData<TeacherDto?> = _teacher

    fun setTeacher(value: TeacherDto?) {
        _teacher.postValue(value)
    }

    private val _lessonsList = MutableLiveData(listOf<LessonDto>())
    val lessonsList: LiveData<List<LessonDto>> = _lessonsList

    fun setLessonsList(value: List<LessonDto>) {
        _lessonsList.postValue(value)
    }

    private val _currentQuestions = MutableLiveData(listOf<QuestionDto>())
    val currentQuestions: LiveData<List<QuestionDto>> = _currentQuestions

    fun setCurrentQuestions(value: List<QuestionDto>) {
        _currentQuestions.value = value
    }

    private val _studentsList = MutableLiveData(listOf<StudentDto>())
    val studentsList: LiveData<List<StudentDto>> = _studentsList

    fun setStudentsList(value: List<StudentDto>) {
        _studentsList.postValue(value)
    }

    private val _scheduleList = MutableLiveData(listOf<ScheduleDto>())
    val scheduleList: LiveData<List<ScheduleDto>> = _scheduleList

    fun setSchedulesList(value: List<ScheduleDto>) {
        _scheduleList.postValue(value)
    }
}