package com.finkid.presentation.screens.student

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.finkid.MainActivity
import com.finkid.databinding.FragmentStudentTestBinding
import com.finkid.network.dto.AnswerDto

class StudentTestFragment : Fragment() {
    private val binding by lazy { FragmentStudentTestBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
    private val studentViewModel by lazy { (requireActivity() as MainActivity).studentViewModel }
    private val schedule by lazy { studentViewModel.selectedSchedule.value }
    private val lesson by lazy { studentViewModel.selectedLesson.value!! }
    private var questionIndex = 0
    private val questionsCount by lazy { lesson.homework.size }
    private var documentUri: Uri? = null
    private val pickDocument =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                documentUri = uri
                binding.btnPinDocument.text = "Решение прикреплено"
            } ?: run {
                binding.btnPinDocument.text = "Прикрепить решение"
            }
        }
    private var answersList = mutableListOf<AnswerDto>()
    private var errorAnswersList = mutableListOf<AnswerDto>()
    private var errorFixing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initLesson()
        initListeners()
    }

    private fun initLesson() {
        binding.title.text = lesson.theme
        setNewQuestion()
    }

    private fun setNewQuestion() {
        binding.answersGroup.clearCheck()
        if (questionIndex < questionsCount) {
            lesson.homework[questionIndex].let { question ->
                binding.question.text = question.question
                listOf(
                    question.answer1,
                    question.answer2,
                    question.answer3,
                    question.answerTrue
                ).shuffled()
                    .let { answers ->
                        binding.answer1.text = answers[0]
                        binding.answer2.text = answers[1]
                        binding.answer3.text = answers[2]
                        binding.answer4.text = answers[3]
                    }
            }
        } else {
            if (errorAnswersList.isNotEmpty()) {
                errorFixing = true
                binding.errorsTitle.visibility = View.VISIBLE
                lesson.homework.find { it.question == errorAnswersList[0].question }
                    ?.let { question ->
                        binding.question.text = question.question
                        listOf(
                            question.answer1,
                            question.answer2,
                            question.answer3,
                            question.answerTrue
                        ).shuffled()
                            .let { answers ->
                                binding.answer1.text = answers[0]
                                binding.answer2.text = answers[1]
                                binding.answer3.text = answers[2]
                                binding.answer4.text = answers[3]
                            }
                    }
            } else {
                binding.errorsTitle.visibility = View.GONE
                binding.answersGroup.visibility = View.GONE
                binding.question.text =
                    "Домашнее задание выполнено.\nПрикрепите решение и сохраните результат."
                errorFixing = false
                binding.btnPinDocument.visibility = View.VISIBLE
                binding.btnNext.text = "Сохранить результат"
            }
        }
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnDownload.setOnClickListener {
            downloadFile()
        }
        binding.btnNext.setOnClickListener {
            checkStudentAnswer()
        }
        binding.btnPinDocument.setOnClickListener {
            pickDocument.launch("*/*")
        }
    }

    private fun checkStudentAnswer() {
        if (questionIndex < questionsCount) {
            val selectedRadioButtonId = binding.answersGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val selectedRadioButton = view?.findViewById<RadioButton>(selectedRadioButtonId)
                val selectedAnswer = selectedRadioButton?.text.toString()
                val isTrue = selectedAnswer == lesson.homework[questionIndex].answerTrue
                val answerDto = AnswerDto(
                    question = binding.question.text.toString().trim(),
                    answer = selectedAnswer,
                    isTrue = isTrue
                )
                if (!answersList.contains(answerDto))
                    answersList.add(answerDto)
                if (!isTrue)
                    errorAnswersList.add(answerDto)
                questionIndex++
                setNewQuestion()
            } else {
                Toast.makeText(requireContext(), "Выберите вариант ответа", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            if (errorFixing) {
                val selectedRadioButtonId = binding.answersGroup.checkedRadioButtonId
                if (selectedRadioButtonId != -1) {
                    val question = binding.question.text.toString().trim()
                    val selectedRadioButton = view?.findViewById<RadioButton>(selectedRadioButtonId)
                    val selectedAnswer = selectedRadioButton?.text.toString()
                    lesson.homework.find { it.question == question }?.let { homework ->
                        val isTrue = selectedAnswer == homework.answerTrue
                        answersList.find { it.question == question }?.let {
                            it.isTrue = isTrue
                            it.answer = selectedAnswer
                        }
                    }
                    errorAnswersList.removeAt(0)
                    setNewQuestion()
                } else {
                    Toast.makeText(requireContext(), "Выберите вариант ответа", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                if (documentUri != null) {
                    homeworkCompleted()
                    binding.btnNext.apply {
                        text = "Сохраняем результат"
                        isEnabled = false
                    }
                } else {
                    Toast.makeText(requireContext(), "Прикрепите решение", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun homeworkCompleted() {
        schedule?.let { scheduleDto ->
            databaseManager.saveHomeworkResult(scheduleDto,
                documentUri!!,
                answersList,
                result = { message, homeworkDto ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (homeworkDto != null) {
                        scheduleDto.homeworkDto = homeworkDto
                        binding.result.apply {
                            visibility = View.VISIBLE
                            text =
                                "Вы выполнили домашнее задание.\nВаш результат - ${answersList.count { it.isTrue }} из $questionsCount.\nДля более детальной информации перейдите на экран домашних заданий."
                        }
                        binding.btnPinDocument.visibility = View.GONE
                        binding.answersGroup.visibility = View.GONE
                        binding.question.visibility = View.GONE
                        binding.btnNext.apply {
                            isEnabled = true
                            text = "Вернуться назад"
                            setOnClickListener {
                                findNavController().navigateUp()
                            }
                        }
                    }
                })
        } ?: run {
            Toast.makeText(requireContext(), "Занятие не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile() {
        val downloadManager =
            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(lesson.document)
        val request = DownloadManager.Request(uri)
        request.setTitle(lesson.theme)
        request.setDescription("Материал урока ${lesson.theme}")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, null, "FinKid_${lesson.theme}")
        downloadManager.enqueue(request)
    }

}