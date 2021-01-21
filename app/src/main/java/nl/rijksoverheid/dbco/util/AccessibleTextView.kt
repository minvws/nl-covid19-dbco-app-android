/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util

import android.content.Context
import android.text.Spanned
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView

/**
 * The first link in a ClickableSpan is activated whenever a
 * TYPE_VIEW_CLICKED accessibility event is dispatched.
 *
 * Note: there is no support (yet) for multiple embedded links.
 */
class AccessibleTextView: TextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private fun getClickableSpans(text: CharSequence): Array<ClickableSpan?>? {
        try {
            if (text is Spanned) {
                return text.getSpans(0, text.length, ClickableSpan::class.java)
            }
        } catch (e: Exception) {
            // Ignored
        }
        return null
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        if (event != null) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                // Activate first clickable span if it exists.
                getClickableSpans(text)?.firstOrNull()?.onClick(this)
            }
        }
        return super.dispatchPopulateAccessibilityEvent(event)
    }
}