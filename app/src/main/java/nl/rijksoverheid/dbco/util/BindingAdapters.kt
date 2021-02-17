/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util

import android.view.View
import android.widget.Button
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.databinding.BindingAdapter

object BindingAdapters {

    private fun View.setAccessibilityDelegate(callback: (host: View, info: AccessibilityNodeInfoCompat) -> Unit) {
        ViewCompat.setAccessibilityDelegate(
            this,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    callback(host, info)
                }
            }
        )
    }

    @JvmStatic
    @BindingAdapter("accessibilityButton")
    fun accessibilityButton(view: View, isButton: Boolean) {
        view.setAccessibilityDelegate { _, info ->
            info.className = if (isButton) {
                Button::class.java.name
            } else {
                view::class.java.name
            }
        }
    }

    @JvmStatic
    @BindingAdapter("accessibilityHeading")
    fun accessibilityHeading(view: View, isHeading: Boolean) {
        view.setAccessibilityDelegate { _, info ->
            info.isHeading = isHeading
        }
    }
}