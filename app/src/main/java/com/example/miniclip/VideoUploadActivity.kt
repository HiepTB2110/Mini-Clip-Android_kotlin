package com.example.miniclip

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.miniclip.databinding.ActivityVideoUploadBinding
import com.example.miniclip.model.VideoModel
import com.example.miniclip.util.UiUtil
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class VideoUploadActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoUploadBinding
    private var selectedVideoUri : Uri? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //đăng ký một hoạt động (activity) để nhận kết quả trả về từ một intent khởi chạy
        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == RESULT_OK) {
                selectedVideoUri = result.data?.data
                showPostView()
            }
        }
        binding.uploadVideo.setOnClickListener{
            checkPermissionAndOpenVideoPicker()
        }
        binding.submitPostBtn.setOnClickListener {
            postVideo()
        }
        binding.cancelPostBtn.setOnClickListener {
            finish()
        }
    }

    private fun postVideo() {
        // ktra cap video co rong ko
        if (binding.postCaptionInput.text.toString().isEmpty()){
            binding.postCaptionInput.setError("Write somthing")
            return
        }

        setInProgress(true);
        selectedVideoUri?.apply {
            // store in firebase cloud stroage
            val videoRef = FirebaseStorage.getInstance().reference.child("videos/"+ this.lastPathSegment)
            videoRef.putFile(this).addOnSuccessListener {
                videoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // video model in firebase firestore
                    postToFirebase(downloadUrl.toString())
                }
            }

        }
    }

    private fun postToFirebase(url: String) {
        // tao model video
        val videoModel = VideoModel(
            FirebaseAuth.getInstance().currentUser?.uid!! + "_"+Timestamp.now().toString(),
            binding.postCaptionInput.text.toString(),
            url,
            FirebaseAuth.getInstance().currentUser?.uid!!,
            Timestamp.now(),
        )
        // dat vao kho luu tru
        Firebase.firestore.collection("videos")
            .document(videoModel.videoId)
            .set(videoModel)
            .addOnSuccessListener {
                setInProgress(false)
                UiUtil.showToast(applicationContext, "Video Uploaded")
                finish()
            }.addOnFailureListener {
                setInProgress(false)
                UiUtil.showToast(applicationContext, "Video failed to Upload")
            }
    }

    private fun setInProgress(inProgress: Boolean){
        if (inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.submitPostBtn.visibility = View.GONE
        }else{
            binding.progressBar.visibility = View.GONE
            binding.submitPostBtn.visibility = View.VISIBLE
        }
    }

    // chon video xong thi giao dien postvideo dc cho hien len
    private fun showPostView() {
        //hien video sau khi chon
        selectedVideoUri?.let {
            binding.postVideo.visibility = View.VISIBLE
            binding.uploadVideo.visibility = View.GONE
            Glide.with(binding.postThumbnailView).load(it).into(binding.postThumbnailView)
        }
    }

    //kiem tra và cap quyen lay video tu thiet bi
    private fun checkPermissionAndOpenVideoPicker() {
        var readExternalVideo : String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            readExternalVideo = android.Manifest.permission.READ_MEDIA_VIDEO
        }else{
            readExternalVideo = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this,readExternalVideo) == PackageManager.PERMISSION_GRANTED){
            openVideoPicker()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalVideo),
                100
            )
        }
    }

    // mo trinh chon video
    private fun openVideoPicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        // dam bao rang chi file video dc chon
        intent.type = "video/*"
        videoLauncher.launch(intent)
    }
}