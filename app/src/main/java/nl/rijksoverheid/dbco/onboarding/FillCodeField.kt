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
import android.widget.EditText
import nl.rijksoverheid.dbco.util.numbers
import nl.rijksoverheid.dbco.util.updateText

class FillCodeField : androidx.appcompat.widget.AppCompatEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    interface Callback {
        fun onTextChanged(field: FillCodeField, string: CharSequence?)
    }

    var callback: Callback? = null

    val code: String
        get() {
            return text?.numbers(MAX_LENGTH) ?: ""
        }

    val isFilled: Boolean
        get() {
            return code.length == MAX_LENGTH
        }

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(string: CharSequence?, start: Int, count: Int, after: Int) {
                // Ignored
            }

            override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
                removeTextChangedListener(this)

                val text = string.toString()

                // Extract numbers, convert into separated groups
                val formatted = text.numbers(MAX_LENGTH).chunked(GROUP_SIZE).joinToString(SEPARATOR)

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

    companion object {
        private const val MAX_LENGTH = 12
        private const val GROUP_SIZE = 4
        private const val SEPARATOR = "-"
    }
}