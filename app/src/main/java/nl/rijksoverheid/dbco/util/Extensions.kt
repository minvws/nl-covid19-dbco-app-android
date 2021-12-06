/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.TextInputLayout
import com.xwray.groupie.ExpandableGroup
import kotlinx.serialization.json.JsonPrimitive
import nl.rijksoverheid.dbco.R
import java.util.*

fun delay(milliseconds: Long, block: () -> Unit) {
    Handler().postDelayed(Runnable(block), milliseconds)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboardWhenInPortrait(delay: Long = 0) {
    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Only auto show keyboard in portrait because it takes up the whole screen in landscape.
        postDelayed({
            requestFocus()
            showKeyboard()
        }, delay)
    }
}

fun View.setContentResource(stringId: Int) {
    contentDescription = context.getString(stringId)
}

fun ImageView.setImageResource(resId: Int, stringId: Int) {
    setImageResource(resId)
    setContentResource(stringId)
}

fun TextInputLayout.setCompleted(completed: Boolean) {
    if (completed) {
        endIconMode = TextInputLayout.END_ICON_CUSTOM
        setEndIconDrawable(R.drawable.ic_valid_small)
        setEndIconContentDescription(R.string.completed)
        setEndIconTintList(ContextCompat.getColorStateList(context, R.color.green))
        isEndIconVisible = true
    } else {
        endIconMode = TextInputLayout.END_ICON_NONE
        endIconDrawable = null
        endIconContentDescription = null
        setEndIconTintList(null)
        isEndIconVisible = false
    }
}

fun TextInputLayout.setError(
    @DrawableRes iconRes: Int,
    @StringRes messageRes: Int,
    @ColorRes errorColor: Int
) {
    setErrorIconDrawable(iconRes)
    setErrorIconTintList(ContextCompat.getColorStateList(context, errorColor))
    boxStrokeErrorColor = ContextCompat.getColorStateList(context, errorColor)
    setErrorTextColor(ContextCompat.getColorStateList(context, errorColor))
    error = context.getString(messageRes)
}

fun View.accessibilityAnnouncement(stringId: Int) {
    announceForAccessibility(context.getString(stringId))
}

fun Context.accessibilityAnnouncement(stringId: Int) {
    val accessibilityManager =
        getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (accessibilityManager.isEnabled) {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(getString(stringId))
        accessibilityManager.sendAccessibilityEvent(event)
    }
}

fun NestedScrollView.scrollTo(view: View, delay: Long = 0) {
    postDelayed({
        smoothScrollTo(0, view.bottom)
    }, delay)
}

fun String.removeHtmlTags(): String {
    return this
        .replace("<br/>", "\n")
        .replace("<br>", "\n")
        .replace("<b>", "")
        .replace("</b>", "")
        .replace("<ul>", "")
        .replace("</ul>", "")
        .replace("<u>", "")
        .replace("</u>", "")
        .replace("</li>", "")
        .replace("<li>", "\n• ")
        .replace("<a href=\"", "")
        .replace(Regex("\">(.*)</a>"), "")
}

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

fun String.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

fun String.removeWhiteSpace(): String = this.filter { char -> !char.isWhitespace() }

fun String?.numeric(): Int? {
    if (this == null) return null
    return filter { char -> char.isDigit() }.toInt()
}

fun ExpandableGroup.removeAllChildren() {
    if (itemCount <= 1) {
        return
    }
    val start = itemCount - 1
    for (i in start downTo 1) { // 0 is a header, keep it
        remove(getItem(i))
    }
}

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun EditText.updateText(text: CharSequence) {
    setText(text)
    setSelection(text.length)
}

fun CharSequence.numbers(limit: Int? = null): String {
    val numbers = replace("[^0-9]".toRegex(), "")
    return when (limit != null) {
        true -> numbers.take(limit)
        false -> numbers
    }
}

fun DatePicker.getDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth)
    return calendar.time
}

fun View.margin(
    @DimenRes start: Int? = null,
    @DimenRes top: Int? = null,
    @DimenRes end: Int? = null,
    @DimenRes bottom: Int? = null
) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        start?.run { leftMargin = context.resources.getDimensionPixelSize(start) }
        top?.run { topMargin = context.resources.getDimensionPixelSize(top) }
        end?.run { rightMargin = context.resources.getDimensionPixelSize(end) }
        bottom?.run { bottomMargin = context.resources.getDimensionPixelSize(bottom) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

