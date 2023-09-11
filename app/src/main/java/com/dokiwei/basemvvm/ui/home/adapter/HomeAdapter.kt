package com.dokiwei.basemvvm.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.model.data.ArticleDatas
import java.io.Serializable

/**
 * @author DokiWei
 * @date 2023/9/11 18:39
 */
class HomeAdapter : PagingDataAdapter<ArticleDatas, HomeAdapter.HomeViewHolder>(DiffCallback()),
    Serializable {
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return HomeViewHolder(view)
    }

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.title)
        private val shareUser: TextView = view.findViewById(R.id.shareUser)
        private val likeIcon: ImageButton = view.findViewById(R.id.like)

        //        private val avatar: ShapeableImageView =view.findViewById(R.id.avatar)
        fun bind(item: ArticleDatas?) {
            item?.let {
                title.text = it.title
                shareUser.text = it.shareUser
                likeIcon.setImageResource(if (it.collect) R.drawable.favorite_icon else R.drawable.un_favorite_icon)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ArticleDatas>() {

        override fun areItemsTheSame(oldItem: ArticleDatas, newItem: ArticleDatas): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ArticleDatas, newItem: ArticleDatas): Boolean {
            return oldItem == newItem
        }
    }
}