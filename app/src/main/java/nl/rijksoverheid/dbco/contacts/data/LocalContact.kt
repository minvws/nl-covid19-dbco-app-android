/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import android.text.SpannableStringBuilder
import androidx.core.text.bold
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import java.io.Serializable

/**
 * Class representing a user's contact from their contacts list
 */
class LocalContact(
    val id: String,
    val displayName: String
) : BaseBindableItem<ItemContactBinding>(), Serializable {
    var numbers = ArrayList<String>()
    var emails = ArrayList<String>()
    var address = ArrayList<ContactAddress>()
    var name = ContactName("", "")

    override fun bind(viewBinding: ItemContactBinding, position: Int) {
        viewBinding.contactName.text = formatDisplayName(displayName)
    }

    override fun getLayout() = R.layout.item_contact

    override fun toString(): String {
        return "Contact(id='$id', displayName='$displayName', numbers=$numbers, emails=$emails, address=$address, name=$name)"
    }

    override fun isClickable(): Boolean {
        return true
    }

    // Format name to show first word as bold
    private fun formatDisplayName(displayName: String): SpannableStringBuilder {
        val nameSplit = displayName.split(" ", limit = 2)
        return SpannableStringBuilder()
            .bold { append(nameSplit[0]) }.also {
                if (nameSplit.size > 1) {
                    it.append(" ${nameSplit[1]}")
                }
            }
    }
}
