package com.shakib.mediapicker.presentation.gallery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.shakib.mediapicker.R
import com.shakib.mediapicker.common.extensions.invisible
import com.shakib.mediapicker.common.extensions.showLongToast
import com.shakib.mediapicker.common.extensions.visible
import com.shakib.mediapicker.databinding.ItemImageBinding
import com.shakib.mediapicker.api.Media
import com.shakib.mediapicker.common.utils.Constants.VIDEO_EXTENSION

class GalleryAdapter(
    private val selectedMedia: ArrayList<Media>,
    private val maxSelection: Int
) : RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {

    private val mediaList: ArrayList<Media> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageViewHolder(
        ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentImage = mediaList[position]
        holder.binding.apply {
            ivSelect.invisible()
            ivImage.apply {
                Glide.with(context)
                    .load(currentImage.uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(this)
            }
            currentImage.title.apply {
                tvName.text = this
                if (this.contains(VIDEO_EXTENSION))
                    ivPlay.visible()
                else
                    ivPlay.invisible()
            }
            itemView.setOnClickListener {
                if (selectedMedia.contains(currentImage)) {
                    selectedMedia.remove(currentImage)
                    ivSelect.invisible()
                } else if (selectedMedia.size >= maxSelection)
                    it.context.showLongToast(it.context.getString(R.string.max_selection))
                else {
                    selectedMedia.add(currentImage)
                    ivSelect.visible()
                }
            }
        }
    }

    override fun getItemCount() = mediaList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(mediaList: List<Media>) {
        this.mediaList.clear()
        this.mediaList.addAll(mediaList)
        notifyDataSetChanged()
    }

    class ImageViewHolder(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)
}
