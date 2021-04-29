/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.databinding.ItemContactBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import java.io.Serializable

class LocalContactItem(
    val contact: LocalContact
) : BaseBindableItem<ItemContactBinding>(), Serializable {

    override fun bind(viewBinding: ItemContactBinding, position: Int) {
        viewBinding.contactName.text = contact.getDisplayName()
    }

    override fun getLayout() = R.layout.item_contact


    override fun isClickable(): Boolean {
        return true
    }

    override fun toString(): String {
        return "LocalContactItem(contact=$contact)"
    }
}
