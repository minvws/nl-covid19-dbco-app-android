/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSymptomBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import timber.log.Timber

class SymptomItem(val text: CharSequence, var selected: Boolean = false) : BaseBindableItem<ItemSymptomBinding>() {

    override fun getLayout(): Int = R.layout.item_symptom
    override fun isClickable(): Boolean {
        return true
    }
    override fun isSameAs(other: Item<*>): Boolean =
        other is SymptomItem && other.text == text

    override fun hasSameContentAs(other: Item<*>) =
        other is SymptomItem && other.text == text


    private var binding : ItemSymptomBinding? = null
    override fun bind(viewBinding: ItemSymptomBinding, position: Int) {
        Timber.d("For $text we've stored selected = $selected")
        binding = viewBinding
        viewBinding.text = text
        viewBinding.checked = selected
    }

    fun setChecked(){
        binding?.checked = selected
    }

}