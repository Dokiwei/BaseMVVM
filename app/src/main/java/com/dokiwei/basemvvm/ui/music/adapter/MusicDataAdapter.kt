package com.dokiwei.basemvvm.ui.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.model.data.MusicData
import com.dokiwei.basemvvm.util.MetadataReaderUtils
import com.dokiwei.basemvvm.util.setAnim
import com.google.android.material.imageview.ShapeableImageView

class MusicDataAdapter(
    private val musicDataList: List<MusicData>,
    private val fragment: Fragment
) : RecyclerView.Adapter<MusicDataAdapter.ViewHolder>() {
    private lateinit var onItemClickListener: OnItemClickListener

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val album: TextView = view.findViewById(R.id.album)
        private val avatar: ShapeableImageView = view.findViewById(R.id.avatar)
        private val likeIcon: ImageButton = view.findViewById(R.id.like)
        fun bind(item: MusicData?, fragment: Fragment) {
            item?.let {
                name.text = it.name
                album.text = it.album
                it.imgId?.let { imgId ->
                    Glide.with(fragment)
                        .load(MetadataReaderUtils.getAlbumArt(fragment.requireContext(), imgId))
                        .skipMemoryCache(true)
                        .transition(withCrossFade(DrawableCrossFadeFactory.Builder().build()) )
                        .into(avatar)
                }
                likeIcon.setImageResource(R.drawable.un_favorite_icon)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(musicData: MusicData, position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = musicDataList[position]
        holder.bind(data,fragment)
        holder.itemView.setAnim(fragment.requireContext(), R.anim.item_anim)
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(data, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicDataList.size
    }


}
