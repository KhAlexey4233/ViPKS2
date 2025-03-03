package com.finkid.presentation.screens.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.databinding.FragmentStudentProgressBinding
import com.finkid.network.dto.HomeworkDto
import com.finkid.presentation.adapter.AchievementAdapter
import com.finkid.presentation.dto.AchievementDto
import com.finkid.presentation.dto.RankDto

class StudentProgressFragment : Fragment() {
    private val binding by lazy { FragmentStudentProgressBinding.inflate(layoutInflater) }
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
            binding.tvRating.text = "${value?.rating ?: 0}%"
        }
        studentViewModel.homeworkList.observe(viewLifecycleOwner) { value ->
            initRank(value)
            initAchievements(value)
        }
    }

    private fun initAchievements(list: List<HomeworkDto>) {
        val completedTasks = list.filter { it.answersList.isNotEmpty() }
        val achievementList = listOf(
            AchievementDto(
                title = "Первый шаг",
                resInt = R.drawable.img_achievement_1,
                opened = completedTasks.isNotEmpty()
            ),
            AchievementDto(
                title = "Знаток финансов",
                resInt = R.drawable.img_achievement_2,
                opened = completedTasks.size >= 5
            ),
            AchievementDto(
                title = "Финансовый мудрец",
                resInt = R.drawable.img_achievement_3,
                opened = completedTasks.size >= 10
            ),
            AchievementDto(
                title = "Мастер ответов",
                resInt = R.drawable.img_achievement_4,
                opened = list.any { it.answersList.map { answer -> answer.isTrue }.size * 2 > it.answersList.size }
            ),
            AchievementDto(
                title = "Экономический чемпион",
                resInt = R.drawable.img_achievement_5,
                opened = list.any { it.answersList.all { answer -> answer.isTrue } }
            ),
//            AchievementDto(
//                title = "Гуру финансовых тестов",
//                resInt = R.drawable.img_achievement_6,
//            ),
//            AchievementDto(
//                title = "Богатей знаний",
//                resInt = R.drawable.img_achievement_7,
//            ),
            AchievementDto(
                title = "Финансовый шопоголик",
                resInt = R.drawable.img_achievement_8,
                opened = false
            ),
        )
        val achievementAdapter = AchievementAdapter(adapterList = achievementList)
        binding.rvAchievements.apply {
            adapter = achievementAdapter
        }
    }

    private fun initRank(list: List<HomeworkDto>) {
        val completedTasks = list.filter { it.answersList.isNotEmpty() }.size
        val currentLevel = completedTasks / 5 + 1
        val completedForCurrentLevel = completedTasks % 5
        val remainingTasksToNextLevel = 5 - completedForCurrentLevel
        val progressToNextLevel = (completedForCurrentLevel * 20)
        val totalProgress = if (currentLevel < 4) {
            progressToNextLevel + (remainingTasksToNextLevel * 20 / 5) - 20
        } else {
            100
        }
        binding.progressBar.progress = totalProgress
        binding.tvLevel.text = "$currentLevel уровень"
        val rankList = listOf(
            RankDto(
                title = "Новичок",
                resInt = R.drawable.img_rank_1
            ),
            RankDto(
                title = "Финансовый консультант",
                resInt = R.drawable.img_rank_2
            ),
            RankDto(
                title = "Инвестиционный аналитик",
                resInt = R.drawable.img_rank_3
            ),
            RankDto(
                title = "Финансовый стратег",
                resInt = R.drawable.img_rank_4
            ),
            RankDto(
                title = "Гуру инвестиций",
                resInt = R.drawable.img_rank_5
            ),
            RankDto(
                title = "Профессиональный трейдер",
                resInt = R.drawable.img_rank_6
            ),
            RankDto(
                title = "Маститый финансист",
                resInt = R.drawable.img_rank_7
            ),
            RankDto(
                title = "Финансовый магнат",
                resInt = R.drawable.img_rank_8
            )
        )
        val rankIndex = if (currentLevel - 1 < 7) currentLevel - 1 else 7
        val levelRank = rankList[rankIndex]
        binding.tvRank.text = levelRank.title
        binding.ivRank.setImageResource(levelRank.resInt)
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}