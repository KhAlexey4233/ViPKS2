package com.finkid.presentation.screens.teacher

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.presentation.adapter.LessonsAdapter
import com.finkid.databinding.FragmentTeacherLessonsBinding
import com.finkid.network.dto.LessonDto

class TeacherLessonsFragment : Fragment() {
  private val binding by lazy { FragmentTeacherLessonsBinding.inflate(layoutInflater) }
  private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
  private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
  private var documentUri: Uri? = null
  private val pickDocument =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      uri?.let {
        documentUri = uri
        binding.btnPinDocument.text = "Материал прикреплен"
      } ?: run {
        binding.btnPinDocument.text = "Прикрепить материал"
      }
    }
  private val lessonsAdapter by lazy {
    LessonsAdapter(removeItem = { item ->
      databaseManager.removeLesson(item.id, result = { message, success ->
        val tempList = mutableListOf<LessonDto>()
        tempList.addAll(teacherViewModel.lessonsList.value ?: listOf())
        tempList.remove(item)
        teacherViewModel.setLessonsList(tempList)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
      })
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
    initRecyclerView()
    initViewModel()
    initListeners()
  }

  private fun initRecyclerView() {
    binding.itemsRv.layoutManager = LinearLayoutManager(requireContext())
    binding.itemsRv.adapter = lessonsAdapter
  }

  private fun initViewModel() {
    teacherViewModel.currentQuestions.observe(viewLifecycleOwner) { value ->
      if (value.isNotEmpty()) {
        binding.btnCreateHw.text = "Д/з создано"
      } else {
        binding.btnCreateHw.text = "Создать домашнее задание"
      }
    }
    teacherViewModel.lessonsList.observe(viewLifecycleOwner) { value ->
      lessonsAdapter.updateList(value)
    }
  }

  private fun initListeners() {
    binding.btnBack.setOnClickListener {
      findNavController().navigateUp()
    }
    binding.btnAdd.setOnClickListener {
      binding.alphaView.visibility = View.VISIBLE
      binding.bottomCreateLesson.visibility = View.VISIBLE
      ObjectAnimator.ofFloat(binding.bottomCreateLesson, View.TRANSLATION_Y, 2000f, 0f).apply {
        duration = 500
        start()
      }
    }
    binding.btnCancel.setOnClickListener {
      hideBottomAdd()
    }
    binding.btnPinDocument.setOnClickListener {
      pickDocument.launch("*/*")
    }
    binding.btnCreateHw.setOnClickListener {
      findNavController().navigate(R.id.teacherCreateTestFragment)
    }
    binding.alphaView.setOnClickListener { }
    binding.bottomCreateLesson.setOnClickListener { }
    binding.btnSave.setOnClickListener {
      trySaveLesson(binding.inputTheme.text.toString().trim(), it as Button)
    }
  }

  private fun hideBottomAdd() {
    binding.alphaView.visibility = View.GONE
    ObjectAnimator.ofFloat(binding.bottomCreateLesson, View.TRANSLATION_Y, 0f, 2000f).apply {
      duration = 500
      start()
    }
    Handler(Looper.getMainLooper()).postDelayed({
      if (isAdded) {
        binding.bottomCreateLesson.visibility = View.INVISIBLE
      }
    }, 500)
  }

  private fun trySaveLesson(theme: String, btnSave: Button) {
    if (theme.isNotEmpty() && documentUri != null && (teacherViewModel.currentQuestions.value
        ?: listOf()).isNotEmpty()
    ) {
      btnSave.isEnabled = false
      val lesson = LessonDto(
        id = "",
        theme = theme,
        document = documentUri.toString(),
        homework = teacherViewModel.currentQuestions.value ?: listOf(),
      )
      databaseManager.createLesson(lesson, result = { message, id ->
        Toast.makeText(requireContext(), "$message", Toast.LENGTH_SHORT).show()
        if (id != null) {
          val tempList = mutableListOf<LessonDto>()
          tempList.addAll(teacherViewModel.lessonsList.value ?: listOf())
          tempList.add(lesson.copy(id = id))
          teacherViewModel.setLessonsList(tempList)
          teacherViewModel.setCurrentQuestions(listOf())
          hideBottomAdd()
        } else {
          btnSave.isEnabled = true
        }
      })
    } else {
      Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
    }
  }
}