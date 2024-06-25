package com.example.ittalk.ui.home
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.ittalk.R
import com.example.ittalk.databinding.ActivityHomeBinding
import com.example.ittalk.model.IncomingCall
import com.example.ittalk.model.User
import com.example.ittalk.ui.adapter.CodingAdapter
import com.example.ittalk.ui.adapter.ViewPagerAdapter2
import com.example.ittalk.ui.profile.ProfileActivity
import com.example.ittalk.util.HorizontalMarginItemDecoration
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.kira.healthcare.utils.SharedPrefrenceUtil
import java.lang.Math.abs

class HomeActivity : AppCompatActivity() {
    var isOnline = true
    var userSelfRef: DatabaseReference? = null
    var userRandomRef: DatabaseReference? = null
    private lateinit var mediaPlayer: MediaPlayer
    lateinit var binding: ActivityHomeBinding
    private val handler = Handler(Looper.getMainLooper())
    private val autoScrollInterval: Long = 4000
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            var nextItem = binding.viewPagerHome2.currentItem + 1
            if (nextItem >= binding.viewPagerHome2.adapter?.itemCount ?: 0)
            {
                nextItem = 0
            }
            binding.viewPagerHome2.setCurrentItem(nextItem, true)
            handler.postDelayed(this, autoScrollInterval)
        }
    }
    val onlineUserIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(512,512)
        mediaPlayer = MediaPlayer.create(this, R.raw.online_audio) // Replace your_sound_file with your actual sound file in res/raw
        setupBanner()
        initViews()
        initControl()
        realTimeDatabase()
        setupOnlineDb()
        setupOnDisconnect()
        setupScreenOn()
        setupRealTimeDB()
    }
    fun setupRealTimeDB()
    {
        userSelfRef = Firebase.database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
        userRandomRef = Firebase.database.getReference("users").child("randomId")

    }
    private fun setupScreenOn() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }

        registerReceiver(screenStateReceiver, filter)
    }


    val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> { userSelfRef?.child("online")?.setValue(false) }
                Intent.ACTION_SCREEN_ON -> { userSelfRef?.child("online")?.setValue(true) }
            }
        }
    }



    fun setupOnDisconnect()
    {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        usersRef.child(SharedPrefrenceUtil.getInstance(this).socialId).child("online").onDisconnect().setValue(false)

    }

    private fun setupOnlineDb() {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onlineUserIds.clear()
                for (userSnapshot in snapshot.children)
                {
                    val isOnline = userSnapshot.child("online").getValue(Boolean::class.java) ?: false
                    val currentTopic = userSnapshot.child("currentTopic").getValue(String::class.java) ?: ""
                    if (isOnline&&currentTopic=="home")
                    {
                        val userId = userSnapshot.key
                        userId?.let { onlineUserIds.add(it) }
                    }
                }
                val otherOnlineUserIds = onlineUserIds.filterNot { it == SharedPrefrenceUtil.getInstance(this@HomeActivity).socialId }
                binding.tvOnline.text = "${otherOnlineUserIds.size} Online"
                println("Online User IDs: $onlineUserIds")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                println("Database error: $databaseError")
            }
        })

    }



    fun realTimeDatabase()
     {
        val database = Firebase.database
        val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
        val newUser = User(
            name = SharedPrefrenceUtil.getInstance(this).first_name,
            profilePic = SharedPrefrenceUtil.getInstance(this).profilePic,
            online = true,
            callStatus = "",
            currentTopic = "home",
            incomingCall = null
        )

        userRef.setValue(newUser)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }

    }



    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vibrator.vibrate(50)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun initControl() {
        binding.mToggleLayout.setOnClickListener {
            val database = Firebase.database
            val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
            if (isOnline)
            {
                binding.wifi1.isVisible = true
                binding.wifi2.isVisible = false
                binding.tvStatus.text = "Offline"
                binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle2)
                vibratePhone()
                binding.animationView.isVisible = false
                binding.bottomLayout.isVisible = false
                SharedPrefrenceUtil.getInstance(this).isOnline = false
                isOnline = false
                userRef.child("online").setValue(false)


            }
            else
            {
                binding.wifi1.isVisible = false
                binding.wifi2.isVisible = true
                binding.tvStatus.text = "Online"
                binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle)
                vibratePhone()
                binding.animationView.isVisible = true
                binding.bottomLayout.isVisible = true
                SharedPrefrenceUtil.getInstance(this).isOnline = true
                isOnline = true
                userRef.child("online").setValue(true)


            }
        }





        binding.share.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "Please, Follow this link to Download the1 APP \n www.google.com" // your message here
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }



        binding.logoProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }
    }

     fun initViews() {
        Glide.with(this)
            .load(SharedPrefrenceUtil.getInstance(this).profilePic)
            .placeholder(R.drawable.user)
            .into(binding.logoProfile)


        var list = ArrayList<Int>()
        list.add(R.drawable.img1)
        list.add(R.drawable.img2)
        list.add(R.drawable.img3)
        binding.rvCoding.adapter =CodingAdapter(this,list)
    }

    fun setupBanner()
     {
        val imagesList = arrayListOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner1,R.drawable.banner1, R.drawable.banner2, R.drawable.banner1)
        binding.viewPagerHome2.adapter = ViewPagerAdapter2(this@HomeActivity,imagesList)
         binding.viewPagerHome2.offscreenPageLimit = 1
         val nextItemVisiblePx = resources.getDimension(com.intuit.sdp.R.dimen._40sdp)
         val currentItemHorizontalMarginPx = resources.getDimension(com.intuit.sdp.R.dimen._40sdp)
         val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
         val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
             page.translationX = -pageTranslationX * position
             page.scaleY = 1 - (0.2f * abs(position))
             page.alpha = 0.5f + (1 - abs(position)) }
         binding.viewPagerHome2.setPageTransformer(pageTransformer)
         val itemDecoration = HorizontalMarginItemDecoration(this, com.intuit.sdp.R.dimen._40sdp)
         binding.viewPagerHome2.addItemDecoration(itemDecoration)
         val initialPosition = if (imagesList.size > 1) 1 else 0
         binding.viewPagerHome2.setCurrentItem(initialPosition, false)


     }


     fun startAutoScroll() {
        handler.postDelayed(autoScrollRunnable, autoScrollInterval)
    }
     fun stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable)
    }
    override fun onResume() {
        super.onResume()
        startAutoScroll()
        val database = Firebase.database
        val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
            userRef.child("currentTopic").setValue("home")
        if (SharedPrefrenceUtil.getInstance(this).isOnline)
        {
            binding.wifi1.isVisible = false
            binding.wifi2.isVisible = true
            binding.tvStatus.text = "Online"
            binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle)
            binding.animationView.isVisible = true
            binding.bottomLayout.isVisible = true
            isOnline = true
            userRef.child("online").setValue(true)

        }
        else
        {
            binding.wifi1.isVisible = true
            binding.wifi2.isVisible = false
            binding.tvStatus.text = "Offline"
            binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle2)
            binding.animationView.isVisible = false
            binding.bottomLayout.isVisible = false
            isOnline = false
            userRef.child("online").setValue(false)

        }

    }
    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }
}