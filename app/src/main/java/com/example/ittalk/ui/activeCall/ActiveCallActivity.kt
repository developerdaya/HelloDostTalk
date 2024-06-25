package com.example.ittalk.ui.activeCall

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.ittalk.R
import com.example.ittalk.databinding.ActivityActiveCallBinding
import com.example.ittalk.model.IncomingCall
import com.example.ittalk.ui.adapter.QuestionAdapter
import com.example.ittalk.ui.learnCourse.LearnCourseActivity
import com.example.ittalk.util.showToast
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.kira.healthcare.utils.SharedPrefrenceUtil
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ActiveCallActivity : AppCompatActivity() {
    lateinit var binding: ActivityActiveCallBinding
    var topic = ""
    var randomId = ""
    var userSelfRef: DatabaseReference? = null
    var usersListRef: DatabaseReference? = null
    var userRandomRef: DatabaseReference? = null
    var isOnline = false
    var onlineUserIds = mutableListOf<String>()
    var isMute = false
    var isSpeaker = true
    var isCallSender = false
    var randomUserId = ""
    lateinit var mediaPlayer: MediaPlayer
    lateinit var mediaPlayer2: MediaPlayer

    companion object {
        const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    private var secondsElapsed = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    var myId = "100475531662029202287"
    private var initialX: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initControl()
        dbObserver()
    }

    fun initViews() {
        askPermission()
        setupRealTimeDB()
        getIntentData()
        setupAdapter()
        setupOnline()
        setupOnlineDb()
        setupScreenOn()
        setupRunnable()
    }

    fun setupRunnable() {
        runnable = object : Runnable {
            override fun run() {
                updateTimerText()
                secondsElapsed++
                handler.postDelayed(this, 1000)
            }
        }
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
                Intent.ACTION_SCREEN_OFF -> {
                    userSelfRef?.child("online")?.setValue(false)
                }

                Intent.ACTION_SCREEN_ON -> {
                    userSelfRef?.child("online")?.setValue(true)
                }
            }
        }
    }


    fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                LearnCourseActivity.REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    fun setupRealTimeDB() {
        userSelfRef = Firebase.database.getReference("users")
            .child(SharedPrefrenceUtil.getInstance(this).socialId)
        userRandomRef = Firebase.database.getReference("users").child(randomId)
        usersListRef = Firebase.database.getReference("users")

    }

    fun setupAdapter() {
        binding.rvCoding.adapter = QuestionAdapter()

    }

    fun getIntentData() {
        window.setFlags(512, 512)
        if (intent.hasExtra("topic")) {
            topic = intent.getStringExtra("topic").toString()
            binding.tvTitle.text = topic
            userSelfRef!!.child("currentTopic").setValue(topic)
        }
    }

    fun setupOnline() {
        if (SharedPrefrenceUtil.getInstance(this).isOnline) {
            binding.wifi1.isVisible = false
            binding.wifi2.isVisible = true
            binding.tvStatus.text = "Online"
            binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle)
            binding.startCall.isVisible = true
            binding.bottomLayout1.isVisible = true
            isOnline = true
        } else {
            binding.wifi1.isVisible = true
            binding.wifi2.isVisible = false
            binding.tvStatus.text = "Offline"
            binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle2)
            binding.startCall.isVisible = false
            binding.bottomLayout1.isVisible = false
            isOnline = false


        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initControl() {
        binding.mToggleLayout.setOnClickListener {
            if (isOnline) {
                binding.wifi1.isVisible = true
                binding.wifi2.isVisible = false
                binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle2)
                vibratePhone()
                binding.bottomLayout1.isVisible = false
                binding.startCall.isVisible = false
                binding.tvStatus.text = "Offline"
                isOnline = false
                SharedPrefrenceUtil.getInstance(this).isOnline = false
                userSelfRef?.child("online")?.setValue(false)


            } else {
                binding.wifi1.isVisible = false
                binding.wifi2.isVisible = true
                binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle)
                vibratePhone()
                binding.bottomLayout1.isVisible = true
                binding.startCall.isVisible = true
                binding.tvStatus.text = "Online"
                SharedPrefrenceUtil.getInstance(this).isOnline = true
                isOnline = true
                userSelfRef?.child("online")?.setValue(true)

            }
        }
        binding.startCall.setOnClickListener {
            isCallSender = true
            if (onlineUserIds.isNotEmpty()) {
                val random = Random.nextInt(onlineUserIds.size)
                randomUserId = onlineUserIds[random]
                println("Randomly selected user ID: $randomUserId")
                observeReceiverCall(randomUserId)
                val incomingCallData = IncomingCall(
                    isIncomingCall = true,
                    callerId = SharedPrefrenceUtil.getInstance(this).socialId,
                    callerName = SharedPrefrenceUtil.getInstance(this).first_name,
                    callerProfilePic = SharedPrefrenceUtil.getInstance(this).profilePic
                )
                val userRef = Firebase.database.getReference("users").child(randomUserId)
                userRef.child("incomingCall").setValue(incomingCallData)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Successfully updated incoming call data")
                        fetchUserDetails(randomUserId)
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Log.d("Firebase", "Error updating incoming call data", e)
                    }


            } else {
                println("No other online users available")
            }


        }
        binding.disconnectAfterCall.setOnClickListener {
            if (isCallSender)
            {
                val database = Firebase.database
                val userRef = database.getReference("users").child(randomUserId)
                var incomingCall1 = userRef.child("incomingCall")
                incomingCall1.removeValue()

            }
            else
            {
                val database = Firebase.database
                val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
                var incomingCall = userRef.child("incomingCall")
                incomingCall.removeValue()
            }
            resetLayout()
            resetTimer()

        }
        binding.outgoingDisconnect.setOnClickListener {
            if (isCallSender)
            {
                val database = Firebase.database
                val userRef = database.getReference("users").child(randomUserId)
                var incomingCall1 = userRef.child("incomingCall")
                incomingCall1.removeValue()
            }
            resetLayout()
            resetTimer()
        }
        binding.mute.setOnClickListener {
            isMute = !isMute
            if (isMute) {
                binding.mute.setImageResource(R.drawable.mic_off_blue)

            } else {
                binding.mute.setImageResource(R.drawable.mic_off_gray)


            }

        }
        binding.speaker.setOnClickListener {
            isSpeaker = !isSpeaker
            if (isSpeaker) {
                binding.speaker.setImageResource(R.drawable.speaker_blue)
            } else {
                binding.speaker.setImageResource(R.drawable.speaker_gray)

            }

        }
        binding.icBack.setOnClickListener { onBackPressed() }
        binding.incoming.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val distanceX = event.x - initialX
                    val intendedTranslationX =
                        (view.translationX + distanceX).coerceAtLeast(0f) // Prevent leftward movement
                    val widthInPixels = binding.incomingCallUI.width
                    val widthInPixels2 = binding.incoming.width
                    val swipeRightLimit1 = widthInPixels - widthInPixels2
                    val swipeRightLimit = swipeRightLimit1 - 15

                    if (intendedTranslationX > swipeRightLimit) {
                        view.translationX = swipeRightLimit.toFloat()
                    } else {
                        view.translationX = intendedTranslationX
                    }
                    true
                }


                MotionEvent.ACTION_DOWN -> {
                    binding.disconnect.isVisible = false
                    binding.incomingCallUI.background = getDrawable(R.drawable.bg_accept)
                    initialX = event.x
                    true
                }

                MotionEvent.ACTION_UP -> {
                    binding.disconnect.isVisible = true
                    binding.incomingCallUI.background = getDrawable(R.drawable.bg_trans)
                    if (view.translationX > view.width / 2) {
                        connectedCallUI()
                     //   mediaPlayer.stop()
                        view.animate().translationX(0f).setDuration(200).start()
                        binding.incoming.isVisible = true
                        binding.disconnect.isVisible = true
                        binding.incomingCallUI.background = getDrawable(R.drawable.bg_trans)
                        binding.incomingCallUI.isVisible = false
                        val database = Firebase.database
                        val callStatus = database.getReference("users").child(
                            SharedPrefrenceUtil
                                .getInstance(this@ActiveCallActivity).socialId
                        )
                        callStatus.child("callStatus").setValue("1")
                        callStatus.child("callStatus").setValue("")


                    }
                    // Always reset the view's position after release

                    true
                }

                else -> false
            }
        }
        binding.disconnect.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val distanceX = event.x - initialX
                    val newTranslationX = view.translationX + distanceX

                    val widthInPixels = binding.incomingCallUI.width
                    val widthInPixels2 = binding.incoming.width
                    val swipeRightLimit1 = widthInPixels - widthInPixels2
                    val swipeRightLimit = swipeRightLimit1 - 15
                    val swipeLeftLimit = -swipeRightLimit

                    // Allow only leftward movement
                    if (newTranslationX < 0 && newTranslationX >= swipeLeftLimit) {
                        view.translationX = newTranslationX
                    } else if (newTranslationX >= 0) { // Prevent rightward movement by setting translationX to 0 or the left limit
                        view.translationX = 0f
                    }

                    true
                }


                MotionEvent.ACTION_DOWN -> {
                    binding.incoming.isVisible = false
                    binding.incomingCallUI.background = getDrawable(R.drawable.bg_reject)
                    initialX = event.x
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (view.translationX < -view.width / 4) {
                        resetLayout()
                        binding.incomingLayout.isVisible = false
                        binding.mainLayout.isVisible = true
                        view.animate().translationX(0f).setDuration(200).start()
                        binding.incoming.isVisible = true
                        binding.disconnect.isVisible = true
                        binding.incomingCallUI.isVisible = false
                        binding.incomingCallUI.background = getDrawable(R.drawable.bg_trans)
                        val database = Firebase.database
                        val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
                        var incomingCall = userRef.child("incomingCall")
                        incomingCall.removeValue()

                    }

                    true
                }

                else -> false
            }
        }


    }

    fun dbObserver() {
        observeIncomingCall(SharedPrefrenceUtil.getInstance(this).socialId)
    }

    fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vibrator.vibrate(100)
        }
    }

    fun setupOnlineDb() {
        usersListRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onlineUserIds.clear()
                for (userSnapshot in snapshot.children) {
                    val isOnline =
                        userSnapshot.child("online").getValue(Boolean::class.java) ?: false
                    val currentTopic =
                        userSnapshot.child("currentTopic").getValue(String::class.java) ?: ""
                    if (isOnline && currentTopic == topic) {
                        val userId = userSnapshot.key
                        userId?.let { onlineUserIds.add(it) }
                    }
                }
                onlineUserIds =
                    onlineUserIds.filterNot { it == SharedPrefrenceUtil.getInstance(this@ActiveCallActivity).socialId } as MutableList<String>
                binding.tvOnline.text = "${onlineUserIds.size} Online"
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    fun observeReceiverCall(userId: String) {
        val database = Firebase.database
        val callStatus = database.getReference("users").child(userId)
        callStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val callStatus = snapshot.child("callStatus").getValue(String::class.java) ?: ""
                val incomingCall = snapshot.child("incomingCall").getValue(IncomingCall::class.java)
                if (incomingCall != null)
                {
                    if (callStatus == "1")
                    {
                        showToast("Call Connected")
                        runOnUiThread { connectedCallUI() }
                    }
                }
                else
                {
                    showToast("Call Dis-Connected")
                    resetLayout()
                    resetTimer()
                    if (::mediaPlayer.isInitialized)
                       {
                       // mediaPlayer.stop()
                    }
                    if (::mediaPlayer2.isInitialized)
                       {
                       // mediaPlayer2.stop()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                println("Database error: $databaseError")
            }
        })
    }

    fun fetchUserDetails(userId: String) {
        val database = Firebase.database
        val callStatus = database.getReference("users").child(userId)
        callStatus.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val profilePic =
                    snapshot.child("profilePic").getValue(String::class.java) ?: "No picture"

                // Use the name and profilePic as needed
                println("Name: $name, Profile Picture: $profilePic")
                outgoingCallUI(name, profilePic)

                // If you have a TextView or an ImageView in your layout to show these details
                // binding.userName.text = name
                // Glide.with(this@YourActivity).load(profilePic).into(binding.userProfilePic)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                println("Database error: $databaseError")
            }
        })
    }

    fun connectedCallUI() {
        if (::mediaPlayer.isInitialized) {
          //  mediaPlayer.stop()
        }

        if (::mediaPlayer2.isInitialized) {
         //   mediaPlayer2.stop()
        }


        binding.incomingLayout.isVisible = true
        binding.mainLayout.isVisible = false
        binding.inComingCalls.text = ""
        binding.outgoingDisconnect.isVisible = false
        binding.incomingCallUI.isVisible = false
        binding.callConnectedUI.isVisible = true
        binding.callConnectedUI.isVisible = true
        handler.postDelayed(runnable, 1000)
    }

    override fun onStop() {
        super.onStop()
        if (::mediaPlayer.isInitialized) {
          //  mediaPlayer.release()

        }
        if (::mediaPlayer2.isInitialized) {
          //  mediaPlayer2.release()

        }

    }

    fun resetLayout() {
        binding.incomingLayout.isVisible = false
        binding.mainLayout.isVisible = true
        handler.removeCallbacks(runnable)

        if (::mediaPlayer.isInitialized) {
            //   mediaPlayer.stop()
        }
        if (::mediaPlayer2.isInitialized) {
            //   mediaPlayer2.stop()
        }


    }

    fun resetTimer() {
        secondsElapsed = 0L
        updateTimerText()
    }

    fun outgoingCallUI(name: String, profile: String) {
       // mediaPlayer2 = MediaPlayer.create(this, R.raw.ringing_sound)
      //  mediaPlayer2.start()
        binding.incomingLayout.isVisible = true
        binding.mainLayout.isVisible = false
        binding.inComingCalls.text = "Ringing..."
        binding.outgoingDisconnect.isVisible = true
        binding.incomingCallUI.isVisible = false
        binding.callConnectedUI.isVisible = false
        Glide.with(this)
            .load(profile)
            .into(binding.incomingProfile)
        binding.callerName.text = name
    }

    fun updateTimerText() {
        val hours = TimeUnit.SECONDS.toHours(secondsElapsed)
        val minutes = TimeUnit.SECONDS.toMinutes(secondsElapsed) % 60
        val seconds = secondsElapsed % 60
        binding.inComingCalls.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to prevent memory leaks
        unregisterReceiver(screenStateReceiver)
    }

    fun observeIncomingCall(userId: String) {
        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)
        val incomingCallRef = database.getReference("users").child(userId).child("incomingCall")
        userRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (incomingCallRef!=null)
                {
                    showToast("Ye lo Call Cur Gya")
                    resetLayout()
                    resetTimer()
                    if (::mediaPlayer.isInitialized) {
                        //  mediaPlayer.release()

                    }
                    if (::mediaPlayer2.isInitialized) {
                        //   mediaPlayer2.release()

                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        incomingCallRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isIncomingCall = snapshot.child("incomingCall").getValue(Boolean::class.java) ?: false
                val callerName = snapshot.child("callerName").getValue(String::class.java) ?: ""
                val callerProfilePic = snapshot.child("callerProfilePic").getValue(String::class.java) ?: ""
                    if (isIncomingCall)
                    {
                        showToast("incomingCall")
                        runOnUiThread {
                            incomingCallUI(callerName, callerProfilePic)
                        }
                    }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                println("Database error: $databaseError")
            }
        })
    }

    fun incomingCallUI(name: String, profile: String) {
        isCallSender = false
      //  mediaPlayer = MediaPlayer.create(this, R.raw.ringtone_calling)
       // mediaPlayer.start()
        binding.incomingLayout.isVisible = true
        binding.mainLayout.isVisible = false
        binding.inComingCalls.text = "in coming call..."
        binding.outgoingDisconnect.isVisible = false
        binding.incomingCallUI.isVisible = true
        binding.callConnectedUI.isVisible = false
        Glide.with(this)
            .load(profile)
            .into(binding.incomingProfile)
        binding.callerName.text = name
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LearnCourseActivity.REQUEST_RECORD_AUDIO_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    showToast("Please enable microphone permission for calling")
                }
                return
            }
            // Add other 'when' lines to check for other permissions this app might request.
        }
    }


}