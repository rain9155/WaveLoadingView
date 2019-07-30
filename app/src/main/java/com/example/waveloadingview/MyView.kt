package com.example.waveloadingview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 *
 * Created by 陈健宇 at 2019/7/28
 */
class MyView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action){
            MotionEvent.ACTION_DOWN -> {
                Log.d("rain", "MyView, dispatchTouchEvent（）， ACTION_DOWN")
                return true
            }
            MotionEvent.ACTION_MOVE ->{
                Log.d("rain", "MyView, dispatchTouchEvent（）， ACTION_MOVE")
            }
            MotionEvent.ACTION_UP -> Log.d("rain", "MyView, dispatchTouchEvent（）， ACTION_UP")
            MotionEvent.ACTION_CANCEL -> Log.d("rain", "MyView, dispatchTouchEvent（）， ACTION_CANCEL")
        }
        return super.dispatchTouchEvent(ev)
    }



}