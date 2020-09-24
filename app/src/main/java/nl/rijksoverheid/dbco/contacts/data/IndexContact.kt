/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import androidx.annotation.Keep
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_contact.*
import kotlinx.android.synthetic.main.item_contact.view.*
import nl.rijksoverheid.dbco.R

class Contact(
    val id: String,
    val displayName : String,
    var state: State = State.PRESENT // Present by default
) : Item() {
    var numbers = ArrayList<String>()
    var emails = ArrayList<String>()
    var address = ArrayList<ContactAddress>()
    var name = ContactName("","")

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.contactName.text = displayName
    }

    override fun getLayout() = R.layout.item_contact

    override fun toString(): String {
        return "Contact(id='$id', displayName='$displayName', state=$state, numbers=$numbers, emails=$emails, address=$address, name=$name)"
    }


}



@Keep
enum class State { PRESENT, REMOVED }