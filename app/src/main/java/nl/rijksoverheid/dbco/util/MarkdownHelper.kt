/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.content.ContextCompat
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.items.ui.BulletPointSpan

/**
 * Created by Dima Kovalenko.
 */
object MarkdownHelper {

    fun formatText(text: String, context: Context): SpannableStringBuilder {

        val squashedSpaces = text.replace("* ", "*")
        val clearedFromAsterisks = squashedSpaces.replace("*", " ")
        val spannableBuilder = SpannableStringBuilder(clearedFromAsterisks)

        var startIndex = 0

        while (squashedSpaces.indexOf("*", startIndex) != -1) {
            val start = squashedSpaces.indexOf("*", startIndex)
            var end = squashedSpaces.indexOf("\n", start)
            if (end < start) {
                end = squashedSpaces.length - 1
            }
            if (end > start) {
                spannableBuilder.setSpan(
                    BulletPointSpan(
                        gapWidth = context.resources.getDimensionPixelSize(R.dimen.bullet_gap_size),
                        bulletRadius = context.resources.getDimension(R.dimen.bullet_radius),
                        color = ContextCompat.getColor(context, R.color.color_primary)
                    ),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            startIndex = end
        }

        return spannableBuilder
    }
}