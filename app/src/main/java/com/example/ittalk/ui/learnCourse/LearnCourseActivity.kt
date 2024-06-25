
package com.example.ittalk.ui.learnCourse
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.ittalk.R
import com.example.ittalk.databinding.ActivityLearnCourseBinding
import com.example.ittalk.model.IncomingCall
import com.example.ittalk.ui.adapter.QuestionAdapter
import com.example.ittalk.ui.agora.RtcTokenBuilder2
import com.example.ittalk.ui.home.HomeActivity
import com.example.ittalk.util.showToast
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.kira.healthcare.utils.SharedPrefrenceUtil
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LearnCourseActivity : AppCompatActivity() {
    private var secondsElapsed = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    var callerId = "1111"
    var callerName = ""
    var callerProfilePic  =  ""
    var topic  = ""

    var randomUserId = ""
    lateinit var binding: ActivityLearnCourseBinding
    private var initialX: Float = 0f
    var isOnline = false
    lateinit var mediaPlayer: MediaPlayer
    lateinit var mediaPlayer2: MediaPlayer
    companion object { const val REQUEST_RECORD_AUDIO_PERMISSION = 200 }
    private val appId = "5fb926599aeb4ba391c29247cc3b6f71"
    var appCertificate = "b5065fbfa5ed4d8aba0c25de974502b1"
    var expirationTimeInSeconds = 3600
    private val channelName = "curiousdaya"
    private var token: String? = null
    private val uid = 0
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(Manifest.permission.RECORD_AUDIO)
    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) == PackageManager.PERMISSION_GRANTED
    }
    var isMute = false
    var isSpeaker = true
    var isCallSender  = false


    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }


    private fun initCall() {
        val tokenBuilder =
            RtcTokenBuilder2()
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()
        val result = tokenBuilder.buildTokenWithUid(
            appId, appCertificate,
            channelName, uid, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp
        )
        println(result)
        token = result
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }
        setupSDKEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")

        }
    }

    fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            agoraEngine!!.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun leaveChannel() {
        if (!isJoined) {
            showMessage("Join a channel first")
        } else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
            isJoined = false
        }
    }


    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }


    private fun resetTimer() {
        secondsElapsed = 0L
        updateTimerText()
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun updateTimerText() {
        val hours = TimeUnit.SECONDS.toHours(secondsElapsed)
        val minutes = TimeUnit.SECONDS.toMinutes(secondsElapsed) % 60
        val seconds = secondsElapsed % 60
        binding.inComingCalls.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    val onlineUserIds = mutableListOf<String>()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)
       if (intent.hasExtra("topic"))
       {
           topic = intent.getStringExtra("topic").toString()
           binding.tvTitle.text = topic
           val database = Firebase.database
           val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
           userRef.child("currentTopic").setValue(topic)
       }
        setupOnDisconnect()
        setupOnlineDb()
        initControl()
        initCall()
        observeIncomingCall(SharedPrefrenceUtil.getInstance(this).socialId)
        runnable = object : Runnable {
            override fun run() {
                updateTimerText()
                secondsElapsed++
                handler.postDelayed(this, 1000)
            }
        }
        window.setFlags(512, 512)
        binding.rvCoding.adapter = QuestionAdapter()





        binding.startCall.setOnClickListener {
            isCallSender = true
            val otherOnlineUserIds = onlineUserIds.filterNot { it == SharedPrefrenceUtil.getInstance(this).socialId }
            if (otherOnlineUserIds.isNotEmpty())
            {
                val random = Random.nextInt(otherOnlineUserIds.size)
                 randomUserId = otherOnlineUserIds[random]
                println("Randomly selected user ID: $randomUserId")
                observeReceiverCall(randomUserId)
                val incomingCallData = IncomingCall(
                    isIncomingCall = true,
                    callerId = SharedPrefrenceUtil.getInstance(this).socialId,
                    callerName = SharedPrefrenceUtil.getInstance(this).first_name,
                    callerProfilePic = SharedPrefrenceUtil.getInstance(this).profilePic)
                val database = Firebase.database
                val userRef = database.getReference("users").child(randomUserId)
                userRef.child("incomingCall").setValue(incomingCallData)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Successfully updated incoming call data")
                        fetchUserDetails(randomUserId)
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Log.d("Firebase", "Error updating incoming call data", e)
                    }



            }

            else
            {
                println("No other online users available")
            }



        }

        binding.icBack.setOnClickListener {
            onBackPressed()
        }


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
                        joinChannel()
                        mediaPlayer.stop()
                        view.animate().translationX(0f).setDuration(200).start()
                        binding.incoming.isVisible = true
                        binding.disconnect.isVisible = true
                        binding.incomingCallUI.background = getDrawable(R.drawable.bg_trans)
                        binding.incomingCallUI.isVisible = false

                        val database = Firebase.database
                        val callStatus = database.getReference("users").child(SharedPrefrenceUtil
                            .getInstance(this@LearnCourseActivity).socialId)
                         callStatus.child("callStatus").setValue("1")


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
                        val callStatus = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this@LearnCourseActivity).socialId)
                          callStatus.child("callStatus").setValue("0")

                        val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
                        var incomingCall1=  userRef.child("incomingCall")
                        incomingCall1.removeValue()
                    }

                    true
                }

                else -> false
            }
        }

    }
    fun setupOnDisconnect()
    {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        usersRef.child(SharedPrefrenceUtil.getInstance(this).socialId).child("online").onDisconnect().setValue(false)

    }



    private fun observeIncomingCall(userId: String) {
        val database = Firebase.database
        val incomingCallRef = database.getReference("users").child(userId).child("incomingCall")

        incomingCallRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isIncomingCall = snapshot.child("incomingCall").getValue(Boolean::class.java) ?: false
                val callerName = snapshot.child("callerName").getValue(String::class.java) ?: ""
                val callerProfilePic = snapshot.child("callerProfilePic").getValue(String::class.java) ?: ""

                if (isIncomingCall)
                {
                    runOnUiThread {
                        incomingCallUI(callerName,callerProfilePic)
                    }
                }
                else
                {
                    resetLayout()
                    resetTimer()
                    if (::mediaPlayer.isInitialized) {
                        mediaPlayer.release()

                    }
                    if (::mediaPlayer2.isInitialized) {
                        mediaPlayer2.release()

                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                println("Database error: $databaseError")
            }
        })
    }

    private fun observeReceiverCall(userId: String) {

        val database = Firebase.database
        val callStatus = database.getReference("users").child(userId)
        callStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val callStatus = snapshot.child("callStatus").getValue(String::class.java) ?: ""

                if (callStatus=="1")
                {
                    showToast("Call Connected")
                    runOnUiThread {
                        connectedCallUI()
                    }
                }
                else
                {
                    showToast("Call Dis Connected")

                    resetLayout()
                    resetTimer()
                    if (::mediaPlayer.isInitialized) {
                        mediaPlayer.stop()

                    }
                    if (::mediaPlayer2.isInitialized) {
                        mediaPlayer2.stop()

                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                println("Database error: $databaseError")
            }
        })
    }



    private fun fetchUserDetails(userId: String) {
        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val profilePic = snapshot.child("profilePic").getValue(String::class.java) ?: "No picture"

                // Use the name and profilePic as needed
                println("Name: $name, Profile Picture: $profilePic")
                outgoingCallUI(name,profilePic)

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
                    if (isOnline&&currentTopic==topic)
                    {
                        val userId = userSnapshot.key
                        userId?.let { onlineUserIds.add(it) }
                    }
                }
                val otherOnlineUserIds = onlineUserIds.filterNot { it == SharedPrefrenceUtil.getInstance(this@LearnCourseActivity).socialId }
                binding.tvOnline.text = "${otherOnlineUserIds.size} Online"
                println("Online User IDs: $onlineUserIds")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                println("Database error: $databaseError")
            }
        })

    }




    fun resetLayout() {
        binding.incomingLayout.isVisible = false
        binding.mainLayout.isVisible = true
        handler.removeCallbacks(runnable)

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
        }
        if (::mediaPlayer2.isInitialized) {
            mediaPlayer2.stop()
        }


    }

    fun outgoingCallUI(name:String,profile:String)
    {
        mediaPlayer2 = MediaPlayer.create(this, R.raw.ringing_sound)
        mediaPlayer2.start()
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


    fun incomingCallUI(name:String,profile:String) {
        isCallSender = false
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone_calling)
        mediaPlayer.start()
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


    fun connectedCallUI() {
        if (::mediaPlayer.isInitialized)
        {
            mediaPlayer.stop()
        }

          if (::mediaPlayer2.isInitialized)
        {
            mediaPlayer2.stop()
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
            mediaPlayer.release()

        }
        if (::mediaPlayer2.isInitialized) {
            mediaPlayer2.release()

        }

    }

    override fun onResume() {
        super.onResume()
        resetTimer()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    showToast("Please enable microphone permission for calling")
                }
                return
            }
            // Add other 'when' lines to check for other permissions this app might request.
        }
    }


     fun initControl() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
        binding.mToggleLayout.setOnClickListener {
            val database = Firebase.database
            val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
            if (isOnline)
            {
                binding.wifi1.isVisible = true
                binding.wifi2.isVisible = false
                binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle2)
                vibratePhone()
                binding.bottomLayout1.isVisible = false
                binding.startCall.isVisible = false
                binding.tvStatus.text = "Offline"
                isOnline = false
                SharedPrefrenceUtil.getInstance(this).isOnline = false
                userRef.child("online").setValue(false)


            }
            else
            {
                binding.wifi1.isVisible = false
                binding.wifi2.isVisible = true
                binding.mToggleLayout.setBackgroundResource(R.drawable.bg_round_toggle)
                vibratePhone()
                binding.bottomLayout1.isVisible = true
                binding.startCall.isVisible = true
                binding.tvStatus.text = "Online"
                SharedPrefrenceUtil.getInstance(this).isOnline = true
                isOnline = true
                userRef.child("online").setValue(true)

            }
        }
        binding.disconnectAfterCall.setOnClickListener {
            if (isCallSender)
            {
                val database = Firebase.database
                val userRef = database.getReference("users").child(randomUserId)
                var incomingCall1=  userRef.child("incomingCall")
                incomingCall1.child("incomingCall").setValue(false)
                userRef.removeValue()
            }
            else
            {
                val database = Firebase.database
                val userRef = database.getReference("users").child(SharedPrefrenceUtil.getInstance(this).socialId)
                var incomingCall1=  userRef.child("incomingCall")
                userRef.child("callStatus").setValue("0")
                incomingCall1.removeValue()

            }
            resetLayout()
            resetTimer()

        }
        binding.outgoingDisconnect.setOnClickListener {
            if (isCallSender)
            {
                val database = Firebase.database
                val userRef = database.getReference("users").child(randomUserId)
                var incomingCall1=  userRef.child("incomingCall")
                incomingCall1.child("incomingCall").setValue(false)
                incomingCall1 .removeValue()
            }
            resetLayout()
            resetTimer()
        }
        binding.mute.setOnClickListener {
            isMute = !isMute
               if (isMute)
               {
                   binding.mute.setImageResource(R.drawable.mic_off_blue)
                   agoraEngine!!.muteLocalAudioStream(true)
                   agoraEngine?.setEnableSpeakerphone(true)
                   agoraEngine?.setDefaultAudioRoutetoSpeakerphone(true)
                   agoraEngine?.enableInEarMonitoring(false)
                   agoraEngine?.setInEarMonitoringVolume(0)
                   agoraEngine?.adjustPlaybackSignalVolume(100)
               }
               else{
                   binding.mute.setImageResource(R.drawable.mic_off_gray)
                   agoraEngine!!.muteLocalAudioStream(false)
                   agoraEngine?.setEnableSpeakerphone(false)
                   agoraEngine?.setDefaultAudioRoutetoSpeakerphone(false)
                   agoraEngine?.enableInEarMonitoring(true)
                   agoraEngine?.setInEarMonitoringVolume(300)
                   agoraEngine?.adjustPlaybackSignalVolume(300)


               }

        }
        binding.speaker.setOnClickListener {
            isSpeaker = !isSpeaker
               if (isSpeaker)
               {
                   binding.speaker.setImageResource(R.drawable.speaker_blue)
               }
               else{
                   binding.speaker.setImageResource(R.drawable.speaker_gray)

               }

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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(
            androidx.appcompat.R.anim.abc_fade_in,
            androidx.appcompat.R.anim.abc_fade_out
        )
        finishAffinity()
    }
}
