package com.finkid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemLessonSelectBinding
import com.finkid.network.dto.LessonDto

class SelectLessonAdapter(
  private val adapterList: List<LessonDto>,
  private val selectItem: (LessonDto) -> Unit
) :
  RecyclerView.Adapter<SelectLessonAdapter.ItemHolder>() {

  class ItemHolder(val binding: ItemLessonSelectBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
    return ItemHolder(
      ItemLessonSelectBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun getItemCount(): Int {
    return adapterList.size
  }

  override fun onBindViewHolder(holder: ItemHolder, position: Int) {
    adapterList[position].let { item ->
      with(holder.binding) {
        val homeworkWord = when (item.homework.size % 10) {
          1 -> "вопроса"
          else -> "вопросов"
        }
        itemHomework.text = "Д/з из ${item.homework.size} $homeworkWord"
        itemTheme.text = item.theme
        root.setOnClickListener {
          selectItem(item)
        }
      }
    }
  }
}