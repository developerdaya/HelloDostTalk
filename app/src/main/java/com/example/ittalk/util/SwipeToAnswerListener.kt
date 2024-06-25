package com.example.ittalk.util

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat

class SwipeToAnswerListener(context: Context, private val onSwipeRight: () -> Unit) : View.OnTouchListener {

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener()
    {

        // Correct method signature for onFling
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null && e2 != null) {
                val distanceX = e2.x - e1.x
                val SWIPE_THRESHOLD = 100
                val SWIPE_VELOCITY_THRESHOLD = 100

                if (Math.abs(distanceX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0) {
                        // Swipe right detected
                        onSwipeRight()
                        return true
                    }
                    // Optionally, handle swipe left
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

    })

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }
}

// Example usage
// val yourView: View = findViewById(R.id.your_view_id)
// yourView.setOnTouchListener(com.example.ittalk.util.SwipeToAnswerListener(this) {
//     // Logic to answer call
//     println("Call answered")
// })
