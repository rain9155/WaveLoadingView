package com.example.waveloadingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * 用贝塞尔曲线绘制loading图: init - > onMeasure -> onSizeChange -> onLayout -> onGlobalLayout() -> onDraw
 * Created by 陈健宇 at 2019/7/27
 */
class WaveLoadingView : View{

     companion object Config{
         val TAG = WaveLoadingView::class.java.simpleName!!
         const val ANIM_TIME = 3000L
         const val WAVE_OFFSET = 5
         const val DEFAULT_SHAPE = 0
         const val DEFAULT_SHAPE_CORNER = 10f
         const val DEFAULT_WAVE_COLOR = "#212121"
         const val DEFAULT_WAVE_BACKGROUND_COLOR = "#ffffffff"
         const val DEFAULT_BORDER_COLOR = "#212121"
         const val DEFAULT_BORDER_WIDTH = 10f
         const val DEFAULT_PROCESS = 0
     }

    private lateinit var wavePaint: Paint
    private lateinit var borderPaint: Paint
    private lateinit var bitmapPaint : Paint
    private lateinit var textPaint: Paint
    private lateinit var valueAnim : ValueAnimator
    private val wavePath = Path()
    private val clipPath = Path()
    private var offsetXFast = 0f
    private var offsetXSlow = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var cricleRaidus = 0f

    var corner = 0f
    var shape = 0
    var waveColor  = 0
    var waveSecondColor  = 0
    var waveBackgroundColor = 0
    var borderColor = 0
    var borderWidth = 0f
    var process = 0


    constructor(context: Context?) : super(context){
        init(context, null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init(context, attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?){

        val typeArray = context?.obtainStyledAttributes(attrs, R.styleable.WaveLoadingView)
        shape = typeArray?.getInteger(R.styleable.WaveLoadingView_wl_shape, DEFAULT_SHAPE)!!
        corner = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_shapeCorner, DEFAULT_SHAPE_CORNER)
        waveColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_waveColor, Color.parseColor(DEFAULT_WAVE_COLOR))
        waveSecondColor = adjustAlpha(waveColor, 0.5f)
        waveBackgroundColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_waveBackgroundColor, Color.parseColor(DEFAULT_WAVE_BACKGROUND_COLOR))
        borderColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_borderColor, Color.parseColor(DEFAULT_BORDER_COLOR))
        borderWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_borderWidth, DEFAULT_BORDER_WIDTH)
        process = typeArray?.getInteger(R.styleable.WaveLoadingView_wl_process, DEFAULT_PROCESS)
        typeArray?.recycle()

        wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        wavePaint.style = Paint.Style.FILL_AND_STROKE
        bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = Color.RED
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 3f

        valueAnim = ValueAnimator.ofFloat(0f, 1f)
        valueAnim.apply {
            duration = ANIM_TIME
            addUpdateListener {animation ->
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
        Log.d(TAG, "onMeasure()")
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        val size = if(measureHeight < measureWidth) measureHeight else measureWidth
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged()")
        centerX = w / 2f
        centerY = h / 2f
        when(shape){
            1 -> {}
            2 -> {}
            3 -> {}
            else -> {
                cricleRaidus = centerX
                clipPath.reset()
                clipPath.addCircle(centerX, centerY, cricleRaidus, Path.Direction.CCW)
            }
        }
        drawWavePath()
    }

    override fun onDraw(canvas: Canvas?) {

        when(shape){
            1 -> {}
            2 -> {}
            3 -> {}
            else -> {
                canvas?.clipPath(clipPath)
                canvas?.drawCircle(centerX, centerY, cricleRaidus, borderPaint)
            }
        }


        canvas?.translate(-offsetXSlow, process.toFloat())
        wavePaint.color = Color.BLUE
        canvas?.drawPath(wavePath, wavePaint)

        canvas?.save()
        canvas?.translate(-offsetXFast,  process.toFloat())
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
        wavePath.reset()
        val waveLen = width / 2
        val waveHeight = height / 10
        wavePath.moveTo(-waveLen.toFloat(), height / 2f)
        val rang = -waveLen..width * 3 + waveLen
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
        wavePath.rLineTo(-(width * 3f + 2f * waveLen), 0f)
        wavePath.close()
    }

    private fun adjustAlpha(color : Int?,  factor : Float?) : Int{
        val alpha = Math.round(Color.alpha(color!!) * factor!!)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

}