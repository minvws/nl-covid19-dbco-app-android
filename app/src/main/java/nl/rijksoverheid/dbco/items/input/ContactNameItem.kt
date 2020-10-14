/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.ContactName
import nl.rijksoverheid.dbco.databinding.ItemContactNameBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType

class ContactNameItem(private val firstName: String?, private val lastName: String?) :
    BaseBindableItem<ItemContactNameBinding>() {
    override fun getLayout() = R.layout.item_contact_name
    override fun isRequired() = true
    override val itemType = ItemType.INPUT_NAME
    private var binding: ItemContactNameBinding? = null;

    override fun bind(viewBinding: ItemContactNameBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.firstName.editText?.setText(firstName)
        viewBinding.lastName.editText?.setText(lastName)
    }

    fun getFirstNameAndLastName(): ContactName {
        return ContactName(
            binding?.firstName?.editText?.text.toString(),
            binding?.lastName?.editText?.text.toString()
        )
    }
}