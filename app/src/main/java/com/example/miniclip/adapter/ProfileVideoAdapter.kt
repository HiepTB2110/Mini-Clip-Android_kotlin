package com.example.miniclip.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniclip.SingleVideoPlayerActivity
import com.example.miniclip.databinding.ProfileVideoItemRowBinding
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ProfileVideoAdapter(options: FirestoreRecyclerOptions<VideoModel>) : FirestoreRecyclerAdapter<VideoModel, ProfileVideoAdapter.VideoViewHolder>(options) {
    inner class VideoViewHolder(private val binding: ProfileVideoItemRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(video : VideoModel){
            Glide.with(binding.thmbnailImageView)
                .load(video.url)
                .into(binding.thmbnailImageView)
            binding.thmbnailImageView.setOnClickListener {
                val intent = Intent(binding.thmbnailImageView.context, SingleVideoPlayerActivity::class.java)
                intent.putExtra("videoId", video.videoId)
                binding.thmbnailImageView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ProfileVideoItemRowBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bind(model)
    }
}