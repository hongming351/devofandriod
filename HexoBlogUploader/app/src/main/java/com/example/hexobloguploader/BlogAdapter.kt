package com.example.hexobloguploader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class BlogAdapter(
    private val onItemClick: (Blog) -> Unit
) : ListAdapter<Blog, BlogAdapter.BlogViewHolder>(BlogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blog, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blog = getItem(position)
        holder.bind(blog)
        holder.itemView.setOnClickListener { onItemClick(blog) }
    }

    class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textBlogTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.textBlogDate)
        private val contentPreviewTextView: TextView = itemView.findViewById(R.id.textBlogContentPreview)
        private val tagsChipGroup: ChipGroup = itemView.findViewById(R.id.chipGroupTags)

        fun bind(blog: Blog) {
            titleTextView.text = blog.title
            dateTextView.text = blog.date
            contentPreviewTextView.text = blog.content.take(100) + if (blog.content.length > 100) "..." else ""

            // 清除旧的标签
            tagsChipGroup.removeAllViews()

            // 添加新的标签
            blog.tags.forEach { tag ->
                val chip = Chip(itemView.context).apply {
                    text = tag
                    isClickable = false
                    setEnsureMinTouchTargetSize(false)
                    chipMinHeight = 24f
                }
                tagsChipGroup.addView(chip)
            }
        }
    }
}

class BlogDiffCallback : DiffUtil.ItemCallback<Blog>() {
    override fun areItemsTheSame(oldItem: Blog, newItem: Blog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Blog, newItem: Blog): Boolean {
        return oldItem == newItem
    }
}