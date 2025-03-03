package com.finkid.presentation.screens.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.databinding.FragmentTeacherHomeBinding
import com.finkid.network.dto.ScheduleDto
import com.finkid.presentation.adapter.ScheduleAdapter
import com.finkid.presentation.adapter.SelectLessonAdapter
import com.finkid.presentation.adapter.SelectStudentAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.vicmikhailau.maskededittext.MaskedEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class TeacherHomeFragment : Fragment() {
    private val binding by lazy { FragmentTeacherHomeBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
    private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
    private var selectedLesson = ""
    private var selectedLessonTheme = ""
    private var selectedStudentEmail = ""
    private var selectedStudentName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initViewModel()
        initListeners()
    }

    private fun initViewModel() {
        teacherViewModel.teacher.observe(viewLifecycleOwner) { value ->
            binding.name.text = value?.name ?: "Заполните профиль"
        }
        teacherViewModel.scheduleList.observe(viewLifecycleOwner) { value ->
            binding.calendar.removeDecorators()
            val eventDates = value.map { it.timeStart }
            binding.calendar.addDecorator(object : DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay?): Boolean {
                    return eventDates.any { isDateInDay(it, day?.date) }
                }

                override fun decorate(view: DayViewFacade?) {
                    val selectionDrawable =
                        ResourcesCompat.getDrawable(resources, R.drawable.calendar_date, null)
                    selectionDrawable?.let { view?.setSelectionDrawable(it) }
                }
            })
        }
    }

    private fun initListeners() {
        binding.btnLessons.setOnClickListener {
            findNavController().navigate(R.id.teacherLessonsFragment)
        }
        binding.btnStudents.setOnClickListener {
            findNavController().navigate(R.id.teacherStudentsFragment)
        }
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.teacherProfileFragment)
        }
        binding.btnExit.setOnClickListener {
            showBottomExit()
        }
        binding.calendar.setOnDateChangedListener { widget, date, selected ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.date.time
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val scheduleList = teacherViewModel.scheduleList.value ?: listOf()
            val hasLesson = scheduleList.any { isDateInDay(it.timeStart, date.date) }
            if (hasLesson) {
                scheduleList.find { isDateInDay(it.timeStart, date.date) }
                    ?.let { showBottomLesson(calendar.timeInMillis) }
            } else {
                showBottomAddSchedule(calendar.timeInMillis)
            }
        }
    }

    private fun showBottomAddSchedule(timestamp: Long) {
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_add_schedule_full, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(true)
        bottomDialog.show()
        dialogView.findViewById<TextInputEditText>(R.id.input_date)
            .setText(SimpleDateFormat("dd.MM.yyyy").format(timestamp))
        dialogView.findViewById<TextView>(R.id.btn_pin_lesson).setOnClickListener {
            showBottomList("lesson", it as TextView)
        }
        dialogView.findViewById<TextView>(R.id.btn_pin_student).setOnClickListener {
            showBottomList("student", it as TextView)
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener { btnSave ->
            val subject =
                dialogView.findViewById<TextInputEditText>(R.id.input_subject).text.toString()
                    .trim()
            val timeStart =
                dialogView.findViewById<MaskedEditText>(R.id.input_time_start).text.toString()
                    .trim()
            val timeEnd =
                dialogView.findViewById<MaskedEditText>(R.id.input_time_end).text.toString().trim()
            try {
                val timeStartLong = timeStringToMillis(timeStart, timestamp)
                val timeEndLong = timeStringToMillis(timeEnd, timestamp)
                if (selectedStudentEmail.isNotEmpty() && selectedLesson.isNotEmpty() && subject.isNotEmpty() && timeStart.isNotEmpty() && timeEnd.isNotEmpty()) {
                    btnSave.isEnabled = false
                    val schedule = ScheduleDto(
                        creatorEmail = "",
                        id = "",
                        lesson = selectedLesson,
                        theme = selectedLessonTheme,
                        students = selectedStudentEmail,
                        subject = subject,
                        timeEnd = timeEndLong,
                        timeStart = timeStartLong,
                        teacherEmail = null
                    )
                    databaseManager.addSchedule(schedule, result = { message, id ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        if (id != null) {
                            val tempList = mutableListOf<ScheduleDto>()
                            val scheduleList = teacherViewModel.scheduleList.value ?: listOf()
                            tempList.addAll(scheduleList)
                            tempList.add(schedule.copy(id = id))
                            teacherViewModel.setSchedulesList(tempList)
                            bottomDialog.dismiss()
                        } else {
                            btnSave.isEnabled = true
                        }
                    })
                } else {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Время заполнено неверно", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showBottomList(type: String, textView: TextView) {
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_list, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(true)
        bottomDialog.show()
        val bottomAdapter = when (type) {
            "lesson" -> SelectLessonAdapter((teacherViewModel.lessonsList.value ?: listOf()),
                selectItem = { item ->
                    selectedLesson = item.id
                    selectedLessonTheme = item.theme
                    textView.text = selectedLessonTheme
                    bottomDialog.dismiss()
                })

            else -> {
                val filteredStudents = teacherViewModel.studentsList.value?.filter {
                    teacherViewModel.teacher.value?.studentIds?.contains(
                        it.email
                    ) == true
                }
                SelectStudentAdapter((filteredStudents ?: listOf()), selectItem = { item ->
                    selectedStudentEmail = item.email
                    selectedStudentName = item.name
                    textView.text = selectedStudentName
                    bottomDialog.dismiss()
                })
            }
        }
        dialogView.findViewById<RecyclerView>(R.id.bottom_rv).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bottomAdapter
        }
    }

    private fun showBottomLesson(timestamp: Long) {
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_schedule, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(true)
        bottomDialog.show()
        dialogView.findViewById<TextView>(R.id.bottom_title).text =
            "Занятия на ${SimpleDateFormat("dd.MM.yyyy, EE").format(timestamp)}"
        val scheduleList = teacherViewModel.scheduleList.value ?: listOf()
        val filteredSchedule =
            scheduleList.filter { it.timeStart in timestamp..timestamp + 86400000 }
        val bottomAdapter =
            ScheduleAdapter(filteredSchedule.sortedBy { it.timeStart }, selectItem = { item ->
                showBottomUpdateSchedule(item)
                bottomDialog.dismiss()
            }, removeItem = { item ->
                databaseManager.removeSchedule(item, result = { message, success ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        val tempList = mutableListOf<ScheduleDto>()
                        tempList.addAll(teacherViewModel.scheduleList.value ?: listOf())
                        tempList.remove(item)
                        teacherViewModel.setSchedulesList(tempList)
                        bottomDialog.dismiss()
                    }
                })
            })
        dialogView.findViewById<Button>(R.id.bottom_add).setOnClickListener {
            showBottomAddSchedule(timestamp)
            bottomDialog.dismiss()
        }
        dialogView.findViewById<RecyclerView>(R.id.bottom_rv).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bottomAdapter
        }
    }

    private fun showBottomUpdateSchedule(scheduleDto: ScheduleDto) {
        val timestamp = getDayStart(scheduleDto.timeStart)
        selectedLesson = scheduleDto.lesson.toString()
        selectedLessonTheme = scheduleDto.theme.toString()
        selectedStudentEmail = scheduleDto.students
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_add_schedule_full, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(true)
        bottomDialog.show()
        dialogView.findViewById<TextView>(R.id.bottom_title).text = "Редактирование занятия"
        dialogView.findViewById<TextInputEditText>(R.id.input_subject).setText(scheduleDto.subject)
        dialogView.findViewById<MaskedEditText>(R.id.input_time_start).setText(
            SimpleDateFormat("HH:mm").format(scheduleDto.timeStart)
        )
        dialogView.findViewById<MaskedEditText>(R.id.input_time_end).setText(
            SimpleDateFormat("HH:mm").format(scheduleDto.timeEnd)
        )
        dialogView.findViewById<TextInputEditText>(R.id.input_date)
            .setText(SimpleDateFormat("dd.MM.yyyy").format(timestamp))
        dialogView.findViewById<TextView>(R.id.btn_pin_lesson).apply {
            text = if (scheduleDto.lesson != null) "Урок прикреплен" else "Выбрать урок"
            setOnClickListener {
                showBottomList("lesson", it as TextView)
            }
        }
        dialogView.findViewById<TextView>(R.id.btn_pin_student).apply {
            text = selectedStudentEmail.replace("-", ".").replace("_", "@")
            setOnClickListener {
                showBottomList("student", it as TextView)
            }
            isEnabled = scheduleDto.teacherEmail == null
        }
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener { btnSave ->
            val subject =
                dialogView.findViewById<TextInputEditText>(R.id.input_subject).text.toString()
                    .trim()
            val timeStart =
                dialogView.findViewById<MaskedEditText>(R.id.input_time_start).text.toString()
                    .trim()
            val timeEnd =
                dialogView.findViewById<MaskedEditText>(R.id.input_time_end).text.toString().trim()
            try {
                val timeStartLong = timeStringToMillis(timeStart, timestamp)
                val timeEndLong = timeStringToMillis(timeEnd, timestamp)
                if (selectedStudentEmail.isNotEmpty() && selectedLesson.isNotEmpty() && subject.isNotEmpty() && timeStart.isNotEmpty() && timeEnd.isNotEmpty()) {
                    btnSave.isEnabled = false
                    val schedule = ScheduleDto(
                        creatorEmail = scheduleDto.creatorEmail,
                        id = scheduleDto.id,
                        lesson = selectedLesson,
                        theme = selectedLessonTheme,
                        students = selectedStudentEmail,
                        subject = subject,
                        timeEnd = timeEndLong,
                        timeStart = timeStartLong,
                        teacherEmail = null
                    )
                    databaseManager.addSchedule(schedule, result = { message, id ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        if (id != null) {
                            val tempList = mutableListOf<ScheduleDto>()
                            val scheduleList = teacherViewModel.scheduleList.value ?: listOf()
                            tempList.addAll(scheduleList)
                            tempList.remove(scheduleDto)
                            tempList.add(schedule)
                            teacherViewModel.setSchedulesList(tempList)
                            bottomDialog.dismiss()
                        } else {
                            btnSave.isEnabled = true
                        }
                    })
                } else {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Время заполнено неверно", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getDayStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun timeStringToMillis(time: String, timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun isDateInDay(timestamp: Long, date: Date?): Boolean {
        date ?: return false
        val calendar = Calendar.getInstance()
        calendar.time = date
        val startOfDay = calendar.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = calendar.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        return timestamp in startOfDay.timeInMillis..endOfDay.timeInMillis
    }

    private fun showBottomExit() {
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_exit, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(true)
        bottomDialog.show()
        dialogView.findViewById<Button>(R.id.bottom_yes).setOnClickListener {
            bottomDialog.dismiss()
            databaseManager.signOutUser()
            findNavController().navigate(R.id.splashFragment)
            findNavController().popBackStack(R.id.splashFragment, false)
        }
        dialogView.findViewById<Button>(R.id.bottom_no).setOnClickListener {
            bottomDialog.dismiss()
        }
    }
}