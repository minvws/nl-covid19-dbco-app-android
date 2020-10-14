/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.Editable
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSearchFieldBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class SearchFieldItem(
    private val afterTextCallback: (text: Editable?) -> Unit,
    @StringRes private val text: Int
) :
    BaseBindableItem<ItemSearchFieldBinding>() {
    override fun getLayout() = R.layout.item_search_field

    override fun bind(viewBinding: ItemSearchFieldBinding, position: Int) {
        viewBinding.searchView.editText!!.doAfterTextChanged(afterTextCallback)
        viewBinding.searchView.apply {
            if (text > 0) {
                this.hint = this.context.getString(text)
            }
        }
    }

}
