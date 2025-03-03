package com.finkid.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemStudentBinding
import com.finkid.network.dto.StudentDto

class StudentsAdapter(
    private val selectItem: (StudentDto) -> Unit,
    private val removeItem: (StudentDto) -> Unit,
    private val isSchool: Boolean = false,
    private val canRemove: Boolean = true
) :
    RecyclerView.Adapter<StudentsAdapter.ItemHolder>() {
    private val adapterList = mutableListOf<StudentDto>()

    class ItemHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemStudentBinding.inflate(
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
                itemName.text = item.name
                itemRating.text = "Рейтинг: ${item.rating}%"
                itemEmail.text = item.email.replace("-", ".").replace("_", "@")
                itemPhone.text = item.phone ?: "Номер телефона не определен"
                itemRemove.setOnLongClickListener {
                    removeItem(item)
                    return@setOnLongClickListener true
                }
                if (isSchool) {
                    itemBtns.visibility = View.GONE
                } else {
                    itemBtns.visibility = View.VISIBLE
                    if (canRemove) {
                        itemRemove.visibility = View.VISIBLE
                    } else {
                        itemRemove.visibility = View.GONE
                    }
                }
                root.setOnClickListener {
                    selectItem(item)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<StudentDto>) {
        adapterList.clear()
        adapterList.addAll(list)
        notifyDataSetChanged()
    }
}