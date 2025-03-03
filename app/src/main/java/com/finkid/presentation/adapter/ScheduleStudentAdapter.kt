package com.finkid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.R
import com.finkid.databinding.ItemScheduleStudentBinding
import com.finkid.network.dto.ScheduleDto
import java.text.SimpleDateFormat

class ScheduleStudentAdapter(
  private val adapterList: List<ScheduleDto>,
  private val selectItem: (ScheduleDto) -> Unit,
) :
  RecyclerView.Adapter<ScheduleStudentAdapter.ItemHolder>() {

  class ItemHolder(val binding: ItemScheduleStudentBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
    return ItemHolder(
      ItemScheduleStudentBinding.inflate(
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
        if (item.theme != null) {
          itemTheme.text = item.theme
        } else {
          itemTheme.text = "Тема не задана"
        }
        itemSubject.text = item.subject
        item.homeworkDto?.let {
          itemState.setImageResource(R.drawable.icon_check)
          root.setOnClickListener {
          }
        } ?: run {
          itemState.setImageResource(R.drawable.icon_cross)
          root.setOnClickListener {
            selectItem(item)
          }
        }
        itemTimes.text = "${SimpleDateFormat("HH:mm").format(item.timeStart)} " +
          "- ${SimpleDateFormat("HH:mm").format(item.timeEnd)}"
      }
    }
  }
}