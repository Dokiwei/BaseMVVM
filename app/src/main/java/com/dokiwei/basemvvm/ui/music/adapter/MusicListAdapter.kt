package com.dokiwei.basemvvm.ui.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.model.entity.MusicEntity

/**
 * @author DokiWei
 * @date 2023/9/26 4:58
 */
class MusicListAdapter(
    musicList: List<MusicEntity>
) :
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {
    private lateinit var onItemClickListener: OnItemClickListener
    private val itemCallback = object : DiffUtil.ItemCallback<MusicEntity>() {
        override fun areItemsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean {
            return oldItem.title == newItem.title
        }

    }
    private val mDiffer = AsyncListDiffer(this, itemCallback)
    fun submit(callback: (MutableList<MusicEntity>) -> List<MusicEntity>) {
        mDiffer.submitList(callback(mDiffer.currentList))
    }
    init {
        submit { musicList }
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.musicFragment_item_list_title)
        private val author = view.findViewById<TextView>(R.id.musicFragment_item_list_author)
        private val remove = view.findViewById<ImageButton>(R.id.musicFragment_item_list_remove)
        fun bind(
            musicEntity: MusicEntity,
            position: Int,
            musicListAdapter: MusicListAdapter,
            omRemoveClick: () -> Unit
        ) {
            title.text = musicEntity.title
            author.text = musicEntity.author
            remove.setOnClickListener {
                omRemoveClick()
                musicListAdapter.notifyItemRemoved(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(musicEntity: MusicEntity, position: Int)
        fun omRemoveClick(position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_music_list, parent, false)
        return ViewHolder(view)
    }

    private var prevSelectedPosition = -1
    private var selectedPosition = -1
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mDiffer.currentList[position]
        holder.bind(
            data,
            position,
            this
        ) { onItemClickListener.omRemoveClick(holder.absoluteAdapterPosition) }
        val currentPlay = holder.itemView.findViewById<ImageView>(R.id.musicFragment_item_list_selected)
        if (holder.absoluteAdapterPosition==selectedPosition){
            currentPlay.visibility=View.VISIBLE
        }else{
            currentPlay.visibility=View.GONE
        }
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(data, holder.absoluteAdapterPosition)
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    fun setCurrentSelectItem(position: Int){
        prevSelectedPosition = selectedPosition
        selectedPosition = position
        prevSelectedPosition.takeIf { it!=-1 }?.let { notifyItemChanged(it) }
        selectedPosition.takeIf { it!=-1 }?.let { notifyItemChanged(it) }
    }
}