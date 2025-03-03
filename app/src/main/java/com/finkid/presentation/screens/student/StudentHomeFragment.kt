package com.finkid.presentation.screens.student

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
import com.finkid.databinding.FragmentStudentHomeBinding
import com.finkid.presentation.adapter.ScheduleStudentAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class StudentHomeFragment : Fragment() {
    private val binding by lazy { FragmentStudentHomeBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
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
        initViewModel()
        initListeners()
    }

    private fun initViewModel() {
        studentViewModel.student.observe(viewLifecycleOwner) { value ->
            binding.name.text = value?.name ?: "Заполните профиль"
        }
        studentViewModel.scheduleList.observe(viewLifecycleOwner) { value ->
            binding.calendar.removeDecorators()
            val eventDates = value.filter { it.lesson != null }.map { it.timeStart }
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
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.studentProfileFragment)
        }
        binding.btnHomeworks.setOnClickListener {
            findNavController().navigate(R.id.studentAnswersFragment)
        }
        binding.btnProgress.setOnClickListener {
            findNavController().navigate(R.id.studentProgressFragment)
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
            val scheduleList =
                studentViewModel.scheduleList.value?.filter { it.lesson != null } ?: listOf()
            val hasLesson = scheduleList.any { isDateInDay(it.timeStart, date.date) }
            if (hasLesson) {
                scheduleList.find { isDateInDay(it.timeStart, date.date) }
                    ?.let { showBottomLesson(calendar.timeInMillis) }
            }
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
        val scheduleList =
            studentViewModel.scheduleList.value?.filter { it.lesson != null } ?: listOf()
        val filteredSchedule =
            scheduleList.filter { it.timeStart in timestamp..timestamp + 86400000 }
        val bottomAdapter =
            ScheduleStudentAdapter(
                filteredSchedule.sortedBy { it.timeStart },
                selectItem = { item ->
                    studentViewModel.setSchedule(item)
                    studentViewModel.lessonsList.value?.find { it.id == item.lesson }
                        ?.let { lesson ->
                            studentViewModel.setLesson(lesson)
                            bottomDialog.dismiss()
                            findNavController().navigate(R.id.studentTestFragment)
                        } ?: run {
                        Toast.makeText(requireContext(), "Урок не найден", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        dialogView.findViewById<Button>(R.id.bottom_add).visibility = View.GONE
        dialogView.findViewById<RecyclerView>(R.id.bottom_rv).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bottomAdapter
        }
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