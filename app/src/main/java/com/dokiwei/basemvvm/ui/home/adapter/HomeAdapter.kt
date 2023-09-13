package com.dokiwei.basemvvm.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.model.data.ArticleDatas
import com.dokiwei.basemvvm.util.randomAvatar
import com.dokiwei.basemvvm.util.setAnim
import com.dokiwei.basemvvm.util.setImg
import com.dokiwei.basemvvm.util.text
import com.google.android.material.imageview.ShapeableImageView
import java.io.Serializable

/**
 * @author DokiWei
 * @date 2023/9/11 18:39
 */
class HomeAdapter : PagingDataAdapter<ArticleDatas, HomeAdapter.HomeViewHolder>(DiffCallback()),
    Serializable {
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, holder.itemView.context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return HomeViewHolder(view)
    }

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.title)
        private val type: TextView = view.findViewById(R.id.type)
        private val shareUser: TextView = view.findViewById(R.id.shareUser)
        private val date:TextView = view.findViewById(R.id.date)
        private val likeIcon: ImageButton = view.findViewById(R.id.like)
        private val homeItem: CardView = view.findViewById(R.id.homeItem)

        private val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        fun bind(item: ArticleDatas?, context: Context) {
            item?.let {
                val typeText = "分类:${it.chapterName}"
                homeItem.setAnim(context, R.anim.item_anim)
                avatar.setImageResource(randomAvatar())
                title.text = it.title
                type.text = typeText
                date.text = it.niceShareDate
                shareUser.text(it.shareUser!="",it.shareUser,it.author)
                likeIcon.setImg(it.collect, R.drawable.favorite_icon, R.drawable.un_favorite_icon)
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