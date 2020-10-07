/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.Editable
import androidx.core.widget.doAfterTextChanged
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSearchFieldBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType

class SearchFieldItem(private val afterTextCallback: (text: Editable?) -> Unit) :
    BaseBindableItem<ItemSearchFieldBinding>() {
    override fun getLayout() = R.layout.item_search_field

    override val itemType = ItemType.INPUT_SEARCH

    override fun bind(viewBinding: ItemSearchFieldBinding, position: Int) {
        viewBinding.searchView.editText!!.doAfterTextChanged(afterTextCallback)
    }

}
