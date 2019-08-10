package com.example.library

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

/**
 * 用贝塞尔曲线绘制loading图: init - > onMeasure -> onSizeChange -> onLayout -> onGlobalLayout() -> onDraw
 * Created by 陈健宇 at 2019/7/27
 */
class WaveLoadingView : View{

     companion object Config{
         val TAG = WaveLoadingView::class.java.simpleName!!
         const val ANIM_TIME = 500L
         const val WAVE_OFFSET = 20
         const val DEFAULT_SHAPE = 0
         const val DEFAULT_SHAPE_CORNER = 0f
         const val DEFAULT_WAVE_COLOR = "#00BCD4"
         const val DEFAULT_WAVE_BACKGROUND_COLOR = "#ffffffff"
         const val DEFAULT_WAVE_AMPLITUDE = 0.2f
         const val MAX_WAVE_AMPLITUDE = 0.9f
         const val DEFAULT_WAVE_VELOCITY = 0.5f
         const val MAX_WAVE_VELOCITY = 1f
         const val DEFAULT_BORDER_COLOR = "#00BCD4"
         const val DEFAULT_BORDER_WIDTH = 0f
         const val MAX_BORDER_WIDTH = 20f
         const val DEFAULT_PROCESS = 50
         const val MAX_PROCESS = 100
         const val DEFAULT_TEXT_COLOR = "#ffffffff"
         const val DEFAULT_TEXT_SIZE = 20f
         const val DEFAULT_TEXT_STROKE_WIDTH = 0f
         const val MAX_TEXT_STROKE_WIDTH = 10f
         const val DEFAULT_TEXT_STROKE_COLOR = "#00BCD4"
     }

    private lateinit var wavePaint: Paint
    private lateinit var borderPaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var waveValueAnim : ValueAnimator
    private val wavePath = Path()
    private val clipPath = Path()
    private val textPath = Path()
    private var animProcess = 0f
    private var textWaveDirectionReverse = false
    private var canvasOffsetXFast = 0f
    private var canvasOffsetXSlow = 0f
    private var canvasOffsetY = 0f
    private var canvasWidth = 0
    private var canvasHeight = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var waveLen = 0
    private var waveHeight = 0
    private var waveStartY = 0f
    private val shapeRect = RectF()
    private val shapeCircle = Circle()
    private var shape = Shape.CIRCLE
    private var textWidth = 0
    private var textHeight = 0

    var shapeCorner = 0f
    set(value) {
        field = value
        invalidate()
    }
    var waveColor  = 0
    set(value) {
        field = value
        invalidate()
    }
    var waveSecondColor  = 0
    var waveBackgroundColor = 0
    set(value) {
        field = value
        invalidate()
    }
    var waveAmplitude = 0f
    set(value) {
        checkWaveAmplitude(value)
        invalidate()
    }
    var waveVelocity = 0f
    var borderColor = 0
    var borderWidth = 0f
    var process = 0
    var text : String? = ""
    var textSize = 0f
    var textColor = 0
    var textStrokeWidth = 0f
    var textStrokeColor = 0
    var isTextWave = false
    var isTextBold = false

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            background = null
        }
        //init Attrs
        val typeArray = context?.obtainStyledAttributes(attrs, R.styleable.WaveLoadingView)

        shape = when(typeArray?.getInteger(R.styleable.WaveLoadingView_wl_shape, DEFAULT_SHAPE)){
            0 -> Shape.CIRCLE
            1 -> Shape.SQUARE
            2 -> Shape.RECT
            else -> Shape.NONE
        }
        shapeCorner = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_shapeCorner, DEFAULT_SHAPE_CORNER)!!

        waveColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_waveColor, Color.parseColor(DEFAULT_WAVE_COLOR))
        waveSecondColor = adjustAlpha(waveColor, 0.7f)
        waveBackgroundColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_waveBackgroundColor, Color.parseColor(DEFAULT_WAVE_BACKGROUND_COLOR))
        waveAmplitude = typeArray?.getFloat(R.styleable.WaveLoadingView_wl_waveAmplitude, DEFAULT_WAVE_AMPLITUDE)
        checkWaveAmplitude(waveAmplitude)
        waveVelocity = typeArray?.getFloat(R.styleable.WaveLoadingView_wl_waveVelocity, DEFAULT_WAVE_VELOCITY)
        checkWaveVelocity(waveVelocity)

        borderColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_borderColor, Color.parseColor(DEFAULT_BORDER_COLOR))
        borderWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_borderWidth, dpTopx(DEFAULT_BORDER_WIDTH))
        checkBorderWidth(borderWidth)

        process = typeArray?.getInteger(R.styleable.WaveLoadingView_wl_process, DEFAULT_PROCESS)
        checkProcess(process)

        text = typeArray?.getString(R.styleable.WaveLoadingView_wl_text)
        if(TextUtils.isEmpty(text)) text = ""
        textSize = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_textSize, spTopx(DEFAULT_TEXT_SIZE))
        textStrokeWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_textStrokeWidth, spTopx(DEFAULT_TEXT_STROKE_WIDTH))
        checkTextStrokeWidth(textStrokeWidth)
        textStrokeColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_textStrokeColor, Color.parseColor(DEFAULT_TEXT_STROKE_COLOR))
        textColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_textColor, Color.parseColor(DEFAULT_TEXT_COLOR))
        isTextWave = typeArray?.getBoolean(R.styleable.WaveLoadingView_wl_textWave, false)
        isTextBold = typeArray?.getBoolean(R.styleable.WaveLoadingView_wl_textBold, false)

        typeArray?.recycle()

        //init Paint
        wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        wavePaint.style = Paint.Style.FILL_AND_STROKE

        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        textPaint.isFakeBoldText = isTextBold
        textWidth = textPaint.measureText(text).toInt()
        val textRect = Rect()
        textPaint.getTextBounds(text, 0, text!!.length, textRect)
        textHeight = textRect.height()

        //init Anim
        val fastWaveOffset = waveVelocity * WAVE_OFFSET
        val slowWaveOffset = waveVelocity * WAVE_OFFSET / 2
        waveValueAnim = ValueAnimator.ofFloat(0f, 1f, 0f)
        waveValueAnim.apply {
            duration = ANIM_TIME
            LinearOutSlowInInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener{ animation ->
                animProcess = animation.animatedValue as Float
                canvasOffsetXFast = (canvasOffsetXFast + fastWaveOffset) % canvasWidth
                canvasOffsetXSlow = (canvasOffsetXSlow + slowWaveOffset) % canvasWidth
                postInvalidate()
            }

            if(isTextWave) addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationRepeat(animation: Animator?) {
                    textWaveDirectionReverse = !textWaveDirectionReverse
                }
            })
        }
        Log.d(TAG, "init()")
    }

    private fun checkTextStrokeWidth(width : Float) {
        if (width > spTopx(MAX_TEXT_STROKE_WIDTH)) textStrokeWidth = spTopx(MAX_TEXT_STROKE_WIDTH)
    }

    private fun checkProcess(pro : Int) {
        if (pro < 0) process = 0
        if (pro > MAX_PROCESS) process = MAX_PROCESS
    }

    private fun checkBorderWidth(width : Float) {
        if (width > dpTopx(MAX_BORDER_WIDTH)) borderWidth = dpTopx(MAX_BORDER_WIDTH)
    }

    private fun checkWaveVelocity(velocity : Float) {
        if (velocity < 0f) waveVelocity = 0f
        if (velocity > MAX_WAVE_VELOCITY) waveVelocity = MAX_WAVE_VELOCITY
    }

    private fun checkWaveAmplitude(amplitude : Float) {
        if (amplitude < 0f) waveAmplitude = 0f
        if (amplitude > MAX_WAVE_AMPLITUDE) waveAmplitude = MAX_WAVE_AMPLITUDE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        when(shape){
            Shape.CIRCLE, Shape.SQUARE -> {
                val measureSpec = if(measureHeight < measureWidth) heightMeasureSpec else widthMeasureSpec
                super.onMeasure(measureSpec, measureSpec)
            }else -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
        Log.d(TAG, "onMeasure(), measureHeight = $measuredHeight, measureWidth = $measuredWidth")
    }

    /**
     * ConstraintLayout布局会让setMeasuredDimension()失效,所以导致 measuredHeight 和 h 不一样
     * 所以计算圆形和正方形时要注意居中
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        canvasWidth = measuredWidth
        canvasHeight = measuredHeight
        viewWidth = canvasWidth
        viewHeight = canvasHeight
        drawShapePath(w, h)
        drawWavePath()
        if(!isTextWave){
            textPath.reset()
            textPath.moveTo((viewWidth - textWidth) / 2f, waveStartY + textHeight / 2)
            textPath.rLineTo(textWidth.toFloat(), 0f)
        }
        Log.d(TAG, "onSizeChanged(), w = $w, h = $h")
    }

    override fun onDraw(canvas: Canvas?) {
        ClipShap(canvas)
        drawWave(canvas)
        drawBorder(canvas)
        drawText(canvas)
        startLoading()
    }

    /**
     * 绘制文字
     */
    private fun drawText(canvas: Canvas?) {
        if (TextUtils.isEmpty(text)) return

        if(isTextWave){
            textPath.reset()
            textPath.moveTo((viewWidth - textWidth) / 2f, waveStartY + textHeight / 2)
            if(!textWaveDirectionReverse){
                textPath.rQuadTo(
                    textWidth / 4f, -textHeight * animProcess / 2,
                    textWidth / 2f, 0f
                )
                textPath.rQuadTo(
                    textWidth / 4f, textHeight * animProcess / 2,
                    textWidth / 2f, 0f
                )
            }else{
                textPath.rQuadTo(
                    textWidth / 4f, textHeight * animProcess / 2,
                    textWidth / 2f, 0f
                )
                textPath.rQuadTo(
                    textWidth / 4f, -textHeight * animProcess / 2,
                    textWidth / 2f, 0f
                )
            }
        }

        if(textStrokeWidth != 0f){
            textPaint.style = Paint.Style.STROKE
            textPaint.color = textStrokeColor
            textPaint.strokeWidth = textStrokeWidth
            canvas?.drawTextOnPath(text, textPath, 0f, 0f, textPaint)
        }

        textPaint.style = Paint.Style.FILL
        textPaint.color = textColor
        canvas?.drawTextOnPath(text, textPath, 0f, 0f, textPaint)
    }

    /**
     * 绘制边框
     */
    private fun drawBorder(canvas: Canvas?) {
        if(borderWidth == 0f) return

        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth
        when (shape) {
            Shape.CIRCLE -> {
                canvas?.drawCircle(
                    shapeCircle.centerX, shapeCircle.centerY,
                    shapeCircle.circleRadius,
                    borderPaint
                )
            }
            Shape.SQUARE, Shape.RECT -> {
                if (shapeCorner == 0f)
                    canvas?.drawRect(shapeRect, borderPaint)
                else
                    canvas?.drawRoundRect(
                        shapeRect,
                        shapeCorner, shapeCorner,
                        borderPaint
                    )
            }
        }
    }

    /**
     * 绘制波浪
     */
    private fun drawWave(canvas: Canvas?) {
        canvas?.save()
        canvas?.translate(canvasOffsetXSlow, canvasOffsetY)
        wavePaint.color = waveSecondColor
        canvas?.drawPath(wavePath, wavePaint)
        canvas?.restore()

        canvas?.save()
        canvas?.translate(canvasOffsetXFast, canvasOffsetY)
        wavePaint.color = waveColor
        canvas?.drawPath(wavePath, wavePaint)
        canvas?.restore()
    }

    /**
     * 裁剪画布形状
     */
    private fun ClipShap(canvas: Canvas?) {
        if (shape != Shape.NONE) canvas?.clipPath(clipPath)
        canvas?.drawColor(waveBackgroundColor)
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
        Log.d(TAG, "onDetachedFromWindow()")
        waveValueAnim.cancel()
        waveValueAnim.removeAllUpdateListeners()
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow()")
        super.onAttachedToWindow()
    }

    private fun drawShapePath(w: Int, h: Int) {
        clipPath.reset()
        when (shape) {
            Shape.CIRCLE -> {
                viewWidth = w
                viewHeight = h
                shapeCircle.centerX = viewWidth / 2f
                shapeCircle.centerY = viewHeight / 2f
                shapeCircle.circleRadius = if (viewHeight < viewWidth) viewHeight / 2f else viewWidth / 2f
                clipPath.addCircle(
                    shapeCircle.centerX, shapeCircle.centerY,
                    shapeCircle.circleRadius,
                    Path.Direction.CCW
                )
            }
            Shape.SQUARE -> {
                viewWidth = w
                viewHeight = h
                shapeRect.set(
                    Math.abs(viewWidth - canvasWidth) / 2f,
                    Math.abs(viewHeight - canvasHeight) / 2f,
                    Math.abs(viewWidth - canvasWidth) / 2f + canvasWidth,
                    Math.abs(viewHeight - canvasHeight) / 2f + canvasHeight
                )
                if (shapeCorner == 0f)
                    clipPath?.addRect(shapeRect, Path.Direction.CCW)
                else
                    clipPath.addRoundRect(
                        shapeRect,
                        shapeCorner, shapeCorner,
                        Path.Direction.CCW
                    )
            }
            Shape.RECT -> {
                shapeRect.set(
                    0f,
                    0f,
                    canvasWidth.toFloat(),
                    canvasHeight.toFloat()
                )
                if (shapeCorner == 0f)
                    clipPath?.addRect(shapeRect, Path.Direction.CCW)
                else
                    clipPath.addRoundRect(
                        shapeRect,
                        shapeCorner, shapeCorner,
                        Path.Direction.CCW
                    )
            }
        }
    }

    private fun drawWavePath() {
        wavePath.reset()
        waveLen = viewWidth
        waveHeight = (waveAmplitude * viewHeight).toInt()
        waveStartY = calculateYbyProcess()
        wavePath.moveTo(-viewWidth * 2f, waveStartY)
        val rang = -viewWidth * 2..viewWidth
        for (i in rang step waveLen) {
            wavePath.rQuadTo(
                waveLen / 4f, waveHeight / 2f,
                waveLen / 2f, 0f
            )
            wavePath.rQuadTo(
                waveLen / 4f, -waveHeight / 2f,
                waveLen / 2f, 0f
            )
        }
        wavePath.rLineTo(0f, viewHeight.toFloat())
        wavePath.rLineTo(-viewWidth * 3f, 0f)
        wavePath.close()
    }

    private fun calculateYbyProcess(): Float {
        val valueProcess = process / 100f
        return  if (viewHeight == canvasHeight)
                    (1 - valueProcess) * canvasHeight
                else
                     Math.abs(viewHeight - canvasHeight) / 2f + (1 - valueProcess) * canvasHeight
    }

    private fun adjustAlpha(color : Int?,  factor : Float?) : Int{
        val alpha = Math.round(Color.alpha(color!!) * factor!!)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun dpTopx(dp : Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    private fun spTopx(sp : Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)

    private class Circle{
        var centerX = 0f
        var centerY = 0f
        var circleRadius = 0f
    }

    private enum class Shape{
        CIRCLE, SQUARE, RECT, NONE
    }

    fun startLoading(){
        if(!waveValueAnim.isStarted){
            waveValueAnim.start()
        }
    }

    fun cancelLoading() = waveValueAnim.cancel()

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun pauseLoading() = waveValueAnim.pause()

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun resumeLoading() = waveValueAnim.resume()

}