package com.dokiwei.basemvvm.ui.music.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.model.entity.MusicEntity
import com.dokiwei.basemvvm.util.ColorUtil
import com.dokiwei.basemvvm.util.Conversion
import com.dokiwei.basemvvm.util.setAnim
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.utilities.CorePalette
import com.google.android.material.imageview.ShapeableImageView
import com.mpatric.mp3agic.Mp3File

class MusicDataAdapter(
    private val fragment: Fragment,
    private val recyclerview: RecyclerView,
    @LayoutRes private val layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var onItemClickListener: OnItemClickListener

    companion object {
        private const val TYPE_EMPTY = 1
        private const val TYPE_NORMAL = 0
    }


    private val itemCallback = object : DiffUtil.ItemCallback<MusicEntity>() {
        override fun areItemsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean {
            return oldItem.title == newItem.title
        }

    }

    val mDiffer = AsyncListDiffer(this, itemCallback)

    /**
     * 提交新数据
     *
     * @param callback 在作用域中传递一个旧的数据,传回一个新的数据
     */
    fun submit(callback: (MutableList<MusicEntity>) -> List<MusicEntity>) {
        mDiffer.submitList(callback(mDiffer.currentList))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NORMAL -> {
                val view = layoutInflater.inflate(layout, parent, false)
                view.setAnim(fragment.requireContext(), R.anim.anim_item)
                ViewHolder(view)
            }

            else -> {
                val view = layoutInflater.inflate(R.layout.empty_layout, parent, false)
                EmptyViewHolder(view)
            }
        }
    }
    private var bitmap: Bitmap? = null
    private var prevSelectedPosition = -1
    var selectedPosition = -1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val data = mDiffer.currentList[position]
            holder.bind(data, fragment)
            if (recyclerview.scrollState == SCROLL_STATE_DRAGGING || recyclerview.scrollState == SCROLL_STATE_SETTLING) {
                holder.itemView.setAnim(fragment.requireContext(), R.anim.anim_item)
            } else {
                holder.itemView.clearAnimation()
            }
            val currentPlay = holder.itemView.findViewById<ShapeableImageView>(R.id.musicFragment_item_play)
            val cardView = holder.itemView.findViewById<MaterialCardView>(R.id.item_music)
            val title: TextView = holder.itemView.findViewById(R.id.musicFragment_item_title)
            val album: TextView = holder.itemView.findViewById(R.id.musicFragment_item_album)
            val author: TextView = holder.itemView.findViewById(R.id.musicFragment_item_author)
            if (holder.absoluteAdapterPosition == selectedPosition) {
                mDiffer.currentList[selectedPosition].path?.let {file->
                    Mp3File(file).apply {
                        if (hasId3v2Tag()) {
                            val tag = id3v2Tag
                            bitmap = Conversion.byteArrayToBitmap(tag.albumImage)
                            bitmap?.let {img->
                                Palette.from(img).generate { palette ->
                                    val pair = ColorUtil.paletteColor(palette)
                                    pair?.let { colors ->
                                        currentPlay.setBackgroundColor(colors.first)
                                        currentPlay.setColorFilter(colors.second)
                                        cardView.setCardBackgroundColor(colors.first)
                                        title.setTextColor(colors.second)
                                        album.setTextColor(colors.second)
                                        author.setTextColor(colors.second)
                                    }
                                }
                            }
                        }
                    }
                }
                currentPlay.visibility = View.VISIBLE
            } else {
                currentPlay.visibility = View.GONE
                cardView.setCardBackgroundColor(Color.TRANSPARENT)
                val color = ContextCompat.getColor(fragment.requireContext(), R.color.colorOnBackground)
                title.setTextColor(color)
                album.setTextColor(color)
                author.setTextColor(color)
            }
            holder.itemView.setOnClickListener {
                onItemClickListener.onItemClick(
                    mDiffer.currentList[holder.absoluteAdapterPosition],
                    holder.absoluteAdapterPosition
                )
            }
        }
    }

    fun setCurrentSelectItem(position: Int) {
        prevSelectedPosition = selectedPosition
        selectedPosition = position
        prevSelectedPosition.takeIf { it != -1 }?.let { notifyItemChanged(it) }
        selectedPosition.takeIf { it != -1 }?.let { notifyItemChanged(it) }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDiffer.currentList.isEmpty()) TYPE_EMPTY else TYPE_NORMAL
    }

    override fun getItemCount(): Int {
        return if (mDiffer.currentList.isEmpty()) 1 else mDiffer.currentList.size
    }

    interface OnItemClickListener {
        fun onItemClick(musicData: MusicEntity, position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        Glide.with(fragment).clear(holder.itemView.findViewById<ShapeableImageView>(R.id.musicFragment_item_avatar))
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.musicFragment_item_title)
        private val album: TextView = view.findViewById(R.id.musicFragment_item_album)
        private val author: TextView = view.findViewById(R.id.musicFragment_item_author)
        private val avatar: ShapeableImageView = view.findViewById(R.id.musicFragment_item_avatar)

        fun bind(item: MusicEntity?, fragment: Fragment) {
            item?.let { entity ->
                title.text = entity.title
                album.text = entity.album
                author.text = entity.author
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    entity.imgId?.let { imgId ->
                        if (avatar.tag != imgId) {
                            avatar.post {
                                Glide.with(fragment).load(
                                    Conversion.albumIdToBitmap(
                                        imgId, avatar.width
                                    )
                                ).placeholder(R.drawable.ic_music).override(
                                    avatar.width, avatar.height
                                ).diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .addListener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Drawable>,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable,
                                            model: Any,
                                            target: Target<Drawable>?,
                                            dataSource: DataSource,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            avatar.tag = imgId
                                            return false
                                        }

                                    }).into(avatar)
                            }
                        }
                    }
                } else {
                    entity.path?.let { path ->
                        if (avatar.tag != path) {
                            avatar.post {
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(path)
                                val art = retriever.embeddedPicture
                                retriever.release()
                                art?.let {
                                    Glide.with(fragment).load(it).placeholder(R.drawable.ic_music)
                                        .override(
                                            avatar.width, avatar.height
                                        ).diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .addListener(object : RequestListener<Drawable> {
                                            override fun onLoadFailed(
                                                e: GlideException?,
                                                model: Any?,
                                                target: Target<Drawable>,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                return false
                                            }

                                            override fun onResourceReady(
                                                resource: Drawable,
                                                model: Any,
                                                target: Target<Drawable>?,
                                                dataSource: DataSource,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                avatar.tag = path
                                                return false
                                            }

                                        }).into(avatar)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
