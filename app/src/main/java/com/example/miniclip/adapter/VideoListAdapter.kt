package com.example.miniclip.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.example.miniclip.ProfileActivity2
import com.example.miniclip.R
import com.example.miniclip.databinding.ActivityVideoUploadBinding
import com.example.miniclip.databinding.VideoItemRowBinding
import com.example.miniclip.model.UserModel
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

//Lớp VideoListAdapter:
//Lớp này mở rộng từ FirestoreRecyclerAdapter và nhận một tham số là FirestoreRecyclerOptions với kiểu VideoModel. Nó được sử dụng để chuyển đổi dữ liệu từ Firestore sang RecyclerView.
//Lớp VideoViewHolder bên trong:
//Đây là một lớp bên trong trong VideoListAdapter mở rộng từ RecyclerView.ViewHolder. Nó đại diện cho giao diện cho mỗi mục trong RecyclerView.
//Phương thức bindVideo:
//Phương thức này liên kết dữ liệu của một đối tượng VideoModel vào các thành phần trong VideoViewHolder. Nó thiết lập đường dẫn của video, chuẩn bị video và xử lý sự kiện nhấn để phát/ tạm dừng video.
//Phương thức onCreateViewHolder:
//Phương thức này tạo một VideoViewHolder mới. Nó bao gồm việc inflate layout cho mỗi mục trong RecyclerView sử dụng VideoItemRowBinding.
//Phương thức onBindViewHolder:
//Phương thức này được RecyclerView gọi để hiển thị dữ liệu tại một vị trí cụ thể. Nó gọi phương thức bindVideo của VideoViewHolder để liên kết dữ liệu cho VideoModel tương ứng.

class VideoListAdapter(options: FirestoreRecyclerOptions<VideoModel>) : FirestoreRecyclerAdapter<VideoModel, VideoListAdapter.VideoViewHolder>(options) {

    inner class VideoViewHolder(private val binding: VideoItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindVideo(videoModel: VideoModel) {

            //bindUserData
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get().addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        // bind profile pic
                        Glide.with(binding.profileIcon).load(profilePic)
                            .circleCrop()
                            .apply(
                                RequestOptions().placeholder(R.drawable.icon_account_circle)
                            ).into(binding.profileIcon)
                        binding.userDetailLayout.setOnClickListener{
                            val intent = Intent(binding.userDetailLayout.context, ProfileActivity2::class.java)
                            intent.putExtra("profile_user_id", id)
                            binding.userDetailLayout.context.startActivity(intent)
                        }
                    }
                }

            binding.captionView.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE

            //bindVideo
            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }
                setOnClickListener {
                    if (isPlaying) {
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    } else {
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bindVideo(model)
    }
}