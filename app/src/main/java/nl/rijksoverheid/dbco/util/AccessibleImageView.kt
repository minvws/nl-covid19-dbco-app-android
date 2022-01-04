/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util

import android.content.Context
import android.util.AttributeSet
import kotlin.math.min

/**
 * Limits the height of the ImageView to 50% of the screen height in landscape orientation.
 */
class AccessibleImageView : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxHeightMeasureSpec = heightMeasureSpec

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        if (screenHeight in 1 until screenWidth) {
            // Landscape orientation
            val maxHeight = screenHeight / 2

            val hSize = MeasureSpec.getSize(heightMeasureSpec)

            when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.AT_MOST -> maxHeightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(min(hSize, maxHeight), MeasureSpec.AT_MOST)
                MeasureSpec.UNSPECIFIED -> maxHeightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
                MeasureSpec.EXACTLY -> maxHeightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(min(hSize, maxHeight), MeasureSpec.EXACTLY)
            }
        }

        super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec)
    }
}