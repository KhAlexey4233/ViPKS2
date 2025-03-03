package com.finkid.presentation.screens.student

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.presentation.adapter.HomeworkAdapter
import com.finkid.databinding.FragmentStudentAnswersBinding

class StudentAnswersFragment : Fragment() {
  private val binding by lazy { FragmentStudentAnswersBinding.inflate(layoutInflater) }
  private val studentViewModel by lazy { (requireActivity() as MainActivity).studentViewModel }
  private val scheduleList by lazy {
    val allScheduleList =studentViewModel.scheduleList.value ?: listOf()
    allScheduleList.filter { it.lesson != null }
  }
  private val homeworkAdapter by lazy {
    HomeworkAdapter(
      adapterList = scheduleList,
      downloadDocument = { schedule ->
        downloadFile(schedule.homeworkDto?.document, schedule.theme.toString())
      },
      selectItem = { schedule ->
        studentViewModel.setSchedule(schedule)
        studentViewModel.lessonsList.value?.find { it.id == schedule.lesson }?.let { lesson ->
          studentViewModel.setLesson(lesson)
          findNavController().navigate(R.id.studentTestFragment)
        } ?: run {
          Toast.makeText(requireContext(), "Урок не найден", Toast.LENGTH_SHORT).show()
        }
      })
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = binding.root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initialize()
  }

  private fun initialize() {
    initListeners()
    initRecyclerView()
  }

  private fun initRecyclerView() {
    binding.itemsRv.layoutManager = LinearLayoutManager(requireContext())
    binding.itemsRv.adapter = homeworkAdapter
  }

  private fun initListeners() {
    binding.btnBack.setOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun downloadFile(document: String?, theme: String) {
    document?.let { url ->
      val downloadManager =
        requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
      val uri = Uri.parse(url)
      val request = DownloadManager.Request(uri)
      request.setTitle(theme)
      request.setDescription("Downloading")
      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
      request.setDestinationInExternalFilesDir(context, null, "umka_${theme}")
      downloadManager.enqueue(request)
    }

  }
}