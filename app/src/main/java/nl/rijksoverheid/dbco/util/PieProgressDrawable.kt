/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.util

import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import timber.log.Timber

/**
 * Based on https://gist.github.com/dcow/9493477
 */
class PieProgressDrawable : Drawable() {

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var boundsF: RectF? = null
    private var innerBoundsF: RectF? = null
    private val startAngle = 0f
    private var drawTo = 0f

    /**
     * Set the border width.
     * @param widthDp in dip for the pie border
     */
    fun setBorderWidth(widthDp: Float, dm: DisplayMetrics) {
        val borderWidth = widthDp * dm.density
        paint.strokeWidth = borderWidth
    }

    /**
     * @param color you want the pie to be drawn in
     */
    fun setColor(color: Int) {
        paint.color = color
    }

    override fun draw(canvas: Canvas) {
        // Rotate the canvas around the center of the pie by 90 degrees
        // counter clockwise so the pie stars at 12 o'clock.
        try {
            canvas.rotate(-90f, bounds.centerX().toFloat(), bounds.centerY().toFloat())
            paint.style = Paint.Style.STROKE
            canvas.drawOval(boundsF!!, paint)
            paint.style = Paint.Style.FILL
            canvas.drawArc(innerBoundsF!!, startAngle, drawTo, true, paint)
        } catch (e: Exception) {
            Timber.e(e)
        }
        // Draw inner oval and text on top of the pie (or add any other
        // decorations such as a stroke) here..
        // Don't forget to rotate the canvas back if you plan to add text!
        // ...
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        val halfBorder = (paint.strokeWidth / 2f + 0.5f).toInt()
        boundsF = RectF(bounds)
        boundsF?.inset(halfBorder.toFloat(), halfBorder.toFloat())

        val twoBorders = 2 * paint.strokeWidth
        innerBoundsF = RectF(bounds)
        innerBoundsF?.inset(twoBorders, twoBorders)
    }

    public override fun onLevelChange(level: Int): Boolean {
        val drawTo = startAngle + 360.toFloat() * level / 100f
        val update = drawTo != this.drawTo
        this.drawTo = drawTo
        return update
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return paint.alpha
    }

}