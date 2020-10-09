/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ContactNameItem
import nl.rijksoverheid.dbco.items.input.EmailAdressItem
import nl.rijksoverheid.dbco.items.input.PhoneNumberItem
import timber.log.Timber

class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_list) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactDetailsInputFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentListBinding.bind(view)
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            VerticalSpaceItemDecoration(32)
        )

        Timber.d("Found selected user ${args.selectedContact}");

        args.selectedContact.also { contact ->
            binding.toolbar.title = contact.displayName
            setupBasicFields(contact)
        }
    }

    private fun setupBasicFields(contact: LocalContact) {
        val nameParts = contact.displayName.split(" ", limit = 2)
        val firstName = nameParts[0] ?: ""
        val lastName = if (nameParts.size > 1) {
            nameParts[1]
        } else {
            ""
        }

        val primaryPhone = if (contact.numbers.isNotEmpty()) {
            contact.numbers[0]
        } else {
            ""
        }

        val primaryEmail = if (contact.emails.isNotEmpty()) {
            contact.emails[0]
        } else {
            ""
        }

        // Default items to always add
        adapter.add(
            Section(
                listOf(
                    ContactNameItem(firstName, lastName),
                    PhoneNumberItem(primaryPhone),
                    EmailAdressItem(primaryEmail),
                    ButtonItem(R.string.save, {
                        parseInput()
                    })
                )
            )
        )
    }


    private fun parseInput() {
        var i = 0
        while (i < adapter.itemCount) {
            val item = adapter.getItem(i)
            if (item is BaseBindableItem<*>) {
                when (item.itemType) {
                    ItemType.INPUT_NAME -> {
                        Timber.d("Found name field with content ${(item as ContactNameItem).getFirstNameAndLastName()}")
                    }

                    else -> {
                        // Todo: Handle rest of input fields
                    }
                }
                i++
            }
        }
    }

}