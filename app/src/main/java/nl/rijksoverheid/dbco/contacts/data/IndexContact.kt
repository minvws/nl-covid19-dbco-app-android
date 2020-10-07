/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import androidx.annotation.Keep
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemIndexContactBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import java.io.Serializable

class IndexContact(
    val id: String,
    val displayName: String,
    var state: State = State.PRESENT // Present by default
) : BaseBindableItem<ItemIndexContactBinding>(), Serializable {
    var linkedContact: LocalContact? = null

    override fun bind(viewBinding: ItemIndexContactBinding, position: Int) {
        viewBinding.indexContactName.text = displayName
    }

    override fun getLayout() = R.layout.item_index_contact


    override fun isClickable(): Boolean {
        return true
    }


}


@Keep
enum class State { PRESENT, REMOVED }