package com.finkid.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemQuestionBinding
import com.finkid.network.dto.QuestionDto
import com.finkid.utils.AppTextWatcher

class TestAdapter : RecyclerView.Adapter<TestAdapter.ItemHolder>() {
  private val adapterList = mutableListOf<QuestionDto>()

  class ItemHolder(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
    return ItemHolder(
      ItemQuestionBinding.inflate(
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
        itemQuestion.addTextChangedListener(AppTextWatcher {
          adapterList[position].question = itemQuestion.text.toString().trim()
        })
        itemAnswer1.addTextChangedListener(AppTextWatcher {
          adapterList[position].answer1 = itemAnswer1.text.toString().trim()
        })
        itemAnswer2.addTextChangedListener(AppTextWatcher {
          adapterList[position].answer2 = itemAnswer2.text.toString().trim()
        })
        itemAnswer3.addTextChangedListener(AppTextWatcher {
          adapterList[position].answer3 = itemAnswer3.text.toString().trim()
        })
        itemAnswerTrue.addTextChangedListener(AppTextWatcher {
          adapterList[position].answerTrue = itemAnswerTrue.text.toString().trim()
        })
        itemRemove.setOnClickListener {
          adapterList.remove(item)
          notifyDataSetChanged()
        }
      }
    }
  }

  fun addItem(item: QuestionDto) {
    adapterList.add(item)
    notifyItemInserted(adapterList.size)
  }

  fun getList() = adapterList
}