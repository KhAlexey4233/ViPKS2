package com.finkid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemAchievementBinding
import com.finkid.presentation.dto.AchievementDto

class AchievementAdapter(
    private val adapterList: List<AchievementDto>,
) : RecyclerView.Adapter<AchievementAdapter.ItemHolder>() {

    class ItemHolder(val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemAchievementBinding.inflate(
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
                itemImage.setImageResource(item.resInt)
                itemTitle.text = item.title
                root.alpha = if (item.opened) 1f else 0.5f
            }
        }
    }
}