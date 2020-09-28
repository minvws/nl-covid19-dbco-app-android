/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import java.io.Serializable

class Contact(
    val id: String,
    val displayName: String,
    var state: State = State.PRESENT // Present by default
) : BaseBindableItem<ItemContactBinding>(), Serializable {
    var numbers = ArrayList<String>()
    var emails = ArrayList<String>()
    var address = ArrayList<ContactAddress>()
    var name = ContactName("", "")

    override fun bind(viewBinding: ItemContactBinding, position: Int) {
        viewBinding.contactName.text = displayName
    }

    override fun getLayout() = R.layout.item_contact

    override fun toString(): String {
        return "Contact(id='$id', displayName='$displayName', state=$state, numbers=$numbers, emails=$emails, address=$address, name=$name)"
    }

    override fun isClickable(): Boolean {
        return false
    }



}



@Keep
enum class State { PRESENT, REMOVED }