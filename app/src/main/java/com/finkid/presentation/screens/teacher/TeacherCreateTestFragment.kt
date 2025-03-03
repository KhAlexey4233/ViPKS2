package com.finkid.presentation.screens.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finkid.MainActivity
import com.finkid.presentation.adapter.TestAdapter
import com.finkid.databinding.FragmentTeacherCreateTestBinding
import com.finkid.network.dto.QuestionDto

class TeacherCreateTestFragment : Fragment() {
  private val binding by lazy { FragmentTeacherCreateTestBinding.inflate(layoutInflater) }
  private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
  private val testAdapter by lazy { TestAdapter() }

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
    initListeners()
  }

  private fun initRecyclerView() {
    binding.questionsRv.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = testAdapter
    }
  }

  private fun initListeners() {
    binding.btnBack.setOnClickListener {
      findNavController().navigateUp()
    }
    binding.btnSave.setOnClickListener {
      trySaveTest()
    }
    binding.btnAddQuestion.setOnClickListener {
      testAdapter.addItem(QuestionDto("", "", "", "", ""))
    }
  }

  private fun trySaveTest() {
    val adapterList = testAdapter.getList()
    if (adapterList.any {
        it.answer1.isEmpty() || it.answer2.isEmpty() || it.answer3.isEmpty()
          || it.question.isEmpty()
      }) {
      Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
    } else {
      teacherViewModel.setCurrentQuestions(adapterList)
      findNavController().navigateUp()
    }
  }
}