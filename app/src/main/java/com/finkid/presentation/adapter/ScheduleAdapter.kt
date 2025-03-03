package com.finkid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemScheduleBinding
import com.finkid.network.dto.ScheduleDto
import java.text.SimpleDateFormat

class ScheduleAdapter(
    private val adapterList: List<ScheduleDto>,
    private val selectItem: (ScheduleDto) -> Unit,
    private val removeItem: (ScheduleDto) -> Unit = {},
) :
    RecyclerView.Adapter<ScheduleAdapter.ItemHolder>() {

    class ItemHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemScheduleBinding.inflate(
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
                itemFullname.text = item.students.replace("|", ", ")
                itemTimes.text = "${SimpleDateFormat("HH:mm").format(item.timeStart)} " +
                        "- ${SimpleDateFormat("HH:mm").format(item.timeEnd)}"
                itemRemove.setOnLongClickListener {
                    removeItem(item)
                    return@setOnLongClickListener true
                }
                root.setOnClickListener {
                    selectItem(item)
                }
            }
        }
    }
}