package com.vtutranslate.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vtutranslate.R
import com.vtutranslate.models.StringResource

class StringResourceAdapter : ListAdapter<StringResource, StringResourceAdapter.ViewHolder>(StringResourceDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStringName: TextView = itemView.findViewById(R.id.tvStringName)
        private val tvStringValue: TextView = itemView.findViewById(R.id.tvStringValue)
        private val tvViValue: TextView = itemView.findViewById(R.id.tvViValue)
        
        fun bind(item: StringResource) {
            tvStringName.text = item.name
            tvStringValue.text = item.value
            tvViValue.text = if (item.translatedValue.isEmpty()) "-" else item.translatedValue
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_string_value, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class StringResourceDiffCallback : DiffUtil.ItemCallback<StringResource>() {
    override fun areItemsTheSame(oldItem: StringResource, newItem: StringResource): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: StringResource, newItem: StringResource): Boolean {
        return oldItem == newItem
    }
} 