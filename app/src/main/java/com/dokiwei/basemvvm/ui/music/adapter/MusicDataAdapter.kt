package com.dokiwei.basemvvm.ui.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.model.data.MusicData
import com.dokiwei.basemvvm.util.MetadataReaderUtils
import com.dokiwei.basemvvm.util.setAnim
import com.google.android.material.imageview.ShapeableImageView

class MusicDataAdapter(
    private val musicDataList: List<MusicData>,
    private val fragment: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var onItemClickListener: OnItemClickListener

    companion object {
        private const val TYPE_EMPTY = 1
        private const val TYPE_NORMAL = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater=LayoutInflater.from(parent.context)
        return when(viewType){
            TYPE_NORMAL -> {
                val view = layoutInflater.inflate(R.layout.item_music, parent, false)
                ViewHolder(view)
            }
            else ->{
                val view = layoutInflater.inflate(R.layout.empty_layout, parent, false)
                EmptyViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (musicDataList.isEmpty()) TYPE_EMPTY else TYPE_NORMAL
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is  ViewHolder){
            val data = musicDataList[position]
            holder.bind(data, fragment)
            holder.itemView.setAnim(fragment.requireContext(), R.anim.item_anim)
            holder.itemView.setOnClickListener {
                onItemClickListener.onItemClick(data, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (musicDataList.isEmpty()) 1 else musicDataList.size
    }

    interface OnItemClickListener {
        fun onItemClick(musicData: MusicData, position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val album: TextView = view.findViewById(R.id.musicController_album_fragmentMusic)
        private val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        private val likeIcon: ImageButton = view.findViewById(R.id.like)
        fun bind(item: MusicData?, fragment: Fragment) {
            item?.let {
                name.text = it.name
                album.text = it.album
                it.imgId?.let { imgId ->
                    Glide.with(fragment)
                        .load(
                            MetadataReaderUtils.getAlbumArt(fragment.requireContext(), imgId,avatar.width)
                        )
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.music_icon)
                        .override(avatar.width, avatar.height)
                        .into(avatar)
                }
                likeIcon.setImageResource(R.drawable.un_favorite_icon)
            }
        }
    }
    class EmptyViewHolder(view: View):RecyclerView.ViewHolder(view)
}
