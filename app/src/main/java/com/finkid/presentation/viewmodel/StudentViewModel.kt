package com.finkid.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.finkid.network.dto.HomeworkDto
import com.finkid.network.dto.LessonDto
import com.finkid.network.dto.ScheduleDto
import com.finkid.network.dto.StudentDto

class StudentViewModel : ViewModel() {
    private val _student = MutableLiveData<StudentDto?>(null)
    val student: LiveData<StudentDto?> = _student

    fun setStudent(value: StudentDto?) {
        _student.postValue(value)
    }

    private val _scheduleList = MutableLiveData(listOf<ScheduleDto>())
    val scheduleList: LiveData<List<ScheduleDto>> = _scheduleList

    fun setSchedulesList(value: List<ScheduleDto>) {
        _scheduleList.postValue(value)
    }

    private val _homeworkList = MutableLiveData(listOf<HomeworkDto>())
    val homeworkList: LiveData<List<HomeworkDto>> = _homeworkList

    fun setHomeworkList(value: List<HomeworkDto>) {
        _homeworkList.postValue(value)
    }

    private val _selectedSchedule = MutableLiveData<ScheduleDto?>(null)
    val selectedSchedule: LiveData<ScheduleDto?> = _selectedSchedule

    fun setSchedule(value: ScheduleDto?) {
        _selectedSchedule.value = value
    }

    private val _selectedLesson = MutableLiveData<LessonDto?>(null)
    val selectedLesson: LiveData<LessonDto?> = _selectedLesson

    fun setLesson(value: LessonDto?) {
        _selectedLesson.value = value
    }

    private val _lessonsList = MutableLiveData(listOf<LessonDto>())
    val lessonsList: LiveData<List<LessonDto>> = _lessonsList

    fun setLessonsList(value: List<LessonDto>) {
        _lessonsList.postValue(value)
    }
}