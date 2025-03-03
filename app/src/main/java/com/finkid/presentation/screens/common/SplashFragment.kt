package com.finkid.presentation.screens.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.databinding.FragmentSplashBinding
import com.finkid.network.dto.HomeworkDto
import com.finkid.network.dto.ScheduleDto
import com.finkid.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashFragment : Fragment() {
    private val binding by lazy { FragmentSplashBinding.inflate(layoutInflater) }
    private val networkRepository by lazy { (requireActivity() as MainActivity).networkRepository }
    private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
    private val studentViewModel by lazy { (requireActivity() as MainActivity).studentViewModel }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        checkUserData()
    }

    private fun checkUserData() {
        var screenState = 0
        lifecycleScope.launch(Dispatchers.IO) {
            val isAuthed = networkRepository.checkAuth()
            if (isAuthed) {
                val studentsListAll = networkRepository.getStudentsList()
                val homeworksListAll = networkRepository.getHomeworksAll()
                studentsListAll.map { user ->
                    var userRating = 0
                    homeworksListAll.filter { it.userEmail == user.email }.let { homeworks ->
                        homeworks.map {
                            val correctAnswers = it.answersList.count { it.isTrue }
                            val answersCount = it.answersList.size
                            if (answersCount != 0) {
                                val percentResult =
                                    (correctAnswers.toFloat() * 100 / answersCount.toFloat()).toInt()
                                userRating += percentResult
                            }
                        }
                        if (homeworks.isNotEmpty()) {
                            user.rating = userRating / homeworks.size
                        }
                    }
                }
                val userRole = networkRepository.getUserRole()
                when (userRole) {
                    Constant.STUDENT -> {
                        screenState = 2
                        val student = networkRepository.getStudent()
                        student?.let { studentDto ->
                            val scheduleList = studentDto.email.let {
                                networkRepository.getStudentScheduleList(
                                    it
                                )
                            }
                            val homeworkList =
                                homeworksListAll.filter { it.userEmail == studentDto.email }
                            studentViewModel.setHomeworkList(homeworkList)
                            scheduleList.forEach { schedule ->
                                homeworkList.find { it.id == schedule.id }?.let { foundItem ->
                                    schedule.homeworkDto = foundItem
                                }
                            }
                            var totalPercent = 0
                            homeworkList.map {
                                val correctAnswers = it.answersList.count { it.isTrue }
                                val answersCount = it.answersList.size
                                if (answersCount != 0) {
                                    val percentResult =
                                        (correctAnswers.toFloat() * 100 / answersCount.toFloat()).toInt()
                                    totalPercent += percentResult
                                }
                            }
                            student.rating =
                                if (homeworkList.isNotEmpty()) totalPercent / homeworkList.size else 0
                            studentViewModel.setSchedulesList(scheduleList)
                            val lessonsList = networkRepository.getStudentLessonList(scheduleList)
                            studentViewModel.setLessonsList(lessonsList)
                            studentViewModel.setStudent(student)
                        }
                    }

                    Constant.TEACHER -> {
                        screenState = 1
                        val teacher = networkRepository.getTeacher()
                        teacherViewModel.setTeacher(teacher)
                        val lessonsList = networkRepository.getTeacherLessonList()
                        teacherViewModel.setLessonsList(lessonsList)
                        val scheduleList = mutableListOf<ScheduleDto>()
                        scheduleList.addAll(networkRepository.getTeacherScheduleList())
                        scheduleList.forEach { schedule ->
                            val scheduleHomeworks = mutableListOf<HomeworkDto>()
                            homeworksListAll.find { it.id == schedule.id }?.let { foundItem ->
                                scheduleHomeworks.add(foundItem)
                            }
                            schedule.homeworksList = scheduleHomeworks
                        }
                        teacherViewModel.setSchedulesList(scheduleList)
                        teacherViewModel.setStudentsList(studentsListAll)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                findNavController().popBackStack()
                when (screenState) {
                    0 -> findNavController().navigate(R.id.authFragment)
                    1 -> findNavController().navigate(R.id.teacherHomeFragment)
                    2 -> findNavController().navigate(R.id.studentHomeFragment)
                }
            }
        }
    }
}
