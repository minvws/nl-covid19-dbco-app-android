/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.xwray.groupie.ExpandableGroup
import kotlinx.serialization.json.JsonPrimitive
import nl.rijksoverheid.dbco.onboarding.FillCodeField

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun String.removeHtmlTags(): String{
    return this.replace("<br/>", "\n")
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("<ul>", "")
            .replace("</ul>", "")
            .replace("</li>", "")
            .replace("<li>", "\n• ")
            .replace("<a href=\"", "")
            .replace(Regex("\">(.*)</a>"), "")
}

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

fun String.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

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