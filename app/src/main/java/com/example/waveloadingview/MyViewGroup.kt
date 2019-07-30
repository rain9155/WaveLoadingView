package com.example.waveloadingview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout

/**
 *
 * Created by 陈健宇 at 2019/7/28
 */
class MyViewGroup : LinearLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)



    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> Log.d("rain", "MyViewGroup, dispatchTouchEvent（）， ACTION_DOWN")
            MotionEvent.ACTION_MOVE -> Log.d("rain", "MyViewGroup, dispatchTouchEvent（）， ACTION_MOVE")
            MotionEvent.ACTION_UP -> Log.d("rain", "MyViewGroup, dispatchTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "MyViewGroup, dispatchTouchEvent（）， ACTION_CANCEL")
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> {
                Log.d("rain", "MyViewGroup, onInterceptTouchEvent（）， ACTION_DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("rain", "MyViewGroup, onInterceptTouchEvent（）， ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> Log.d("rain", "MyViewGroup, onInterceptTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "MyViewGroup, onInterceptTouchEvent（）， ACTION_CANCEL")
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN -> Log.d("rain", "MyViewGroup, onTouchEvent（）， ACTION_DOWN")
            MotionEvent.ACTION_MOVE -> {
                Log.d("rain", "MyViewGroup, onTouchEvent（）， ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> Log.d("rain", "MyViewGroup, onTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "MyViewGroup, onTouchEvent（）， ACTION_CANCEL")
        }
        return super.onTouchEvent(event)
    }
}