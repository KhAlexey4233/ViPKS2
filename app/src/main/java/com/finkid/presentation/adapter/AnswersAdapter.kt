package com.finkid.presentation.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.finkid.R
import com.finkid.databinding.ItemAnswerBinding
import com.finkid.network.dto.AnswerDto

class AnswersAdapter(
    private val adapterList: List<AnswerDto>,
) : RecyclerView.Adapter<AnswersAdapter.ItemHolder>() {

    class ItemHolder(val binding: ItemAnswerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemAnswerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return adapterList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        adapterList[position].let { item ->
            with(holder.binding) {
                itemQuestion.setText(item.question)
                itemAnswer.setText(item.answer)
                if (item.isTrue) {
                    itemAnswer.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            holder.itemView.context, R.color.blue_100
                        )
                    )
                } else {
                    itemAnswer.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            holder.itemView.context, R.color.red_400
                        )
                    )
                }
            }
        }
    }
}