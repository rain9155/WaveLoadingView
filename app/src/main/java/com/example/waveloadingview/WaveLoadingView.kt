package com.example.waveloadingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
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
         const val ANIM_TIME = 1000L
         const val WAVE_OFFSET = 6
         const val DEFAULT_SHAPE = 0
         const val DEFAULT_SHAPE_CORNER = 0f
         const val DEFAULT_WAVE_COLOR = "#00BCD4"
         const val DEFAULT_WAVE_BACKGROUND_COLOR = "#ffffffff"
         const val DEFAULT_BORDER_COLOR = "#00BCD4"
         const val DEFAULT_BORDER_WIDTH = 5f
         const val MAX_BORDER_WIDTH = 20f
         const val DEFAULT_PROCESS = 50
         const val DEFAULT_TEXT_COLOR = "#ffffffff"
         const val DEFAULT_TEXT_SIZE = 25f
         const val DEFAULT_TEXT_STROKE_WIDTH = 2f
         const val MAX_TEXT_STROKE_WIDTH = 10f
         const val DEFAULT_TEXT_STROKE_COLOR = "#00BCD4"
     }

    private lateinit var wavePaint: Paint
    private lateinit var borderPaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var waveValueAnim : ValueAnimator
    private lateinit var textValueAnim : ValueAnimator
    private val wavePath = Path()
    private val clipPath = Path()
    private val textPath = Path()
    private var animProcess = 0f
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
    var waveColor  = 0
    var waveSecondColor  = 0
    var waveBackgroundColor = 0
    var borderColor = 0
    var borderWidth = 0f
    var process = 0
    var text : String? = ""
    var textSize = 0f
    var textColor = 0
    var textStrokeWidth = 0f
    var textStrokeColor = 0

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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            background = null
//        }

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

        borderColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_borderColor, Color.parseColor(DEFAULT_BORDER_COLOR))
        borderWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_borderWidth, dpTopx(DEFAULT_BORDER_WIDTH))
        if(borderWidth > dpTopx(MAX_BORDER_WIDTH)) borderWidth = dpTopx(MAX_BORDER_WIDTH)

        process = typeArray?.getInteger(R.styleable.WaveLoadingView_wl_process, DEFAULT_PROCESS)
        if(process < 0) process = 0
        if(process > 100) process = 100

        text = typeArray?.getString(R.styleable.WaveLoadingView_wl_text)
        if(TextUtils.isEmpty(text)) text = ""
        textSize = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_textSize, spTopx(DEFAULT_TEXT_SIZE))
        textStrokeWidth = typeArray?.getDimension(R.styleable.WaveLoadingView_wl_textStrokeWidth, spTopx(DEFAULT_TEXT_STROKE_WIDTH))
        if(textStrokeWidth > spTopx(MAX_TEXT_STROKE_WIDTH)) textStrokeWidth = spTopx(MAX_TEXT_STROKE_WIDTH)
        textStrokeColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_textStrokeColor, Color.parseColor(DEFAULT_TEXT_STROKE_COLOR))
        textColor = typeArray?.getColor(R.styleable.WaveLoadingView_wl_textColor, Color.parseColor(DEFAULT_TEXT_COLOR))

        typeArray?.recycle()

        //init Paint
        wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        wavePaint.style = Paint.Style.FILL_AND_STROKE

        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        textPaint.isFakeBoldText = true
        textWidth = textPaint.measureText(text).toInt()
        val textRect = Rect()
        textPaint.getTextBounds(text, 0, text!!.length, textRect)
        textHeight = textRect.height()

        //init Anim
        waveValueAnim = ValueAnimator.ofFloat(0f, 1f)
        waveValueAnim.apply {
            duration = ANIM_TIME
            addUpdateListener{ animation ->
                animProcess = animation.animatedValue as Float
                canvasOffsetXFast = (canvasOffsetXFast + WAVE_OFFSET) % canvasWidth
                canvasOffsetXSlow = (canvasOffsetXSlow + WAVE_OFFSET / 2) % canvasWidth
                postInvalidate()
            }
            LinearOutSlowInInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }

        Log.d(TAG, "init()")
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
        Log.d(TAG, "onSizeChanged(), w = $w, h = $h")
    }

    override fun onDraw(canvas: Canvas?) {
        ClipShap(canvas)
        drawWave(canvas)
        drawBorder(canvas)
        drawText(canvas)
        if(!waveValueAnim.isRunning) waveValueAnim.start()
    }

    /**
     * 绘制文字
     */
    private fun drawText(canvas: Canvas?) {
        if (!TextUtils.isEmpty(text)) {

            textPath.reset()
            textPath.moveTo((viewWidth - textWidth) / 2f, waveStartY + textHeight / 2f)
            textPath.rQuadTo(textWidth / 4f, -waveHeight.toFloat() * (1 - animProcess) / 4, textWidth / 2f, 0f)
            textPath.rQuadTo(textWidth / 4f, waveHeight.toFloat() * (1 - animProcess) / 4, textWidth / 2f, 0f)

            textPaint.style = Paint.Style.STROKE
            textPaint.color = textStrokeColor
            textPaint.strokeWidth = textStrokeWidth
            canvas?.drawTextOnPath(text, textPath, 0f, 0f, textPaint)
            textPaint.style = Paint.Style.FILL
            textPaint.color = textColor
            canvas?.drawTextOnPath(text, textPath, 0f, 0f, textPaint)
        }
    }

    /**
     * 绘制边框
     */
    private fun drawBorder(canvas: Canvas?) {
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
        waveValueAnim.cancel()
        waveValueAnim.removeAllUpdateListeners()
        super.onDetachedFromWindow()
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
        waveLen = viewWidth / 2
        waveHeight = viewHeight / 10
        waveStartY = calculateYbyProcess()
        wavePath.moveTo(-viewWidth * 2f, waveStartY)
        val rang = -viewWidth * 2..viewWidth + waveLen
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
        wavePath.rLineTo(0f, viewHeight.toFloat())
        wavePath.rLineTo(-(viewWidth * 3f + waveLen), 0f)
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


}