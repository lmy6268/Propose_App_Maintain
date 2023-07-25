package com.example.camera.core.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class SurfaceViewOverViewfinder(context: Context?, attrs: AttributeSet?) :
    SurfaceView(context, attrs) {
    private val mHolder: SurfaceHolder
    private val screenRatio: Float
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    var isCanvasDrawn = false
    private var afRectToDraw: RectF? = RectF()
    private var aeRectToDraw: RectF? = RectF()
    private var debugText: String? = null
    private var lockinImage: Bitmap? = null

    init {
        setZOrderOnTop(true)
        mHolder = this.holder
        mHolder.setFormat(PixelFormat.TRANSPARENT)
        val dm = resources.displayMetrics
        screenRatio = dm.heightPixels.coerceAtLeast(dm.widthPixels).toFloat() / Math.min(
            dm.heightPixels,
            dm.widthPixels
        )
        initPaints()
    }

    //이건 나중에 포커스나 화면에 무언가 그릴 때 쓰면 될듯
    private fun initPaints() {
        whitePaint.color = Color.WHITE
        whitePaint.strokeWidth = 1.5f
        textPaint.color = Color.WHITE
        textPaint.textSize = 25f
        textPaint.textAlign = Paint.Align.LEFT
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        drawGrid(canvas)
//        drawRoundEdges(canvas)
        drawLockIn(canvas)
    }

    private fun drawLockIn(canvas: Canvas) {
        canvas.setBitmap(lockinImage)
    }

    fun setLockin(bitmap: Bitmap) {
        lockinImage = bitmap
    }
//    private fun drawGrid(canvas: Canvas) {
//        when (PreferenceKeys.getGridValue()) {
//            1 -> draw3x3(canvas)
//            2 -> draw4x4(canvas)
//            3 -> drawGoldenRatio(canvas)
//            4 -> drawSuperDiag(canvas)
//            else -> {}
//        }
//    }

    private fun draw3x3(canvas: Canvas) {
        val w = canvas.width
        val h = canvas.height
        canvas.drawLine(w / 3f, 0f, w / 3f, h.toFloat(), whitePaint)
        canvas.drawLine(2f * w / 3f, 0f, 2f * w / 3f, h.toFloat(), whitePaint)
        canvas.drawLine(0f, h / 3f, w.toFloat(), h / 3f, whitePaint)
        canvas.drawLine(0f, 2f * h / 3f, w.toFloat(), 2f * h / 3f, whitePaint)
    }

    private fun draw4x4(canvas: Canvas) {
        val w = canvas.width
        val h = canvas.height
        canvas.drawLine(w / 4f, 0f, w / 4f, h.toFloat(), whitePaint)
        canvas.drawLine(w / 2f, 0f, w / 2f, h.toFloat(), whitePaint)
        canvas.drawLine(3 * w / 4f, 0f, 3 * w / 4f, h.toFloat(), whitePaint)
        canvas.drawLine(0f, h / 4f, w.toFloat(), h / 4f, whitePaint)
        canvas.drawLine(0f, h / 2f, w.toFloat(), h / 2f, whitePaint)
        canvas.drawLine(0f, 3 * h / 4f, w.toFloat(), 3 * h / 4f, whitePaint)
    }

    private fun drawGoldenRatio(canvas: Canvas) {
        val w = canvas.width
        val h = canvas.height
        val gr = goldenRatio(1.0, 1.0).toFloat()
        canvas.drawLine(w / (1 + gr), 0f, w / (1 + gr), h.toFloat(), whitePaint)
        canvas.drawLine(gr * w / (1 + gr), 0f, gr * w / (1 + gr), h.toFloat(), whitePaint)
        canvas.drawLine(0f, h / (1 + gr), w.toFloat(), h / (1 + gr), whitePaint)
        canvas.drawLine(0f, gr * h / (1 + gr), w.toFloat(), gr * h / (1 + gr), whitePaint)
    }

    private fun drawSuperDiag(canvas: Canvas) {
        val w = canvas.width
        val h = canvas.height
        //float gr = (float) goldenRatio(1, 1);
        canvas.drawLine(0f, 0f, w.toFloat(), h.toFloat(), whitePaint)
        canvas.drawLine(w / 3f, h / 3f, w.toFloat(), 0f, whitePaint)
        canvas.drawLine(2f * w / 3f, 2f * h / 3f, 0f, h.toFloat(), whitePaint)
    }

    private fun goldenRatio(a: Double, b: Double): Double {
        val e = 0.00001
        return if (Math.abs(b / a - (a + b) / b) < e) {
            (a + b) / b
        } else {
            goldenRatio(b, a + b)
        }
    }

    private fun drawRoundEdges(canvas: Canvas) {
//        if (PreferenceKeys.isRoundEdgeOn()) {
//            path.reset()
//            path.addRoundRect(RectF(canvas.clipBounds), 40f, 40f, Path.Direction.CW)
//            path.fillType = Path.FillType.INVERSE_EVEN_ODD
//            canvas.clipPath(path)
//            canvas.drawColor(Color.BLACK)
//        }
    }

    fun setAFRect(rect: RectF?) {
        afRectToDraw = rect
    }

    fun setAERect(rect: RectF?) {
        aeRectToDraw = rect
    }

    fun setDebugText(debugText: String?) {
        this.debugText = debugText
    }

//    fun refresh() {
//        drawOnCanvas(mHolder)
//    }

//    private fun drawOnCanvas(surfaceHolder: SurfaceHolder) {
//        try {
//            val canvas = surfaceHolder.lockHardwareCanvas()
//            if (canvas == null) {
//                Log.e(TAG, "Canvas is null")
//            } else {
//                canvas.drawColor(0, PorterDuff.Mode.CLEAR) //Clears the canvas
//                drawAFRect(canvas)
//                drawAERect(canvas)
//                drawAFDebugText(canvas)
//                surfaceHolder.unlockCanvasAndPost(canvas)
//                isCanvasDrawn = true
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    fun clear() {
        try {
            val canvas = mHolder.lockHardwareCanvas()
            if (canvas == null) {
                Log.e(TAG, "Canvas is null")
            } else {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR) //Clears the canvas
                mHolder.unlockCanvasAndPost(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        afRectToDraw = null
        aeRectToDraw = null
        debugText = null
        isCanvasDrawn = false
    }

//    private fun drawAFDebugText(canvas: Canvas) {
//        if (PreferenceKeys.isAfDataOn()) {
//            if (debugText != null) {
//                var y = 180
//                if (screenRatio > 16 / 9f) y = 50
//                for (line in debugText!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
//                    .toTypedArray()) {
//                    if (line.contains("AF_RECT")) {
//                        textPaint.color = Color.GREEN
//                    } else if (line.contains("AE_RECT")) {
//                        textPaint.color = Color.YELLOW
//                    } else {
//                        textPaint.color = Color.WHITE
//                    }
//                    canvas.drawText(line, 50f, y.toFloat(), textPaint)
//                    y += (textPaint.descent() - textPaint.ascent()).toInt()
//                }
//            }
//        }
//    }

//    private fun drawAFRect(canvas: Canvas) {
//        if (PreferenceKeys.isAfDataOn()) {
//            if (afRectToDraw != null && !afRectToDraw!!.isEmpty) {
//                rectPaint.color = Color.GREEN
//                canvas.drawRect(afRectToDraw!!, rectPaint)
//            }
//        }
//    }

//    private fun drawAERect(canvas: Canvas) {
//        if (PreferenceKeys.isAfDataOn()) {
//            if (aeRectToDraw != null && !aeRectToDraw!!.isEmpty) {
//                rectPaint.color = Color.YELLOW
//                canvas.drawRect(aeRectToDraw!!, rectPaint)
//            }
//        }
//    }

    companion object {
        private const val TAG = "com.example.camera.core.camera.SurfaceViewOverViewfinder"
    }
}