package com.example.waveloadingview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 *
 * Created by 陈健宇 at 2019/7/29
 */
class MyViewGroup2 : LinearLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> Log.d("rain", "MyViewGroup2, dispatchTouchEvent（）， ACTION_DOWN")
            MotionEvent.ACTION_MOVE -> Log.d("rain", "MyViewGroup2, dispatchTouchEvent（）， ACTION_MOVE")
            MotionEvent.ACTION_UP -> Log.d("rain", "MyViewGroup2, dispatchTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "MyViewGroup2, dispatchTouchEvent（）， ACTION_CANCEL")
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> {
                Log.d("rain", "MyViewGroup2, onInterceptTouchEvent（）， ACTION_DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("rain", "MyViewGroup2, onInterceptTouchEvent（）， ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> Log.d("rain", "MyViewGroup2, onInterceptTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "MyViewGroup2, onInterceptTouchEvent（）， ACTION_CANCEL")
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN -> Log.d("rain", "MyViewGroup2, onTouchEvent（）， ACTION_DOWN")
            MotionEvent.ACTION_MOVE -> {
                Log.d("rain", "MyViewGroup2, onTouchEvent（）， ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> Log.d("rain", "MyViewGroup2, onTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "v, onTouchEvent（）， ACTION_CANCEL")
        }
        return super.onTouchEvent(event)
    }

}