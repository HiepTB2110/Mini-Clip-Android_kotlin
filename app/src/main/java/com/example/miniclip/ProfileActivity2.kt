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
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.miniclip.adapter.ProfileVideoAdapter
import com.example.miniclip.databinding.ActivityProfile2Binding
import com.example.miniclip.model.UserModel
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityProfile2Binding
    lateinit var profileUserId: String
    lateinit var currentUserId: String
    lateinit var photoLauncher: ActivityResultLauncher<Intent>

    lateinit var adapter : ProfileVideoAdapter

    lateinit var profileUserModel: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfile2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // lay userId tu ben profileactivity gui
        profileUserId = intent.getStringExtra("profile_user_id")!!
        // lay userId cua nguoi an vao profile nguoi dang video
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    //upload photo
                    uploadToFirestore(result.data?.data!!)
                }
            }

        if (profileUserId == currentUserId) {
            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener {
                logout()
            }
            binding.profilePic.setOnClickListener {
                checkPermissionAndPickPhoto()
            }
        } else {
            binding.profileBtn.text = "Follow"
            binding.profileBtn.setOnClickListener {
                followUnfollowUser()
            }
        }
        getProfileDataFromFirebase()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("uploaderId", profileUserId)
                    .orderBy("createdTime", Query.Direction.DESCENDING),
                VideoModel::class.java
            ).build()
        adapter = ProfileVideoAdapter(options)
        binding.recyclerView.layoutManager = GridLayoutManager(this,3)
        binding.recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

    private fun followUnfollowUser() {
        Firebase.firestore.collection("users").document(currentUserId).get().addOnSuccessListener {
            val currentUserModel = it.toObject(UserModel::class.java)!!

            // check xem co follow chua
            if (profileUserModel.followerList.contains(currentUserId)){
                //unfollow
                profileUserModel.followerList.remove(currentUserId)
                currentUserModel.followingList.remove((profileUserId))
                binding.profileBtn.text = "Follow"
            }else{
                //follow user
                profileUserModel.followerList.add(currentUserId)
                currentUserModel.followingList.add((profileUserId))
                binding.profileBtn.text = "Unfollow"
            }
            updateUserData(profileUserModel)
            updateUserData(currentUserModel)
        }
    }

    private fun updateUserData(model: UserModel) {
        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    private fun uploadToFirestore(photoUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        val photoRef = FirebaseStorage.getInstance().reference.child("profilePic/" + currentUserId)
        photoRef.putFile(photoUri).addOnSuccessListener {
            photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                // video model in firebase firestore
                postToFirebase(downloadUrl.toString())
            }
        }
    }

    private fun postToFirebase(url: String) {
        Firebase.firestore.collection("users").document(currentUserId).update("profilePic", url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    private fun checkPermissionAndPickPhoto() {
        var readExternalPhoto: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                this, readExternalPhoto
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(readExternalPhoto), 100
            )
        }
    }

    private fun openPhotoPicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        // dam bao rang chi file video dc chon
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getProfileDataFromFirebase() {
        Firebase.firestore.collection("users").document(profileUserId).get().addOnSuccessListener {
            profileUserModel = it.toObject(UserModel::class.java)!!
            setUI()
        }
    }

    private fun setUI() {
        profileUserModel.apply {
            Glide.with(binding.profilePic).load(profilePic)
                .apply(RequestOptions().placeholder(R.drawable.icon_account_circle)).circleCrop()
                .into(binding.profilePic)
            binding.profileUsername.text = "@" + username
            if (profileUserModel.followerList.contains(currentUserId)){
                binding.profileBtn.text = "Unfollow"
            }
            binding.progressBar.visibility = View.INVISIBLE
            binding.followingCount.text = followingList.size.toString()
            binding.followerCount.text = followerList.size.toString()
            Firebase.firestore.collection("videos").whereEqualTo("uploaderId", profileUserId).get()
                .addOnSuccessListener {
                    binding.postsCount.text = it.size().toString()
                }
        }
    }
}