package com.example.library

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.FloatRange
import android.support.annotation.IntRange
import android.support.annotation.RequiresApi
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

/**
 * 用贝塞尔曲线绘制loading图:
 * init - > onAttachedToWindow -> onMeasure -> onSizeChange -> onLayout -> onGlobalLayout -> onDraw
 * Created by 陈健宇 at 2019/7/27
 */
class WaveLoadingView : View{

     companion object Config{
         val TAG = WaveLoadingView::class.java.simpleName
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
         const val DEFAULT_PROCESS = 50
         const val MAX_PROCESS = 100
         const val DEFAULT_TEXT_LOCATION = 0
         const val DEFAULT_TEXT_COLOR = "#00BCD4"
         const val DEFAULT_TEXT_SIZE = 20f
         const val DEFAULT_TEXT_STROKE_WIDTH = 0f
         const val DEFAULT_TEXT_STROKE_COLOR = "#00BCD4"
     }

    private val waveValueAnim = ValueAnimator.ofFloat(0f, 1f, 0f)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint =  Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePath = Path()
    private val clipPath = Path()
    private val textPath = Path()
    private var animProcess = 0f
    private var textWaveDirectionReverse = false
    private var canvasFastOffsetX = 0f
    private var canvasSlowOffsetX = 0f
    private var fastWaveOffsetX = 0f
    private var slowWaveOffsetX = 0f
    private var canvasOffsetY = 0f
    private var canvasWidth = 0
    private var canvasHeight = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var waveStartY = 0f
    private var textWidth = 0
    private var textHeight = 0
    private var textStartX = 0f
    private var textStartY = 0f
    private val shapeRect = RectF()
    private val shapeCircle = Circle()

    var shape = Shape.CIRCLE
    set(value) {
        field = value
        requestLayout()
    }

    var shapeCorner = 0f
    set(value) {
        field = value
        requestLayout()
    }

    var waveColor  = 0
    set(@ColorInt value) {
        field = value
        invalidate()
    }

    var waveBackgroundColor = 0
    set(@ColorInt value) {
        field = value
        invalidate()
    }

    var waveAmplitude = 0f
    set(value) {
        field = value
        if (value < 0f) field = 0f
        if (value > MAX_WAVE_AMPLITUDE) field = MAX_WAVE_AMPLITUDE
        requestLayout()
    }

    var waveVelocity = 0f
    set(value) {
        field = value
        if (value < 0f) field = 0f
        if (value > MAX_WAVE_VELOCITY) field = MAX_WAVE_VELOCITY
        fastWaveOffsetX= field * WAVE_OFFSET
        slowWaveOffsetX = field * WAVE_OFFSET / 2
    }

    var borderColor = 0
    set(@ColorInt value) {
        field = value
        invalidate()
    }

    var borderWidth = 0f
    set(value) {
        field = value
        invalidate()
    }

    var process = 0
    set(value) {
        field = value
        if (value < 0) field = 0
        if (value > MAX_PROCESS) field = MAX_PROCESS
        requestLayout()
    }

    var textLocation = Location.FLOW
    set(value) {
        field = value
        requestLayout()
    }

    var text : String? = ""
    set(value) {
        field = value
        invalidate()
    }

    var textSize = 0f
    set(value) {
        field = value
        requestLayout()
    }

    var textColor = 0
    set(@ColorInt value) {
        field = value
        invalidate()
    }

    var textStrokeWidth = 0f
    set(value) {
        field = value
        invalidate()
    }

    var textStrokeColor = 0
    set(@ColorInt value) {
        field = value
        invalidate()
    }

    var isTextWave = false
    set(value) {
        field = value
        if(field) waveValueAnim.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationRepeat(animation: Animator?) {
                textWaveDirectionReverse = !textWaveDirectionReverse
            }
        })
        invalidate()
    }

    var isTextBold = false
    set(value) {
        field = value
        invalidate()
    }

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
        waveBackgroundColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_waveBackgroundColor, Color.parseColor(DEFAULT_WAVE_BACKGROUND_COLOR))
        waveAmplitude = typeArray?.getFloat(R.styleable.WaveLoadingView_wl_waveAmplitude, DEFAULT_WAVE_AMPLITUDE)
        waveVelocity = typeArray?.getFloat(R.styleable.WaveLoadingView_wl_waveVelocity, DEFAULT_WAVE_VELOCITY)

        borderColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_borderColor, Color.parseColor(DEFAULT_BORDER_COLOR))
        borderWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_borderWidth, dpTopx(DEFAULT_BORDER_WIDTH))

        process = typeArray?.getInteger(R.styleable.WaveLoadingView_wl_process, DEFAULT_PROCESS)

        textLocation = when(typeArray?.getInteger(R.styleable.WaveLoadingView_wl_textLocation, DEFAULT_TEXT_LOCATION)){
            0 -> Location.FLOW
            1 -> Location.CENTER
            2 -> Location.TOP
            else -> Location.BOTTOM
        }
        text = typeArray?.getString(R.styleable.WaveLoadingView_wl_text)
        if(TextUtils.isEmpty(text)) text = ""
        textSize = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_textSize, spTopx(DEFAULT_TEXT_SIZE))
        textStrokeWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_textStrokeWidth, spTopx(DEFAULT_TEXT_STROKE_WIDTH))
        textStrokeColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_textStrokeColor, Color.parseColor(DEFAULT_TEXT_STROKE_COLOR))
        textColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_textColor, Color.parseColor(DEFAULT_TEXT_COLOR))
        isTextWave = typeArray?.getBoolean(R.styleable.WaveLoadingView_wl_textWave, false)
        isTextBold = typeArray?.getBoolean(R.styleable.WaveLoadingView_wl_textBold, false)

        typeArray?.recycle()

        //init Anim
        fastWaveOffsetX= waveVelocity * WAVE_OFFSET
        slowWaveOffsetX = waveVelocity * WAVE_OFFSET / 2
        waveValueAnim.apply {
            duration = ANIM_TIME
            LinearOutSlowInInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener{ animation ->
                animProcess = animation.animatedValue as Float
                canvasFastOffsetX = (canvasFastOffsetX + fastWaveOffsetX) % canvasWidth
                canvasSlowOffsetX = (canvasSlowOffsetX + slowWaveOffsetX) % canvasWidth
                if(canvasOffsetY < 0) canvasOffsetY += WAVE_OFFSET  else  canvasOffsetY = 0f
                invalidate()
            }
        }

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
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        canvasWidth = measuredWidth
        canvasHeight = measuredHeight
        viewWidth = canvasWidth
        viewHeight = canvasHeight
        canvasOffsetY = -(calculateWaveStartYbyProcess() - calculateRelativeY())
    }

    /**
     * ConstraintLayout布局会让setMeasuredDimension()失效,所以导致 measuredHeight 和 height 不一样, 宽同理
     * 所以计算圆形和正方形时要注意居中
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        preDrawShapePath(width, height)
        preDrawWavePath()
        preDrawTextPath()
    }

    override fun onDraw(canvas: Canvas?) {
        clipCanvasShape(canvas)
        drawWave(canvas)
        drawBorder(canvas)
        drawText(canvas)
        startLoading()
    }

    override fun onDetachedFromWindow() {
        cancelLoading()
        waveValueAnim.removeAllUpdateListeners()
        super.onDetachedFromWindow()
    }

    private fun preDrawTextPath() {
        textPaint.textSize = textSize
        textWidth = textPaint.measureText(text).toInt()
        val textRect = Rect()
        textPaint.getTextBounds(text, 0, text!!.length, textRect)
        textHeight = textRect.height()

        textStartX = (viewWidth - textWidth) / 2f
        textStartY = when(textLocation){
            Location.FLOW -> waveStartY + textHeight / 2f
            Location.TOP -> calculateRelativeY() + textHeight
            Location.CENTER -> (viewHeight + textHeight) / 2f
            else -> calculateRelativeY() + canvasHeight - textHeight
        }
        textPath.reset()
        textPath.moveTo(textStartX, textStartY)
        textPath.rLineTo(textWidth.toFloat(), 0f)
    }

    private fun preDrawShapePath(w: Int, h: Int) {
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

    private fun preDrawWavePath() {
        wavePath.reset()
        val waveLen = canvasWidth
        val waveHeight = (waveAmplitude * canvasHeight).toInt()
        waveStartY = calculateWaveStartYbyProcess()
        wavePath.moveTo(-canvasWidth * 2f, waveStartY)
        val rang = -canvasWidth * 2..canvasWidth
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
        wavePath.rLineTo(0f, canvasHeight.toFloat())
        wavePath.rLineTo(-canvasWidth * 3f, 0f)
        wavePath.close()
    }

    private fun drawText(canvas: Canvas?) {
        if (TextUtils.isEmpty(text)) return

        textPaint.isFakeBoldText = isTextBold

        if(isTextWave && textLocation == Location.FLOW){
            textPath.reset()
            textPath.moveTo(textStartX, textStartY)
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

    private fun drawBorder(canvas: Canvas?) {
        if(borderWidth == 0f) return

        borderPaint.style = Paint.Style.STROKE
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

    private fun drawWave(canvas: Canvas?) {
        wavePaint.style = Paint.Style.FILL_AND_STROKE

        canvas?.save()
        canvas?.save()

        canvas?.translate(canvasSlowOffsetX, canvasOffsetY)
        wavePaint.color = adjustAlpha(waveColor, 0.7f)
        canvas?.drawPath(wavePath, wavePaint)
        canvas?.restore()

        canvas?.translate(canvasFastOffsetX, canvasOffsetY)
        wavePaint.color = waveColor
        canvas?.drawPath(wavePath, wavePaint)
        canvas?.restore()
    }

    private fun clipCanvasShape(canvas: Canvas?) {
        if (shape != Shape.NONE) canvas?.clipPath(clipPath)
        canvas?.drawColor(waveBackgroundColor)
    }

    private fun calculateWaveStartYbyProcess(): Float {
        val valueProcess = process / 100f
        return  if (viewHeight == canvasHeight)
            (1 - valueProcess) * canvasHeight
        else
             Math.abs(viewHeight - canvasHeight) / 2f + (1 - valueProcess) * canvasHeight
    }

    private fun calculateRelativeY(): Float {
        return if (viewHeight == canvasHeight)
                0f
             else
                Math.abs(viewHeight - canvasHeight) / 2f
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

    enum class Shape{
        CIRCLE, SQUARE, RECT, NONE
    }

    enum class Location{
       FLOW, TOP, CENTER, BOTTOM
    }

    fun startLoading(){
        if(!waveValueAnim.isStarted){
            waveValueAnim.start()
        }
    }

    fun cancelLoading(){
        waveValueAnim.cancel()
    }

    fun pauseLoading(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            waveValueAnim.pause()
        }
    }

    fun resumeLoading(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            waveValueAnim.resume()
        }
    }

}