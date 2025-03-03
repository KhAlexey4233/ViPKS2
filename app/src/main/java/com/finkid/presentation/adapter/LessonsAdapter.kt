package com.finkid.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemLessonBinding
import com.finkid.network.dto.LessonDto

class LessonsAdapter(private val removeItem: (LessonDto) -> Unit) :
    RecyclerView.Adapter<LessonsAdapter.ItemHolder>() {
    private val adapterList = mutableListOf<LessonDto>()

    class ItemHolder(val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemLessonBinding.inflate(
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
                itemRemove.setOnClickListener {
                    removeItem(item)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<LessonDto>) {
        adapterList.clear()
        adapterList.addAll(list)
        notifyDataSetChanged()
    }
}