/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.util.numbers
import nl.rijksoverheid.dbco.util.updateText

class FillCodeField : androidx.appcompat.widget.AppCompatEditText {

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context, attrs)
    }

    interface Callback {
        fun onTextChanged(field: FillCodeField, string: CharSequence?)
    }

    var separator: String = "-"

    var maxLength: Int = 12

    var groupSize: Int = 4

    var callback: Callback? = null

    val code: String
        get() {
            return text?.numbers(maxLength) ?: ""
        }

    val isFilled: Boolean
        get() {
            return code.length == maxLength
        }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.FillCodeField)
            separator = a.getString(R.styleable.FillCodeField_separator) ?: "-"
            maxLength = a.getInt(R.styleable.FillCodeField_maxLength, 12)
            groupSize = a.getInt(R.styleable.FillCodeField_groupSize, 4)
            a.recycle()
        } else {
            separator = "-"
            maxLength = 12
            groupSize = 4
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                string: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Ignored
            }

            override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
                removeTextChangedListener(this)

                val text = string.toString()

                // Extract numbers, convert into separated groups
                val formatted = text.numbers(maxLength).chunked(groupSize).joinToString(separator)

                // Only update text if format has changed to avoid unwanted keyboard accessibility events (Android bug)
                if (text != formatted) {
                    updateText(formatted)
                }

                addTextChangedListener(this)
                callback?.onTextChanged(this@FillCodeField, formatted)
            }

            override fun afterTextChanged(text: Editable?) {
                // Ignored
            }
        })
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        if (event != null) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                if (event.addedCount > 0) {
                    return true // Do not repeat characters when typing
                }
            } else if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                return true // Do not announce text selection changes
            }
        }

        return super.dispatchPopulateAccessibilityEvent(event)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSelectionChanged(start: Int, end: Int) {
        setSelection(length()) // Disables cursor movement
    }
}