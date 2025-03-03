package com.finkid.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finkid.R
import com.finkid.databinding.ItemHomeworkBinding
import com.finkid.network.dto.ScheduleDto

class HomeworkAdapter(
  private val adapterList: List<ScheduleDto>,
  private val downloadDocument: (ScheduleDto) -> Unit,
  private val selectItem: (ScheduleDto) -> Unit
) :
  RecyclerView.Adapter<HomeworkAdapter.ItemHolder>() {

  class ItemHolder(val binding: ItemHomeworkBinding) :
    RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
    return ItemHolder(
      ItemHomeworkBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun getItemCount(): Int {
    return adapterList.size
  }

  @SuppressLint("NotifyDataSetChanged")
  override fun onBindViewHolder(holder: ItemHolder, position: Int) {
    adapterList[position].let { item ->
      with(holder.binding) {
        item.homeworkDto?.let { homeworkDto ->
          val answersAdapter = AnswersAdapter(homeworkDto.answersList)
          itemAnswers.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = answersAdapter
          }
          if (item.isExpanded) {
            itemAnswers.visibility = View.VISIBLE
          } else {
            itemAnswers.visibility = View.GONE
          }
          val correctAnswers = homeworkDto.answersList.count { it.isTrue }
          val answersCount = homeworkDto.answersList.size
          val percentResult = (correctAnswers.toFloat() * 100 / answersCount.toFloat()).toInt()
          itemResult.text = "$correctAnswers из $answersCount | $percentResult%"
          itemDownload.setImageResource(R.drawable.icon_download)
          root.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyDataSetChanged()
          }
        } ?: run {
          itemResult.text = "Задание не выполнено"
          itemDownload.setImageResource(R.drawable.icon_cross)
          root.setOnClickListener {
            selectItem(item)
          }
        }
        itemTheme.text = item.theme
        itemDownload.setOnClickListener {
          downloadDocument(item)
        }
      }
    }
  }
}