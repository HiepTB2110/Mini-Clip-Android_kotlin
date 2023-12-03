package com.example.miniclip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.miniclip.adapter.VideoListAdapter
import com.example.miniclip.databinding.ActivityMainBinding
import com.example.miniclip.model.VideoModel
import com.example.miniclip.util.UiUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavBar.setOnItemSelectedListener {menuItem ->
            when(menuItem.itemId){
                R.id.bottom_menu_home -> {
                    UiUtil.showToast(this, "home")
                }
                R.id.bottom_menu_add_video -> {
                    startActivity(Intent(this, VideoUploadActivity::class.java))
                }
                R.id.bottom_menu_profile -> {
                    // goto profileActivity
                    val intent = Intent(this, ProfileActivity2::class.java)
                    intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid)
                    startActivity(intent)
                }
            }
            false
        }

        setupViewPager()
    }

    private fun setupViewPager() {
        //Tạo Options cho FirestoreRecyclerAdapter:
        val options = FirestoreRecyclerOptions.Builder<VideoModel>().setQuery(//Tạo Options cho FirestoreRecyclerAdapter:
            Firebase.firestore.collection("videos"),
            VideoModel::class.java
        ).build()
        //Khởi tạo Adapter
        adapter = VideoListAdapter(options)
        //Thiết lập Adapter cho ViewPager
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.startListening()
    }
}