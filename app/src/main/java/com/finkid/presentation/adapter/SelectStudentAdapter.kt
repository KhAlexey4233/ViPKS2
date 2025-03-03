package com.finkid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finkid.databinding.ItemStudentSelectBinding
import com.finkid.network.dto.StudentDto

class SelectStudentAdapter(
    private val adapterList: List<StudentDto>,
    private val selectItem: (StudentDto) -> Unit
) :
    RecyclerView.Adapter<SelectStudentAdapter.ItemHolder>() {

    class ItemHolder(val binding: ItemStudentSelectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemStudentSelectBinding.inflate(
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
                itemEmail.text = item.email.replace("-", ".").replace("_", "@")
                root.setOnClickListener {
                    selectItem(item)
                }
            }
        }
    }
}