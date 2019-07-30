package com.example.waveloadingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver

/**
 * 用贝塞尔曲线绘制loading图: init - > onMeasure -> onSizeChange -> onLayout -> onGlobalLayout() -> onDraw
 * Created by 陈健宇 at 2019/7/27
 */
class WaveLoadingView : View{

     companion object Config{
         const val WAVE_LENGTH = 60
         const val WAVE_HEIGHT = 60
         const val WAVE_OFFSET = 5
         val TAG = WaveLoadingView::class.java.simpleName
    }

    private lateinit var wavePaint: Paint
    private lateinit var borderPaint: Paint
    private lateinit var bitmapPaint : Paint
    private lateinit var textPaint: Paint
    private lateinit var valueAnim : ValueAnimator
    private lateinit var bitmapShader : Shader
    private var waveBitmap : Bitmap? = null
    private val wavePath = Path()
    private val clipPath = Path()
    private var offsetXFast = 0f
    private var offsetXSlow = 0f
    private var offsetY = 0f

    constructor(context: Context?) : super(context){
        init(context, null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init(context, attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(context, attrs)
    }

    fun init(context: Context?, attrs: AttributeSet?){

        wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        wavePaint.style = Paint.Style.FILL_AND_STROKE
        bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = Color.RED
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 3f

        valueAnim = ValueAnimator.ofFloat(0f, 1f)
        valueAnim.apply {
            duration = 3000
            addUpdateListener {animation ->
                val value : Float = animation.animatedValue as Float
                offsetXFast = (offsetXFast + WAVE_OFFSET) % width
                offsetXSlow = (offsetXSlow + WAVE_OFFSET / 2) % width
                postInvalidate()
            }
            LinearOutSlowInInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }

        viewTreeObserver.addOnGlobalLayoutListener{
            Log.d(TAG, "onGlobalLayout()")
        }

        Log.d(TAG, "init()")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "onMeasure()")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        drawWavePath()
        Log.d(TAG, "onLayout()")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged()")

    }

    override fun onDraw(canvas: Canvas?) {

        clipPath.reset()
        clipPath.addCircle(width / 2f, height / 2f, width / 4f, Path.Direction.CCW)
        canvas?.clipPath(clipPath)

        canvas?.drawCircle(width / 2f, height / 2f, width / 4f, borderPaint)

        canvas?.translate(-offsetXSlow, offsetY)
        wavePaint.color = Color.BLUE
        canvas?.drawPath(wavePath, wavePaint)

        canvas?.save()
        canvas?.translate(-offsetXFast, offsetY)
        wavePaint.color = Color.DKGRAY
        canvas?.drawPath(wavePath, wavePaint)
        canvas?.restore()

        if(!valueAnim.isRunning){
            valueAnim.start()
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        Log.d(TAG, "onWindowVisibilityChanged()")
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        Log.d(TAG, "onWindowFocusChanged()")
    }



    override fun onDetachedFromWindow() {
        valueAnim.cancel()
        valueAnim.removeAllUpdateListeners()
        super.onDetachedFromWindow()
    }



    private fun drawWavePath() {
        val waveLen = width / 3
        val waveHeight = height / 10
        wavePath.moveTo(-waveLen.toFloat(), height / 2f)
        val rang = -waveLen..width * 2 + waveLen
        for (i in rang step waveLen) {
            wavePath.rQuadTo(
                waveLen / 4f, waveHeight / 4f,
                waveLen / 2f, 0f
            )
            wavePath.rQuadTo(
                waveLen / 4f, -waveHeight / 4f,
                waveLen / 2f, 0f
            )
        }
        wavePath.rLineTo(0f, height / 2f)
        wavePath.rLineTo(-(width * 2f + 2f * waveLen), 0f)
        wavePath.close()
    }

}