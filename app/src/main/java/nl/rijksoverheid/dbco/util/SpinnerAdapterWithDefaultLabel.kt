/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes

/**
 * Adapter with default label (e.g. "Choose option...")
 * Based on https://stackoverflow.com/a/21734833/1011496
 */
class SpinnerAdapterWithDefaultLabel(
    context: Context,
    @LayoutRes textViewResourceId: Int,
    private var objects: Array<String?>,
    defaultText: String?
) : ArrayAdapter<String>(
    context, textViewResourceId, objects
) {
    private var firstElement: String? = null
    private var isFirstTime = true

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (isFirstTime) {
            objects[0] = firstElement
            isFirstTime = false
        }
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        notifyDataSetChanged()
        return getCustomView(position, convertView, parent)
    }

    private fun setDefaultText(defaultText: String?) {
        firstElement = objects[0]
        objects[0] = defaultText
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val label = row.findViewById<View>(android.R.id.text1) as TextView
        label.text = objects[position]
        return row
    }

    init {
        setDefaultText(defaultText)
    }
}